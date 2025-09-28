package ru.itmo.is.space_marine_backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;
import ru.itmo.is.space_marine_backend.repository.ChapterRepository;
import ru.itmo.is.space_marine_backend.repository.SpaceMarineRepository;
import ru.itmo.is.space_marine_backend.service.ChapterService;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final SpaceMarineRepository marineRepository;

    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              SpaceMarineRepository marineRepository) {
        this.chapterRepository = chapterRepository;
        this.marineRepository = marineRepository;
    }

    @Override
    @Transactional
    public void dissolveChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));

        List<SpaceMarine> marines = marineRepository.findByChapter(chapter);
        if (!marines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot dissolve chapter: there are marines assigned to it");
        }

        chapterRepository.delete(chapter);
    }
}