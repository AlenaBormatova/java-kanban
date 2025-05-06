package manager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyMap = new HashMap<>(); // HashMap для быстрого доступа к узлам по id задачи
    private Node head; // Ссылка на начало списка
    private Node tail; // Ссылка на конец списка

    @Override
    public void add(Task task) {
        int id = task.getId();
        if (historyMap.containsKey(id)) {
            remove(id); // Удаляем старую версию задачи, если она уже есть
        }
        linkLast(task); // Добавляем новую версию задачи
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id); // Получаем узел по ключу (ID)
        if (node != null) {
            historyMap.remove(id); // Удаляем запись из HashMap
            removeNode(node); // Удаляем узел из двусвязного списка
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(task, tail, null);

        if (tail == null) {
            head = newNode; // Первая задача становится головой
        } else {
            tail.next = newNode; // Следующий элемент текущего хвоста — новый узел
            newNode.prev = tail; // Предыдущий элемент нового узла — текущий хвост
        }
        tail = newNode; // Новый узел становится хвостом
        historyMap.put(task.getId(), newNode); // Сохраняем ссылку на узел в map
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev == null) { // Если это первый элемент
            head = node.next;
        } else {
            node.prev.next = node.next;
        }

        if (node.next == null) { // Если это последний элемент
            tail = node.prev;
        } else {
            node.next.prev = node.prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    private static class Node {
        Task task; // Задача
        Node prev; // Ссылка на предыдущий узел
        Node next; // Ссылка на следующий узел

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}