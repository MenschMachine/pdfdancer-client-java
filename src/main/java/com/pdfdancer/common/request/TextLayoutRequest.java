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

    @JsonProperty("mode")
    private final Mode mode;
    @JsonProperty("profile")
    private final Profile profile;
    @JsonProperty("hyphenationEnabled")
    private final Boolean hyphenationEnabled;

    @JsonCreator
    public TextLayoutRequest(@JsonProperty("mode") Mode mode,
                             @JsonProperty("profile") Profile profile,
                             @JsonProperty("hyphenationEnabled") Boolean hyphenationEnabled) {
        this.mode = mode;
        this.profile = profile;
        this.hyphenationEnabled = hyphenationEnabled;
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
        return new TextLayoutRequest(mode, profile, enabled);
    }

    public Mode mode() { return mode; }
    public Profile profile() { return profile; }
    public Boolean hyphenationEnabled() { return hyphenationEnabled; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextLayoutRequest) obj;
        return mode == that.mode &&
                Objects.equals(profile, that.profile) &&
                Objects.equals(hyphenationEnabled, that.hyphenationEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, profile, hyphenationEnabled);
    }

    @Override
    public String toString() {
        return "TextLayoutRequest[" +
                "mode=" + mode + ", " +
                "profile=" + profile + ", " +
                "hyphenationEnabled=" + hyphenationEnabled + ']';
    }
}
