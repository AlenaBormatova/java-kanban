package task;

import tools.Status;
import tools.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                " name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toStringFromFile() { // Метод преобразует объект подзадачи в CSV-строку
        // Обработка продолжительности (duration)
        long durationValue;
        if (getDuration() != null) {
            durationValue = getDuration().toMinutes(); // Если продолжительность задана, получаем минуты
        } else {
            durationValue = 0; // Если продолжительность не задана, используем 0
        }

        // Обработка времени начала (startTime)
        String startTimeStr;
        if (getStartTime() != null) {
            startTimeStr = getStartTime().toString(); // Если время задано, преобразуем в строку
        } else {
            startTimeStr = "null"; // Если время не задано, используем строку "null"
        }

        return String.format("%d,%s,%s,%s,%s,%d,%s,%d",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                durationValue, // Подставляем обработанное значение продолжительности
                startTimeStr,  // Подставляем обработанное значение времени начала
                getEpicId());  // ID эпика, к которому относится подзадача
    }
}