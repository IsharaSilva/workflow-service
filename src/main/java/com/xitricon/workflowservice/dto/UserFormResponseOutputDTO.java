package com.xitricon.workflowservice.dto;

import java.util.List;

import java.time.LocalDateTime;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xitricon.workflowservice.util.CommonConstant;

@Getter
public class UserFormResponseOutputDTO {
    private final String id;
    private final String title;

    @JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime modifiedAt;
    private final String modifiedBy;
    private final String activitiType;
    private final List<Page> pages;

    @JsonCreator
    public UserFormResponseOutputDTO(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("createdBy") String createdBy,
    @JsonProperty("createdAt") LocalDateTime createdAt, @JsonProperty("modifiedBy") String modifiedBy,
    @JsonProperty("modifiedAt") LocalDateTime modifiedAt, @JsonProperty("activitiType") String activitiType, @JsonProperty("pages") List<Page> pages) {
        super();
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.activitiType = activitiType;
        this.pages = pages;
    }
}
