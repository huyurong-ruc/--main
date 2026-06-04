package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record TermCourseUpsertRequest(
        @NotBlank(message = "学期编码不能为空")
        String termCode,
        @NotBlank(message = "课程编码不能为空")
        String courseCode,
        @NotBlank(message = "教学班编码不能为空")
        String teachingClassCode,
        @NotBlank(message = "授课教师不能为空")
        String teacherName,
        String courseLocation,
        @Min(value = 0, message = "学分不能小于0")
        Double credits,
        @Min(value = 0, message = "学时不能小于0")
        Double totalHours,
        @Min(value = 1, message = "容量必须大于 0")
        Integer capacity
) {
}
