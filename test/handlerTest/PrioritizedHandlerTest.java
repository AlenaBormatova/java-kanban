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

public class PrioritizedHandlerTest {
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

    @Test
    void testGetPrioritizedTasks() throws Exception {
        Task task1 = new Task("Тестовая задача № 1", "Описание тестовой задачи № 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 10, 0));
        taskManager.addTask(task1);

        Task task2 = new Task("Тестовая задача № 2", "Описание тестовой задачи № 2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 5, 1, 12, 0));
        taskManager.addTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Тестовая задача № 1"), "Должен содержать первую задачу");
        assertTrue(response.body().contains("Тестовая задача № 2"), "Должен содержать вторую задачу");

        // Проверяем порядок задач (ранняя задача должна быть первой)
        int indexOfTask1 = response.body().indexOf("Тестовая задача № 1");
        int indexOfTask2 = response.body().indexOf("Тестовая задача № 2");
        assertTrue(indexOfTask1 < indexOfTask2,
                "Тестовая задача № 1 должна быть перед тестовой задачей № 2 в приоритизированном списке");
    }
}
