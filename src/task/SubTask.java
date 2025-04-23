package task;

import tools.Status;
import tools.TaskType;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, Status status, int epicId) {
        super(name, description, status);
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
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toStringFromFile() { // Метод преобразует объект подзадачи в CSV-строку
        return String.format("%d,%s,%s,%s,%s,%d",
                getId(),
                getType().name(),
                getName(),
                getStatus().name(),
                getDescription(),
                getEpicId());
    }
}