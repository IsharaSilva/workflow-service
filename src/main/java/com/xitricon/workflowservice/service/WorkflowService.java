package com.xitricon.workflowservice.service;


import org.springframework.stereotype.Service;

import com.xitricon.workflowservice.dto.TaskOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;


@Service
public interface WorkflowService {
    public UserFormResponseOutputDTO getRequestQuestionnaire();
    public UserFormResponseOutputDTO handleQuestionnaireSubmission(UserFormRequestInputDTO inputDto);
    public TaskOutputDTO getListOfWorkflows();
}
