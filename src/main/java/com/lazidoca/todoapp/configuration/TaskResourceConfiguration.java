package com.lazidoca.todoapp.configuration;

import com.lazidoca.todoapp.model.Task;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

/**
 * It is helpful to expose the Task entity's id in the {@link org.springframework.hateoas.PagedResources} so that we
 * can easily delete, update tasks.
 */
@Configuration
public class TaskResourceConfiguration implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(final RepositoryRestConfiguration config) {
        config.exposeIdsFor(Task.class);
    }
}
