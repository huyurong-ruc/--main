package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record QaTicketReplyRequest(
        @NotBlank(message = "回复内容不能为空") String content,
        Boolean publishAsFaq,
        Boolean closeTicket
) {
}

