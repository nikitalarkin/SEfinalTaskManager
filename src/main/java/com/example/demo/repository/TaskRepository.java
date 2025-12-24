package com.example.demo.repository;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProjectId(Long projectId);
    List<Task> findAllByAssignedToId(Long assignedToId);
    List<Task> findAllByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findAllByProjectIdIn(Collection<Long> projectIds);
}
