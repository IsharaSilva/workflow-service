package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.Getter;

@Getter
public class QuestionnaireOutputDTO {
    private final String id;
    private final String title;

    @JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
    private final LocalDateTime createdAt;
    
    private final String createdBy;
    
    @JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
    private final LocalDateTime modifiedAt;
    
    private final String modifiedBy;
    private final List<Page> pages;

    @JsonCreator
    public QuestionnaireOutputDTO(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("createdBy") String createdBy,
    @JsonProperty("createdAt") LocalDateTime createdAt, @JsonProperty("modifiedBy") String modifiedBy,
			@JsonProperty("modifiedAt") LocalDateTime modifiedAt,
			@JsonProperty("pages") List<Page> pages) {
        super();
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.pages = pages;
    }    
}
