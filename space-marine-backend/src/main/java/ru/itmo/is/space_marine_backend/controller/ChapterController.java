package ru.itmo.is.space_marine_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.repository.ChapterRepository;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterRepository chapterRepository;

    @PostMapping
    public Chapter create(@RequestBody Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    @GetMapping
    public List<Chapter> getAll() {
        return chapterRepository.findAll();
    }
}