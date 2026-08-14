package com.marginalia.api.dto;

import com.marginalia.api.domain.StepStyle;

import java.util.List;

public record StepListBlockResponse(
        StepStyle stepStyle,
        List<String> steps
) {
}
