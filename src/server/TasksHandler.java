package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.equals("/tasks")) {
                        List<Task> tasks = taskManager.getAllTasks();
                        sendText(exchange, gson.toJson(tasks), 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        Task task = taskManager.getTaskById(id);
                        if (task != null) {
                            sendText(exchange, gson.toJson(task), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "POST":
                    Task newTask = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Task.class);

                    // Проверяем, новая это задача или обновление существующей
                    if (newTask.getId() == 0) { // Новая задача
                        if (taskManager.isTaskOverlapping(newTask)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.addTask(newTask);
                            sendText(exchange, "Задача создана.", 201);
                        }
                    } else {
                        Task existing = taskManager.getTaskById(newTask.getId()); // Обновление существующей задачи
                        if (existing == null) {
                            sendNotFound(exchange);
                            break;
                        }
                        if (taskManager.isTaskOverlapping(newTask)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.updateTask(newTask);
                            sendText(exchange, "Задача обновлена.", 200);
                        }
                    }
                    break;
                case "DELETE":
                    if (path.equals("/tasks")) {
                        taskManager.deleteAllTasks();
                        sendText(exchange, "Все задачи удалены.", 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        taskManager.deleteTaskById(id);
                        sendText(exchange, "Задача удалена.", 200);
                    }
                    break;
            }
        } catch (IOException e) {
            sendInternalError(exchange);
        } catch (Exception e) {
            sendNotFound(exchange);
        }
    }
}