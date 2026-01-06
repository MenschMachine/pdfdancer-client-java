package com.pdfdancer.common.model;

/**
 * Defines text reflow behavior when replacing template placeholders.
 * Controls how text is handled when the replacement text differs in size from the placeholder.
 */
public enum ReflowPreset {
    /**
     * Attempts to reflow text to fit, adjusting layout as needed.
     * May not preserve exact original formatting.
     */
    BEST_EFFORT,

    /**
     * Requires text to fit exactly in the placeholder space.
     * Operation fails if text cannot fit.
     */
    FIT_OR_FAIL,

    /**
     * No reflow applied - text is placed as-is without adjustments.
     */
    NONE
}
