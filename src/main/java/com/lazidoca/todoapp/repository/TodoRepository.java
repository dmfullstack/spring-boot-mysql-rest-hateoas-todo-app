package com.lazidoca.todoapp.repository;

import com.lazidoca.todoapp.model.Task;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends PagingAndSortingRepository<Task, String> {
}
