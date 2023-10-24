package com.xitricon.workflowservice.dto;

import java.util.ArrayList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Page{
    public int index;
    public String id;
    public String title;
    public ArrayList<Question> questions;
}