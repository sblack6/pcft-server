package com.sblack.pcftserver.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
public class DateSearchConstraints {
    private boolean isValid;
    private boolean isDateSearch;
    private LocalDate start;
    private LocalDate end;
    private String errorMessage;
}
