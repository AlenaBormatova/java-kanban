package managerTest;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void addTaskToHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(1, "Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.getFirst(), "Задача в истории не совпадает с добавленной задачей.");
    }

    @Test
    void testDuplicateTasks() { // Проверка на наличие дубликатов
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        historyManager.add(task);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История должна содержать только один экземпляр задачи");
    }

    @Test
    void testRemoveTaskFromHistory() { // Проверка на удаление задачи из истории
        TaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        Task task2 = new Task("Тестовая задача № 2", "Описание тестовой задачи № 2", Status.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteTaskById(task2.getId());

        List<Task> history = taskManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void testRemoveFirstTask() { // Проверка на удаление первой задачи
        HistoryManager taskManager = new InMemoryHistoryManager();
        Task task1 = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Тестовая задача № 2", "Описание тестовой задачи № 2", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Тестовая задача № 3", "Описание тестовой задачи № 3", Status.NEW);
        task3.setId(3);

        taskManager.add(task1);
        taskManager.add(task2);
        taskManager.add(task3);
        taskManager.remove(1);

        assertEquals(2, taskManager.getHistory().size(), "В истории должно остаться две задачи");
        assertEquals(task2, taskManager.getHistory().getFirst(), "Вторая задача стала первой в истории");
        assertEquals(task3, taskManager.getHistory().get(1), "Третья задача стала второй в истории");
    }

    @Test
    void testRemoveLastTask() { // Проверка на удаление последней задачи
        InMemoryHistoryManager taskManager = new InMemoryHistoryManager();
        Task task1 = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Тестовая задача № 2", "Описание тестовой задачи № 2", Status.NEW);
        task2.setId(2);

        taskManager.add(task1);
        taskManager.add(task2);
        taskManager.remove(2);

        assertEquals(1, taskManager.getHistory().size(), "В истории должна остаться одна задача");
        assertEquals(task1, taskManager.getHistory().getFirst(), "Первая задача должна остаться первой");
    }

    @Test
    void shouldMaintainInsertionOrder() { // Проверка на соблюдение порядка вставки
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task(1, "Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        Task task2 = new Task(2, "Тестовая задача № 2", "Описание тестовой задачи № 2", Status.NEW);
        Task task3 = new Task(3, "Тестовая задача № 3", "Описание тестовой задачи № 3", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task3);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи");
        assertEquals(task1, history.get(0), "Первая задача должна быть 'Тестовая задача № 1'");
        assertEquals(task3, history.get(1), "Вторая задача должна быть 'Тестовая задача № 3'");
        assertEquals(task2, history.get(2), "Третья задача должна быть 'Тестовая задача № 2' (добавлена последней)");
    }

    @Test
    void shouldRemoveSubtaskIdFromEpicWhenSubtaskDeleted() { // Внутри эпиков не остается неактуальных ID подзадач
        TaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик", "Описание эпика", Status.NEW);
        taskManager.addEpic(epic);
        int epicId = epic.getId();

        SubTask subTask1 = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Подзадача № 2", "Описание подзадачи № 2", Status.NEW, epicId);
        SubTask subTask3 = new SubTask("Подзадача № 3", "Описание подзадачи № 3", Status.NEW, epicId);

        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);
        taskManager.addSubTask(subTask3);

        assertEquals(3, epic.getSubTaskIds().size(), "Эпик должен содержать 3 подзадачи");
        assertTrue(epic.getSubTaskIds().containsAll(List.of(subTask1.getId(), subTask2.getId(), subTask3.getId())),
                "Эпик должен содержать все добавленные подзадачи");

        taskManager.deleteSubTaskById(subTask2.getId());

        assertEquals(2, epic.getSubTaskIds().size(), "Эпик должен содержать 2 подзадачи после удаления");
        assertTrue(epic.getSubTaskIds().contains(subTask1.getId()), "Эпик должен содержать подзадачу 1");
        assertTrue(epic.getSubTaskIds().contains(subTask3.getId()), "Эпик должен содержать подзадачу 3");
        assertFalse(epic.getSubTaskIds().contains(subTask2.getId()), "Эпик НЕ должен содержать удаленную подзадачу 2");

        taskManager.deleteSubTaskById(subTask1.getId());
        taskManager.deleteSubTaskById(subTask3.getId());

        assertTrue(epic.getSubTaskIds().isEmpty(), "Список подзадач эпика должен быть пустым после их удаления");

        assertTrue(taskManager.getSubTasksByEpicId(epicId).isEmpty(),
                "Менеджер не должен возвращать удаленные подзадачи для эпика");
    }

    @Test
    void testEmptyHistory() { // Проверяем, что история изначально пуста
        HistoryManager historyManager = new InMemoryHistoryManager();
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой изначально");
    }

    @Test
    void testRemoveFromMiddle() { //  Проверка на удаление задачи из середины истории
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Задача № 1", "Описание задачи № 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача № 2", "Описание задачи № 2", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Задача № 3", "Описание задачи № 3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории должно быть 2 задачи после удаления.");
        assertEquals(task1, history.get(0), "Первая задача должна остаться.");
        assertEquals(task3, history.get(1), "Третья задача должна переместиться на вторую позицию.");
    }
}