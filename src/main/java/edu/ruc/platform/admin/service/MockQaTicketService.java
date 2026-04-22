package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.QaTicketDetailResponse;
import edu.ruc.platform.admin.dto.QaTicketListItemResponse;
import edu.ruc.platform.admin.dto.QaTicketMessageResponse;
import edu.ruc.platform.admin.dto.QaTicketReplyRequest;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockQaTicketService implements QaTicketApplicationService {

    private final AdminApplicationService adminService;
    private final AtomicLong idGen = new AtomicLong(100);
    private final AtomicLong messageIdGen = new AtomicLong(1000);
    private final List<Ticket> tickets = new ArrayList<>();

    @Override
    public PageResponse<QaTicketListItemResponse> pageTickets(String status, int page, int size) {
        init();
        String s = QueryFilterSupport.normalizeUpper(status);
        List<Ticket> filtered = tickets.stream()
                .filter(t -> s == null || s.equalsIgnoreCase(t.status))
                .sorted(Comparator.comparing((Ticket t) -> t.createdAt).reversed())
                .toList();
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        List<QaTicketListItemResponse> content = filtered.subList(fromIndex, toIndex).stream().map(this::toListItem).toList();
        return new PageResponse<>(content, filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public QaTicketDetailResponse getDetail(Long id) {
        init();
        Ticket t = tickets.stream().filter(x -> x.id.equals(id)).findFirst().orElseThrow(() -> new BusinessException("工单不存在"));
        return toDetail(t);
    }

    @Override
    public QaTicketDetailResponse take(Long id) {
        init();
        Ticket t = tickets.stream().filter(x -> x.id.equals(id)).findFirst().orElseThrow(() -> new BusinessException("工单不存在"));
        if ("OPEN".equalsIgnoreCase(t.status)) {
            t.status = "IN_PROGRESS";
        }
        return toDetail(t);
    }

    @Override
    public QaTicketDetailResponse reply(Long id, QaTicketReplyRequest request) {
        init();
        Ticket t = tickets.stream().filter(x -> x.id.equals(id)).findFirst().orElseThrow(() -> new BusinessException("工单不存在"));
        if (Boolean.TRUE.equals(request.publishAsFaq())) {
            adminService.createKnowledgeItem(new AdminKnowledgeUpsertRequest(
                    "FAQ：" + (t.summary == null ? "" : t.summary),
                    "FAQ管理",
                    request.content(),
                    null,
                    "qa-ticket",
                    "全体学生",
                    "系统管理员",
                    true
            ));
        }
        t.messages.add(new Msg(messageIdGen.incrementAndGet(), "赵老师", "COUNSELOR", request.content(), LocalDateTime.now()));
        if (Boolean.TRUE.equals(request.closeTicket())) {
            t.status = "CLOSED";
        } else {
            t.status = "IN_PROGRESS";
        }
        return toDetail(t);
    }

    @Override
    public QaTicketDetailResponse close(Long id) {
        init();
        Ticket t = tickets.stream().filter(x -> x.id.equals(id)).findFirst().orElseThrow(() -> new BusinessException("工单不存在"));
        t.status = "CLOSED";
        return toDetail(t);
    }

    @Override
    public QaTicketDetailResponse deleteMessage(Long messageId) {
        init();
        Ticket ticket = tickets.stream()
                .filter(t -> t.messages.stream().anyMatch(m -> m.id.equals(messageId)))
                .findFirst()
                .orElseThrow(() -> new BusinessException("处理记录不存在"));
        ticket.messages.removeIf(m -> m.id.equals(messageId));
        return toDetail(ticket);
    }

    private void init() {
        if (!tickets.isEmpty()) {
            return;
        }
        tickets.add(new Ticket(101L, 10001L, "张三", "OPEN", "流程节点如何管理？", "流程节点如何管理？", LocalDateTime.of(2024, 3, 27, 14, 20), new ArrayList<>()));
        Ticket t2 = new Ticket(102L, 10002L, "李明", "IN_PROGRESS", "关于学校专业的要求", "关于学校专业的要求", LocalDateTime.of(2024, 3, 27, 14, 20), new ArrayList<>());
        t2.messages.add(new Msg(messageIdGen.incrementAndGet(), "赵老师", "COUNSELOR", "您好，关于专业的要求是…", LocalDateTime.of(2024, 3, 27, 15, 30)));
        tickets.add(t2);
        tickets.add(new Ticket(103L, 10003L, "王五", "CLOSED", "实习证明怎么开", "实习证明怎么开", LocalDateTime.of(2024, 3, 26, 9, 35), new ArrayList<>()));
        idGen.set(200);
    }

    private QaTicketListItemResponse toListItem(Ticket t) {
        return new QaTicketListItemResponse(t.id, t.studentName, t.status, t.summary, format(t.createdAt));
    }

    private QaTicketDetailResponse toDetail(Ticket t) {
        List<QaTicketMessageResponse> msgs = t.messages.stream()
                .map(m -> new QaTicketMessageResponse(m.id, m.actorName, m.actorRole, format(m.createdAt), m.content))
                .toList();
        return new QaTicketDetailResponse(t.id, t.studentName, t.studentId, format(t.createdAt), t.status, t.questionText, msgs);
    }

    private static String format(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT));
    }

    private static class Ticket {
        final Long id;
        final Long studentId;
        final String studentName;
        String status;
        final String summary;
        final String questionText;
        final LocalDateTime createdAt;
        final List<Msg> messages;

        Ticket(Long id, Long studentId, String studentName, String status, String summary, String questionText, LocalDateTime createdAt, List<Msg> messages) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.status = status;
            this.summary = summary;
            this.questionText = questionText;
            this.createdAt = createdAt;
            this.messages = messages;
        }
    }

    private record Msg(Long id, String actorName, String actorRole, String content, LocalDateTime createdAt) {
    }
}
