package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.QaTicketDetailResponse;
import edu.ruc.platform.admin.dto.QaTicketListItemResponse;
import edu.ruc.platform.admin.dto.QaTicketReplyRequest;
import edu.ruc.platform.common.api.PageResponse;

public interface QaTicketApplicationService {

    PageResponse<QaTicketListItemResponse> pageTickets(String status, int page, int size);

    QaTicketDetailResponse getDetail(Long id);

    QaTicketDetailResponse take(Long id);

    QaTicketDetailResponse reply(Long id, QaTicketReplyRequest request);

    QaTicketDetailResponse close(Long id);
}

