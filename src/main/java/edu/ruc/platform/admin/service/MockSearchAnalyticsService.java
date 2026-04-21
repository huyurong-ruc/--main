package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.HotKeywordStatsResponse;
import edu.ruc.platform.admin.dto.SearchAnalyticsSummaryResponse;
import edu.ruc.platform.admin.dto.SearchTrendPointResponse;
import edu.ruc.platform.admin.dto.ZeroResultKeywordStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockSearchAnalyticsService implements SearchAnalyticsApplicationService {

    @Override
    public SearchAnalyticsSummaryResponse getSummary(int days) {
        return new SearchAnalyticsSummaryResponse(
                List.of(
                        new SearchTrendPointResponse("03-22", 430),
                        new SearchTrendPointResponse("03-23", 380),
                        new SearchTrendPointResponse("03-24", 450),
                        new SearchTrendPointResponse("03-25", 520),
                        new SearchTrendPointResponse("03-26", 470),
                        new SearchTrendPointResponse("03-27", 560),
                        new SearchTrendPointResponse("03-28", 590)
                ),
                List.of(
                        new HotKeywordStatsResponse("在读证明", 234, 15),
                        new HotKeywordStatsResponse("奖学金", 198, 8),
                        new HotKeywordStatsResponse("请假", 156, -3)
                ),
                List.of(
                        new ZeroResultKeywordStatsResponse("校外实习申请", 23, "2024-03-28"),
                        new ZeroResultKeywordStatsResponse("需审批政策", 18, "2024-03-27"),
                        new ZeroResultKeywordStatsResponse("奖学金说明", 15, "2024-03-26")
                )
        );
    }
}

