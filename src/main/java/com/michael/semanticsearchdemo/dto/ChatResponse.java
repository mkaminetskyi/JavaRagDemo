package com.michael.semanticsearchdemo.dto;

import java.util.List;

public record ChatResponse(String answer, List<String> relevantSources) {
} 