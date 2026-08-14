package com.marginalia.api.dto;

import com.marginalia.api.domain.StepStyle;

import java.util.List;

/**
 * Returns nested data for a STEP_LIST content block.
 *
 * @param stepStyle marker style used by the list
 * @param steps ordered step text
 */
public record StepListBlockResponse(
        StepStyle stepStyle,
        List<String> steps
) {
}
