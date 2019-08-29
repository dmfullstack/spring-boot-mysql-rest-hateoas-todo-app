package com.lazidoca.todoapp.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Task entity
 *
 * @author lazido
 */
@Entity
public class Task implements Serializable {
    private static final long serialVersionUID = 2029368876538814360L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime dueDate;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public Task() {
        // Default constructor for JPA
    }

    public Task(final String id, final String name, final LocalDateTime createdAt, final LocalDateTime dueDate,
                final TaskStatus status) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(final LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(final TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("startAt", createdAt)
                .append("endAt", dueDate)
                .append("status", status)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Task task = (Task) o;

        return new EqualsBuilder()
                .append(getName(), task.getName())
                .append(getCreatedAt(), task.getCreatedAt())
                .append(getDueDate(), task.getDueDate())
                .append(getStatus(), task.getStatus())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getCreatedAt())
                .append(getDueDate())
                .append(getStatus())
                .toHashCode();
    }
}
