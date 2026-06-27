package edu.ruc.platform.honor.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.honor.dto.HonorRecipientAdminResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientFilterRequest;
import edu.ruc.platform.honor.dto.HonorRecipientMemberResponse;
import edu.ruc.platform.honor.dto.HonorRecipientMemberUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientUpsertRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseAdminResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseFilterRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseUpsertRequest;
import edu.ruc.platform.honor.service.HonorApplicationService;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/honors")
@RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
@RequiredArgsConstructor
public class AdminHonorController {

    private final HonorApplicationService honorService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<HonorShowcaseAdminResponse>> page(@RequestParam(required = false) Integer awardYear,
                                                                      @RequestParam(required = false) String honorCategory,
                                                                      @RequestParam(required = false) String recipientType,
                                                                      @RequestParam(required = false) Boolean publicVisible,
                                                                      @RequestParam(required = false) String keyword,
                                                                      @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                      @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(honorService.pageAdminShowcases(
                new HonorShowcaseFilterRequest(awardYear, honorCategory, recipientType, publicVisible, keyword),
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<HonorShowcaseAdminResponse> detail(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(honorService.getAdminShowcase(id));
    }

    @PostMapping
    public ApiResponse<HonorShowcaseAdminResponse> create(@Valid @RequestBody HonorShowcaseUpsertRequest request) {
        return ApiResponse.success("荣誉展示模块已创建", honorService.createShowcase(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HonorShowcaseAdminResponse> update(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                          @Valid @RequestBody HonorShowcaseUpsertRequest request) {
        return ApiResponse.success("荣誉展示模块已更新", honorService.updateShowcase(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        honorService.deleteShowcase(id);
        return ApiResponse.success("荣誉展示模块已删除", null);
    }

    @GetMapping("/{id}/recipients/page")
    public ApiResponse<PageResponse<HonorRecipientAdminResponse>> pageRecipients(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                                                 @RequestParam(required = false) String recipientType,
                                                                                 @RequestParam(required = false) Boolean publicVisible,
                                                                                 @RequestParam(required = false) String keyword,
                                                                                 @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                 @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(honorService.pageAdminRecipients(
                id,
                new HonorRecipientFilterRequest(recipientType, publicVisible, keyword),
                page,
                size
        ));
    }

    @PostMapping("/{id}/recipients")
    public ApiResponse<HonorRecipientAdminResponse> createRecipient(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                                    @Valid @RequestBody HonorRecipientUpsertRequest request) {
        return ApiResponse.success("荣誉获得者已创建", honorService.createRecipient(id, request));
    }

    @PostMapping("/{id}/recipients/import")
    public ApiResponse<BatchImportResultResponse> importRecipients(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                                   @RequestParam("file") MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        HonorShowcaseAdminResponse showcase = honorService.getAdminShowcase(id);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel文件为空");
            }
            int baseOrder = 0;
            try {
                PageResponse<HonorRecipientAdminResponse> existing = honorService.pageAdminRecipients(
                        id,
                        new HonorRecipientFilterRequest(null, null, null),
                        0,
                        2000
                );
                baseOrder = existing == null ? 0 : existing.content().size();
            } catch (Exception ignored) {
                baseOrder = 0;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String recipientName = getCellStringValue(row, 0);
                String studentNo = getCellStringValue(row, 1);
                String major = getCellStringValue(row, 2);
                String grade = getCellStringValue(row, 3);
                String className = getCellStringValue(row, 4);
                String awardIntro = getCellStringValue(row, 5);
                String advancedDeeds = getCellStringValue(row, 6);
                String photoUrl = getCellStringValue(row, 7);

                if ((recipientName == null || recipientName.isBlank())
                        && (studentNo == null || studentNo.isBlank())
                        && (major == null || major.isBlank())
                        && (grade == null || grade.isBlank())
                        && (className == null || className.isBlank())
                        && (awardIntro == null || awardIntro.isBlank())
                        && (advancedDeeds == null || advancedDeeds.isBlank())
                        && (photoUrl == null || photoUrl.isBlank())) {
                    continue;
                }

                totalRows++;
                try {
                    if (recipientName == null || recipientName.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "recipientName", "获得者名称不能为空", ""));
                        continue;
                    }

                    HonorRecipientAdminResponse created = honorService.createRecipient(id, new HonorRecipientUpsertRequest(
                            showcase.recipientType(),
                            null,
                            studentNo == null ? "" : studentNo,
                            recipientName,
                            major == null ? "" : major,
                            grade == null ? "" : grade,
                            className == null ? "" : className,
                            awardIntro == null ? "" : awardIntro,
                            advancedDeeds == null ? "" : advancedDeeds,
                            null,
                            true,
                            baseOrder + totalRows,
                            showcase.displayStartAt(),
                            showcase.displayEndAt(),
                            null
                    ));
                    if (photoUrl != null && !photoUrl.isBlank()) {
                        String fileName = deriveFileName(photoUrl);
                        honorService.createAttachment(created.id(), new HonorRecipientAttachmentUpsertRequest(
                                null,
                                "PHOTO",
                                fileName,
                                null,
                                null,
                                photoUrl.trim(),
                                "照片",
                                true,
                                0
                        ));
                    }
                    successRows++;
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        return ApiResponse.success("批量导入完成", new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors));
    }

    @GetMapping("/recipients/{recipientId}")
    public ApiResponse<HonorRecipientAdminResponse> recipientDetail(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId) {
        return ApiResponse.success(honorService.getAdminRecipient(recipientId));
    }

    @PutMapping("/recipients/{recipientId}")
    public ApiResponse<HonorRecipientAdminResponse> updateRecipient(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                    @Valid @RequestBody HonorRecipientUpsertRequest request) {
        return ApiResponse.success("荣誉获得者已更新", honorService.updateRecipient(recipientId, request));
    }

    @DeleteMapping("/recipients/{recipientId}")
    public ApiResponse<Void> deleteRecipient(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId) {
        honorService.deleteRecipient(recipientId);
        return ApiResponse.success("荣誉获得者已删除", null);
    }

    @PostMapping("/recipients/{recipientId}/members")
    public ApiResponse<HonorRecipientMemberResponse> createMember(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                  @Valid @RequestBody HonorRecipientMemberUpsertRequest request) {
        return ApiResponse.success("荣誉集体成员已创建", honorService.createMember(recipientId, request));
    }

    @PutMapping("/members/{memberId}")
    public ApiResponse<HonorRecipientMemberResponse> updateMember(@Positive(message = "荣誉成员ID必须大于 0") @PathVariable Long memberId,
                                                                  @Valid @RequestBody HonorRecipientMemberUpsertRequest request) {
        return ApiResponse.success("荣誉集体成员已更新", honorService.updateMember(memberId, request));
    }

    @DeleteMapping("/members/{memberId}")
    public ApiResponse<Void> deleteMember(@Positive(message = "荣誉成员ID必须大于 0") @PathVariable Long memberId) {
        honorService.deleteMember(memberId);
        return ApiResponse.success("荣誉集体成员已删除", null);
    }

    @PostMapping("/recipients/{recipientId}/attachments")
    public ApiResponse<HonorRecipientAttachmentResponse> createAttachment(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                          @Valid @RequestBody HonorRecipientAttachmentUpsertRequest request) {
        return ApiResponse.success("荣誉附件已创建", honorService.createAttachment(recipientId, request));
    }

    @PutMapping("/attachments/{attachmentId}")
    public ApiResponse<HonorRecipientAttachmentResponse> updateAttachment(@Positive(message = "荣誉附件ID必须大于 0") @PathVariable Long attachmentId,
                                                                          @Valid @RequestBody HonorRecipientAttachmentUpsertRequest request) {
        return ApiResponse.success("荣誉附件已更新", honorService.updateAttachment(attachmentId, request));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@Positive(message = "荣誉附件ID必须大于 0") @PathVariable Long attachmentId) {
        honorService.deleteAttachment(attachmentId);
        return ApiResponse.success("荣誉附件已删除", null);
    }

    private String getCellStringValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return null;
        }
        try {
            row.getCell(cellIndex).setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        } catch (Exception ignored) {
        }
        String value = row.getCell(cellIndex).getStringCellValue();
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private String deriveFileName(String path) {
        String value = path == null ? "" : path.trim();
        if (value.isEmpty()) {
            return "photo";
        }
        int queryIdx = value.indexOf('?');
        if (queryIdx >= 0) {
            value = value.substring(0, queryIdx);
        }
        value = value.replace('\\', '/');
        int idx = value.lastIndexOf('/');
        if (idx >= 0 && idx < value.length() - 1) {
            String name = value.substring(idx + 1);
            return name.isBlank() ? "photo" : name;
        }
        return "photo";
    }
}
