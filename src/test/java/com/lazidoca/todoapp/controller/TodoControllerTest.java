package com.lazidoca.todoapp.controller;

import com.lazidoca.todoapp.model.Task;
import com.lazidoca.todoapp.model.TaskStatus;
import com.lazidoca.todoapp.repository.TodoRepository;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(TodoController.class)
public class TodoControllerTest {
    private final Map<String, Task> repository = new HashMap<>();
    private final Task task1 = new Task(UUID.randomUUID().toString(), "Task 1", LocalDateTime.now(), null,
            TaskStatus.PLANNING);
    private final Task task2 = new Task(UUID.randomUUID().toString(), "Task 2", LocalDateTime.now(),
            LocalDateTime.of(2019, 10, 1, 9, 30, 0), TaskStatus.COMPLETED);

    @Autowired
    private MockMvc mvc;
    @MockBean
    private TodoRepository todoRepository;

    @Before
    public void setUp() {
        repository.clear();
        repository.put(task1.getId(), task1);
        repository.put(task2.getId(), task2);

        given(todoRepository.save(any(Task.class))).willAnswer((InvocationOnMock invocation) -> {
            final Task task = invocation.getArgument(0);
            repository.put(task.getId(), task);
            return task;
        });

        given(todoRepository.findById(any(String.class))).willAnswer((InvocationOnMock invocation) -> {
            final String id = invocation.getArgument(0);
            return Optional.of(repository.get(id));
        });

        given(todoRepository.findAll(any(Pageable.class))).willAnswer((InvocationOnMock invocation) -> {
            final Pageable pageable = invocation.getArgument(0);
            final List<Task> totalTasks = Arrays.asList(task1, task2);
            return new PageImpl<>(totalTasks.subList(0, Math.min(pageable.getPageSize(), totalTasks.size())));
        });

        willAnswer((InvocationOnMock invocation) -> {
            final String id = invocation.getArgument(0);
            if (!repository.containsKey(id)) {
                throw new Exception("Not Found.");
            }
            repository.remove(id);
            return null;
        }).given(todoRepository).deleteById(any(String.class));
    }

    @After
    public void tearDown() {
        repository.clear();
    }

    @Test
    public void testCanGetTaskItem() throws Exception {
        verifyNoMoreInteractions(todoRepository);
        mvc.perform(get("/todos/" + task1.getId()))
                .andExpect(jsonPath("$.id", is(task1.getId())))
                .andExpect(jsonPath("$.name", is(task1.getName())))
                .andExpect(jsonPath("$.createdAt",
                        is(task1.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.dueDate", nullValue()))
                .andExpect(status().isOk());
    }

    @Test
    public void testCouldNotGetNonExistingTaskItem() throws Exception {
        final Task task = new Task(UUID.randomUUID().toString(), "Task 3", LocalDateTime.now(), null,
                TaskStatus.PLANNING);
        mvc.perform(get("/todos/" + task.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void canGetAllTodoItemsWithPagination() throws Exception {
        mvc.perform(get("/todos").param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(0)))
                .andExpect(jsonPath("$.page.totalElements", is(1)))
                .andExpect(jsonPath("$.page.totalPages", is(1)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$._embedded.tasks[0].id", is(task1.getId())))
                .andExpect(jsonPath("$._embedded.tasks[0].name", is(task1.getName())))
                .andExpect(jsonPath("$._embedded.tasks[0].createdAt",
                        is(task1.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$._embedded.tasks[0].dueDate",
                        is(task1.getDueDate())))
                .andExpect(jsonPath("$._embedded.tasks[0].status", is(task1.getStatus().toString())))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn();
    }

    @Test
    public void canGetAllTodoItemsWithSortingByNameDesc() throws Exception {
        mvc.perform(get("/todos").param("sort", "name,desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(0)))
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$.page.totalPages", is(1)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn();
    }

    @Test
    public void canAddNewTodoItem() throws Exception {
        final int size = repository.size();
        final Task task3 = new Task(UUID.randomUUID().toString(), "Task 3", LocalDateTime.now(),
                LocalDateTime.of(2019, 10, 1, 9, 30, 0), TaskStatus.DOING);
        final String payload = String.format("{\n" +
                "\t\"name\": \"%s\",\n" +
                "\t\"dueDate\": \"%s\",\n" +
                "\t\"status\": \"%s\"\n" +
                "}", task3.getName(), task3.getDueDate(), task3.getStatus().toString());
        mvc.perform(post("/todos/").contentType(MediaType.APPLICATION_JSON_VALUE).content(payload))
                .andDo(print())
                .andExpect(status().isCreated());
        assertEquals(size + 1, repository.size());
    }

    @Test
    public void canUpdateTodoItem() throws Exception {
        final String payload = String.format("{\n" +
                "\t\"id\": \"%s\",\n" +
                "\t\"name\": \"Another task\",\n" +
                "\t\"dueDate\": \"2019-09-29T12:54:49\",\n" +
                "\t\"status\": \"COMPLETED\"\n" +
                "}", task1.getId());
        mvc.perform(put("/todos/" + task1.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(payload))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals("Another task", repository.get(task1.getId()).getName());
        assertEquals("2019-09-29T12:54:49",
                repository.get(task1.getId()).getDueDate().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals(TaskStatus.COMPLETED, repository.get(task1.getId()).getStatus());
    }

    @Test
    public void canDeleteTodoItem() throws Exception {
        final int size = repository.size();
        verifyNoMoreInteractions(todoRepository);
        mvc.perform(delete("/todos/" + task1.getId()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
        assertEquals(size - 1, repository.size());
        assertFalse(repository.containsKey(task1.getId()));
        verify(todoRepository, times(1)).deleteById(task1.getId());
    }

    @Test
    public void cannotDeleteNonExistingTodoItem() throws Exception {
        final int size = repository.size();
        mvc.perform(delete("/todos/not-existing-id"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
        TestCase.assertEquals(size, repository.size());
    }
}