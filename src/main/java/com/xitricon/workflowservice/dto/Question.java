package com.xitricon.workflowservice.dto;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Question{
    public int index;
    public int id;
    public String label;
    public String type;
    public String group;
    public List<Validation> validations;
    public boolean editable;
    public List<String> response;
}





