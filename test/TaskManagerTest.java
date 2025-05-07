import manager.ManagerSaveException;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    @Test // Проверка добавления и получения задачи
    void testAddAndGetTask() {
        Task task = new Task("Задача № 1", "Описание задачи № 1", Status.NEW);
        taskManager.addTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        assertNotNull(savedTask, "Задача должна быть сохранена и доступна для извлечения");
        assertEquals(task, savedTask, "Сохраненная задача должна быть равна исходной");
    }

    @Test // Проверка добавления и получения эпика
    void testAddAndGetEpic() {
        Epic epic = new Epic("Эпик № 1", "Описание эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epic.getId());
        assertNotNull(savedEpic, "Epic должен быть сохранен и доступен для извлечения");
        assertEquals(epic, savedEpic, "Сохраненный эпик должен соответствовать исходному");
    }

    @Test // Проверка добавления и получения подзадачи
    void testAddAndGetSubTask() {
        Epic epic = new Epic("Эпик № 1", "Описание эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask);

        SubTask savedSubTask = taskManager.getSubTaskById(subTask.getId());
        assertNotNull(savedSubTask, "Подзадача должна быть сохранена и доступна для извлечения");
        assertEquals(subTask, savedSubTask, "Сохраненная подзадача должна быть равна исходной");
    }

    @Test // Проверка обновления статуса задачи
    void shouldUpdateTaskStatus() {
        Task task = new Task("Задача № 1", "Описание задачи № 1", Status.NEW);
        taskManager.addTask(task);

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        assertEquals(Status.IN_PROGRESS, taskManager.getTaskById(task.getId()).getStatus());
    }

    @Test // Проверяем пересекающиеся задачи
    void testTaskOverlapping() {
        Task task1 = new Task("Задача № 1", "Описание задачи № 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 0));
        taskManager.addTask(task1);

        Task overlappingTask = new Task("Пересекающаяся задача", "Описание пересекающейся задачи", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 30));

        assertThrows(ManagerSaveException.class, () -> taskManager.addTask(overlappingTask),
                "Должно генерироваться исключение для пересекающихся задач.");
    }

    @Test // Проверяем непересекающиеся задачи
    void testNoOverlapping() {
        Task task1 = new Task("Задача № 1", "Описание задачи № 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 0));
        taskManager.addTask(task1);

        Task nonOverlappingTask = new Task("Непересекающаяся задача", "Описание непересекающейся задачи", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 11, 0));

        assertDoesNotThrow(() -> taskManager.addTask(nonOverlappingTask),
                "Не следует создавать исключение для непересекающихся задач.");
    }

    @Test // Проверяем корректность расчёта статуса эпика, когда все подзадачи имеют статус NEW
    void testEpicStatusWithSubtasks() {
        Epic epic = new Epic("Эпик № 1", "Описание эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        SubTask subTask1 = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        SubTask subTask2 = new SubTask("Подзадача № 2", "Описание подзадачи № 2", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        assertEquals(Status.NEW, epic.getStatus(),
                "Если все подзадачи Epic имеют статус NEW, то и Epic должен иметь статус NEW.");

        // Проверяем корректность расчёта статуса эпика, когда подзадачи имеют разные статусы (NEW и DONE)
        subTask2.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус Epic должен быть IN_PROGRESS, если подзадачи имеют разные статусы.");

        // Проверяем корректность расчёта статуса эпика, когда все подзадачи имеют статус DONE
        subTask1.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask1);
        assertEquals(Status.DONE, epic.getStatus(),
                "Если все подзадачи Epic имеют статус DONE, то и Epic должен иметь статус DONE.");

        // Проверяем корректность расчёта статуса эпика, когда все подзадачи имеют статус IN_PROGRESS
        subTask1.setStatus(Status.IN_PROGRESS);
        subTask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Если все подзадачи Epic имеют статус IN_PROGRESS, то и Epic должен иметь статус IN_PROGRESS.");
    }
}