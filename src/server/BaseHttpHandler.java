package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        sendResponse(exchange, text, statusCode);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Задачи не существует.", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Задача пересекается с существующими.", 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Ошибка при обработке запроса.", 500);
    }
}