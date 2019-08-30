package com.lazidoca.todoapp.controller;

import com.lazidoca.todoapp.model.Task;
import com.lazidoca.todoapp.model.TaskStatus;
import com.lazidoca.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
public class TodoController {
    private final TodoRepository todoRepository;

    @Autowired
    public TodoController(final TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Get a task item with a given id
     *
     * @param id the task's id
     * @return the task item if it is existing, not found status code otherwise
     */
    @GetMapping(value = "/todos/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Task> getTodoItem(@PathVariable("id") final String id) {
        try {
            return todoRepository.findById(id)
                    .map(task -> ResponseEntity.ok().body(task))
                    .orElse(ResponseEntity.notFound().build());
        } catch (final Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all tasks with supporting pagination and sorting
     *
     * @return the tasks, not found 404 status code if there is error.
     */
    @GetMapping(value = "/todos", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getAllTodoItems(final Pageable pageable, final PagedResourcesAssembler<Task> assembler) {
        try {
            return new ResponseEntity<>(assembler.toResource(todoRepository.findAll(pageable)),
                    HttpStatus.OK);
        } catch (final Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add new task
     *
     * @return the created status code (201) if success, conflict status code (409) otherwise
     */
    @PostMapping(value = "/todos", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addNewTodoItem(@RequestBody final Task task) {
        try {
            task.setId(UUID.randomUUID().toString());
            task.setStatus(TaskStatus.PLANNING);
            final Task createdTask = todoRepository.save(task);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (final Exception e) {
            return new ResponseEntity<>("Task creation failed", HttpStatus.CONFLICT);
        }
    }

    /**
     * Edit a todo item
     *
     * @param task the edited task from user side
     * @return the updated task if success, not found 404 status code otherwise
     */
    @PutMapping(value = "/todos/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Task> updateTodoItem(@PathVariable("id") final String id,
                                               @Valid @RequestBody final Task task) {
        try {
            return todoRepository.findById(id)
                    .map(todoData -> {
                        todoData.setName(task.getName());
                        todoData.setDueDate(task.getDueDate());
                        todoData.setStatus(task.getStatus());
                        final Task updatedTask = todoRepository.save(todoData);
                        return ResponseEntity.ok().body(updatedTask);
                    }).orElse(ResponseEntity.notFound().build());
        } catch (final Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a task
     *
     * @param id task's id
     * @return the 204 status code if success, bad request otherwise
     */
    @DeleteMapping(value = "/todos/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> deleteTodoItem(@PathVariable("id") final String id) {
        try {
            todoRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (final Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
