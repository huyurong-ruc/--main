package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record CourseUpsertRequest(
        @NotBlank(message = "课程编码不能为空")
        String courseCode,
        @NotBlank(message = "课程名称不能为空")
        String courseName,
        @Min(value = 0, message = "学分不能小于0")
        Double credits,
        String courseType
) {
}
