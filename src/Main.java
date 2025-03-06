public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Задача №1", "Описание задачи №1", Status.NEW);
        Task task2 = new Task("Задача №2", "Описание задачи №2", Status.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик №1", "Описание эпика №1", Status.NEW);
        taskManager.addEpic(epic1);
        SubTask subtask1 = new SubTask("Подзадача №1", "Описание подзадачи №1", Status.NEW, epic1.getId());
        SubTask subtask2 = new SubTask("Подзадача №2", "Описание подзадачи №2", Status.NEW, epic1.getId());
        taskManager.addSubTask(subtask1);
        taskManager.addSubTask(subtask2);

        Epic epic2 = new Epic("Эпик №2", "Описание эпика №2", Status.NEW);
        taskManager.addEpic(epic2);
        SubTask subtask3 = new SubTask("Подзадача №3", "Описание подзадачи №3", Status.NEW, epic2.getId());
        taskManager.addSubTask(subtask3);

        System.out.println("Список всех задач:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nСписок всех эпиков:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nСписок всех подзадач:");
        for (SubTask subTask : taskManager.getAllSubTasks()) {
            System.out.println(subTask);
        }

        System.out.println("\nПолучаем задачу с id=1:");
        System.out.println(taskManager.getTaskById(1));

        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);

        System.out.println("\nСписок всех задач после обновления:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        taskManager.deleteTaskById(2);
        System.out.println("\nСписок всех задач после удаления задачи с id=2:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        taskManager.deleteAllTasks();
        System.out.println("\nСписок всех задач после удаления всех задач:");
        System.out.println(taskManager.getAllTasks());

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubTask(subtask1);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subtask2);
        subtask3.setStatus(Status.DONE);
        taskManager.updateSubTask(subtask3);

        System.out.println("\nСписок всех подзадач после обновления:");
        for (SubTask subTask : taskManager.getAllSubTasks()) {
            System.out.println(subTask);
        }

        System.out.println("\nСписок всех эпиков после обновления:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        taskManager.deleteEpicById(3);
        System.out.println("\nСписок всех эпиков после удаления эпика с id=3:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
    }
}