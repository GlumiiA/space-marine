package ru.itmo.is.space_marine_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.dto.response.ApiMessage;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.service.ChapterService;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @PostMapping
    public Chapter create(@RequestBody Chapter chapter) {
        return chapterService.createChapter(chapter);
    }

    @GetMapping
    public List<Chapter> getAll() {
        return chapterService.getAllChapters();
    }

    @PostMapping("/{id}/dissolve")
    public ResponseEntity<ApiMessage> dissolveChapter(@PathVariable Long id) {
        chapterService.dissolveChapter(id);
        return ResponseEntity.ok(ApiMessage.success("Chapter dissolved successfully"));
    }
}