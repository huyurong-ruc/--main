package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.dto.KnowledgeDetailResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.service.NoticeApplicationService;
import edu.ruc.platform.party.service.PartyProgressApplicationService;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockKnowledgeService implements KnowledgeApplicationService {

    private final MockDataStore mockDataStore;
    private final StudentProfileApplicationService studentProfileService;
    private final NoticeApplicationService noticeService;
    private final CertificateApplicationService certificateService;
    private final PartyProgressApplicationService partyProgressService;
    private final ObjectMapper stateMapper = new ObjectMapper().findAndRegisterModules();
    private final Path searchLogPath = Paths.get(System.getProperty("user.home"), ".ssp", "mock", "search-query-log.jsonl");
    private final Path adminStatePath = Paths.get(System.getProperty("user.home"), ".ssp", "mock", "admin-state.json");
    private final boolean persistEnabled = !isTestRuntime();

    private static boolean isTestRuntime() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("surefire.real.class.path") != null;
    }

    private static class SearchLogEntry {
        public String keyword;
        public int resultCount;
        public LocalDateTime createdAt;
    }

    private static class AdminStateSnapshot {
        public List<AdminKnowledgeItemResponse> knowledgeItems;
        public List<KnowledgeAttachmentResponse> knowledgeAttachments;
        public Map<Long, String> knowledgeContents;
    }

    private AdminStateSnapshot loadAdminSnapshot() {
        if (!persistEnabled) {
            return null;
        }
        try {
            if (!Files.exists(adminStatePath)) {
                return null;
            }
            return stateMapper.readValue(adminStatePath.toFile(), AdminStateSnapshot.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<KnowledgeSearchResponse> listDynamicPublishedDocuments() {
        AdminStateSnapshot s = loadAdminSnapshot();
        if (s == null || s.knowledgeItems == null || s.knowledgeItems.isEmpty()) {
            return List.of();
        }
        Map<Long, String> contentMap = s.knowledgeContents == null ? Map.of() : s.knowledgeContents;
        return s.knowledgeItems.stream()
                .filter(item -> item != null && Boolean.TRUE.equals(item.published()))
                .map(item -> new KnowledgeSearchResponse(
                        item.id(),
                        item.title(),
                        item.category(),
                        item.officialUrl(),
                        contentMap.getOrDefault(item.id(), ""),
                        "STANDARD_ANSWER",
                        false
                ))
                .toList();
    }

    private void recordSearch(String keyword, int resultCount) {
        if (!persistEnabled) {
            return;
        }
        String k = QueryFilterSupport.trimToNull(keyword);
        if (k == null) {
            return;
        }
        try {
            Files.createDirectories(searchLogPath.getParent());
            SearchLogEntry entry = new SearchLogEntry();
            entry.keyword = k;
            entry.resultCount = Math.max(resultCount, 0);
            entry.createdAt = LocalDateTime.now();
            String line = stateMapper.writeValueAsString(entry) + "\n";
            Files.writeString(searchLogPath, line, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<KnowledgeSearchResponse> search(String keyword) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }
        List<KnowledgeSearchResponse> merged = new ArrayList<>();
        Map<Long, KnowledgeSearchResponse> seen = new HashMap<>();
        mockDataStore.knowledgeDocuments().forEach(item -> {
            if (item == null || item.id() == null) return;
            seen.put(item.id(), item);
        });
        listDynamicPublishedDocuments().forEach(item -> {
            if (item == null || item.id() == null) return;
            KnowledgeSearchResponse existing = seen.get(item.id());
            boolean existingHasAnswer = existing != null && QueryFilterSupport.trimToNull(existing.answer()) != null;
            boolean dynamicHasAnswer = QueryFilterSupport.trimToNull(item.answer()) != null;
            if (!existingHasAnswer || dynamicHasAnswer) {
                seen.put(item.id(), item);
            }
        });
        seen.values().forEach(item -> {
            if (QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                    || QueryFilterSupport.containsIgnoreCase(item.category(), normalizedKeyword)
                    || QueryFilterSupport.containsIgnoreCase(item.answer(), normalizedKeyword)) {
                merged.add(toSafeSearchResponse(item));
            }
        });
        List<KnowledgeSearchResponse> result = merged.stream()
                .sorted(Comparator.comparing(KnowledgeSearchResponse::id))
                .toList();
        recordSearch(normalizedKeyword, result.size());
        return result;
    }

    @Override
    public List<KnowledgeSearchResponse> listTemplates() {
        return List.of(
                new KnowledgeSearchResponse(101L, "在读证明模板", "模板下载", "/templates/study-certificate.docx", "标准在读证明模板下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(102L, "请假申请表", "模板下载", "/templates/leave-request.docx", "标准请假申请表模板下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(103L, "活动预算表", "模板下载", "/templates/activity-budget.xlsx", "活动预算表下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(104L, "党团事务材料模板", "模板下载", "/templates/party-materials.zip", "入党入团常用材料模板下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(105L, "知识库导入模板", "模板下载", "/templates/knowledge-import.xlsx", "管理员批量导入知识条目模板", "STANDARD_ANSWER", false)
        );
    }

    @Override
    public KnowledgeDetailResponse getDetail(Long id) {
        KnowledgeSearchResponse item = null;
        AdminStateSnapshot snap = loadAdminSnapshot();
        if (snap != null && snap.knowledgeItems != null) {
            AdminKnowledgeItemResponse found = snap.knowledgeItems.stream()
                    .filter(x -> x != null && x.id() != null && x.id().equals(id))
                    .findFirst()
                    .orElse(null);
            if (found != null && Boolean.TRUE.equals(found.published())) {
                Map<Long, String> contentMap = snap.knowledgeContents == null ? Map.of() : snap.knowledgeContents;
                String answer = contentMap.getOrDefault(found.id(), "");
                item = new KnowledgeSearchResponse(
                        found.id(),
                        found.title(),
                        found.category(),
                        found.officialUrl(),
                        answer,
                        "STANDARD_ANSWER",
                        false
                );
            }
        }
        KnowledgeSearchResponse base = mockDataStore.knowledgeDocuments().stream()
                .filter(doc -> doc != null && doc.id() != null && doc.id().equals(id))
                .findFirst()
                .orElse(null);
        if (item == null) {
            if (base == null) {
                throw new BusinessException("知识条目不存在");
            }
            item = base;
        } else {
            boolean dynamicHasAnswer = QueryFilterSupport.trimToNull(item.answer()) != null;
            boolean baseHasAnswer = base != null && QueryFilterSupport.trimToNull(base.answer()) != null;
            if (!dynamicHasAnswer && baseHasAnswer) {
                item = base;
            }
        }

        List<KnowledgeAttachmentResponse> attachments = List.of();
        if (snap != null && snap.knowledgeAttachments != null) {
            attachments = snap.knowledgeAttachments.stream()
                    .filter(a -> a != null && a.knowledgeId() != null && a.knowledgeId().equals(id))
                    .toList();
        }
        if (attachments.isEmpty() && item.id().equals(2L)) {
            attachments = List.of(new KnowledgeAttachmentResponse(
                    51L,
                    2L,
                    "party-process.pdf",
                    "application/pdf",
                    1024L,
                    "/uploads/knowledge/2/party-process.pdf",
                    "胡浩老师",
                    LocalDateTime.of(2026, 3, 22, 11, 0)
            ));
        }

        List<KnowledgeSearchResponse> relatedPool = new ArrayList<>();
        relatedPool.addAll(mockDataStore.knowledgeDocuments());
        relatedPool.addAll(listDynamicPublishedDocuments());
        String currentCategory = item.category();
        List<KnowledgeSearchResponse> relatedItems = relatedPool.stream()
                .filter(doc -> doc != null && doc.id() != null && !doc.id().equals(id))
                .filter(doc -> doc.category() != null && doc.category().equals(currentCategory))
                .limit(3)
                .map(this::toSafeSearchResponse)
                .toList();
        return new KnowledgeDetailResponse(
                item.id(),
                item.title(),
                item.category(),
                item.officialUrl(),
                resolveAnswer(item),
                resolveResponseStrategy(item),
                isOfficialLinkOnly(item),
                buildSafetyTip(item),
                item.id().equals(2L) ? "入党全流程说明.pdf" : item.title() + ".pdf",
                "全体学生",
                attachments,
                relatedItems
        );
    }

    @Override
    public List<KnowledgeSearchResponse> recommendForStudent(Long studentId) {
        StudentProfileResponse profile = studentProfileService.getStudent(studentId);
        List<TargetedNoticeResponse> notices = noticeService.listTargetedNotices(studentId);
        List<CertificateRequestResponse> certificateRequests = certificateService.listByStudentId(studentId);
        String currentStage = partyProgressService.getProgress(studentId).currentStage();
        return mockDataStore.knowledgeDocuments()
                .stream()
                .sorted(Comparator
                        .comparingInt((KnowledgeSearchResponse item) -> scoreRecommendation(item, profile, notices, certificateRequests, currentStage))
                        .reversed()
                        .thenComparing(KnowledgeSearchResponse::id))
                .limit(4)
                .map(this::toSafeSearchResponse)
                .toList();
    }

    private int scoreRecommendation(KnowledgeSearchResponse item,
                                    StudentProfileResponse profile,
                                    List<TargetedNoticeResponse> notices,
                                    List<CertificateRequestResponse> certificateRequests,
                                    String currentStage) {
        int score = 0;
        String text = (item.title() + " " + item.category() + " " + item.answer()).toLowerCase(Locale.ROOT);
        if (profile.major() != null && text.contains(profile.major().toLowerCase(Locale.ROOT))) {
            score += 2;
        }
        if (currentStage != null && !currentStage.isBlank() && text.contains("党")) {
            score += 3;
        }
        boolean hasPartyNotice = notices.stream()
                .anyMatch(notice -> notice.tags().stream().anyMatch(tag -> "党团事务".equals(tag) || "流程".equals(tag)));
        if (hasPartyNotice && text.contains("党")) {
            score += 2;
        }
        boolean hasCertificateRequest = certificateRequests.stream()
                .anyMatch(request -> "PENDING".equalsIgnoreCase(request.status()) || "WITHDRAWN".equalsIgnoreCase(request.status()));
        if (hasCertificateRequest && (text.contains("证明") || text.contains("表格"))) {
            score += 4;
        }
        if (text.contains("数据安全") || text.contains("保密")) {
            score += 1;
        }
        return score;
    }

    private KnowledgeSearchResponse toSafeSearchResponse(KnowledgeSearchResponse item) {
        return new KnowledgeSearchResponse(
                item.id(),
                item.title(),
                item.category(),
                item.officialUrl(),
                resolveAnswer(item),
                resolveResponseStrategy(item),
                isOfficialLinkOnly(item)
        );
    }

    private String resolveAnswer(KnowledgeSearchResponse item) {
        if (isOfficialLinkOnly(item)) {
            return "该主题涉及敏感信息或权限边界，前台仅展示公开口径，请优先查看官方链接或联系负责老师。";
        }
        return item.answer();
    }

    private String resolveResponseStrategy(KnowledgeSearchResponse item) {
        return isOfficialLinkOnly(item) ? "OFFICIAL_LINK_ONLY" : "STANDARD_ANSWER";
    }

    private boolean isOfficialLinkOnly(KnowledgeSearchResponse item) {
        return "数据安全".equals(item.category())
                || QueryFilterSupport.containsIgnoreCase(item.title(), "保密")
                || QueryFilterSupport.containsIgnoreCase(item.answer(), "严格控制");
    }

    private String buildSafetyTip(KnowledgeSearchResponse item) {
        if (isOfficialLinkOnly(item)) {
            return "敏感字段不在知识库详情中直接展开，需按权限走官方渠道查询。";
        }
        return "当前回复优先采用知识库标准答案，如遇特殊场景请以学院正式通知为准。";
    }

}
