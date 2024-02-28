package ru.prodcontest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReasonedError extends Throwable {
    private String reason;
}
