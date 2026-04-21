package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.QaTicketDetailResponse;
import edu.ruc.platform.admin.dto.QaTicketListItemResponse;
import edu.ruc.platform.admin.dto.QaTicketMessageResponse;
import edu.ruc.platform.admin.dto.QaTicketReplyRequest;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.domain.KnowledgeQaTicket;
import edu.ruc.platform.knowledge.domain.KnowledgeQaTicketMessage;
import edu.ruc.platform.knowledge.repository.KnowledgeQaTicketMessageRepository;
import edu.ruc.platform.knowledge.repository.KnowledgeQaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class QaTicketService implements QaTicketApplicationService {

    private final KnowledgeQaTicketRepository ticketRepository;
    private final KnowledgeQaTicketMessageRepository messageRepository;
    private final CurrentUserService currentUserService;
    private final AdminApplicationService adminService;

    @Override
    public PageResponse<QaTicketListItemResponse> pageTickets(String status, int page, int size) {
        String normalized = QueryFilterSupport.normalizeUpper(status);
        List<KnowledgeQaTicket> tickets = normalized == null
                ? ticketRepository.findAll().stream().sorted(Comparator.comparing(KnowledgeQaTicket::getCreatedAt).reversed()).toList()
                : ticketRepository.findByStatusOrderByCreatedAtDesc(normalized);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, tickets.size());
        int toIndex = Math.min(fromIndex + normalizedSize, tickets.size());
        int totalPages = (int) Math.ceil(tickets.size() / (double) normalizedSize);
        List<QaTicketListItemResponse> content = tickets.subList(fromIndex, toIndex).stream().map(this::toListItem).toList();
        return new PageResponse<>(content, tickets.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public QaTicketDetailResponse getDetail(Long id) {
        KnowledgeQaTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new BusinessException("工单不存在"));
        return toDetail(ticket);
    }

    @Override
    public QaTicketDetailResponse take(Long id) {
        KnowledgeQaTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new BusinessException("工单不存在"));
        if ("OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
            AuthenticatedUser user = currentUserService.requireCurrentUser();
            ticket.setHandledBy(user.userId());
            ticket.setHandledAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());
            ticket = ticketRepository.save(ticket);
        }
        return toDetail(ticket);
    }

    @Override
    public QaTicketDetailResponse reply(Long id, QaTicketReplyRequest request) {
        KnowledgeQaTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new BusinessException("工单不存在"));
        AuthenticatedUser user = currentUserService.requireCurrentUser();

        KnowledgeQaTicketMessage msg = new KnowledgeQaTicketMessage();
        msg.setTicketId(ticket.getId());
        msg.setActorName(user.name());
        msg.setActorRole(user.role());
        msg.setMessageText(request.content());
        msg.setCreatedAt(LocalDateTime.now());
        messageRepository.save(msg);

        if (Boolean.TRUE.equals(request.publishAsFaq())) {
            adminService.createKnowledgeItem(new AdminKnowledgeUpsertRequest(
                    "FAQ：" + (ticket.getQuestionText() == null ? "" : ticket.getQuestionText()),
                    "FAQ管理",
                    request.content(),
                    null,
                    null,
                    "全体学生",
                    user.name(),
                    true
            ));
        }
        ticket.setStatus(Boolean.TRUE.equals(request.closeTicket()) ? "CLOSED" : "IN_PROGRESS");
        ticket.setHandledBy(user.userId());
        ticket.setHandledAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        return toDetail(ticket);
    }

    @Override
    public QaTicketDetailResponse close(Long id) {
        KnowledgeQaTicket ticket = ticketRepository.findById(id).orElseThrow(() -> new BusinessException("工单不存在"));
        ticket.setStatus("CLOSED");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        return toDetail(ticket);
    }

    private QaTicketListItemResponse toListItem(KnowledgeQaTicket t) {
        String name = t.getAskName() == null || t.getAskName().isBlank() ? (t.getAskUsername() == null ? "-" : t.getAskUsername()) : t.getAskName();
        String summary = t.getQuestionText();
        if (summary != null && summary.length() > 40) {
            summary = summary.substring(0, 40);
        }
        return new QaTicketListItemResponse(t.getId(), name, t.getStatus(), summary, format(t.getCreatedAt()));
    }

    private QaTicketDetailResponse toDetail(KnowledgeQaTicket t) {
        List<QaTicketMessageResponse> msgs = messageRepository.findByTicketIdOrderByCreatedAtAsc(t.getId()).stream()
                .map(m -> new QaTicketMessageResponse(m.getActorName(), m.getActorRole(), format(m.getCreatedAt()), m.getMessageText()))
                .toList();
        String name = t.getAskName() == null || t.getAskName().isBlank() ? (t.getAskUsername() == null ? "-" : t.getAskUsername()) : t.getAskName();
        return new QaTicketDetailResponse(t.getId(), name, t.getAskUserId(), format(t.getCreatedAt()), t.getStatus(), t.getQuestionText(), msgs);
    }

    private static String format(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT));
    }
}
