import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() throws IOException {
        return new InMemoryTaskManager();
    }

    @Test // Проверяем создание задачи
    void testAddNewTask() {
        TaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);

        taskManager.addTask(task);

        final Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test // Проверяем неизменность задачи (по всем полям) при добавлении задачи в менеджер
    void testTaskImmutabilityWhenAddedToManager() {
        TaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Задача №  1", "Описание задачи № 1", Status.NEW);
        task.setId(1);
        taskManager.addTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertEquals(task.getName(), savedTask.getName(), "Имя задачи изменилось.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи изменилось.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи изменился.");
        assertEquals(task.getId(), savedTask.getId(), "ID задачи изменился.");
    }

    @Test // Проверяем обновление статуса эпика при изменении статуса подзадачи
    void testEpicStatusUpdateWhenSubTaskStatusChanges() {
        TaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик № 1", "Описание эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask);

        subTask.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test  // Проверяем правильность приоритета задач
    void testPrioritizeTasksCorrectly() {
        TaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task("Задача № 1", "Описание задачи № 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 11, 0));
        Task task2 = new Task("Задача № 2", "Описание задачи № 2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 0));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(task2, taskManager.getPrioritizedTasks().getFirst(),
                "Задачи должны быть отсортированы от более ранних к более поздним");
    }
}