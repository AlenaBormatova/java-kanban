package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.SubTask;

import java.io.IOException;
import java.util.List;

class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                    if (path.equals("/subtasks")) {
                        List<SubTask> subtasks = taskManager.getAllSubTasks();
                        sendText(exchange, gson.toJson(subtasks), 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        SubTask subtask = taskManager.getSubTaskById(id);
                        if (subtask != null) {
                            sendText(exchange, gson.toJson(subtask), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "POST":
                    SubTask newSubTask = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), SubTask.class);

                    SubTask existing = taskManager.getSubTaskById(newSubTask.getId());
                    if (existing != null) {
                        if (taskManager.isTaskOverlapping(newSubTask)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.updateSubTask(newSubTask);
                            sendText(exchange, "Подзадача обновлена.", 200);
                        }
                    } else {
                        if (taskManager.isTaskOverlapping(newSubTask)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.addSubTask(newSubTask);
                            sendText(exchange, "Подзадача создана.", 201);
                        }
                    }
                    break;
                case "DELETE":
                    if (path.equals("/subtasks")) {
                        taskManager.deleteAllSubTasks();
                        sendText(exchange, "Все подзадачи удалены.", 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        taskManager.deleteSubTaskById(id);
                        sendText(exchange, "Подзадача удалена.", 200);
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