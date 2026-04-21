package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.SearchQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchQueryLogRepository extends JpaRepository<SearchQueryLog, Long> {

    List<SearchQueryLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

