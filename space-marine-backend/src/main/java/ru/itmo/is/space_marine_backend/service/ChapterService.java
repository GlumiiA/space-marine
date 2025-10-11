package ru.itmo.is.space_marine_backend.service;

import ru.itmo.is.space_marine_backend.entity.Chapter;

import java.util.List;

public interface ChapterService {
    void dissolveChapter(Long chapterId);
    List<Chapter> getAllChapters();
    Chapter createChapter(Chapter chapter);
}
