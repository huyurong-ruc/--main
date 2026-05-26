package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeFilterRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeStatsResponse;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogStatsResponse;
import edu.ruc.platform.admin.dto.AdminStatsResponse;
import edu.ruc.platform.admin.dto.AdminCertTemplateResponse;
import edu.ruc.platform.admin.dto.AdminCertTemplateUpsertRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeFilterRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingUpsertRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeStatsResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeAdvisorStatsResponse;
import edu.ruc.platform.admin.dto.CourseResponse;
import edu.ruc.platform.admin.dto.CourseUpsertRequest;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskStatsResponse;
import edu.ruc.platform.admin.dto.DataImportTaskCreateRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest;
import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.admin.dto.KnowledgeCategoryStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogModuleStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogRoleStatsResponse;
import edu.ruc.platform.admin.dto.PartyReminderTaskFilterRequest;
import edu.ruc.platform.admin.dto.PartyReminderTaskResponse;
import edu.ruc.platform.admin.dto.RoleResponse;
import edu.ruc.platform.admin.dto.RoleUpsertRequest;
import edu.ruc.platform.admin.dto.TermCourseResponse;
import edu.ruc.platform.admin.dto.TermCourseUpsertRequest;
import edu.ruc.platform.admin.dto.WorkflowDefinitionResponse;
import edu.ruc.platform.admin.dto.WorkflowDefinitionUpsertRequest;
import edu.ruc.platform.admin.dto.WorkflowInstanceFilterRequest;
import edu.ruc.platform.admin.dto.WorkflowInstanceResponse;
import edu.ruc.platform.admin.dto.WorkflowNodeResponse;
import edu.ruc.platform.admin.dto.WorkflowNodeUpsertRequest;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.DataImportTaskStatus;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockAdminService implements AdminApplicationService {

    private final MockDataStore mockDataStore;
    private final CurrentUserService currentUserService;
    private final AtomicLong noticeIdGenerator = new AtomicLong(10);
    private final AtomicLong knowledgeIdGenerator = new AtomicLong(200);
    private final AtomicLong importTaskIdGenerator = new AtomicLong(20);
    private final AtomicLong advisorScopeIdGenerator = new AtomicLong(10);
    private final AtomicLong operationLogIdGenerator = new AtomicLong(10);
    private final AtomicLong knowledgeAttachmentIdGenerator = new AtomicLong(50);
    private final AtomicLong importErrorIdGenerator = new AtomicLong(80);
    private final AtomicLong workflowDefIdGenerator = new AtomicLong(10);
    private final AtomicLong workflowNodeIdGenerator = new AtomicLong(50);
    private final AtomicLong certTemplateIdGenerator = new AtomicLong(10);
    private final List<TargetedNoticeResponse> notices = new ArrayList<>();
    private final List<AdminKnowledgeItemResponse> knowledgeItems = new ArrayList<>();
    private final List<DataImportTaskResponse> importTasks = new ArrayList<>();
    private final List<AdminOperationLogResponse> operationLogs = new ArrayList<>();
    private final List<WorkflowDefinitionResponse> workflowDefinitions = new ArrayList<>();
    private final List<WorkflowNodeResponse> workflowNodes = new ArrayList<>();
    private final List<AdminCertTemplateResponse> certTemplates = new ArrayList<>();
    private final List<KnowledgeAttachmentResponse> knowledgeAttachments = new ArrayList<>(List.of(
            new KnowledgeAttachmentResponse(51L, 101L, "party-process.pdf", "application/pdf", 1024L, "/uploads/knowledge/101/party-process.pdf", "胡浩老师", LocalDateTime.of(2026, 3, 22, 11, 0))
    ));
    private final Map<Long, String> knowledgeContents = new HashMap<>();
    private final Map<Long, String> knowledgeSummaries = new HashMap<>();
    private final List<DataImportErrorItemResponse> importErrors = new ArrayList<>(List.of(
            new DataImportErrorItemResponse(81L, 2L, 7, "title", "标题为空", null, LocalDateTime.of(2026, 3, 23, 9, 5)),
            new DataImportErrorItemResponse(82L, 2L, 12, "officialUrl", "URL 格式不合法", "htp://bad-url", LocalDateTime.of(2026, 3, 23, 9, 6))
    ));
    private final Path persistedStatePath = Paths.get(System.getProperty("user.home"), ".ssp", "mock", "admin-state.json");
    private final ObjectMapper stateMapper = new ObjectMapper().findAndRegisterModules();
    private boolean stateLoaded = false;
    private final boolean persistEnabled = !isTestRuntime();
    private final Map<Long, ImportExecutionContext> importExecutionContexts = new HashMap<>();
    private final List<AdvisorScopeBindingResponse> advisorScopes = new ArrayList<>(List.of(
            new AdvisorScopeBindingResponse(1L, "advisor01", "王老师", "2023级", "计科一班", 10001L),
            new AdvisorScopeBindingResponse(2L, "advisor02", "赵老师", "2023级", "计科二班", 10002L)
    ));

    private static class PersistedState {
        public List<TargetedNoticeResponse> notices;
        public List<AdminKnowledgeItemResponse> knowledgeItems;
        public List<KnowledgeAttachmentResponse> knowledgeAttachments;
        public Map<Long, String> knowledgeContents;
        public Map<Long, String> knowledgeSummaries;
    }

    private static boolean isTestRuntime() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("surefire.real.class.path") != null;
    }

    private synchronized void ensureStateLoaded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        if (!persistEnabled) {
            return;
        }
        try {
            if (!Files.exists(persistedStatePath)) {
                return;
            }
            PersistedState state = stateMapper.readValue(persistedStatePath.toFile(), PersistedState.class);
            if (state == null) {
                return;
            }
            if (state.notices != null) {
                notices.clear();
                notices.addAll(state.notices);
            }
            if (state.knowledgeItems != null) {
                knowledgeItems.clear();
                for (AdminKnowledgeItemResponse item : state.knowledgeItems) {
                    if (item == null) {
                        continue;
                    }
                    Integer version = item.version() == null ? 1 : item.version();
                    knowledgeItems.add(new AdminKnowledgeItemResponse(
                            item.id(),
                            item.title(),
                            item.category(),
                            item.tags(),
                            version,
                            item.published(),
                            item.officialUrl(),
                            item.sourceFileName(),
                            item.audienceScope(),
                            item.updatedBy(),
                            item.createdAt(),
                            item.updatedAt()
                    ));
                }
            }
            if (state.knowledgeAttachments != null) {
                knowledgeAttachments.clear();
                knowledgeAttachments.addAll(state.knowledgeAttachments);
            }
            if (state.knowledgeContents != null) {
                knowledgeContents.clear();
                knowledgeContents.putAll(state.knowledgeContents);
            }
            if (state.knowledgeSummaries != null) {
                knowledgeSummaries.clear();
                knowledgeSummaries.putAll(state.knowledgeSummaries);
            }
            long maxNoticeId = notices.stream().mapToLong(TargetedNoticeResponse::id).max().orElse(10L);
            long maxKnowledgeId = knowledgeItems.stream().mapToLong(AdminKnowledgeItemResponse::id).max().orElse(200L);
            long maxAttachmentId = knowledgeAttachments.stream().mapToLong(KnowledgeAttachmentResponse::id).max().orElse(50L);
            noticeIdGenerator.set(Math.max(noticeIdGenerator.get(), maxNoticeId));
            knowledgeIdGenerator.set(Math.max(knowledgeIdGenerator.get(), maxKnowledgeId));
            knowledgeAttachmentIdGenerator.set(Math.max(knowledgeAttachmentIdGenerator.get(), maxAttachmentId));
        } catch (Exception ignored) {
        }
    }

    private synchronized void persistState() {
        if (!persistEnabled) {
            return;
        }
        try {
            Files.createDirectories(persistedStatePath.getParent());
            PersistedState state = new PersistedState();
            state.notices = List.copyOf(notices);
            state.knowledgeItems = List.copyOf(knowledgeItems);
            state.knowledgeAttachments = List.copyOf(knowledgeAttachments);
            state.knowledgeContents = Map.copyOf(knowledgeContents);
            state.knowledgeSummaries = Map.copyOf(knowledgeSummaries);
            stateMapper.writeValue(persistedStatePath.toFile(), state);
        } catch (Exception ignored) {
        }
    }

    public String getKnowledgeContent(Long id) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        return knowledgeContents.get(id);
    }

    public String getKnowledgeSummary(Long id) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        String summary = QueryFilterSupport.trimToNull(knowledgeSummaries.get(id));
        if (summary != null) {
            return summary;
        }
        String content = QueryFilterSupport.trimToNull(knowledgeContents.get(id));
        return content == null ? null : buildSummary(content);
    }

    private String buildSummary(String content) {
        String normalized = QueryFilterSupport.trimToNull(content);
        if (normalized == null) {
            return null;
        }
        String oneLine = normalized.replace("\r", "\n").replace("\n", " ").replaceAll("\\s+", " ").trim();
        if (oneLine.length() <= 80) {
            return oneLine;
        }
        return oneLine.substring(0, 80);
    }

    @Override
    public List<TargetedNoticeResponse> listNotices() {
        ensureStateLoaded();
        if (notices.isEmpty()) {
            notices.addAll(mockDataStore.notices());
            persistState();
        }
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return notices.stream()
                .filter(item -> canViewNotice(user, item))
                .toList();
    }

    @Override
    public PageResponse<TargetedNoticeResponse> pageNotices(AdminNoticeFilterRequest request, int page, int size) {
        List<TargetedNoticeResponse> filtered = filterNotices(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminNoticeStatsResponse noticeStats(AdminNoticeFilterRequest request) {
        List<TargetedNoticeResponse> filtered = filterNotices(request);
        return new AdminNoticeStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> item.tags() != null && !item.tags().isEmpty()).count(),
                (int) filtered.stream().filter(item -> item.targetDescription() != null && item.targetDescription().contains("级")).count(),
                (int) filtered.stream().filter(item -> "毕业生".equals(item.targetDescription())).count()
        );
    }

    @Override
    public TargetedNoticeResponse createNotice(AdminNoticeCreateRequest request) {
        ensureStateLoaded();
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        List<String> finalTags = mergePublisherTag(request.tags(), null, operator);
        TargetedNoticeResponse response = new TargetedNoticeResponse(
                noticeIdGenerator.incrementAndGet(),
                request.title(),
                request.summary(),
                finalTags,
                request.targetDescription(),
                resolvePriority(request.tags(), request.targetDescription()),
                resolveMatchedRules(request.targetDescription(), request.tags()),
                resolveDeliveryChannels(request.tags()),
                resolveNoticePublishTime(request.published())
        );
        notices.add(0, response);
        writeOperationLog("NOTICE", "CREATE", response.title(), "SUCCESS", response.targetDescription());
        persistState();
        return response;
    }

    @Override
    public TargetedNoticeResponse updateNotice(Long id, AdminNoticeCreateRequest request) {
        ensureStateLoaded();
        for (int i = 0; i < notices.size(); i += 1) {
            TargetedNoticeResponse item = notices.get(i);
            if (item.id().equals(id)) {
                AuthenticatedUser operator = currentUserService.requireCurrentUser();
                List<String> finalTags = mergePublisherTag(request.tags(), item.tags(), operator);
                TargetedNoticeResponse updated = new TargetedNoticeResponse(
                        item.id(),
                        request.title(),
                        request.summary(),
                        finalTags,
                        request.targetDescription(),
                        resolvePriority(request.tags(), request.targetDescription()),
                        resolveMatchedRules(request.targetDescription(), request.tags()),
                        resolveDeliveryChannels(request.tags()),
                        request.published() == null ? item.publishTime() : resolveNoticePublishTime(request.published())
                );
                notices.set(i, updated);
                writeOperationLog("NOTICE", "UPDATE", updated.title(), "SUCCESS", updated.targetDescription());
                persistState();
                return updated;
            }
        }
        throw new BusinessException("通知不存在: " + id);
    }

    private LocalDateTime resolveNoticePublishTime(Boolean published) {
        if (published == null || Boolean.TRUE.equals(published)) {
            return LocalDateTime.now();
        }
        return LocalDateTime.now().plusYears(100);
    }

    private List<String> mergePublisherTag(List<String> requestTags, List<String> existingTags, AuthenticatedUser operator) {
        List<String> result = new ArrayList<>();
        if (requestTags != null) {
            requestTags.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .filter(value -> !value.startsWith("发布人:"))
                    .forEach(result::add);
        }
        String existingPublisher = null;
        if (existingTags != null) {
            for (String t : existingTags) {
                String tag = t == null ? null : t.trim();
                if (tag != null && tag.startsWith("发布人:")) {
                    existingPublisher = tag;
                    break;
                }
            }
        }
        if (existingPublisher != null) {
            result.add(0, existingPublisher);
        } else if (operator != null) {
            String displayName = operator.name() != null && !operator.name().isBlank() ? operator.name() : operator.username();
            String username = operator.username() != null ? operator.username() : "";
            result.add(0, "发布人:" + displayName + "|" + username);
        }
        return result;
    }

    @Override
    public TargetedNoticeResponse toggleNoticePublish(Long id, boolean published) {
        ensureStateLoaded();
        for (int i = 0; i < notices.size(); i += 1) {
            TargetedNoticeResponse item = notices.get(i);
            if (item.id().equals(id)) {
                TargetedNoticeResponse updated = new TargetedNoticeResponse(
                        item.id(),
                        item.title(),
                        item.summary(),
                        item.tags(),
                        item.targetDescription(),
                        item.priority(),
                        item.matchedRules(),
                        item.deliveryChannels(),
                        resolveNoticePublishTime(published)
                );
                notices.set(i, updated);
                writeOperationLog("NOTICE", published ? "PUBLISH" : "UNPUBLISH", updated.title(), "SUCCESS", null);
                persistState();
                return updated;
            }
        }
        throw new BusinessException("通知不存在: " + id);
    }

    @Override
    public void deleteNotice(Long id) {
        ensureStateLoaded();
        boolean removed = notices.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("通知不存在: " + id);
        }
        writeOperationLog("NOTICE", "DELETE", "notice#" + id, "SUCCESS", null);
        persistState();
    }

    @Override
    public List<AdminKnowledgeItemResponse> listKnowledgeItems(AuthenticatedUser user) {
        ensureStateLoaded();

        initializeKnowledgeItems();
        return knowledgeItems.stream()
                .filter(item -> canViewKnowledgeItem(user, item))
                .toList();
    }

    @Override
    public PageResponse<AdminKnowledgeItemResponse> pageKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request, int page, int size) {
        List<AdminKnowledgeItemResponse> filtered = filterKnowledgeItems(user, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminKnowledgeStatsResponse knowledgeStats(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        List<AdminKnowledgeItemResponse> filtered = filterKnowledgeItems(user, request);
        List<KnowledgeCategoryStatsResponse> categoryStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminKnowledgeItemResponse::category))
                .entrySet()
                .stream()
                .map(entry -> new KnowledgeCategoryStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        (int) entry.getValue().stream().filter(AdminKnowledgeItemResponse::published).count()
                ))
                .sorted(java.util.Comparator.comparing(KnowledgeCategoryStatsResponse::itemCount).reversed())
                .toList();
        long totalAttachments = filtered.stream()
                .map(AdminKnowledgeItemResponse::id)
                .mapToLong(id -> knowledgeAttachments.stream().filter(item -> item.knowledgeId().equals(id)).count())
                .sum();
        return new AdminKnowledgeStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(AdminKnowledgeItemResponse::published).count(),
                (int) filtered.stream().filter(item -> !item.published()).count(),
                (int) totalAttachments,
                categoryStats
        );
    }

    @Override
    public AdminKnowledgeItemResponse createKnowledgeItem(AdminKnowledgeUpsertRequest request) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        validateKnowledgeRequest(request);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        AdminKnowledgeItemResponse response = new AdminKnowledgeItemResponse(
                knowledgeIdGenerator.incrementAndGet(),
                request.title(),
                request.category(),
                request.tags(),
                1,
                Boolean.TRUE.equals(request.published()),
                request.officialUrl(),
                request.sourceFileName(),
                request.audienceScope(),
                user.name(),
                now,
                now
        );
        knowledgeItems.add(0, response);
        knowledgeContents.put(response.id(), request.content());
        String summary = QueryFilterSupport.trimToNull(request.summary());
        knowledgeSummaries.put(response.id(), summary == null ? buildSummary(request.content()) : summary.trim());
        writeOperationLog("KNOWLEDGE", "CREATE", response.title(), "SUCCESS", response.sourceFileName());
        persistState();
        return response;
    }

    @Override
    public AdminKnowledgeItemResponse updateKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        validateKnowledgeRequest(request);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        for (int i = 0; i < knowledgeItems.size(); i++) {
            AdminKnowledgeItemResponse item = knowledgeItems.get(i);
            if (item.id().equals(id)) {
                LocalDateTime now = LocalDateTime.now();
                int nextVersion = (item.version() == null ? 1 : item.version()) + 1;
                AdminKnowledgeItemResponse updated = new AdminKnowledgeItemResponse(
                        id,
                        request.title(),
                        request.category(),
                        request.tags(),
                        nextVersion,
                        Boolean.TRUE.equals(request.published()),
                        request.officialUrl(),
                        request.sourceFileName(),
                        request.audienceScope(),
                        user.name(),
                        item.createdAt(),
                        now
                );
                knowledgeItems.set(i, updated);
                knowledgeContents.put(id, request.content());
                String summary = QueryFilterSupport.trimToNull(request.summary());
                knowledgeSummaries.put(id, summary == null ? buildSummary(request.content()) : summary.trim());
                writeOperationLog("KNOWLEDGE", "UPDATE", updated.title(), "SUCCESS", updated.sourceFileName());
                persistState();
                return updated;
            }
        }
        throw new BusinessException("知识条目不存在: " + id);
    }

    @Override
    public void deleteKnowledgeItem(Long id) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        boolean exists = knowledgeItems.stream().anyMatch(item -> item.id().equals(id));
        if (!exists) {
            throw new BusinessException("知识条目不存在: " + id);
        }
        knowledgeItems.removeIf(item -> item.id().equals(id));
        knowledgeAttachments.removeIf(item -> item.knowledgeId().equals(id));
        knowledgeContents.remove(id);
        knowledgeSummaries.remove(id);
        writeOperationLog("KNOWLEDGE", "DELETE", "knowledge#" + id, "SUCCESS", null);
        persistState();
    }

    @Override
    public List<KnowledgeAttachmentResponse> listKnowledgeAttachments(Long knowledgeId) {
        initializeKnowledgeItems();
        boolean exists = knowledgeItems.stream().anyMatch(item -> item.id().equals(knowledgeId));
        if (!exists) {
            throw new BusinessException("知识条目不存在: " + knowledgeId);
        }
        return knowledgeAttachments.stream()
                .filter(item -> item.knowledgeId().equals(knowledgeId))
                .toList();
    }

    @Override
    public KnowledgeAttachmentResponse uploadKnowledgeAttachment(Long knowledgeId, MultipartFile file) {
        ensureStateLoaded();
        initializeKnowledgeItems();
        boolean exists = knowledgeItems.stream().anyMatch(item -> item.id().equals(knowledgeId));
        if (!exists) {
            throw new BusinessException("知识条目不存在: " + knowledgeId);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > 30L * 1024 * 1024) {
            throw new BusinessException("知识附件大小不能超过 30MB");
        }
        validateKnowledgeAttachment(file);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        KnowledgeAttachmentResponse response = new KnowledgeAttachmentResponse(
                knowledgeAttachmentIdGenerator.incrementAndGet(),
                knowledgeId,
                file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                "/uploads/knowledge/" + knowledgeId + "/" + System.currentTimeMillis() + "-" + (file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename()),
                user.name(),
                LocalDateTime.now()
        );
        knowledgeAttachments.add(0, response);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "UPLOAD", "knowledge#" + knowledgeId, "SUCCESS", response.fileName());
        persistState();
        return response;
    }

    @Override
    public void deleteKnowledgeAttachment(Long attachmentId) {
        ensureStateLoaded();
        KnowledgeAttachmentResponse target = knowledgeAttachments.stream()
                .filter(item -> item.id().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("知识附件不存在: " + attachmentId));
        knowledgeAttachments.removeIf(item -> item.id().equals(attachmentId));
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "DELETE", "attachment#" + attachmentId, "SUCCESS", target.fileName());
        persistState();
    }

    @Override
    public List<AdvisorScopeBindingResponse> listAdvisorScopes(String advisorUsername, String grade, String className) {
        String normalizedAdvisorUsername = QueryFilterSupport.trimToNull(advisorUsername);
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return advisorScopes.stream()
                .filter(item -> normalizedAdvisorUsername == null || normalizedAdvisorUsername.equalsIgnoreCase(item.advisorUsername()))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .toList();
    }

    @Override
    public PageResponse<AdvisorScopeBindingResponse> pageAdvisorScopes(AdvisorScopeFilterRequest request, int page, int size) {
        List<AdvisorScopeBindingResponse> filtered = listAdvisorScopes(request.advisorUsername(), request.grade(), request.className());
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdvisorScopeStatsResponse advisorScopeStats(AdvisorScopeFilterRequest request) {
        List<AdvisorScopeBindingResponse> filtered = listAdvisorScopes(request.advisorUsername(), request.grade(), request.className());
        List<AdvisorScopeAdvisorStatsResponse> advisorStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.advisorUsername() + "|" + (item.advisorName() == null ? "" : item.advisorName())
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    AdvisorScopeBindingResponse first = entry.getValue().get(0);
                    long studentCount = entry.getValue().stream().map(AdvisorScopeBindingResponse::studentId).distinct().count();
                    return new AdvisorScopeAdvisorStatsResponse(first.advisorUsername(), first.advisorName(), entry.getValue().size(), (int) studentCount);
                })
                .sorted(java.util.Comparator.comparing(AdvisorScopeAdvisorStatsResponse::bindingCount).reversed())
                .toList();
        return new AdvisorScopeStatsResponse(
                filtered.size(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::advisorUsername).distinct().count(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::grade).filter(java.util.Objects::nonNull).distinct().count(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::className).filter(java.util.Objects::nonNull).distinct().count(),
                advisorStats
        );
    }

    @Override
    public AdvisorScopeBindingResponse createAdvisorScope(AdvisorScopeBindingUpsertRequest request) {
        AdvisorScopeBindingResponse response = new AdvisorScopeBindingResponse(
                advisorScopeIdGenerator.incrementAndGet(),
                request.advisorUsername(),
                request.advisorName(),
                request.grade(),
                request.className(),
                request.studentId()
        );
        advisorScopes.add(0, response);
        writeOperationLog("ADVISOR_SCOPE", "CREATE", buildAdvisorScopeTarget(response), "SUCCESS", response.advisorUsername());
        return response;
    }

    @Override
    public AdvisorScopeBindingResponse updateAdvisorScope(Long id, AdvisorScopeBindingUpsertRequest request) {
        for (int i = 0; i < advisorScopes.size(); i++) {
            AdvisorScopeBindingResponse item = advisorScopes.get(i);
            if (item.id().equals(id)) {
                AdvisorScopeBindingResponse updated = new AdvisorScopeBindingResponse(
                        id,
                        request.advisorUsername(),
                        request.advisorName(),
                        request.grade(),
                        request.className(),
                        request.studentId()
                );
                advisorScopes.set(i, updated);
                writeOperationLog("ADVISOR_SCOPE", "UPDATE", buildAdvisorScopeTarget(updated), "SUCCESS", updated.advisorUsername());
                return updated;
            }
        }
        throw new BusinessException("班主任负责范围不存在: " + id);
    }

    @Override
    public void deleteAdvisorScope(Long id) {
        boolean removed = advisorScopes.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("班主任负责范围不存在: " + id);
        }
        writeOperationLog("ADVISOR_SCOPE", "DELETE", "advisor-scope#" + id, "SUCCESS", null);
    }

    @Override
    public AdminStatsResponse stats(int pendingApprovals) {
        return new AdminStatsResponse(
                1000,
                pendingApprovals,
                listNotices().size(),
                mockDataStore.knowledgeDocuments().size()
        );
    }

    @Override
    public List<AdminOperationLogResponse> listOperationLogs() {
        initializeOperationLogs();
        return List.copyOf(operationLogs);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageOperationLogs(AdminOperationLogFilterRequest request, int page, int size) {
        List<AdminOperationLogResponse> filtered = filterOperationLogs(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminOperationLogStatsResponse operationLogStats(AdminOperationLogFilterRequest request) {
        List<AdminOperationLogResponse> filtered = filterOperationLogs(request);
        List<AdminOperationLogModuleStatsResponse> moduleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminOperationLogResponse::module))
                .entrySet()
                .stream()
                .map(entry -> new AdminOperationLogModuleStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(AdminOperationLogModuleStatsResponse::count).reversed()
                        .thenComparing(AdminOperationLogModuleStatsResponse::module))
                .toList();
        List<AdminOperationLogRoleStatsResponse> roleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminOperationLogResponse::operatorRole))
                .entrySet()
                .stream()
                .map(entry -> new AdminOperationLogRoleStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(AdminOperationLogRoleStatsResponse::count).reversed()
                        .thenComparing(AdminOperationLogRoleStatsResponse::operatorRole))
                .toList();
        return new AdminOperationLogStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> "SUCCESS".equalsIgnoreCase(item.result())).count(),
                (int) filtered.stream().filter(item -> "FAILED".equalsIgnoreCase(item.result())).count(),
                moduleStats.size(),
                roleStats.size(),
                moduleStats,
                roleStats
        );
    }

    @Override
    public List<DataImportTaskResponse> listImportTasks() {
        initializeImportTasks();
        return List.copyOf(importTasks);
    }

    @Override
    public PageResponse<DataImportTaskResponse> pageImportTasks(DataImportTaskFilterRequest request, int page, int size) {
        List<DataImportTaskResponse> filtered = filterImportTasks(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public DataImportTaskStatsResponse importTaskStats(DataImportTaskFilterRequest request) {
        List<DataImportTaskResponse> filtered = filterImportTasks(request);
        return new DataImportTaskStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.CREATED.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.RUNNING.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.SUCCESS.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.PARTIAL_SUCCESS.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.FAILED.name().equals(item.status())).count(),
                filtered.stream().mapToInt(DataImportTaskResponse::totalRows).sum(),
                filtered.stream().mapToInt(DataImportTaskResponse::successRows).sum(),
                filtered.stream().mapToInt(DataImportTaskResponse::failedRows).sum()
        );
    }

    @Override
    public DataImportTaskResponse createImportTask(DataImportTaskCreateRequest request) {
        initializeImportTasks();
        validateImportTaskCreateRequest(request);
        DataImportTaskResponse response = new DataImportTaskResponse(
                importTaskIdGenerator.incrementAndGet(),
                request.taskType(),
                request.fileName(),
                resolveFileType(request.fileName()),
                resolveTemplateName(request.taskType()),
                resolveTemplateDownloadUrl(request.taskType()),
                DataImportTaskStatus.CREATED.name(),
                request.totalRows(),
                0,
                0,
                0,
                request.owner(),
                LocalDateTime.now()
        );
        importTasks.add(0, response);
        writeOperationLog("IMPORT_TASK", "CREATE", response.fileName(), response.status(), response.taskType());
        return response;
    }

    @Override
    public DataImportTaskResponse updateImportTask(Long id, DataImportTaskUpdateRequest request) {
        initializeImportTasks();
        for (int i = 0; i < importTasks.size(); i++) {
            DataImportTaskResponse item = importTasks.get(i);
            if (item.id().equals(id)) {
                validateImportTaskTransition(item, request);
                validateImportTaskRows(item.totalRows(), request.successRows(), request.failedRows());
                DataImportTaskResponse updated = new DataImportTaskResponse(
                        item.id(),
                        item.taskType(),
                        item.fileName(),
                        item.fileType(),
                        item.templateName(),
                        item.templateDownloadUrl(),
                        DataImportTaskStatus.from(request.status()).name(),
                        item.totalRows(),
                        request.successRows(),
                        request.failedRows(),
                        calculateProgressPercent(item.totalRows(), request.successRows(), request.failedRows()),
                        item.owner(),
                        item.createdAt()
                );
                importTasks.set(i, updated);
                writeOperationLog("IMPORT_TASK", "UPDATE", updated.fileName(), updated.status(), request.errorSummary());
                return updated;
            }
        }
        throw new BusinessException("导入任务不存在: " + id);
    }

    @Override
    public List<DataImportErrorItemResponse> listImportErrors(Long taskId) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        return importErrors.stream()
                .filter(item -> item.taskId().equals(taskId))
                .toList();
    }

    @Override
    public PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, DataImportErrorFilterRequest request, int page, int size) {
        List<DataImportErrorItemResponse> filtered = filterImportErrors(taskId, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public DataImportErrorItemResponse createImportError(Long taskId, DataImportErrorItemCreateRequest request) {
        initializeImportTasks();
        DataImportTaskResponse task = importTasks.stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在: " + taskId));
        if (request.rowNumber() > task.totalRows()) {
            throw new BusinessException("错误行号不能超过导入任务总行数");
        }
        DataImportErrorItemResponse response = new DataImportErrorItemResponse(
                importErrorIdGenerator.incrementAndGet(),
                taskId,
                request.rowNumber(),
                request.fieldName(),
                request.errorMessage(),
                request.rawValue(),
                LocalDateTime.now()
        );
        importErrors.add(response);
        writeOperationLog("IMPORT_TASK", "ADD_ERROR", task.fileName(), "SUCCESS", "row=" + request.rowNumber());
        return response;
    }

    @Override
    public void replaceImportErrors(Long taskId, List<DataImportErrorItemCreateRequest> requests) {
        initializeImportTasks();
        DataImportTaskResponse task = importTasks.stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在: " + taskId));
        importErrors.removeIf(item -> item.taskId().equals(taskId));
        List<DataImportErrorItemCreateRequest> safeRequests = requests == null ? List.of() : requests;
        for (DataImportErrorItemCreateRequest request : safeRequests) {
            if (request.rowNumber() > task.totalRows()) {
                throw new BusinessException("错误行号不能超过导入任务总行数");
            }
            importErrors.add(new DataImportErrorItemResponse(
                    importErrorIdGenerator.incrementAndGet(),
                    taskId,
                    request.rowNumber(),
                    request.fieldName(),
                    request.errorMessage(),
                    request.rawValue(),
                    LocalDateTime.now()
            ));
        }
        writeOperationLog("IMPORT_TASK", "REPLACE_ERRORS", task.fileName(), "SUCCESS", "count=" + safeRequests.size());
    }

    @Override
    public void recordImportExecutionContext(Long taskId, String executionBatchNo, String callbackSource) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        importExecutionContexts.put(taskId, new ImportExecutionContext(executionBatchNo, callbackSource, LocalDateTime.now()));
    }

    @Override
    public ImportExecutionContext getImportExecutionContext(Long taskId) {
        initializeImportTasks();
        return importExecutionContexts.get(taskId);
    }

    @Override
    public List<WorkflowDefinitionResponse> listWorkflowDefinitions() {
        initializeWorkflowConfigs();
        return List.copyOf(workflowDefinitions);
    }

    @Override
    public WorkflowDefinitionResponse createWorkflowDefinition(WorkflowDefinitionUpsertRequest request) {
        initializeWorkflowConfigs();
        WorkflowDefinitionResponse def = new WorkflowDefinitionResponse(
                workflowDefIdGenerator.incrementAndGet(),
                request.wfCode().trim(),
                request.wfName().trim(),
                request.wfType().trim(),
                request.businessType() == null ? "" : request.businessType().trim(),
                0,
                !Boolean.FALSE.equals(request.active()),
                LocalDateTime.now()
        );
        workflowDefinitions.add(0, def);
        writeOperationLog("WORKFLOW", "CREATE", def.wfCode(), "SUCCESS", def.wfName());
        return def;
    }

    @Override
    public WorkflowDefinitionResponse updateWorkflowDefinition(Long id, WorkflowDefinitionUpsertRequest request) {
        initializeWorkflowConfigs();
        WorkflowDefinitionResponse old = workflowDefinitions.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("流程不存在"));
        WorkflowDefinitionResponse updated = new WorkflowDefinitionResponse(
                old.id(),
                request.wfCode().trim(),
                request.wfName().trim(),
                request.wfType().trim(),
                request.businessType() == null ? "" : request.businessType().trim(),
                (int) workflowNodes.stream().filter(n -> n.wfId().equals(old.id())).count(),
                !Boolean.FALSE.equals(request.active()),
                old.createdAt()
        );
        workflowDefinitions.replaceAll(item -> item.id().equals(id) ? updated : item);
        writeOperationLog("WORKFLOW", "UPDATE", updated.wfCode(), "SUCCESS", updated.wfName());
        return updated;
    }

    @Override
    public WorkflowDefinitionResponse copyWorkflowDefinition(Long id) {
        initializeWorkflowConfigs();
        WorkflowDefinitionResponse src = workflowDefinitions.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("流程不存在"));
        Long newId = workflowDefIdGenerator.incrementAndGet();
        String newCode = (src.wfCode() == null ? "WF" : src.wfCode()) + "_COPY_" + System.currentTimeMillis();
        WorkflowDefinitionResponse def = new WorkflowDefinitionResponse(
                newId,
                newCode,
                (src.wfName() == null ? "" : src.wfName()) + "（复制）",
                src.wfType(),
                src.businessType(),
                0,
                src.active(),
                LocalDateTime.now()
        );
        workflowDefinitions.add(0, def);
        List<WorkflowNodeResponse> srcNodes = workflowNodes.stream().filter(n -> n.wfId().equals(src.id())).toList();
        for (WorkflowNodeResponse n : srcNodes) {
            workflowNodes.add(new WorkflowNodeResponse(
                    workflowNodeIdGenerator.incrementAndGet(),
                    def.id(),
                    n.seqNo(),
                    n.nodeName(),
                    n.approverRole(),
                    n.slaHours(),
                    n.allowReject()
            ));
        }
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "COPY", def.wfCode(), "SUCCESS", def.wfName());
        return workflowDefinitions.stream().filter(x -> x.id().equals(def.id())).findFirst().orElse(def);
    }

    @Override
    public void deleteWorkflowDefinition(Long id) {
        initializeWorkflowConfigs();
        boolean removed = workflowDefinitions.removeIf(item -> item.id().equals(id));
        workflowNodes.removeIf(n -> n.wfId().equals(id));
        if (!removed) {
            throw new BusinessException("流程不存在");
        }
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "DELETE", "wf#" + id, "SUCCESS", null);
    }

    @Override
    public List<WorkflowNodeResponse> listWorkflowNodes(Long workflowId) {
        initializeWorkflowConfigs();
        return workflowNodes.stream()
                .filter(item -> item.wfId().equals(workflowId))
                .sorted(java.util.Comparator.comparing(WorkflowNodeResponse::seqNo))
                .toList();
    }

    @Override
    public WorkflowNodeResponse createWorkflowNode(Long workflowId, WorkflowNodeUpsertRequest request) {
        initializeWorkflowConfigs();
        boolean exists = workflowDefinitions.stream().anyMatch(item -> item.id().equals(workflowId));
        if (!exists) {
            throw new BusinessException("流程不存在");
        }
        int nextSeq = workflowNodes.stream()
                .filter(n -> n.wfId().equals(workflowId))
                .map(WorkflowNodeResponse::seqNo)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        int seq = request.seqNo() != null ? request.seqNo() : nextSeq;
        WorkflowNodeResponse node = new WorkflowNodeResponse(
                workflowNodeIdGenerator.incrementAndGet(),
                workflowId,
                seq,
                request.nodeName().trim(),
                request.approverRole() == null ? null : request.approverRole().trim(),
                request.slaHours() == null ? 0 : request.slaHours(),
                Boolean.TRUE.equals(request.allowReject())
        );
        workflowNodes.add(node);
        normalizeWorkflowNodeSeq(workflowId);
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "ADD_NODE", "wf#" + workflowId, "SUCCESS", node.nodeName());
        return node;
    }

    @Override
    public WorkflowNodeResponse updateWorkflowNode(Long nodeId, WorkflowNodeUpsertRequest request) {
        initializeWorkflowConfigs();
        WorkflowNodeResponse old = workflowNodes.stream()
                .filter(item -> item.id().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("节点不存在"));
        WorkflowNodeResponse updated = new WorkflowNodeResponse(
                old.id(),
                old.wfId(),
                request.seqNo() != null ? request.seqNo() : old.seqNo(),
                request.nodeName().trim(),
                request.approverRole() == null ? null : request.approverRole().trim(),
                request.slaHours() == null ? 0 : request.slaHours(),
                Boolean.TRUE.equals(request.allowReject())
        );
        workflowNodes.replaceAll(item -> item.id().equals(nodeId) ? updated : item);
        normalizeWorkflowNodeSeq(updated.wfId());
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "UPDATE_NODE", "wf#" + updated.wfId(), "SUCCESS", updated.nodeName());
        return updated;
    }

    @Override
    public List<WorkflowNodeResponse> moveWorkflowNode(Long nodeId, String direction) {
        initializeWorkflowConfigs();
        WorkflowNodeResponse node = workflowNodes.stream()
                .filter(item -> item.id().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("节点不存在"));
        String dir = String.valueOf(direction).trim().toLowerCase(Locale.ROOT);
        List<WorkflowNodeResponse> nodes = listWorkflowNodes(node.wfId());
        int idx = -1;
        for (int i = 0; i < nodes.size(); i += 1) {
            if (nodes.get(i).id().equals(nodeId)) {
                idx = i;
                break;
            }
        }
        int swapWith = dir.equals("up") ? idx - 1 : dir.equals("down") ? idx + 1 : idx;
        if (idx < 0 || swapWith < 0 || swapWith >= nodes.size() || swapWith == idx) {
            return nodes;
        }
        WorkflowNodeResponse a = nodes.get(idx);
        WorkflowNodeResponse b = nodes.get(swapWith);
        workflowNodes.replaceAll(item -> {
            if (item.id().equals(a.id())) {
                return new WorkflowNodeResponse(item.id(), item.wfId(), b.seqNo(), item.nodeName(), item.approverRole(), item.slaHours(), item.allowReject());
            }
            if (item.id().equals(b.id())) {
                return new WorkflowNodeResponse(item.id(), item.wfId(), a.seqNo(), item.nodeName(), item.approverRole(), item.slaHours(), item.allowReject());
            }
            return item;
        });
        normalizeWorkflowNodeSeq(node.wfId());
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "MOVE_NODE", "wf#" + node.wfId(), "SUCCESS", dir);
        return listWorkflowNodes(node.wfId());
    }

    @Override
    public void deleteWorkflowNode(Long nodeId) {
        initializeWorkflowConfigs();
        WorkflowNodeResponse node = workflowNodes.stream()
                .filter(item -> item.id().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("节点不存在"));
        workflowNodes.removeIf(item -> item.id().equals(nodeId));
        normalizeWorkflowNodeSeq(node.wfId());
        refreshWorkflowNodeCounts();
        writeOperationLog("WORKFLOW", "DELETE_NODE", "wf#" + node.wfId(), "SUCCESS", node.nodeName());
    }

    @Override
    public List<AdminCertTemplateResponse> listCertTemplates() {
        initializeCertTemplates();
        return List.copyOf(certTemplates);
    }

    @Override
    public AdminCertTemplateResponse createCertTemplate(AdminCertTemplateUpsertRequest request) {
        initializeCertTemplates();
        String code = request.templateCode().trim();
        boolean exists = certTemplates.stream().anyMatch(item -> code.equalsIgnoreCase(item.templateCode()));
        if (exists) {
            throw new BusinessException("模板编码已存在");
        }
        if (request.fileId() == null) {
            throw new BusinessException("请先上传/绑定模板文件");
        }
        AdminCertTemplateResponse resp = new AdminCertTemplateResponse(
                certTemplateIdGenerator.incrementAndGet(),
                code,
                request.templateName().trim(),
                request.outputFormat().trim().toUpperCase(Locale.ROOT),
                !Boolean.FALSE.equals(request.active()),
                request.keywords() == null ? "" : request.keywords().trim(),
                request.fileId(),
                null,
                LocalDateTime.now()
        );
        certTemplates.add(0, resp);
        writeOperationLog("CERT_TEMPLATE", "CREATE", resp.templateCode(), "SUCCESS", resp.templateName());
        return resp;
    }

    @Override
    public AdminCertTemplateResponse updateCertTemplate(Long id, AdminCertTemplateUpsertRequest request) {
        initializeCertTemplates();
        AdminCertTemplateResponse old = certTemplates.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("模板不存在"));
        String code = request.templateCode().trim();
        boolean exists = certTemplates.stream().anyMatch(item -> !item.id().equals(id) && code.equalsIgnoreCase(item.templateCode()));
        if (exists) {
            throw new BusinessException("模板编码已存在");
        }
        AdminCertTemplateResponse updated = new AdminCertTemplateResponse(
                old.id(),
                code,
                request.templateName().trim(),
                request.outputFormat().trim().toUpperCase(Locale.ROOT),
                !Boolean.FALSE.equals(request.active()),
                request.keywords() == null ? "" : request.keywords().trim(),
                request.fileId() == null ? old.fileId() : request.fileId(),
                old.fileName(),
                old.createdAt()
        );
        certTemplates.replaceAll(item -> item.id().equals(id) ? updated : item);
        writeOperationLog("CERT_TEMPLATE", "UPDATE", updated.templateCode(), "SUCCESS", updated.templateName());
        return updated;
    }

    @Override
    public AdminCertTemplateResponse copyCertTemplate(Long id) {
        initializeCertTemplates();
        AdminCertTemplateResponse src = certTemplates.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("模板不存在"));
        String base = src.templateCode() == null ? "CERT" : src.templateCode();
        String next = base + "_COPY_" + System.currentTimeMillis();
        AdminCertTemplateResponse def = new AdminCertTemplateResponse(
                certTemplateIdGenerator.incrementAndGet(),
                next,
                (src.templateName() == null ? "" : src.templateName()) + "（复制）",
                src.outputFormat(),
                src.active(),
                src.keywords(),
                src.fileId(),
                src.fileName(),
                LocalDateTime.now()
        );
        certTemplates.add(0, def);
        writeOperationLog("CERT_TEMPLATE", "COPY", def.templateCode(), "SUCCESS", def.templateName());
        return def;
    }

    @Override
    public void deleteCertTemplate(Long id) {
        initializeCertTemplates();
        boolean removed = certTemplates.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("模板不存在");
        }
        writeOperationLog("CERT_TEMPLATE", "DELETE", "template#" + id, "SUCCESS", null);
    }

    private void initializeWorkflowConfigs() {
        if (!workflowDefinitions.isEmpty()) {
            return;
        }
        workflowDefinitions.addAll(List.of(
                new WorkflowDefinitionResponse(1L, "AFFAIR_READ_CERT", "在读证明申请流", "CERTIFICATE", "证明申请", 4, true, LocalDateTime.of(2026, 3, 1, 0, 0)),
                new WorkflowDefinitionResponse(2L, "AFFAIR_LEAVE", "请假申请审批流", "AFFAIR", "请假管理", 3, true, LocalDateTime.of(2026, 3, 1, 0, 0)),
                new WorkflowDefinitionResponse(3L, "AFFAIR_SCHOLARSHIP", "奖学金评审流程", "AFFAIR", "奖学金管理", 3, true, LocalDateTime.of(2026, 3, 1, 0, 0)),
                new WorkflowDefinitionResponse(4L, "AFFAIR_PARTY_IDENTITY_CERT", "党员身份证明申请流", "CERTIFICATE", "证明申请", 4, true, LocalDateTime.of(2026, 3, 1, 0, 0))
        ));
        workflowNodes.addAll(List.of(
                new WorkflowNodeResponse(11L, 1L, 1, "提交申请", "学生", 0, false),
                new WorkflowNodeResponse(12L, 1L, 2, "辅导员审批", "辅导员", 24, true),
                new WorkflowNodeResponse(13L, 1L, 3, "教务处审批", "教务处管理员", 48, true),
                new WorkflowNodeResponse(14L, 1L, 4, "完成", "系统", 0, false),
                new WorkflowNodeResponse(41L, 4L, 1, "提交申请", "学生", 0, false),
                new WorkflowNodeResponse(42L, 4L, 2, "辅导员审批", "辅导员", 24, true),
                new WorkflowNodeResponse(43L, 4L, 3, "学院党务终审", "学院管理员", 48, true),
                new WorkflowNodeResponse(44L, 4L, 4, "完成", "系统", 0, false)
        ));
        refreshWorkflowNodeCounts();
        workflowDefIdGenerator.set(100);
        workflowNodeIdGenerator.set(200);
    }

    private void initializeCertTemplates() {
        if (!certTemplates.isEmpty()) {
            return;
        }
        certTemplates.addAll(List.of(
                new AdminCertTemplateResponse(1L, "CERT_001", "在读证明", "PDF", true, "在读,证明,学籍", 5001L, "study-certificate.pdf", LocalDateTime.of(2024, 1, 15, 0, 0)),
                new AdminCertTemplateResponse(2L, "CERT_002", "成绩单", "PDF", true, "成绩,学业", 5002L, "transcript.pdf", LocalDateTime.of(2024, 1, 10, 0, 0)),
                new AdminCertTemplateResponse(3L, "CERT_003", "实习证明", "DOCX", false, "实习,证明", 5003L, "internship-certificate.docx", LocalDateTime.of(2023, 12, 20, 0, 0))
        ));
        certTemplateIdGenerator.set(100);
    }

    private void refreshWorkflowNodeCounts() {
        workflowDefinitions.replaceAll(def -> new WorkflowDefinitionResponse(
                def.id(),
                def.wfCode(),
                def.wfName(),
                def.wfType(),
                def.businessType(),
                (int) workflowNodes.stream().filter(n -> n.wfId().equals(def.id())).count(),
                def.active(),
                def.createdAt()
        ));
    }

    private void normalizeWorkflowNodeSeq(Long wfId) {
        List<WorkflowNodeResponse> nodes = workflowNodes.stream().filter(n -> n.wfId().equals(wfId)).sorted(java.util.Comparator.comparing(WorkflowNodeResponse::seqNo)).toList();
        for (int i = 0; i < nodes.size(); i += 1) {
            WorkflowNodeResponse n = nodes.get(i);
            int seq = i + 1;
            if (n.seqNo() != seq) {
                workflowNodes.replaceAll(item -> item.id().equals(n.id())
                        ? new WorkflowNodeResponse(item.id(), item.wfId(), seq, item.nodeName(), item.approverRole(), item.slaHours(), item.allowReject())
                        : item);
            }
        }
    }

    private void initializeKnowledgeItems() {
        ensureStateLoaded();
        if (!knowledgeItems.isEmpty()) {
            return;
        }
        knowledgeItems.addAll(mockDataStore.knowledgeDocuments().stream()
                .map(item -> new AdminKnowledgeItemResponse(
                        item.id(),
                        item.title(),
                        item.category(),
                        null,
                        1,
                        true,
                        item.officialUrl(),
                        item.title() + ".pdf",
                        "全体学生",
                        "胡浩老师",
                        LocalDateTime.of(2026, 3, 20, 9, 0),
                        LocalDateTime.of(2026, 3, 20, 9, 0)
                ))
                .toList());
        mockDataStore.knowledgeDocuments().forEach(item -> {
            knowledgeContents.putIfAbsent(item.id(), item.answer());
            knowledgeSummaries.putIfAbsent(item.id(), buildSummary(item.answer()));
        });
        knowledgeItems.add(0, new AdminKnowledgeItemResponse(
                299L,
                "辅导员内部口径说明",
                "内部资料",
                "内部",
                1,
                false,
                "https://example.edu/internal",
                "internal-note.pdf",
                "辅导员",
                "系统管理员",
                LocalDateTime.of(2026, 3, 18, 10, 0),
                LocalDateTime.of(2026, 3, 18, 10, 0)
        ));
        knowledgeContents.putIfAbsent(299L, "仅内部可见：用于演示 mock 数据。");
        knowledgeSummaries.putIfAbsent(299L, buildSummary(knowledgeContents.get(299L)));
        persistState();
    }

    private void initializeImportTasks() {
        if (!importTasks.isEmpty()) {
            return;
        }
        importTasks.addAll(List.of(
                new DataImportTaskResponse(1L, "STUDENT_PROFILE", "students-info.xlsx", "xlsx", "学生档案导入模板", "/templates/import/student-profile.xlsx", DataImportTaskStatus.SUCCESS.name(), 1086, 1086, 0, 100, "系统管理员", LocalDateTime.of(2026, 3, 20, 16, 0)),
                new DataImportTaskResponse(2L, "KNOWLEDGE_BASE", "party-knowledge.xlsx", "xlsx", "知识库导入模板", "/templates/import/knowledge-base.xlsx", DataImportTaskStatus.PARTIAL_SUCCESS.name(), 28, 26, 2, 100, "胡浩老师", LocalDateTime.of(2026, 3, 23, 9, 0))
        ));
    }

    private String resolveFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveTemplateName(String taskType) {
        return switch (taskType) {
            case "STUDENT_PROFILE" -> "学生档案导入模板";
            case "KNOWLEDGE_BASE" -> "知识库导入模板";
            case "NOTICE" -> "通知导入模板";
            case "ADVISOR_SCOPE" -> "班主任范围导入模板";
            default -> "通用导入模板";
        };
    }

    private String resolveTemplateDownloadUrl(String taskType) {
        return switch (taskType) {
            case "STUDENT_PROFILE" -> "/templates/import/student-profile.xlsx";
            case "KNOWLEDGE_BASE" -> "/templates/import/knowledge-base.xlsx";
            case "NOTICE" -> "/templates/import/notice.xlsx";
            case "ADVISOR_SCOPE" -> "/templates/import/advisor-scope.xlsx";
            default -> "/templates/import/general.xlsx";
        };
    }

    private int calculateProgressPercent(int totalRows, int successRows, int failedRows) {
        if (totalRows <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round((successRows + failedRows) * 100.0 / totalRows));
    }

    private boolean canViewKnowledgeItem(AuthenticatedUser user, AdminKnowledgeItemResponse item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role()) || "CLASS_ADVISOR".equals(user.role())) {
            return true;
        }
        if (!"LEAGUE_SECRETARY".equals(user.role())) {
            return false;
        }
        if (!item.published()) {
            return false;
        }
        if (item.audienceScope() == null || item.audienceScope().isBlank()) {
            return true;
        }
        return item.audienceScope().contains("学生") || item.audienceScope().contains("全体");
    }

    private List<TargetedNoticeResponse> filterNotices(AdminNoticeFilterRequest request) {
        String normalizedTab = QueryFilterSupport.normalizeUpper(request.tab());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedTag = QueryFilterSupport.trimToNull(request.tag());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return listNotices().stream()
                .filter(item -> matchesNoticeTab(normalizedTab, item.publishTime()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.summary(), normalizedKeyword))
                .filter(item -> normalizedTag == null
                        || (item.tags() != null && item.tags().stream().anyMatch(tag -> tag.equalsIgnoreCase(normalizedTag))))
                .filter(item -> normalizedTargetKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.targetDescription(), normalizedTargetKeyword))
                .toList();
    }

    private boolean matchesNoticeTab(String tab, LocalDateTime publishTime) {
        if (tab == null || tab.isBlank() || "ALL".equals(tab)) {
            return true;
        }
        if (publishTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return switch (tab) {
            case "DRAFT" -> publishTime.isAfter(now);
            case "PENDING" -> publishTime.isAfter(now);
            case "PUBLISHED" -> !publishTime.isAfter(now) && !publishTime.isBefore(now.minusDays(30));
            case "HISTORY" -> publishTime.isBefore(now.minusDays(30));
            default -> true;
        };
    }

    private boolean canViewNotice(AuthenticatedUser user, TargetedNoticeResponse item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            return false;
        }
        if (item.targetDescription() == null || item.targetDescription().isBlank() || "全体学生".equals(item.targetDescription())) {
            return true;
        }
        return user.grade() != null && item.targetDescription().contains(user.grade());
    }

    private String resolvePriority(List<String> tags, String targetDescription) {
        if ((targetDescription != null && targetDescription.contains("级"))
                || tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            return "HIGH";
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveMatchedRules(String targetDescription, List<String> tags) {
        List<String> rules = new ArrayList<>();
        if ("全体学生".equals(targetDescription)) {
            rules.add("全体学生");
        }
        if (targetDescription != null && targetDescription.contains("级")) {
            rules.add("年级匹配");
        }
        if (targetDescription != null && targetDescription.contains("计算机")) {
            rules.add("专业匹配");
        }
        if (tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            rules.add("就业标签");
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            rules.add("党团事务标签");
        }
        return rules;
    }

    private List<String> resolveDeliveryChannels(List<String> tags) {
        if (tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }

    private List<DataImportTaskResponse> filterImportTasks(DataImportTaskFilterRequest request) {
        String normalizedTaskType = QueryFilterSupport.normalizeUpper(request.taskType());
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedOwnerKeyword = QueryFilterSupport.trimToNull(request.ownerKeyword());
        return listImportTasks().stream()
                .filter(item -> normalizedTaskType == null || normalizedTaskType.equals(item.taskType()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedOwnerKeyword == null || QueryFilterSupport.containsIgnoreCase(item.owner(), normalizedOwnerKeyword))
                .toList();
    }

    private List<AdminOperationLogResponse> filterOperationLogs(AdminOperationLogFilterRequest request) {
        String normalizedModule = QueryFilterSupport.trimToNull(request.module());
        String normalizedAction = QueryFilterSupport.trimToNull(request.action());
        String normalizedOperatorRole = QueryFilterSupport.trimToNull(request.operatorRole());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return listOperationLogs().stream()
                .filter(item -> normalizedModule == null || normalizedModule.equalsIgnoreCase(item.module()))
                .filter(item -> normalizedAction == null || normalizedAction.equalsIgnoreCase(item.action()))
                .filter(item -> normalizedOperatorRole == null || normalizedOperatorRole.equalsIgnoreCase(item.operatorRole()))
                .filter(item -> normalizedTargetKeyword == null || QueryFilterSupport.containsIgnoreCase(item.target(), normalizedTargetKeyword))
                .toList();
    }

    private void validateImportTaskRows(int totalRows, int successRows, int failedRows) {
        if (successRows + failedRows > totalRows) {
            throw new BusinessException("成功行数与失败行数之和不能超过总行数");
        }
    }

    private void validateImportTaskCreateRequest(DataImportTaskCreateRequest request) {
        List<String> allowedTaskTypes = List.of("STUDENT_PROFILE", "KNOWLEDGE_BASE", "NOTICE", "ADVISOR_SCOPE");
        if (!allowedTaskTypes.contains(request.taskType())) {
            throw new BusinessException("导入任务类型仅支持 STUDENT_PROFILE、KNOWLEDGE_BASE、NOTICE、ADVISOR_SCOPE");
        }
        String lowerCaseFileName = request.fileName().toLowerCase(Locale.ROOT);
        if (!(lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".csv"))) {
            throw new BusinessException("导入文件仅支持 xlsx、xls、csv");
        }
    }

    private void validateImportTaskTransition(DataImportTaskResponse current, DataImportTaskUpdateRequest request) {
        String currentStatus = current.status();
        String nextStatus = DataImportTaskStatus.from(request.status()).name();
        if (DataImportTaskStatus.SUCCESS.name().equals(currentStatus) || DataImportTaskStatus.FAILED.name().equals(currentStatus)) {
            throw new BusinessException("已完成的导入任务不允许再次更新状态");
        }
        if (DataImportTaskStatus.CREATED.name().equals(currentStatus) && DataImportTaskStatus.CREATED.name().equals(nextStatus)
                && (request.successRows() > 0 || request.failedRows() > 0)) {
            throw new BusinessException("CREATED 状态下成功行数和失败行数必须为 0");
        }
        if (DataImportTaskStatus.RUNNING.name().equals(nextStatus) && request.successRows() + request.failedRows() > current.totalRows()) {
            throw new BusinessException("运行中的导入任务处理行数不能超过总行数");
        }
        if (DataImportTaskStatus.SUCCESS.name().equals(nextStatus) && request.successRows() != current.totalRows()) {
            throw new BusinessException("SUCCESS 状态下成功行数必须等于总行数");
        }
        if (DataImportTaskStatus.SUCCESS.name().equals(nextStatus) && request.failedRows() != 0) {
            throw new BusinessException("SUCCESS 状态下失败行数必须为 0");
        }
        if (DataImportTaskStatus.PARTIAL_SUCCESS.name().equals(nextStatus) && (request.successRows() <= 0 || request.failedRows() <= 0)) {
            throw new BusinessException("PARTIAL_SUCCESS 状态下成功行数和失败行数都必须大于 0");
        }
        if (DataImportTaskStatus.FAILED.name().equals(nextStatus) && request.successRows() != 0) {
            throw new BusinessException("FAILED 状态下成功行数必须为 0");
        }
    }

    private void validateKnowledgeRequest(AdminKnowledgeUpsertRequest request) {
        boolean published = Boolean.TRUE.equals(request.published());
        String category = request.category() == null ? null : request.category().trim();
        boolean isFaq = category != null && "FAQ管理".equalsIgnoreCase(category);
        boolean hasOfficialUrl = request.officialUrl() != null && !request.officialUrl().isBlank();
        boolean hasSourceFile = request.sourceFileName() != null && !request.sourceFileName().isBlank();
        if (published && !isFaq && !hasOfficialUrl && !hasSourceFile) {
            throw new BusinessException("已发布知识条目必须提供官方链接或来源文件名");
        }
        if (hasOfficialUrl && !(request.officialUrl().startsWith("http://") || request.officialUrl().startsWith("https://"))) {
            throw new BusinessException("官方链接格式不正确");
        }
    }

    private void validateKnowledgeAttachment(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("知识附件文件名不能为空");
        }
        String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        if (!(lowerCaseFileName.endsWith(".pdf")
                || lowerCaseFileName.endsWith(".doc")
                || lowerCaseFileName.endsWith(".docx")
                || lowerCaseFileName.endsWith(".xls")
                || lowerCaseFileName.endsWith(".xlsx")
                || lowerCaseFileName.endsWith(".txt"))) {
            throw new BusinessException("知识附件仅支持 pdf、doc、docx、xls、xlsx、txt");
        }
    }

    private List<DataImportErrorItemResponse> filterImportErrors(Long taskId, DataImportErrorFilterRequest request) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        String normalizedFieldName = QueryFilterSupport.trimToNull(request.fieldName());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return importErrors.stream()
                .filter(item -> item.taskId().equals(taskId))
                .filter(item -> request.rowNumber() == null || request.rowNumber().equals(item.rowNumber()))
                .filter(item -> normalizedFieldName == null || normalizedFieldName.equalsIgnoreCase(item.fieldName()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.errorMessage(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.rawValue(), normalizedKeyword))
                .toList();
    }

    private List<AdminKnowledgeItemResponse> filterKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedCategory = QueryFilterSupport.trimToNull(request.category());
        return listKnowledgeItems(user).stream()
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.category(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.sourceFileName(), normalizedKeyword))
                .filter(item -> normalizedCategory == null || normalizedCategory.equalsIgnoreCase(item.category()))
                .filter(item -> request.published() == null || request.published().equals(item.published()))
                .toList();
    }

    private void initializeOperationLogs() {
        if (!operationLogs.isEmpty()) {
            return;
        }
        operationLogs.addAll(List.of(
                new AdminOperationLogResponse(6L, 20001L, "胡浩老师", "COUNSELOR", "APPROVAL", "APPROVE", "证明申请#1001", "SUCCESS", "fromStatus=PENDING,toStatus=COUNSELOR_APPROVED", LocalDateTime.of(2026, 3, 23, 10, 10), "trace-approval-1001", "/api/v1/admin/approvals/1001/actions", "192.168.1.110", "INFO"),
                new AdminOperationLogResponse(1L, 1L, "张老师", "ADMIN", "USER", "UPDATE", "编辑用户", "SUCCESS", "编辑用户", LocalDateTime.of(2024, 3, 28, 14, 30, 25), "abc123", "/api/user/update", "192.168.1.100", "INFO"),
                new AdminOperationLogResponse(2L, 2L, "李老师", "ADMIN", "APPROVAL", "APPROVE", "通过审批", "SUCCESS", "通过审批", LocalDateTime.of(2024, 3, 28, 14, 25, 18), "def456", "/api/approval/approve", "192.168.1.101", "INFO"),
                new AdminOperationLogResponse(3L, 3L, "王老师", "ADMIN", "KNOWLEDGE", "PUBLISH", "发布政策", "SUCCESS", "发布政策", LocalDateTime.of(2024, 3, 28, 14, 20, 42), "ghi789", "/api/policy/publish", "192.168.1.102", "INFO"),
                new AdminOperationLogResponse(4L, 4L, "赵老师", "ADMIN", "NOTICE", "SEND", "发送通知", "FAILED", "发送通知失败", LocalDateTime.of(2024, 3, 28, 14, 15, 30), "jkl012", "/api/auth/login", "192.168.1.103", "ERROR"),
                new AdminOperationLogResponse(5L, 5L, "钱老师", "ADMIN", "NOTICE", "SEND", "发送通知", "FAILED", "发送通知失败", LocalDateTime.of(2024, 3, 28, 14, 10, 15), "mno345", "/api/notification/send", "192.168.1.104", "ERROR")
        ));
    }

    private void writeOperationLog(String module, String action, String target, String result, String detail) {
        initializeOperationLogs();
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        operationLogs.add(0, new AdminOperationLogResponse(
                operationLogIdGenerator.incrementAndGet(),
                user.userId(),
                user.name(),
                user.role(),
                module,
                action,
                target,
                result,
                detail,
                LocalDateTime.now(),
                "trace-mock-" + operationLogIdGenerator.get(),
                null,
                null,
                "SUCCESS".equalsIgnoreCase(result) ? "INFO" : "ERROR"
        ));
    }

    private String buildAdvisorScopeTarget(AdvisorScopeBindingResponse response) {
        return response.advisorUsername() + "/" + response.grade() + "/" + response.className() + "/student#" + response.studentId();
    }

    private final AtomicLong roleIdGenerator = new AtomicLong(100);
    private final AtomicLong workflowInstanceIdGenerator = new AtomicLong(100);
    private final AtomicLong courseIdGenerator = new AtomicLong(100);
    private final AtomicLong termCourseIdGenerator = new AtomicLong(100);
    private final AtomicLong partyReminderIdGenerator = new AtomicLong(100);
    private final List<RoleResponse> roles = new ArrayList<>();
    private final List<WorkflowInstanceResponse> workflowInstances = new ArrayList<>();
    private final List<CourseResponse> courses = new ArrayList<>();
    private final List<TermCourseResponse> termCourses = new ArrayList<>();
    private final List<PartyReminderTaskResponse> partyReminderTasks = new ArrayList<>();

    @Override
    public List<RoleResponse> listRoles() {
        initializeRoles();
        return List.copyOf(roles);
    }

    @Override
    public RoleResponse getRole(Long id) {
        initializeRoles();
        return roles.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
    }

    @Override
    public RoleResponse createRole(RoleUpsertRequest request) {
        initializeRoles();
        boolean exists = roles.stream().anyMatch(item -> item.roleCode().equals(request.roleCode()));
        if (exists) {
            throw new BusinessException("角色编码已存在: " + request.roleCode());
        }
        RoleResponse response = new RoleResponse(
                roleIdGenerator.incrementAndGet(),
                request.roleCode(),
                request.roleName(),
                request.permissions(),
                Boolean.TRUE.equals(request.isActive()),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        roles.add(0, response);
        writeOperationLog("ROLE", "CREATE", response.roleCode(), "SUCCESS", response.roleName());
        return response;
    }

    @Override
    public RoleResponse updateRole(Long id, RoleUpsertRequest request) {
        initializeRoles();
        for (int i = 0; i < roles.size(); i++) {
            RoleResponse item = roles.get(i);
            if (item.id().equals(id)) {
                RoleResponse updated = new RoleResponse(
                        id,
                        request.roleCode(),
                        request.roleName(),
                        request.permissions(),
                        Boolean.TRUE.equals(request.isActive()),
                        item.userCount(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                roles.set(i, updated);
                writeOperationLog("ROLE", "UPDATE", updated.roleCode(), "SUCCESS", updated.roleName());
                return updated;
            }
        }
        throw new BusinessException("角色不存在: " + id);
    }

    @Override
    public RoleResponse copyRole(Long id) {
        initializeRoles();
        RoleResponse src = roles.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
        RoleResponse response = new RoleResponse(
                roleIdGenerator.incrementAndGet(),
                src.roleCode() + "_COPY",
                src.roleName() + "（复制）",
                src.permissions(),
                src.isActive(),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        roles.add(0, response);
        writeOperationLog("ROLE", "COPY", response.roleCode(), "SUCCESS", response.roleName());
        return response;
    }

    @Override
    public RoleResponse toggleRole(Long id) {
        initializeRoles();
        for (int i = 0; i < roles.size(); i++) {
            RoleResponse item = roles.get(i);
            if (item.id().equals(id)) {
                RoleResponse updated = new RoleResponse(
                        id,
                        item.roleCode(),
                        item.roleName(),
                        item.permissions(),
                        !item.isActive(),
                        item.userCount(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                roles.set(i, updated);
                writeOperationLog("ROLE", updated.isActive() ? "ENABLE" : "DISABLE", updated.roleCode(), "SUCCESS", null);
                return updated;
            }
        }
        throw new BusinessException("角色不存在: " + id);
    }

    @Override
    public void deleteRole(Long id) {
        initializeRoles();
        boolean removed = roles.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("角色不存在: " + id);
        }
        writeOperationLog("ROLE", "DELETE", "role#" + id, "SUCCESS", null);
    }

    private void initializeRoles() {
        if (!roles.isEmpty()) {
            return;
        }
        roles.addAll(List.of(
                new RoleResponse(1L, "SUPER_ADMIN", "超级管理员", "全部权限", true, 1, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new RoleResponse(2L, "COLLEGE_ADMIN", "学院管理员", "学生管理,通知管理,知识库,审批管理,数据导入", true, 2, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new RoleResponse(3L, "COUNSELOR", "辅导员", "学生管理(本年级),工作记录,审批(本年级)", true, 5, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new RoleResponse(4L, "CLASS_ADVISOR", "班主任", "学生管理(本班),工作记录,审批(本班)", true, 10, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new RoleResponse(5L, "LEAGUE_SECRETARY", "团支书", "知识库(只读),通知查看", true, 3, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0))
        ));
    }

    @Override
    public List<WorkflowInstanceResponse> listWorkflowInstances(WorkflowInstanceFilterRequest request) {
        initializeWorkflowInstances();
        return filterWorkflowInstances(request);
    }

    @Override
    public PageResponse<WorkflowInstanceResponse> pageWorkflowInstances(WorkflowInstanceFilterRequest request, int page, int size) {
        List<WorkflowInstanceResponse> filtered = filterWorkflowInstances(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public WorkflowInstanceResponse getWorkflowInstance(Long id) {
        initializeWorkflowInstances();
        return workflowInstances.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("流程实例不存在: " + id));
    }

    @Override
    public WorkflowInstanceResponse cancelWorkflowInstance(Long id) {
        initializeWorkflowInstances();
        for (int i = 0; i < workflowInstances.size(); i++) {
            WorkflowInstanceResponse item = workflowInstances.get(i);
            if (item.id().equals(id)) {
                if (!"running".equals(item.status())) {
                    throw new BusinessException("只有运行中的流程实例才能取消");
                }
                WorkflowInstanceResponse updated = new WorkflowInstanceResponse(
                        item.id(),
                        item.wfCode(),
                        item.wfName(),
                        item.businessType(),
                        item.businessTable(),
                        item.businessId(),
                        "canceled",
                        item.startedBy(),
                        item.startedByName(),
                        item.startedAt(),
                        LocalDateTime.now(),
                        item.createdAt()
                );
                workflowInstances.set(i, updated);
                writeOperationLog("WORKFLOW_INSTANCE", "CANCEL", "instance#" + id, "SUCCESS", null);
                return updated;
            }
        }
        throw new BusinessException("流程实例不存在: " + id);
    }

    private void initializeWorkflowInstances() {
        if (!workflowInstances.isEmpty()) {
            return;
        }
        workflowInstances.addAll(List.of(
                new WorkflowInstanceResponse(1L, "AFFAIR_READ_CERT", "在读证明申请流", "CERTIFICATE", "affair_request", 1001L, "running", 10001L, "张三", LocalDateTime.of(2026, 3, 20, 10, 30), null, LocalDateTime.of(2026, 3, 20, 10, 30)),
                new WorkflowInstanceResponse(2L, "AFFAIR_PARTY_IDENTITY_CERT", "党员身份证明申请流", "CERTIFICATE", "affair_request", 1002L, "running", 10002L, "李四", LocalDateTime.of(2026, 3, 19, 15, 0), null, LocalDateTime.of(2026, 3, 19, 15, 0)),
                new WorkflowInstanceResponse(3L, "AFFAIR_SCHOLARSHIP", "奖学金评审流程", "AFFAIR", "affair_request", 2001L, "rejected", 10003L, "王五", LocalDateTime.of(2026, 3, 18, 9, 0), LocalDateTime.of(2026, 3, 19, 16, 0), LocalDateTime.of(2026, 3, 18, 9, 0))
        ));
    }

    private List<WorkflowInstanceResponse> filterWorkflowInstances(WorkflowInstanceFilterRequest request) {
        String normalizedStatus = QueryFilterSupport.trimToNull(request.status());
        String normalizedBusinessType = QueryFilterSupport.trimToNull(request.businessType());
        String normalizedStartedByKeyword = QueryFilterSupport.trimToNull(request.startedByKeyword());
        return workflowInstances.stream()
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedBusinessType == null || normalizedBusinessType.equals(item.businessType()))
                .filter(item -> normalizedStartedByKeyword == null || QueryFilterSupport.containsIgnoreCase(item.startedByName(), normalizedStartedByKeyword))
                .toList();
    }

    @Override
    public List<CourseResponse> listCourses(String keyword, String courseType) {
        initializeCourses();
        return filterCourses(keyword, courseType);
    }

    @Override
    public PageResponse<CourseResponse> pageCourses(String keyword, String courseType, int page, int size) {
        List<CourseResponse> filtered = filterCourses(keyword, courseType);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public CourseResponse getCourse(Long id) {
        initializeCourses();
        return courses.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("课程不存在: " + id));
    }

    @Override
    public CourseResponse createCourse(CourseUpsertRequest request) {
        initializeCourses();
        boolean exists = courses.stream().anyMatch(item -> item.courseCode().equals(request.courseCode()));
        if (exists) {
            throw new BusinessException("课程编码已存在: " + request.courseCode());
        }
        CourseResponse response = new CourseResponse(
                courseIdGenerator.incrementAndGet(),
                request.courseCode(),
                request.courseName(),
                request.credits(),
                request.courseType() == null ? "unknown" : request.courseType(),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        courses.add(0, response);
        writeOperationLog("COURSE", "CREATE", response.courseCode(), "SUCCESS", response.courseName());
        return response;
    }

    @Override
    public CourseResponse updateCourse(Long id, CourseUpsertRequest request) {
        initializeCourses();
        for (int i = 0; i < courses.size(); i++) {
            CourseResponse item = courses.get(i);
            if (item.id().equals(id)) {
                CourseResponse updated = new CourseResponse(
                        id,
                        request.courseCode(),
                        request.courseName(),
                        request.credits(),
                        request.courseType() == null ? "unknown" : request.courseType(),
                        item.referenceCount(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                courses.set(i, updated);
                writeOperationLog("COURSE", "UPDATE", updated.courseCode(), "SUCCESS", updated.courseName());
                return updated;
            }
        }
        throw new BusinessException("课程不存在: " + id);
    }

    @Override
    public void deleteCourse(Long id) {
        initializeCourses();
        boolean removed = courses.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("课程不存在: " + id);
        }
        writeOperationLog("COURSE", "DELETE", "course#" + id, "SUCCESS", null);
    }

    private void initializeCourses() {
        if (!courses.isEmpty()) {
            return;
        }
        courses.addAll(List.of(
                new CourseResponse(1L, "CS101", "计算机导论", 3.0, "required", 5, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new CourseResponse(2L, "CS201", "数据结构", 4.0, "required", 8, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new CourseResponse(3L, "CS301", "操作系统", 4.0, "required", 6, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new CourseResponse(4L, "GE101", "大学英语", 2.0, "general", 15, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0)),
                new CourseResponse(5L, "GE102", "体育", 1.0, "general", 20, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0))
        ));
    }

    private List<CourseResponse> filterCourses(String keyword, String courseType) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        String normalizedCourseType = QueryFilterSupport.trimToNull(courseType);
        return courses.stream()
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.courseCode(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.courseName(), normalizedKeyword))
                .filter(item -> normalizedCourseType == null || normalizedCourseType.equals(item.courseType()))
                .toList();
    }

    @Override
    public List<TermCourseResponse> listTermCourses(String termCode, String keyword) {
        initializeTermCourses();
        return filterTermCourses(termCode, keyword);
    }

    @Override
    public PageResponse<TermCourseResponse> pageTermCourses(String termCode, String keyword, int page, int size) {
        List<TermCourseResponse> filtered = filterTermCourses(termCode, keyword);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public TermCourseResponse getTermCourse(Long id) {
        initializeTermCourses();
        return termCourses.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("开课不存在: " + id));
    }

    @Override
    public TermCourseResponse createTermCourse(TermCourseUpsertRequest request) {
        initializeTermCourses();
        boolean exists = termCourses.stream().anyMatch(item -> item.teachingClassCode().equals(request.teachingClassCode()));
        if (exists) {
            throw new BusinessException("教学班编码已存在: " + request.teachingClassCode());
        }
        TermCourseResponse response = new TermCourseResponse(
                termCourseIdGenerator.incrementAndGet(),
                request.termCode(),
                resolveTermName(request.termCode()),
                request.courseCode(),
                resolveCourseName(request.courseCode()),
                request.teachingClassCode(),
                request.teacherName(),
                request.courseLocation(),
                request.credits(),
                request.totalHours(),
                0,
                request.capacity(),
                "正常",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        termCourses.add(0, response);
        writeOperationLog("TERM_COURSE", "CREATE", response.teachingClassCode(), "SUCCESS", response.courseName());
        return response;
    }

    @Override
    public TermCourseResponse updateTermCourse(Long id, TermCourseUpsertRequest request) {
        initializeTermCourses();
        for (int i = 0; i < termCourses.size(); i++) {
            TermCourseResponse item = termCourses.get(i);
            if (item.id().equals(id)) {
                TermCourseResponse updated = new TermCourseResponse(
                        id,
                        request.termCode(),
                        resolveTermName(request.termCode()),
                        request.courseCode(),
                        resolveCourseName(request.courseCode()),
                        request.teachingClassCode(),
                        request.teacherName(),
                        request.courseLocation(),
                        request.credits(),
                        request.totalHours(),
                        item.selectedCount(),
                        request.capacity(),
                        item.status(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                termCourses.set(i, updated);
                writeOperationLog("TERM_COURSE", "UPDATE", updated.teachingClassCode(), "SUCCESS", updated.courseName());
                return updated;
            }
        }
        throw new BusinessException("开课不存在: " + id);
    }

    @Override
    public void deleteTermCourse(Long id) {
        initializeTermCourses();
        boolean removed = termCourses.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("开课不存在: " + id);
        }
        writeOperationLog("TERM_COURSE", "DELETE", "term-course#" + id, "SUCCESS", null);
    }

    private void initializeTermCourses() {
        if (!termCourses.isEmpty()) {
            return;
        }
        termCourses.addAll(List.of(
                new TermCourseResponse(1L, "2024-2025-1", "2024-2025学年第一学期", "CS101", "计算机导论", "CS101-01", "张老师", "教学楼A101", 3.0, 48.0, 45, 50, "正常", LocalDateTime.of(2024, 9, 1, 0, 0), LocalDateTime.of(2024, 9, 1, 0, 0)),
                new TermCourseResponse(2L, "2024-2025-1", "2024-2025学年第一学期", "CS201", "数据结构", "CS201-01", "李老师", "教学楼B201", 4.0, 64.0, 50, 50, "已满", LocalDateTime.of(2024, 9, 1, 0, 0), LocalDateTime.of(2024, 9, 1, 0, 0)),
                new TermCourseResponse(3L, "2024-2025-1", "2024-2025学年第一学期", "GE101", "大学英语", "GE101-01", "王老师", "教学楼C301", 2.0, 32.0, 48, 50, "正常", LocalDateTime.of(2024, 9, 1, 0, 0), LocalDateTime.of(2024, 9, 1, 0, 0))
        ));
    }

    private List<TermCourseResponse> filterTermCourses(String termCode, String keyword) {
        String normalizedTermCode = QueryFilterSupport.trimToNull(termCode);
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        return termCourses.stream()
                .filter(item -> normalizedTermCode == null || normalizedTermCode.equals(item.termCode()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.courseCode(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.courseName(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.teachingClassCode(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.teacherName(), normalizedKeyword))
                .toList();
    }

    private String resolveTermName(String termCode) {
        if (termCode == null) {
            return "";
        }
        return switch (termCode) {
            case "2024-2025-1" -> "2024-2025学年第一学期";
            case "2024-2025-2" -> "2024-2025学年第二学期";
            case "2023-2024-1" -> "2023-2024学年第一学期";
            case "2023-2024-2" -> "2023-2024学年第二学期";
            default -> termCode;
        };
    }

    private String resolveCourseName(String courseCode) {
        if (courseCode == null) {
            return "";
        }
        return courses.stream()
                .filter(item -> item.courseCode().equals(courseCode))
                .map(CourseResponse::courseName)
                .findFirst()
                .orElse(courseCode);
    }

    @Override
    public List<PartyReminderTaskResponse> listPartyReminderTasks(PartyReminderTaskFilterRequest request) {
        initializePartyReminderTasks();
        return filterPartyReminderTasks(request);
    }

    @Override
    public PageResponse<PartyReminderTaskResponse> pagePartyReminderTasks(PartyReminderTaskFilterRequest request, int page, int size) {
        List<PartyReminderTaskResponse> filtered = filterPartyReminderTasks(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public PartyReminderTaskResponse sendPartyReminder(Long id) {
        initializePartyReminderTasks();
        for (int i = 0; i < partyReminderTasks.size(); i++) {
            PartyReminderTaskResponse item = partyReminderTasks.get(i);
            if (item.id().equals(id)) {
                if (!"pending".equals(item.status())) {
                    throw new BusinessException("只有待发送的提醒任务才能发送");
                }
                PartyReminderTaskResponse updated = new PartyReminderTaskResponse(
                        item.id(),
                        item.progressId(),
                        item.nodeId(),
                        item.nodeName(),
                        item.studentUserId(),
                        item.studentName(),
                        item.studentNo(),
                        item.dueAt(),
                        item.channel(),
                        "sent",
                        LocalDateTime.now(),
                        null,
                        item.createdAt()
                );
                partyReminderTasks.set(i, updated);
                writeOperationLog("PARTY_REMINDER", "SEND", "reminder#" + id, "SUCCESS", null);
                return updated;
            }
        }
        throw new BusinessException("提醒任务不存在: " + id);
    }

    @Override
    public PartyReminderTaskResponse resendPartyReminder(Long id) {
        initializePartyReminderTasks();
        for (int i = 0; i < partyReminderTasks.size(); i++) {
            PartyReminderTaskResponse item = partyReminderTasks.get(i);
            if (item.id().equals(id)) {
                if (!"failed".equals(item.status())) {
                    throw new BusinessException("只有发送失败的提醒任务才能重新发送");
                }
                PartyReminderTaskResponse updated = new PartyReminderTaskResponse(
                        item.id(),
                        item.progressId(),
                        item.nodeId(),
                        item.nodeName(),
                        item.studentUserId(),
                        item.studentName(),
                        item.studentNo(),
                        item.dueAt(),
                        item.channel(),
                        "sent",
                        LocalDateTime.now(),
                        null,
                        item.createdAt()
                );
                partyReminderTasks.set(i, updated);
                writeOperationLog("PARTY_REMINDER", "RESEND", "reminder#" + id, "SUCCESS", null);
                return updated;
            }
        }
        throw new BusinessException("提醒任务不存在: " + id);
    }

    @Override
    public PartyReminderTaskResponse cancelPartyReminder(Long id) {
        initializePartyReminderTasks();
        for (int i = 0; i < partyReminderTasks.size(); i++) {
            PartyReminderTaskResponse item = partyReminderTasks.get(i);
            if (item.id().equals(id)) {
                if (!"pending".equals(item.status())) {
                    throw new BusinessException("只有待发送的提醒任务才能取消");
                }
                PartyReminderTaskResponse updated = new PartyReminderTaskResponse(
                        item.id(),
                        item.progressId(),
                        item.nodeId(),
                        item.nodeName(),
                        item.studentUserId(),
                        item.studentName(),
                        item.studentNo(),
                        item.dueAt(),
                        item.channel(),
                        "canceled",
                        null,
                        null,
                        item.createdAt()
                );
                partyReminderTasks.set(i, updated);
                writeOperationLog("PARTY_REMINDER", "CANCEL", "reminder#" + id, "SUCCESS", null);
                return updated;
            }
        }
        throw new BusinessException("提醒任务不存在: " + id);
    }

    private void initializePartyReminderTasks() {
        if (!partyReminderTasks.isEmpty()) {
            return;
        }
        partyReminderTasks.addAll(List.of(
                new PartyReminderTaskResponse(1L, 101L, 11L, "提交入党申请书", 20001L, "张三", "2024010001", LocalDateTime.of(2026, 4, 15, 0, 0), "miniprogram", "pending", null, null, LocalDateTime.of(2026, 3, 15, 0, 0)),
                new PartyReminderTaskResponse(2L, 102L, 12L, "党课学习", 20002L, "李四", "2024010002", LocalDateTime.of(2026, 4, 20, 0, 0), "miniprogram", "sent", LocalDateTime.of(2026, 3, 10, 10, 0), null, LocalDateTime.of(2026, 3, 10, 0, 0)),
                new PartyReminderTaskResponse(3L, 103L, 13L, "思想汇报", 20003L, "王五", "2024010003", LocalDateTime.of(2026, 4, 10, 0, 0), "sms", "failed", LocalDateTime.of(2026, 3, 12, 14, 0), "用户未绑定手机号", LocalDateTime.of(2026, 3, 12, 0, 0)),
                new PartyReminderTaskResponse(4L, 104L, 14L, "预备党员转正", 20004L, "赵六", "2024010004", LocalDateTime.of(2026, 5, 1, 0, 0), "email", "pending", null, null, LocalDateTime.of(2026, 3, 20, 0, 0))
        ));
    }

    private List<PartyReminderTaskResponse> filterPartyReminderTasks(PartyReminderTaskFilterRequest request) {
        String normalizedStatus = QueryFilterSupport.trimToNull(request.status());
        String normalizedChannel = QueryFilterSupport.trimToNull(request.channel());
        String normalizedStudentKeyword = QueryFilterSupport.trimToNull(request.studentKeyword());
        return partyReminderTasks.stream()
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedChannel == null || normalizedChannel.equals(item.channel()))
                .filter(item -> normalizedStudentKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.studentName(), normalizedStudentKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo(), normalizedStudentKeyword))
                .toList();
    }
}
