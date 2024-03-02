package ru.prodcontest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PostCreationRequest {
    private String content;
    private List<String> tags;
}
