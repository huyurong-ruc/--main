package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.TermCourseResponse;
import edu.ruc.platform.admin.dto.TermCourseUpsertRequest;
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
@RequestMapping("/api/v1/admin/term-courses")
@RequiredArgsConstructor
public class AdminTermCourseController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<TermCourseResponse>> list(@RequestParam(required = false) String termCode,
                                                      @RequestParam(required = false) String keyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listTermCourses(termCode, keyword));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<TermCourseResponse>> page(@RequestParam(required = false) String termCode,
                                                              @RequestParam(required = false) String keyword,
                                                              @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                              @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pageTermCourses(termCode, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TermCourseResponse> detail(@Positive(message = "开课ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.getTermCourse(id));
    }

    @PostMapping
    public ApiResponse<TermCourseResponse> create(@Valid @RequestBody TermCourseUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success("开课创建成功", adminService.createTermCourse(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TermCourseResponse> update(@Positive(message = "开课ID必须大于0") @PathVariable Long id,
                                                  @Valid @RequestBody TermCourseUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success("开课更新成功", adminService.updateTermCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "开课ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        adminService.deleteTermCourse(id);
        return ApiResponse.success("开课删除成功", null);
    }
}
