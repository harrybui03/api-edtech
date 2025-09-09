package com.example.backend.service;

import com.example.backend.dto.model.LessonDto;
import com.example.backend.dto.request.course.LessonRequest;
import com.example.backend.entity.Chapter;
import com.example.backend.entity.Course;
import com.example.backend.entity.Lesson;
import com.example.backend.entity.User;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.LessonMapper;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.LessonRepository;
import com.example.backend.repository.EnrollmentRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final LessonMapper lessonMapper;
    private final EnrollmentRepository enrollmentRepository;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Transactional(readOnly = true)
    public LessonDto getLessonBySlug(String slug) {
        Lesson lesson = lessonRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with slug: " + slug));

        checkLessonViewPermission(lesson.getCourse());
        return lessonMapper.toDto(lesson);
    }

    @Transactional
    public LessonDto createLesson(UUID chapterId, LessonRequest request) {
        Chapter chapter = findChapterById(chapterId);
        checkCourseOwnership(chapter.getCourse());
        int currentLessonCount = chapter.getLessons().size();
        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setChapter(chapter);
        lesson.setCourse(chapter.getCourse());
        lesson.setSlug(generateUniqueSlug(request.getTitle()));

        lesson.setPosition(currentLessonCount + 1);

        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toDto(savedLesson);
    }

    @Transactional
    public LessonDto updateLesson(UUID lessonId, LessonRequest request) {
        Lesson lesson = findLessonById(lessonId);
        checkCourseOwnership(lesson.getCourse());

        if (!lesson.getTitle().equals(request.getTitle())) {
            lesson.setSlug(generateUniqueSlug(request.getTitle()));
        }

        lessonMapper.updateEntityFromRequest(request, lesson);
        Lesson updatedLesson = lessonRepository.save(lesson);
        return lessonMapper.toDto(updatedLesson);
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lessonToDelete = findLessonById(lessonId);
        Chapter chapter = lessonToDelete.getChapter();
        checkCourseOwnership(chapter.getCourse());

        chapter.getLessons().remove(lessonToDelete);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int counter = 1;
        while (lessonRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String toSlug(String input) {
        String whitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(whitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
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
            throw new ForbiddenException("You do not have permission to access or modify this content.");
        }
    }

    private void checkLessonViewPermission(Course course) {
        User currentUser = getCurrentUser();

        boolean isInstructor = course.getInstructors().stream()
                .anyMatch(instructor -> instructor.getUser().getId().equals(currentUser.getId()));

        if (isInstructor) {
            return;
        }

        boolean isEnrolled = enrollmentRepository.existsByMemberIdAndCourseId(currentUser.getId(), course.getId());

        if (!isEnrolled) {
            throw new ForbiddenException("You must be enrolled in the course to view this lesson.");
        }
    }

    private Chapter findChapterById(UUID chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
    }

    private Lesson findLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
    }
}