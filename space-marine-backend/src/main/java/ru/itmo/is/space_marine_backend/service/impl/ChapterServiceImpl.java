package ru.itmo.is.space_marine_backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;
import ru.itmo.is.space_marine_backend.exception.ChapterNotEmptyException;
import ru.itmo.is.space_marine_backend.exception.ChapterNotFoundException;
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
    public Chapter createChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    @Override
    public List<Chapter> getAllChapters() {
        return chapterRepository.findAll();
    }
    @Override
    @Transactional
    public void dissolveChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ChapterNotFoundException(chapterId));

        List<SpaceMarine> marines = marineRepository.findByChapter(chapter);
        if (!marines.isEmpty()) {
            throw new ChapterNotEmptyException(chapterId);
        }

        chapterRepository.delete(chapter);
    }
}