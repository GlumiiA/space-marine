package ru.itmo.is.space_marine_backend.exception;

public class ChapterNotEmptyException extends RuntimeException {
    public ChapterNotEmptyException(Long chapterId) {
        super("Cannot dissolve chapter " + chapterId + ": there are marines assigned to it");
    }
}