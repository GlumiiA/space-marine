package ru.itmo.is.space_marine_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itmo.is.space_marine_backend.dto.response.ApiError;
import ru.itmo.is.space_marine_backend.exception.ChapterNotEmptyException;
import ru.itmo.is.space_marine_backend.exception.ChapterNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ChapterNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ChapterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("CHAPTER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ChapterNotEmptyException.class)
    public ResponseEntity<ApiError> handleChapterNotEmpty(ChapterNotEmptyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("CHAPTER_NOT_EMPTY", ex.getMessage()));
    }
}
