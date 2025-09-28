package ru.itmo.is.space_marine_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.repository.ChapterRepository;
import ru.itmo.is.space_marine_backend.service.ChapterService;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterRepository chapterRepository;
    private final ChapterService chapterService;

    @PostMapping
    public Chapter create(@RequestBody Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    @GetMapping
    public List<Chapter> getAll() {
        return chapterRepository.findAll();
    }

    @PostMapping("/{id}/dissolve")
    public ResponseEntity<String> dissolveChapter(@PathVariable Long id) {
        chapterService.dissolveChapter(id);
        return ResponseEntity.ok("Chapter dissolved successfully");
    }
}