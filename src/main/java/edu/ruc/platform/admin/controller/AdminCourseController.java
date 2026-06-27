package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.CourseResponse;
import edu.ruc.platform.admin.dto.CourseUpsertRequest;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<CourseResponse>> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String courseType) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listCourses(keyword, courseType));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<CourseResponse>> page(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String courseType,
                                                          @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                          @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pageCourses(keyword, courseType, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> detail(@Positive(message = "课程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.getCourse(id));
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success("课程创建成功", adminService.createCourse(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@Positive(message = "课程ID必须大于0") @PathVariable Long id,
                                              @Valid @RequestBody CourseUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success("课程更新成功", adminService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "课程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        adminService.deleteCourse(id);
        return ApiResponse.success("课程删除成功", null);
    }
}
