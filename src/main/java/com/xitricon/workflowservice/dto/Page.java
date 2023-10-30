package com.xitricon.workflowservice.dto;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Page {
	public final int index;
	public final String id;
	public final String title;
	public final List<Question> questions;
}
