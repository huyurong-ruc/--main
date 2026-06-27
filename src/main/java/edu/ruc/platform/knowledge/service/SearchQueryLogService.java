package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.knowledge.domain.SearchQueryLog;
import edu.ruc.platform.knowledge.repository.SearchQueryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class SearchQueryLogService {

    private final SearchQueryLogRepository repository;

    public void record(String keyword, int resultCount) {
        String k = keyword == null ? null : keyword.trim();
        if (k == null || k.isBlank()) {
            return;
        }
        SearchQueryLog log = new SearchQueryLog();
        log.setKeyword(k);
        log.setResultCount(Math.max(resultCount, 0));
        log.setCreatedAt(LocalDateTime.now());
        repository.save(log);
    }
}

