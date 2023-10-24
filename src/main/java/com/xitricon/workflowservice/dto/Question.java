package com.xitricon.workflowservice.dto;

import java.util.ArrayList;

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
    public ArrayList<Validation> validations;
    public boolean editable;
    public ArrayList<String> response;
}





