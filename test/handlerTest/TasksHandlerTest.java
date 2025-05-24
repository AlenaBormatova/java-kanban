package handlerTest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import task.Task;
import tools.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksHandlerTest {
    protected TaskManager taskManager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = taskServer.createGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    public void stop() {
        taskServer.stop();
    }

    // Проверяем успешное создание новой задачи
    @Test
    void testAddTask() throws Exception {
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task createdTask = taskManager.getAllTasks().getFirst();
        assertEquals("Тестовая задача № 1", createdTask.getName());
    }

    // Проверяем успешное получение задачи по id
    @Test
    void testGetTaskById() throws Exception {
        Task task = new Task(1, "Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), receivedTask.getId(), "ID задачи должны совпадать");
    }

    // Проверяем успешное обновление задачи
    @Test
    void testUpdateTask() throws Exception {
        Task task = new Task(1, "Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW);
        taskManager.addTask(task);

        Task updateTask = new Task(1, "Обновлённая задача", "Описание обновлённой задачи", Status.DONE);
        updateTask.setId(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + updateTask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updateTask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Обновлённая задача", taskManager.getTaskById(updateTask.getId()).getName());
    }

    // Проверяем успешное удаление задачи
    @Test
    void testDeleteTask() throws Exception {
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    // Проверяем получение несуществующей задачи
    @Test
    void testNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Задачи не существует.", response.body());
    }

    // Проверка создания задачи с конфликтом времени
    @Test
    void testAddTaskWithTimeConflict() throws Exception {
        Task task = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 0));
        taskManager.addTask(task);

        Task conflictingTask = new Task("Конфликтная задача", "Описание конфликтной задачи", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 30));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(conflictingTask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Должен вернуться статус 406 при конфликте времени.");
        assertEquals("Задача пересекается с существующими.", response.body());
    }
}
