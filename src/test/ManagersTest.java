package test;

import manager.TaskManager;
import manager.HistoryManager;
import manager.Managers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    //Проверяем, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void testGetDefaultReturnsInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач должен быть проинициализирован.");
    }

    @Test
    void testGetDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории должен быть проинициализирован.");
    }
}