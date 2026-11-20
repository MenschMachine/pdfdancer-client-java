package com.pdfdancer.common.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TextLayoutRequest {
    public enum Mode {
        sourceAnchored,
        reflowWhenSupported,
        requireReflow
    }

    public enum Profile {
        DEFAULT("default"),
        BODY_TEXT("bodyText"),
        NO_REFLOW("noReflow");

        private final String value;

        Profile(String value) {
            this.value = value;
        }

        @JsonValue
        public String value() {
            return value;
        }

        @JsonCreator
        public static Profile fromValue(String value) {
            for (Profile profile : values()) {
                if (profile.value.equals(value)) {
                    return profile;
                }
            }
            throw new IllegalArgumentException("Unknown text layout profile: " + value);
        }
    }

    /**
     * Explicit alignment of each edited line. Valid only for text replace.
     *
     * <p>Each edited line keeps its original visible text extent as the reference:
     * {@code LEFT} keeps the left edge, {@code RIGHT} keeps the right edge, {@code CENTERED}
     * keeps the midpoint, and {@code JUSTIFIED} keeps both edges by changing the word gaps
     * (the last line of a paragraph stays left-aligned).
     */
    public enum Alignment {
        LEFT("left"),
        RIGHT("right"),
        CENTERED("centered"),
        JUSTIFIED("justified");

        private final String value;

        Alignment(String value) {
            this.value = value;
        }

        @JsonValue
        public String value() {
            return value;
        }

        @JsonCreator
        public static Alignment fromValue(String value) {
            for (Alignment alignment : values()) {
                if (alignment.value.equals(value)) {
                    return alignment;
                }
            }
            throw new IllegalArgumentException("Unknown text layout alignment: " + value);
        }
    }

    @JsonProperty("mode")
    private final Mode mode;
    @JsonProperty("profile")
    private final Profile profile;
    @JsonProperty("hyphenationEnabled")
    private final Boolean hyphenationEnabled;
    @JsonProperty("alignment")
    private final Alignment alignment;

    /**
     * Creates a layout.
     *
     * <p>A layout with no mode and no profile gets {@link Profile#NO_REFLOW}. An omitted mode
     * means {@code reflowWhenSupported} in the API, and that mode needs a profile. The SDK stores
     * this profile, so {@link #profile()}, {@code equals}, {@code hashCode}, {@code toString} and
     * the JSON that the SDK sends all show the same value.
     */
    @JsonCreator
    public TextLayoutRequest(@JsonProperty("mode") Mode mode,
                             @JsonProperty("profile") Profile profile,
                             @JsonProperty("hyphenationEnabled") Boolean hyphenationEnabled,
                             @JsonProperty("alignment") Alignment alignment) {
        this.mode = mode;
        this.profile = profile == null && mode == null ? Profile.NO_REFLOW : profile;
        this.hyphenationEnabled = hyphenationEnabled;
        this.alignment = alignment;
    }

    public TextLayoutRequest(Mode mode, Profile profile, Boolean hyphenationEnabled) {
        this(mode, profile, hyphenationEnabled, null);
    }

    public TextLayoutRequest(Mode mode, Profile profile) {
        this(mode, profile, null);
    }

    public static TextLayoutRequest sourceAnchored() {
        return new TextLayoutRequest(Mode.sourceAnchored, null);
    }

    public static TextLayoutRequest reflowWhenSupported(Profile profile) {
        return new TextLayoutRequest(Mode.reflowWhenSupported, profile);
    }

    public static TextLayoutRequest requireReflow(Profile profile) {
        return new TextLayoutRequest(Mode.requireReflow, profile);
    }

    public TextLayoutRequest withHyphenationEnabled(boolean enabled) {
        return new TextLayoutRequest(mode, profile, enabled, alignment);
    }

    /** Sets the explicit alignment. Valid only for text replace. */
    public TextLayoutRequest withAlignment(Alignment alignment) {
        return new TextLayoutRequest(mode, profile, hyphenationEnabled, alignment);
    }

    public Mode mode() { return mode; }
    public Profile profile() { return profile; }
    public Boolean hyphenationEnabled() { return hyphenationEnabled; }
    public Alignment alignment() { return alignment; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextLayoutRequest) obj;
        return mode == that.mode &&
                Objects.equals(profile, that.profile) &&
                Objects.equals(hyphenationEnabled, that.hyphenationEnabled) &&
                alignment == that.alignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, profile, hyphenationEnabled, alignment);
    }

    @Override
    public String toString() {
        return "TextLayoutRequest[" +
                "mode=" + mode + ", " +
                "profile=" + profile + ", " +
                "hyphenationEnabled=" + hyphenationEnabled + ", " +
                "alignment=" + alignment + ']';
    }
}
