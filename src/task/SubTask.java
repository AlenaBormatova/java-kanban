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
        return this.epicId;
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
        long durationValue;
        if (getDuration() != null) {
            durationValue = getDuration().toMinutes();
        } else {
            durationValue = 0;
        }

        String startTimeStr;
        if (getStartTime() != null) {
            startTimeStr = getStartTime().toString();
        } else {
            startTimeStr = "null";
        }

        return String.format("%d,%s,%s,%s,%s,%d,%s,%d",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                durationValue,
                startTimeStr,
                getEpicId());
    }
}