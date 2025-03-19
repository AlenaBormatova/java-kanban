package test;

import manager.TaskManager;
import manager.InMemoryTaskManager;
import task.Epic;
import task.SubTask;
import task.Task;
import tools.Status;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test // Проверяем создание задачи
    void testAddNewTask() {
        TaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);

        taskManager.addTask(task);

        final Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");   // Проверяем, что задача найдена
        assertEquals(task, savedTask, "Задачи не совпадают."); /* Проверяем, что добавленная задача совпадает
                                                                           с сохраненной */
        final List<Task> tasks = taskManager.getAllTasks(); // Получаем список всех задач

        assertNotNull(tasks, "Задачи не возвращаются."); // Проверяем, что список задач не пустой
        assertEquals(1, tasks.size(), "Неверное количество задач.");  /* Проверяем, что в списке ровно
                                                                                          одна задача */
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают."); /* Проверяем, что задача в списке
                                                                                  совпадает с добавленной*/
    }

    @Test // Проверяем неизменность задачи (по всем полям) при добавлении задачи в менеджер
    void testTaskImmutabilityWhenAddedToManager() {
        TaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Задача №  1", "Описание задачи № 1", Status.NEW); // Создаем задачу
        task.setId(1);
        taskManager.addTask(task); // Добавляем задачу в менеджер

        Task savedTask = taskManager.getTaskById(task.getId()); // Получаем задачу из менеджера

        // Проверяем, что все поля задачи остались неизменными
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

        subTask.setStatus(Status.DONE); // Меняем статус подзадачи
        taskManager.updateSubTask(subTask);

        // Проверяем, что статус эпика обновился
        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }
}