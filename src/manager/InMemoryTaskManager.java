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
    protected Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId));

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
        getPrioritizedTasks().removeIf(task -> tasks.containsKey(task.getId()));
        tasks.values().forEach(task -> historyManager.remove(task.getId()));
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        getPrioritizedTasks().removeIf(task -> epics.containsKey(task.getId()) ||
                (task instanceof SubTask && epics.containsKey(((SubTask) task).getEpicId())));
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));
        subTasks.values().forEach(subTask -> historyManager.remove(subTask.getId()));
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        getPrioritizedTasks().removeIf(task -> task instanceof SubTask);
        subTasks.values().forEach(subTask -> historyManager.remove(subTask.getId()));
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
            updateStatus(epic);
        }
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public void addTask(Task task) {
        if (isTaskOverlapping(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей");
        }

        task.setId(addId());
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
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
        Task existingTask = tasks.get(task.getId());
        getPrioritizedTasks().remove(existingTask);

        if (isTaskOverlapping(task)) {
            getPrioritizedTasks().add(existingTask);
            throw new ManagerSaveException("Обновленная задача пересекается по времени с существующей");
        }

        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubTask(SubTask subtask) {

        SubTask existingSubTask = subTasks.get(subtask.getId());
        getPrioritizedTasks().remove(existingSubTask);

        if (isTaskOverlapping(subtask)) {
            getPrioritizedTasks().add(existingSubTask);
            throw new ManagerSaveException("Обновленная подзадача пересекается по времени с существующей");
        }

        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        updateStatus(epic);
        addToPrioritizedTasks(subtask);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        getPrioritizedTasks().removeIf(task -> task.getId() == id);
        historyManager.remove(id);

        for (int subTaskId : epic.getSubTaskIds()) {
            subTasks.remove(subTaskId);
            getPrioritizedTasks().removeIf(task -> task.getId() == subTaskId);
            historyManager.remove(subTaskId);
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
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        return getPrioritizedTasks().stream()
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .anyMatch(existingTask -> {
                    LocalDateTime existingStart = existingTask.getStartTime();
                    LocalDateTime existingEnd = existingTask.getEndTime();

                    return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
                });
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
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