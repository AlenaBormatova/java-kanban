package task;

import tools.Status;
import tools.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;
    private Duration duration; // Продолжительность задачи
    private LocalDateTime startTime; // Время начала задачи

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                " name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }

    public String toStringFromFile() { // Метод преобразует объект задачи в CSV-строку
        // Обработка duration
        long durationMinutes;
        if (duration != null) {
            durationMinutes = duration.toMinutes(); // Если duration задан, конвертируем в минуты
        } else {
            durationMinutes = 0; // Если duration не задан, используем 0 по умолчанию
        }

        // Обработка startTime
        String startTimeString;
        if (startTime != null) {
            startTimeString = startTime.toString(); // Если startTime задан, конвертируем в строку
        } else {
            startTimeString = "null"; // Если startTime не задан, используем строку "null"
        }

        return String.format("%d,%s,%s,%s,%s,%d,%s",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                durationMinutes, // Используем вычисленное значение продолжительности
                startTimeString); // Используем вычисленное значение времени начала
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}