package com.xitricon.workflowservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class TaskOutputDTO {
    private final List<WorkflowTaskDTO> tasks;
}
