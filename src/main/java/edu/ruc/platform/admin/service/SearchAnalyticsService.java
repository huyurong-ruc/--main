package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.HotKeywordStatsResponse;
import edu.ruc.platform.admin.dto.SearchAnalyticsSummaryResponse;
import edu.ruc.platform.admin.dto.SearchTrendPointResponse;
import edu.ruc.platform.admin.dto.ZeroResultKeywordStatsResponse;
import edu.ruc.platform.knowledge.domain.SearchQueryLog;
import edu.ruc.platform.knowledge.repository.SearchQueryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class SearchAnalyticsService implements SearchAnalyticsApplicationService {

    private final SearchQueryLogRepository repository;

    @Override
    public SearchAnalyticsSummaryResponse getSummary(int days) {
        int window = Math.max(1, Math.min(days, 90));
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(window - 1L).toLocalDate().atStartOfDay();
        List<SearchQueryLog> logs = repository.findByCreatedAtBetween(start, end);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd", Locale.ROOT);
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (int i = 0; i < window; i += 1) {
            daily.put(start.toLocalDate().plusDays(i), 0);
        }
        for (SearchQueryLog log : logs) {
            LocalDate d = log.getCreatedAt().toLocalDate();
            daily.put(d, daily.getOrDefault(d, 0) + 1);
        }
        List<SearchTrendPointResponse> trend = daily.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new SearchTrendPointResponse(e.getKey().format(fmt), e.getValue()))
                .toList();

        Map<String, Integer> keywordCounts = new HashMap<>();
        Map<String, Integer> missCounts = new HashMap<>();
        Map<String, LocalDateTime> missLast = new HashMap<>();
        for (SearchQueryLog log : logs) {
            String k = normalizeKey(log.getKeyword());
            if (k == null) continue;
            keywordCounts.put(k, keywordCounts.getOrDefault(k, 0) + 1);
            if (log.getResultCount() != null && log.getResultCount() == 0) {
                missCounts.put(k, missCounts.getOrDefault(k, 0) + 1);
                LocalDateTime at = log.getCreatedAt();
                LocalDateTime prev = missLast.get(k);
                if (prev == null || prev.isBefore(at)) {
                    missLast.put(k, at);
                }
            }
        }

        LocalDateTime prevStart = start.minusDays(window);
        LocalDateTime prevEnd = start.minusSeconds(1);
        List<SearchQueryLog> prevLogs = repository.findByCreatedAtBetween(prevStart, prevEnd);
        Map<String, Integer> prevKeywordCounts = new HashMap<>();
        for (SearchQueryLog log : prevLogs) {
            String k = normalizeKey(log.getKeyword());
            if (k == null) continue;
            prevKeywordCounts.put(k, prevKeywordCounts.getOrDefault(k, 0) + 1);
        }

        List<HotKeywordStatsResponse> hot = keywordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(10)
                .map(e -> {
                    int prev = prevKeywordCounts.getOrDefault(e.getKey(), 0);
                    int pct = prev == 0 ? 0 : (int) Math.round((e.getValue() - prev) * 100.0 / prev);
                    return new HotKeywordStatsResponse(e.getKey(), e.getValue(), pct);
                })
                .toList();

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);
        List<ZeroResultKeywordStatsResponse> zero = missCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(10)
                .map(e -> new ZeroResultKeywordStatsResponse(
                        e.getKey(),
                        e.getValue(),
                        missLast.get(e.getKey()) == null ? null : missLast.get(e.getKey()).toLocalDate().format(dayFmt)
                ))
                .toList();

        return new SearchAnalyticsSummaryResponse(trend, hot, zero);
    }

    private static String normalizeKey(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.isBlank()) return null;
        return k;
    }
}

