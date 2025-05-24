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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicsHandlerTest {
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

    // Проверяем успешное добавление эпика
    @Test
    void testAddEpic() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getAllEpics().size());
    }

    // Проверяем получение подзадачи несуществующего эпика
    @Test
    void testGetSubtasksForNonExistentEpic() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Проверяем успешное обновление эпика
    @Test
    void testUpdateEpic() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        Epic updatedEpic = new Epic("Обновлённый эпик № 1", "Описание обновлённого эпика № 1", Status.DONE);
        updatedEpic.setId(epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedEpic)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic epicFromManager = taskManager.getEpicById(epic.getId());
        assertEquals("Обновлённый эпик № 1", epicFromManager.getName());
        assertEquals("Описание обновлённого эпика № 1", epicFromManager.getDescription());
    }

    // Проверяем успешное удаление эпика
    @Test
    void testDeleteEpic() throws Exception {
        Epic epic = new Epic("Тестовый эпик № 1", "Описание тестового эпика № 1", Status.NEW);
        taskManager.addEpic(epic);

        SubTask subTask1 = new SubTask("Подзадача № 1", "Описание подзадачи № 1", Status.NEW, epic.getId());
        SubTask subTask2 = new SubTask("Подзадача № 2", "Описание подзадачи № 2", Status.NEW, epic.getId());
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }

    // Проверяем удаление несуществующего эпика
    @Test
    void testDeleteNonExistentEpic() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}