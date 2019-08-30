package com.lazidoca.todoapp.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
public class TaskTest {
    @Test
    public void testEqualsObject() {
        final LocalDateTime now = LocalDateTime.now();
        final Task task1 = new Task(UUID.randomUUID().toString(), "Task", now, null,
                TaskStatus.PLANNING);
        final Task task2 = new Task(UUID.randomUUID().toString(), "Task", now, null,
                TaskStatus.PLANNING);
        final Task task3 = new Task();
        final Object nonTodoItem = new Object();
        assertNotEquals(nonTodoItem, task3);
        assertEquals(task1, task2);
        assertNotEquals(task1, task3);
        assertNotEquals(task2, task3);
    }
}