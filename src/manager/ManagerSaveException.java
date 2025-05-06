package manager;

public class ManagerSaveException extends RuntimeException { // Исключение для ошибок сохранения
    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}