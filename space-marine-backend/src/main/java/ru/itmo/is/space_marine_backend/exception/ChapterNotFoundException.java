package ru.itmo.is.space_marine_backend.exception;

public class ChapterNotFoundException extends RuntimeException {
    public ChapterNotFoundException(Long chapterId) {
        super("Chapter not found with id " + chapterId);
    }
}