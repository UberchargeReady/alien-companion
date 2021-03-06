package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by George on 1/5/2016.
 */
public enum ImgurThumbnailSize {

    SMALL_SQUARE("s"),
    BIG_SQUARE("b"),
    SMALL_THUMBNAIL("t"),
    MEDIUM_THUMBNAIL("m"),
    LARGE_THUMBNAIL("l"),
    HUGE_THUMBNAIL("h");

    private final String value;

    ImgurThumbnailSize(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
