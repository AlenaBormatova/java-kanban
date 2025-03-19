package test;

import manager.TaskManager;
import manager.InMemoryTaskManager;
import task.Task;
import tools.Status;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryHistoryManagerTest {
    @Test
    void addTaskToHistory() {
        TaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId()); // Добавляем задачу в историю (это происходит при вызове getTaskById)

        final List<Task> history = taskManager.getHistory(); // Получаем историю просмотров

        assertNotNull(history, "История не должна быть null.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.getFirst(), "Задача в истории не совпадает с добавленной задачей.");
    }
}