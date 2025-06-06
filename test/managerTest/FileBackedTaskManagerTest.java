package managerTest;

import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        file = File.createTempFile("Test_file", ".csv");
        return new FileBackedTaskManager(file);
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException { // Проверяет сохранение и загрузку пустого менеджера
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(manager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(manager.getAllSubTasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void testSaveAndLoadTasks() throws IOException { /* Проверяет сохранение и загрузку задач всех типов
                                                       (Task, Epic, SubTask), корректность восстановления данных
                                                       (названия, статусы, связи)*/
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        Task task1 = new Task("Задача № 1", "Описание № 1", Status.NEW);
        manager.addTask(task1);
        Epic epic = new Epic("Эпик № 1", "Описание эпика № 1", Status.NEW);
        manager.addEpic(epic);
        SubTask subTask1 = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        manager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача № 2", "Описание подзадачи № 2", Status.NEW, epic.getId());
        manager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask("Подзадача № 3", "Описание подзадачи № 3", Status.NEW, epic.getId());
        manager.addSubTask(subTask3);
        Task task2 = new Task("Задача № 2", "Описание № 2", Status.NEW);
        manager.addTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> tasks = loadedManager.getAllTasks();
        assertEquals(2, tasks.size(), "Должно быть 2 задачи");
        assertEquals(task1.getName(), tasks.getFirst().getName(), "Названия задач должны совпадать");

        List<Epic> epics = loadedManager.getAllEpics();
        assertEquals(1, epics.size(), "Должен быть 1 эпик");
        assertEquals(epic.getName(), epics.getFirst().getName(), "Названия эпиков должны совпадать");

        List<SubTask> subTasks = loadedManager.getAllSubTasks();
        assertEquals(3, subTasks.size(), "Должно быть 3 подзадачи");
        assertEquals(epic.getId(), subTasks.getFirst().getEpicId(), "ID эпика у подзадачи должен совпадать");
    }

    // Проверяют правильность преобразования задач в CSV-строки, все поля и их порядок
    @Test
    void testTaskSerialization() {
        Task task = new Task("Test Task", "Description", Status.IN_PROGRESS);
        task.setId(1);
        String expected = "1,TASK,Test Task,IN_PROGRESS,Description,0,null";
        assertEquals(expected, task.toStringFromFile(), "Сериализация задачи неверна");
    }

    @Test
    void testEpicSerialization() {
        Epic epic = new Epic("Test Epic", "Epic Description", Status.NEW);
        epic.setId(2);
        String expected = "2,EPIC,Test Epic,NEW,Epic Description,0,null,null,[]";
        assertEquals(expected, epic.toStringFromFile(), "Сериализация эпика неверна");
    }

    @Test
    void testSubTaskSerialization() {
        SubTask subTask = new SubTask("Test SubTask", "Sub Description", Status.DONE, 3);
        subTask.setId(4);
        String expected = "4,SUBTASK,Test SubTask,DONE,Sub Description,0,null,3";
        assertEquals(expected, subTask.toStringFromFile(), "Сериализация подзадачи неверна");
    }
}