package com.xitricon.workflowservice.model;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Page{
    public int index;
    public String id;
    public String title;
    public List<Question> questions;
}
