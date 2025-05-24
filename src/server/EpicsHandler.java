package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.util.List;

class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
                    if (path.equals("/epics")) {
                        List<Epic> epics = taskManager.getAllEpics();
                        sendText(exchange, gson.toJson(epics), 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        Epic epic = taskManager.getEpicById(id);
                        if (epic != null) {
                            sendText(exchange, gson.toJson(epic), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "POST":
                    Epic newEpic = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Epic.class);

                    // Проверяем, новый это эпик или обновление существующего
                    if (newEpic.getId() == 0) { // Новый эпик
                        if (taskManager.isTaskOverlapping(newEpic)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.addEpic(newEpic);
                            sendText(exchange, "Эпик создан.", 201);
                        }
                    } else {
                        Epic existing = taskManager.getEpicById(newEpic.getId()); // Обновление существующего эпика
                        if (existing == null) {
                            sendNotFound(exchange);
                            break;
                        }
                        if (taskManager.isTaskOverlapping(newEpic)) {
                            sendHasInteractions(exchange);
                        } else {
                            taskManager.updateEpic(newEpic);
                            sendText(exchange, "Эпик обновлён.", 200);
                        }
                    }
                    break;
                case "DELETE":
                    if (path.equals("/epics")) {
                        taskManager.deleteAllEpics();
                        sendText(exchange, "Все эпики удалены.", 200);
                    } else {
                        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                        taskManager.deleteEpicById(id);
                        sendText(exchange, "Эпик удалён.", 200);
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