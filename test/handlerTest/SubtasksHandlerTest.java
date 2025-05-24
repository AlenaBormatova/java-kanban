package handlerTest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import task.Epic;
import task.SubTask;
import tools.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksHandlerTest {
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

    // Проверяем успешное добавление подзадачи
    @Test
    void testAddSubTask() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(1, taskManager.getAllSubTasks().size());
    }

    // Проверяем успешное обновление подзадачи
    @Test
    void testUpdateSubTask() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW);
        taskManager.addEpic(epic);
        SubTask subTask = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask);

        SubTask updatedSubTask = new SubTask("Обновленная подзадача № 1", "Новое описание подзадачи № 1",
                Status.IN_PROGRESS, epic.getId());
        updatedSubTask.setId(subTask.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedSubTask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200 при успешном обновлении");

        SubTask subTaskFromManager = taskManager.getSubTaskById(subTask.getId());
        assertNotNull(subTaskFromManager, "Подзадача должна существовать после обновления.");
        assertEquals("Обновленная подзадача № 1", subTaskFromManager.getName());
        assertEquals("Новое описание подзадачи № 1", subTaskFromManager.getDescription());
        assertEquals(Status.IN_PROGRESS, subTaskFromManager.getStatus());

        assertTrue(taskManager.getEpicById(epic.getId()).getSubTaskIds().contains(subTask.getId()),
                "Эпик должен содержать ID подзадачи после обновления");
    }

    // Проверяем обновление несуществующей подзадачи
    @Test
    void testUpdateNonExistentSubTask() throws Exception {
        SubTask nonExistentSubTask = new SubTask("Несуществующая подзадача",
                "Описание несуществующей подзадачи", Status.NEW, 999);
        nonExistentSubTask.setId(999);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nonExistentSubTask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    // Проверяем успешное удаление подзадачи
    @Test
    void testDeleteSubTask() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW);
        taskManager.addEpic(epic);
        SubTask subTask = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subTask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertFalse(taskManager.getEpicById(epic.getId()).getSubTaskIds().contains(subTask.getId()),
                "Эпик не должен содержать ID удаленной подзадачи.");
    }
}
