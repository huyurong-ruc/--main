package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record TermCourseResponse(
        Long id,
        String termCode,
        String termName,
        String courseCode,
        String courseName,
        String teachingClassCode,
        String teacherName,
        String courseLocation,
        Double credits,
        Double totalHours,
        Integer selectedCount,
        Integer capacity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
