package manager;

import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int counter = 1;
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, SubTask> subTasks = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected Set<Task> prioritizedTasks = new TreeSet<>( // Добавляем новое поле для хранения отсортированных задач
            Comparator.comparing(Task::getStartTime, // Компаратор для сортировки задач по startTime
                            Comparator.nullsLast(Comparator.naturalOrder())) // null значения идут в конец
                    .thenComparingInt(Task::getId)); // Если startTime одинаковый, сортируем по id

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void deleteAllTasks() {
        getPrioritizedTasks().removeIf(task -> tasks.containsKey(task.getId())); // Удаляем все задачи из отсортированного списка
        tasks.values().forEach(task -> historyManager.remove(task.getId())); // Удаляем все задачи из истории
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        getPrioritizedTasks().removeIf(task -> epics.containsKey(task.getId()) || // Удаляем все эпики из отсортированного списка
                (task instanceof SubTask && epics.containsKey(((SubTask) task).getEpicId()))); // или их подзадачи
        epics.values().forEach(epic -> historyManager.remove(epic.getId())); // Удаляем эпики из истории
        subTasks.values().forEach(subTask -> historyManager.remove(subTask.getId())); // Удаляем подзадачи из истории
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        getPrioritizedTasks().removeIf(task -> task instanceof SubTask); // Удаляем все подзадачи из отсортированного списка
        subTasks.values().forEach(subTask -> historyManager.remove(subTask.getId())); // Удаляем подзадачи из истории
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
            updateStatus(epic);
        }
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id)); // Добавляем задачу в историю
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id)); // Добавляем эпик в историю
        return epics.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id)); // Добавляем подзадачу в историю
        return subTasks.get(id);
    }

    @Override
    public void addTask(Task task) {
        if (isTaskOverlapping(task)) { // Проверяем пересечение с существующими задачами
            throw new ManagerSaveException("Задача пересекается по времени с существующей");
        }

        task.setId(addId());
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task); // Добавляем в отсортированное множество
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(addId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubTask(SubTask subtask) {
        if (isTaskOverlapping(subtask)) {
            throw new ManagerSaveException("Подзадача пересекается по времени с существующей");
        }

        subtask.setId(addId());
        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTaskId(subtask.getId());
        updateStatus(epic);
        addToPrioritizedTasks(subtask);
    }

    @Override
    public void updateTask(Task task) {
        Task existingTask = tasks.get(task.getId()); // Удаляем старую версию задачи из отсортированного множества
        getPrioritizedTasks().remove(existingTask);

        if (isTaskOverlapping(task)) { // Проверяем пересечение для обновленной задачи
            getPrioritizedTasks().add(existingTask); // Возвращаем обратно
            throw new ManagerSaveException("Обновленная задача пересекается по времени с существующей");
        }

        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task); // Добавляем обновленную задачу
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubTask(SubTask subtask) {

        SubTask existingSubTask = subTasks.get(subtask.getId()); // Удаляем старую версию задачи из отсортированного множества
        getPrioritizedTasks().remove(existingSubTask);

        if (isTaskOverlapping(subtask)) { // Проверяем пересечение для обновленной задачи
            getPrioritizedTasks().add(existingSubTask); // Возвращаем обратно
            throw new ManagerSaveException("Обновленная подзадача пересекается по времени с существующей");
        }

        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateStatus(epic);
        addToPrioritizedTasks(subtask); // Добавляем обновленную задачу
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        getPrioritizedTasks().removeIf(task -> task.getId() == id); // Получаем отсортированный список задач, удаляем все задачи, чей id совпадает с id эпика
        historyManager.remove(id); // Удаляем эпик из истории просмотров по его id

        for (int subTaskId : epic.getSubTaskIds()) { // Удаление подзадач эпика: цикл по всем id подзадач, принадлежащих эпику
            subTasks.remove(subTaskId); // Удаляет подзадачу с указанным ID из subTasks
            getPrioritizedTasks().removeIf(task -> task.getId() == subTaskId); // Получаем отсортированный список подзадач, удаляем подзадачи, чей id совпадает с id эпика
            historyManager.remove(subTaskId); // Удаляем подзадачу из истории просмотров
        }
    }

    @Override
    public void deleteSubTaskById(int id) {
        SubTask subtask = subTasks.remove(id);
        getPrioritizedTasks().remove(subtask);
        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubTaskIds().remove(Integer.valueOf(id));
        updateStatus(epic);
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) {
            return Collections.emptyList();
        }
        return epic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean isTaskOverlapping(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) { // Проверяем, что у задачи задано время начала и продолжительность
            return false; // Если нет - пересечений быть не может
        }

        LocalDateTime newStart = newTask.getStartTime(); // Получаем время начала новой задачи
        LocalDateTime newEnd = newTask.getEndTime(); // Получаем время окончания новой задачи

        return getPrioritizedTasks().stream() // Используем Stream API для проверки пересечений
                .filter(task -> task.getStartTime() != null && task.getDuration() != null) // Фильтруем задачи с заданным временем
                .anyMatch(existingTask -> { // Проверяем пересечение с каждой существующей задачей
                    LocalDateTime existingStart = existingTask.getStartTime();
                    LocalDateTime existingEnd = existingTask.getEndTime();

                    return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd); // Математическая проверка пересечения интервалов
                });
    }

    private void addToPrioritizedTasks(Task task) { // Вспомогательный метод для добавления задачи в отсортированное множество
        if (task.getStartTime() != null) { // Добавляем только задачи с указанным временем начала
            prioritizedTasks.add(task);
        }
    }

    private void updateStatus(Epic epic) {
        boolean allDone = true;
        boolean allNew = true;

        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        for (int subTaskId : epic.getSubTaskIds()) {
            SubTask subtask = subTasks.get(subTaskId);
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private int addId() {
        return counter++;
    }
}