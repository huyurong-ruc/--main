package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.HotKeywordStatsResponse;
import edu.ruc.platform.admin.dto.SearchAnalyticsSummaryResponse;
import edu.ruc.platform.admin.dto.SearchTrendPointResponse;
import edu.ruc.platform.admin.dto.ZeroResultKeywordStatsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockSearchAnalyticsService implements SearchAnalyticsApplicationService {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final Path searchLogPath = Paths.get(System.getProperty("user.home"), ".ssp", "mock", "search-query-log.jsonl");

    private static class SearchLogEntry {
        public String keyword;
        public Integer resultCount;
        public LocalDateTime createdAt;
    }

    @Override
    public SearchAnalyticsSummaryResponse getSummary(int days) {
        int window = Math.max(1, Math.min(days, 90));
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(window - 1L).toLocalDate().atStartOfDay();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd", Locale.ROOT);
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (int i = 0; i < window; i += 1) {
            daily.put(start.toLocalDate().plusDays(i), 0);
        }

        Map<String, Integer> keywordCounts = new HashMap<>();
        Map<String, Integer> missCounts = new HashMap<>();
        Map<String, LocalDateTime> missLast = new HashMap<>();

        if (Files.exists(searchLogPath)) {
            try (Stream<String> lines = Files.lines(searchLogPath)) {
                lines.forEach(line -> {
                    String raw = line == null ? "" : line.trim();
                    if (raw.isBlank()) {
                        return;
                    }
                    try {
                        SearchLogEntry entry = mapper.readValue(raw, SearchLogEntry.class);
                        if (entry == null || entry.keyword == null || entry.createdAt == null) {
                            return;
                        }
                        if (entry.createdAt.isBefore(start) || entry.createdAt.isAfter(end)) {
                            return;
                        }
                        String k = entry.keyword.trim();
                        if (k.isBlank()) {
                            return;
                        }
                        LocalDate d = entry.createdAt.toLocalDate();
                        daily.put(d, daily.getOrDefault(d, 0) + 1);
                        keywordCounts.put(k, keywordCounts.getOrDefault(k, 0) + 1);
                        int rc = entry.resultCount == null ? 0 : entry.resultCount;
                        if (rc == 0) {
                            missCounts.put(k, missCounts.getOrDefault(k, 0) + 1);
                            LocalDateTime prev = missLast.get(k);
                            if (prev == null || prev.isBefore(entry.createdAt)) {
                                missLast.put(k, entry.createdAt);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception ignored) {
            }
        }

        List<SearchTrendPointResponse> trend = daily.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new SearchTrendPointResponse(e.getKey().format(fmt), e.getValue()))
                .toList();

        List<HotKeywordStatsResponse> hot = keywordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(10)
                .map(e -> new HotKeywordStatsResponse(e.getKey(), e.getValue(), 0))
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
}
