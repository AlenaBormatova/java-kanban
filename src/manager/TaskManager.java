package manager;

import task.Task;
import task.Epic;
import task.SubTask;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subtask);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubTaskById(int id);

    List<SubTask> getSubTasksByEpicId(int epicId);

    List<Task> getHistory();
}