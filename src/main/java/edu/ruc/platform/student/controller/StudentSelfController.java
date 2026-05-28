package edu.ruc.platform.student.controller;

import edu.ruc.platform.admin.dto.QaTicketDetailResponse;
import edu.ruc.platform.admin.dto.QaTicketListItemResponse;
import edu.ruc.platform.admin.dto.QaTicketMessageResponse;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.knowledge.domain.KnowledgeQaTicket;
import edu.ruc.platform.knowledge.domain.KnowledgeQaTicketMessage;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.repository.KnowledgeQaTicketMessageRepository;
import edu.ruc.platform.knowledge.repository.KnowledgeQaTicketRepository;
import edu.ruc.platform.knowledge.repository.LatestKnowledgePolicyRepository;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.student.dto.StudentDashboardResponse;
import edu.ruc.platform.student.dto.StudentGrowthSuggestionsResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.service.StudentSelfApplicationService;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentSelfController {

    private final StudentProfileApplicationService studentProfileService;
    private final StudentSelfApplicationService studentSelfService;
    private final CurrentUserService currentUserService;
    private final KnowledgeQaTicketRepository ticketRepository;
    private final KnowledgeQaTicketMessageRepository messageRepository;
    private final LatestKnowledgePolicyRepository latestKnowledgePolicyRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/me")
    public ApiResponse<StudentProfileResponse> me() {
        return ApiResponse.success(studentProfileService.currentStudentProfile());
    }

    @GetMapping("/dashboard")
    public ApiResponse<StudentDashboardResponse> dashboard() {
        return ApiResponse.success(studentSelfService.dashboard());
    }

    @GetMapping("/growth-suggestions")
    public ApiResponse<StudentGrowthSuggestionsResponse> growthSuggestions() {
        return ApiResponse.success(studentSelfService.growthSuggestions());
    }

    @GetMapping("/notices")
    public ApiResponse<List<TargetedNoticeResponse>> myNotices() {
        return ApiResponse.success(studentSelfService.myNotices());
    }

    @GetMapping("/certificates/requests")
    public ApiResponse<List<CertificateRequestResponse>> myCertificateRequests() {
        return ApiResponse.success(studentSelfService.myCertificateRequests());
    }

    @GetMapping("/party-progress")
    public ApiResponse<PartyProgressResponse> myPartyProgress() {
        return ApiResponse.success(studentSelfService.myPartyProgress());
    }

    @GetMapping("/party-progress/reminders")
    public ApiResponse<List<ReminderResponse>> myPartyReminders() {
        return ApiResponse.success(studentSelfService.myPartyReminders());
    }

    @GetMapping("/knowledge/recommended")
    public ApiResponse<List<KnowledgeSearchResponse>> recommendedKnowledge() {
        return ApiResponse.success(studentSelfService.recommendedKnowledge());
    }

    public record StudentPolicyListItemResponse(
            Long id,
            String title,
            String category,
            String officialUrl,
            String sourceFileName,
            String updatedAt
    ) {
    }

    @GetMapping("/policies/page")
    public ApiResponse<PageResponse<StudentPolicyListItemResponse>> pagePolicies(@Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                                @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.STUDENT);
        List<edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy> all = latestKnowledgePolicyRepository.findByIsDeletedAndIsPublished(0, 1).stream()
                .filter(item -> !isFaqPolicy(item))
                .sorted(Comparator.comparing(edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, all.size());
        int toIndex = Math.min(fromIndex + normalizedSize, all.size());
        int totalPages = (int) Math.ceil(all.size() / (double) normalizedSize);
        List<StudentPolicyListItemResponse> content = all.subList(fromIndex, toIndex).stream().map(item -> {
            String category = resolvePolicyCategory(item.getExtJson());
            String sourceFileName = resolvePolicyMeta(item.getExtJson()).get("sourceFileName");
            return new StudentPolicyListItemResponse(
                    item.getId(),
                    item.getTitle(),
                    category == null ? "未分类" : category,
                    item.getSourceUrl(),
                    sourceFileName,
                    format(item.getUpdatedAt())
            );
        }).toList();
        return ApiResponse.success(new PageResponse<>(content, all.size(), totalPages, normalizedPage, normalizedSize));
    }

    public record StudentQaTicketCreateRequest(
            @NotBlank(message = "问题内容不能为空") String questionText,
            String contact
    ) {
    }

    @PostMapping("/qa-tickets")
    public ApiResponse<QaTicketDetailResponse> createTicket(@Valid @RequestBody StudentQaTicketCreateRequest request) {
        AuthenticatedUser user = currentUserService.requireAnyRole(RoleType.STUDENT);
        String actorName = (user.name() == null || user.name().isBlank()) ? user.username() : user.name();
        Long studentId = user.studentId() != null ? user.studentId() : user.userId();

        KnowledgeQaTicket ticket = new KnowledgeQaTicket();
        ticket.setAskUserId(studentId);
        ticket.setAskUsername(user.username());
        ticket.setAskName(actorName);
        ticket.setStatus("OPEN");
        ticket.setQuestionText(buildQuestionText(request.questionText(), request.contact()));
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        return ApiResponse.success(toDetail(ticket, List.of()));
    }

    @GetMapping("/qa-tickets/page")
    public ApiResponse<PageResponse<QaTicketListItemResponse>> pageMyTickets(@RequestParam(required = false) String status,
                                                                            @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                            @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        AuthenticatedUser user = currentUserService.requireAnyRole(RoleType.STUDENT);
        Long studentId = user.studentId() != null ? user.studentId() : user.userId();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        List<KnowledgeQaTicket> tickets = normalizedStatus == null
                ? ticketRepository.findByAskUserIdOrderByCreatedAtDesc(studentId)
                : ticketRepository.findByAskUserIdAndStatusOrderByCreatedAtDesc(studentId, normalizedStatus);

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, tickets.size());
        int toIndex = Math.min(fromIndex + normalizedSize, tickets.size());
        int totalPages = (int) Math.ceil(tickets.size() / (double) normalizedSize);
        List<QaTicketListItemResponse> content = tickets.subList(fromIndex, toIndex).stream().map(this::toListItem).toList();
        return ApiResponse.success(new PageResponse<>(content, tickets.size(), totalPages, normalizedPage, normalizedSize));
    }

    @GetMapping("/qa-tickets/{id}")
    public ApiResponse<QaTicketDetailResponse> myTicketDetail(@Positive(message = "工单ID必须大于0") @PathVariable Long id) {
        AuthenticatedUser user = currentUserService.requireAnyRole(RoleType.STUDENT);
        Long studentId = user.studentId() != null ? user.studentId() : user.userId();
        KnowledgeQaTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new edu.ruc.platform.common.exception.BusinessException("工单不存在"));
        if (ticket.getAskUserId() == null || !ticket.getAskUserId().equals(studentId)) {
            throw new edu.ruc.platform.common.exception.BusinessException("无权查看该工单");
        }
        List<KnowledgeQaTicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        return ApiResponse.success(toDetail(ticket, messages));
    }

    private QaTicketListItemResponse toListItem(KnowledgeQaTicket ticket) {
        String summary = ticket.getQuestionText();
        if (summary != null) {
            summary = summary.strip();
            if (summary.length() > 60) {
                summary = summary.substring(0, 60);
            }
        }
        return new QaTicketListItemResponse(
                ticket.getId(),
                ticket.getAskName() == null ? "-" : ticket.getAskName(),
                ticket.getStatus(),
                summary,
                format(ticket.getCreatedAt())
        );
    }

    private QaTicketDetailResponse toDetail(KnowledgeQaTicket ticket, List<KnowledgeQaTicketMessage> messages) {
        List<QaTicketMessageResponse> msg = (messages == null ? List.<KnowledgeQaTicketMessage>of() : messages).stream()
                .map(m -> new QaTicketMessageResponse(
                        m.getId(),
                        m.getActorName(),
                        m.getActorRole(),
                        format(m.getCreatedAt()),
                        m.getMessageText()
                ))
                .toList();
        return new QaTicketDetailResponse(
                ticket.getId(),
                ticket.getAskName(),
                ticket.getAskUserId(),
                format(ticket.getCreatedAt()),
                ticket.getStatus(),
                ticket.getQuestionText(),
                ticket.getMatchedFaqId(),
                msg
        );
    }

    private static String format(LocalDateTime t) {
        return t == null ? "" : DTF.format(t);
    }

    private static String buildQuestionText(String questionText, String contact) {
        String q = questionText == null ? "" : questionText.trim();
        String c = contact == null ? "" : contact.trim();
        if (c.isBlank()) {
            return q;
        }
        return "联系方式：" + c + "\n\n" + q;
    }

    private String resolvePolicyCategory(String extJson) {
        String value = resolvePolicyMeta(extJson).get("category");
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private boolean isFaqPolicy(edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy item) {
        String category = resolvePolicyCategory(item == null ? null : item.getExtJson());
        if (category == null) {
            return false;
        }
        String normalized = category.trim().toLowerCase();
        return normalized.contains("faq") || category.contains("FAQ管理") || category.contains("问答");
    }

    private Map<String, String> resolvePolicyMeta(String extJson) {
        if (extJson == null || extJson.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(extJson, new TypeReference<>() {});
            return map.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> String.valueOf(entry.getValue()),
                            (left, right) -> right,
                            java.util.LinkedHashMap::new
                    ));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
