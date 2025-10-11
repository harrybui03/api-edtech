package com.example.backend.service;

import com.example.backend.dto.model.ChapterDto;
import com.example.backend.dto.request.course.ChapterRequest;
import com.example.backend.dto.model.ChapterPublicDto;
import com.example.backend.entity.Chapter;
import com.example.backend.entity.Course;
import com.example.backend.entity.User;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.ChapterMapper;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.backend.util.SlugConverter.toSlug;


@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ChapterMapper chapterMapper;

    @Transactional(readOnly = true)
    public List<ChapterDto> getChaptersByCourse(String slug) {
        if (!courseRepository.existsBySlug(slug)) {
            throw new ResourceNotFoundException("Course not found with slug: " + slug);
        }
        List<Chapter> chapters = chapterRepository.findByCourseSlugWithLessonsOrderByPositionAsc(slug);
        return chapters.stream()
                .map(chapterMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChapterPublicDto> getChaptersByCoursePublic(String slug) {
        if (!courseRepository.existsBySlug(slug)) {
            throw new ResourceNotFoundException("Course not found with slug: " + slug);
        }
        List<Chapter> chapters = chapterRepository.findByCourseSlugWithLessonsOrderByPositionAsc(slug);
        return chapters.stream()
                .map(chapterMapper::toPublicDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChapterDto getChapterByIdForInstructor(UUID chapterId) {
        Chapter chapter = findChapterById(chapterId);
        checkCourseOwnership(chapter.getCourse());
        return chapterMapper.toDto(chapter);
    }

    @Transactional
    public ChapterDto createChapter(UUID courseId, ChapterRequest request) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);
        int currentChapterCount = course.getChapters().size();

        Chapter chapter = chapterMapper.toEntity(request);
        chapter.setCourse(course);
        chapter.setSlug(generateUniqueSlug(request.getTitle()));

        chapter.setPosition(currentChapterCount + 1);

        Chapter savedChapter = chapterRepository.save(chapter);
        return chapterMapper.toDto(savedChapter);
    }

    @Transactional
    public ChapterDto updateChapter(UUID chapterId, ChapterRequest request) {
        Chapter chapter = findChapterById(chapterId);
        checkCourseOwnership(chapter.getCourse());
        chapter.setSlug(generateUniqueSlug(request.getTitle()));

        chapterMapper.updateEntityFromRequest(request, chapter);
        Chapter updatedChapter = chapterRepository.save(chapter);
        return chapterMapper.toDto(updatedChapter);
    }

    @Transactional
    public void deleteChapter(UUID chapterId) {
        Chapter chapterToDelete = findChapterById(chapterId);
        Course course = chapterToDelete.getCourse();
        checkCourseOwnership(course);

        course.getChapters().remove(chapterToDelete);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }

    private void checkCourseOwnership(Course course) {
        User currentUser = getCurrentUser();
        boolean isOwner = course.getInstructors().stream()
                .anyMatch(instructor -> instructor.getUser().getId().equals(currentUser.getId()));

        if (!isOwner) {
            throw new ForbiddenException("You are not an instructor for this course and cannot modify it.");
        }
    }

    private Course findCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private Chapter findChapterById(UUID chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int counter = 1;
        while (chapterRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
}