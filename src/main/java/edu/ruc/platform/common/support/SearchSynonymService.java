package edu.ruc.platform.common.support;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchSynonymService {

    private final SearchSynonymProperties properties;

    private volatile Map<String, Set<String>> synonymIndex = Map.of();

    @PostConstruct
    public void init() {
        this.synonymIndex = buildIndex(properties.getSynonyms());
    }

    public Set<String> expandTokens(String keyword) {
        Set<String> expanded = new LinkedHashSet<>(SearchRankingSupport.tokenizeKeyword(keyword));
        if (expanded.isEmpty()) {
            return expanded;
        }
        Set<String> additional = new LinkedHashSet<>();
        for (String token : expanded) {
            Set<String> group = synonymIndex.get(normalize(token));
            if (group != null) {
                additional.addAll(group);
            }
        }
        expanded.addAll(additional);
        return expanded;
    }

    private Map<String, Set<String>> buildIndex(List<List<String>> synonymGroups) {
        if (synonymGroups == null || synonymGroups.isEmpty()) {
            return Map.of();
        }
        List<Set<String>> groups = new ArrayList<>();
        for (List<String> group : synonymGroups) {
            Set<String> cleaned = group == null ? Set.of() : group.stream()
                    .map(this::normalize)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!cleaned.isEmpty()) {
                groups.add(cleaned);
            }
        }

        Map<String, Set<String>> index = new java.util.LinkedHashMap<>();
        for (Set<String> group : groups) {
            for (String token : group) {
                index.computeIfAbsent(token, key -> new LinkedHashSet<>()).addAll(group);
            }
        }
        return index;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
