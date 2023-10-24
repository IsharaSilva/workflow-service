package com.xitricon.workflowservice.dto;

import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserFormResponseOutputDTO {
    private final String id;
    private final String title;
    private final Date createdAt;
    private final String createdBy;
    private final Date modifiedAt;
    private final String modifiedBy;
    private final String activitiType;
    private final ArrayList<Page> pages;
}
