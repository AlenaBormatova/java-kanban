package manager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
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

    private final Map<Integer, Node> historyMap = new HashMap<>(); // HashMap для быстрого доступа к узлам по id задачи
    private Node head; // Ссылка на начало списка
    private Node tail; // Ссылка на конец списка

    @Override
    public void add(Task task) {
        remove(task.getId()); // Удалить старую версию задачи, если она уже есть в истории
        linkLast(task); // Добавить задачу в конец списка
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id); // Удалить задачу из historyMap и получить соответствующий узел
        removeNode(node); // Удалить узел из списка, если он существует
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) { // Добавить задачу в конец списка
        final Node newNode = new Node(task, tail, null);

        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        historyMap.put(task.getId(), newNode); // Добавить узел в historyMap
    }


    private void removeNode(Node node) { // Удалить узел из списка
        if (node != null) {
            if (head == node && tail == node) { // Если единственный элемент в списке
                head = null;
                tail = null;
            } else if (head == node) { // Если удаляем голову списка
                head = node.next; // новая голова - следующий элемент
                head.prev = null; // у новой головы обнуляется ссылка на предыдущий элемент
            } else if (tail == node) { // Если удаляем хвост списка
                tail = node.prev; // новый хвост - предыдущий элемент
                tail.next = null; // у нового хвоста обнуляется ссылка на следующий элемент
            } else { // Удаление из середины
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
        }
    }

    private List<Task> getTasks() { // Собрать все задачи из списка в ArrayList
        List<Task> tasks = new ArrayList<>(); // Создать новый ArrayList
        Node current = head;
        while (current != null) {
            tasks.add(current.task); // Добавить все задачи в ArrayList
            current = current.next;
        }
        return tasks; // Вернуть заполненный список
    }
}