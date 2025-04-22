package task;

import tools.Status;
import tools.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
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
                ", subTaskIds=" + subTaskIds +
                '}';
    }

    @Override
    public String toStringFromFile() { // Метод преобразует объект эпика в CSV-строку
        return String.format("%d,%s,%s,%s,%s,%s",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                subTaskIds);
    }
}