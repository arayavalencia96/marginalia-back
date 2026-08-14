package com.marginalia.api.dto;

import com.marginalia.api.domain.StepStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StepListBlockRequest(
        @NotNull StepStyle stepStyle,
        @NotEmpty List<@NotBlank String> steps
) {
}
