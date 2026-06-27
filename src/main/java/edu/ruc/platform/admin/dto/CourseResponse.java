package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String courseCode,
        String courseName,
        Double credits,
        String courseType,
        Integer referenceCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
