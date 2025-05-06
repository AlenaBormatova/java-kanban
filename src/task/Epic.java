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

    // Метод для получения списка подзадач (должен вызываться из менеджера)
    protected List<SubTask> getSubTasks() {
        return Collections.emptyList();
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    @Override
    public Duration getDuration() {
        List<SubTask> subTasks = getSubTasks(); // Получение списка подзадач
        if (subTaskIds.isEmpty()) { // Проверяем, есть ли у эпика подзадачи
            return Duration.ZERO; // Если нет подзадач - продолжительность 0
        }

        return Duration.ofMinutes( // Преобразуем суммарные минуты обратно в объект Duration, возвращает итоговую продолжительность эпика
                subTasks.stream() // Преобразуем список в поток Stream для функциональной обработки
                        .filter(subTask -> subTask != null) // Отфильтровываем возможные null-значения в списке подзадач
                        .mapToLong(subTask -> { // Преобразование в минуты
                            Duration duration = subTask.getDuration(); // Получаем продолжительность подзадачи
                            if (duration != null) { // Если продолжительность задана
                                return duration.toMinutes(); // Конвертируем в минуты
                            }
                            return 0; // Если продолжительность не задана, считаем 0 минут
                        })
                        .sum() // Суммируем все значения продолжительности подзадач в минутах
        );
    }

    @Override
    public LocalDateTime getStartTime() { // Метод, определяющий самое раннее время начала среди подзадач эпика
        List<SubTask> subTasks = getSubTasks(); // Получение списка всех подзадач текущего эпика
        if (subTaskIds.isEmpty()) { // Проверка на отсутствие подзадач - список ID пуст
            return null; // Если нет подзадач - время начала не определено
        }

        return subTasks.stream() // Преобразуем список подзадач в Stream для функциональной обработки
                .filter(subTask -> subTask != null) // Удаляем из потока все подзадачи, которые могут быть null
                .map(SubTask::getStartTime) // Для каждой подзадачи получаем её время начала
                .filter(startTime -> startTime != null) // Удаляем все null-значения времени начала, остаются только подзадачи с заданным временем начала
                .min(LocalDateTime::compareTo) // Находим самое раннее время начала
                .orElse(null); /* Если после всех фильтраций не осталось ни одного времени (все подзадачи без времени),
                                        возвращаем null, иначе возвращаем найденное минимальное время */
    }

    @Override
    public LocalDateTime getEndTime() {
        List<SubTask> subTasks = getSubTasks(); // Получение списка всех подзадач текущего эпика
        if (subTaskIds.isEmpty()) { // Проверка на отсутствие подзадач
            return null; // Если нет подзадач - время не определено
        }

        return subTasks.stream() // Преобразуем список подзадач в Stream для обработки в функциональном стиле
                .filter(subTask -> subTask != null) // Удаляем из потока все подзадачи, которые могут быть null
                .map(SubTask::getEndTime) // Для каждой подзадачи получаем её время окончания
                .filter(obj -> obj != null) // Удаляем все null-значения времени окончания, остаются только подзадачи с заданным временем окончания
                .max(LocalDateTime::compareTo) // Находим самое позднее время окончания среди всех подзадач
                .orElse(null); // Если Stream пуст (нет подзадач с временем), возвращает null, иначе возвращает найденное максимальное время
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
        // Обработка продолжительности (duration)
        long durationMinutes; // Продолжительность в минутах
        if (getDuration() != null) {
            durationMinutes = getDuration().toMinutes();
        } else {
            durationMinutes = 0;
        }

        // Обработка времени начала (startTime)
        String startTimeStr; // Строковое представление времени начала
        if (getStartTime() != null) {
            startTimeStr = getStartTime().toString();
        } else {
            startTimeStr = "null";
        }

        // Обработка времени окончания (endTime)
        String endTimeStr; // Строковое представление времени окончания
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