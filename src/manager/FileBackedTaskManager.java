package manager;

import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;
import tools.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> fileData = Files.readAllLines(file.toPath());

            for (int i = 1; i < fileData.size(); i++) { // Начинаем с индекса 1 (пропускаем заголовок)
                Task task = fromString(fileData.get(i)); // Каждая строка преобразуется в объект Task через fromString()

                if (task != null) {
                    switch (task.getType()) { // Распределяем задачи в зависимости от типа задачи
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            // manager.addToPrioritizedTasks(task);
                            break;
                        case EPIC:
                            manager.epics.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            SubTask subTask = (SubTask) task;
                            manager.subTasks.put(subTask.getId(), subTask);
                            // manager.addToPrioritizedTasks(subTask);
                            break;
                    }
                    if (task.getId() >= manager.counter) { // Обновление счётчика ID
                        manager.counter = task.getId() + 1; // Если ID больше - новые задачи получают уникальные ID
                    }
                }
            }

            // Связь подзадач с эпиками
            for (SubTask subTask : manager.subTasks.values()) { // Проходим по всем подзадачам
                Epic epic = manager.epics.get(subTask.getEpicId()); // Для каждой задачи находим соответствующий эпик по epicId
                if (epic != null) {
                    epic.addSubTaskId(subTask.getId()); // Если эпик существует - добавляем ID подзадачи в его список
                }
            }

        } catch (IOException e) { // Если возникла ошибка ввода-вывода - исключение
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }
        return manager;
    }

    // Переопределяем все методы, изменяющие состояние, чтобы вызывать save()
    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubTask(SubTask subtask) {
        super.addSubTask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subtask) {
        super.updateSubTask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubTaskById(int id) {
        super.deleteSubTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    private void save() {
        try {
            List<String> lines = new ArrayList<>(); // Создаётся пустой список lines для хранения строк файла
            lines.add("id,type,name,description,status,startTime,duration,endTime,subTaskId"); // Первой добавляется строка-заголовок с названиями полей в CSV-формате

            for (Task task : getAllTasks()) { // Для каждой задачи из getAllTasks()
                lines.add(task.toStringFromFile()); // Каждая задача преобразуется в CSV-строку, строка добавляется в список lines
            }

            for (Epic epic : getAllEpics()) { // Для каждого эпика из getAllEpics()
                lines.add(epic.toStringFromFile()); // Каждый эпик преобразуется в CSV-строку, строка добавляется в список lines
            }

            for (SubTask subtask : getAllSubTasks()) { // Сохраняются все подзадачи через getAllSubTasks()
                lines.add(subtask.toStringFromFile()); // Каждая подзадача преобразуется в CSV-строку, строка добавляется в список lines
            }

            Files.write(file.toPath(), lines); // Весь подготовленный список строк записывается в файл
        } catch (IOException e) { // Если возникла ошибка ввода-вывода - исключение
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private static Task fromString(String value) { // Метод преобразует CSV-строку обратно в объект задачи
        String[] fields = value.split(","); // Разбиваем строку по запятым на массив fields
        if (fields.length < 5) {
            return null; // Если полей меньше 5 (минимально необходимых) → возвращаем null (некорректная строка)
        }

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        Duration duration = (fields.length > 5 && !fields[5].equals("0")) ?
                Duration.ofMinutes(Long.parseLong(fields[5])) : null;
        LocalDateTime startTime = (fields.length > 6 && !fields[6].equals("null")) ?
                LocalDateTime.parse(fields[6]) : null;

        switch (type) { // Создание объекта в зависимости от типа
            case TASK:
                Task task = new Task(name, description, status, duration, startTime); // Создаётся новый Task, устанавливаются поля
                task.setId(id); // Задаётся ID
                return task; // Возвращается созданный объект
            case EPIC:
                Epic epic = new Epic(name, description, status, duration, startTime); // Создаётся новый Epic, устанавливаются поля
                epic.setId(id); // Задаётся ID
                return epic; // Возвращается созданный объект
            case SUBTASK:
                int epicId = parseEpicId(fields);
                SubTask subTask = new SubTask(name, description, status, epicId, duration, startTime); // Создаётся SubTask с привязкой к эпику
                subTask.setId(id);
                return subTask; // Возвращается созданный объект
            default:
                return null; // При ошибках
        }
    }

    private static int parseEpicId(String[] fields) {
        if (fields.length > 7 && !fields[7].isEmpty()) {
            try {
                return Integer.parseInt(fields[7].trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}