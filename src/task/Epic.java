package task;

import tools.Status;
import tools.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    protected List<SubTask> getSubTasks() {
        return Collections.emptyList();
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    @Override
    public Duration getDuration() {
        List<SubTask> subTasks = getSubTasks();
        if (subTaskIds.isEmpty()) {
            return Duration.ZERO;
        }

        return Duration.ofMinutes(
                subTasks.stream()
                        .filter(subTask -> subTask != null)
                        .mapToLong(subTask -> {
                            Duration duration = subTask.getDuration();
                            if (duration != null) {
                                return duration.toMinutes();
                            }
                            return 0;
                        })
                        .sum()
        );
    }

    @Override
    public LocalDateTime getStartTime() { // Метод, определяющий самое раннее время начала среди подзадач эпика
        List<SubTask> subTasks = getSubTasks();
        if (subTaskIds.isEmpty()) {
            return null;
        }

        return subTasks.stream()
                .filter(subTask -> subTask != null)
                .map(SubTask::getStartTime)
                .filter(startTime -> startTime != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        List<SubTask> subTasks = getSubTasks();
        if (subTaskIds.isEmpty()) {
            return null;
        }

        return subTasks.stream()
                .filter(subTask -> subTask != null)
                .map(SubTask::getEndTime)
                .filter(obj -> obj != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", subTaskIds=" + subTaskIds +
                '}';
    }

    @Override
    public String toStringFromFile() { // Метод преобразует объект эпика в CSV-строку
        long durationMinutes;
        if (getDuration() != null) {
            durationMinutes = getDuration().toMinutes();
        } else {
            durationMinutes = 0;
        }

        String startTimeStr;
        if (getStartTime() != null) {
            startTimeStr = getStartTime().toString();
        } else {
            startTimeStr = "null";
        }

        String endTimeStr;
        if (getEndTime() != null) {
            endTimeStr = getEndTime().toString();
        } else {
            endTimeStr = "null";
        }

        return String.format("%d,%s,%s,%s,%s,%d,%s,%s,%s",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                durationMinutes,
                startTimeStr,
                endTimeStr,
                subTaskIds);
    }
}