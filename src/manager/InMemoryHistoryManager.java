package manager;

import task.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private ArrayList<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) { // Добавляем задачи в историю
        if (history.size() == 10) { // Если задач 10, то
            history.removeFirst(); // удалить самую старую задачу
        }
        history.add(task); // Добавляем новую задачу
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}