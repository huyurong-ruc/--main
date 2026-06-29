package edu.ruc.platform.common.support;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class SearchRankingSupport {

    private static final JiebaSegmenter SEGMENTER = new JiebaSegmenter();
    private static final Pattern QUERY_SPLIT_PATTERN = Pattern.compile("[\\s,，。；;、|/\\\\()+\\-]+");
    private static final Pattern ALNUM_OR_CHINESE_PATTERN = Pattern.compile("[\\p{IsHan}A-Za-z0-9_]+");

    private SearchRankingSupport() {
    }

    public static String normalizeKeyword(String keyword) {
        return QueryFilterSupport.trimToNull(keyword);
    }

    public static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public static List<String> tokenizeKeyword(String keyword) {
        String normalized = normalizeKeyword(keyword);
        if (normalized == null) {
            return List.of();
        }

        Set<String> tokens = new LinkedHashSet<>();
        for (String part : QUERY_SPLIT_PATTERN.split(normalized)) {
            addSegmentedTokens(tokens, part);
        }
        addSegmentedTokens(tokens, normalized);
        return tokens.stream()
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    public static int countHits(String text, String token) {
        if (text == null || text.isBlank() || token == null || token.isBlank()) {
            return 0;
        }
        int count = 0;
        int fromIndex = 0;
        while (fromIndex >= 0) {
            int next = text.indexOf(token, fromIndex);
            if (next < 0) {
                break;
            }
            count += 1;
            fromIndex = next + token.length();
        }
        return count;
    }

    private static void addSegmentedTokens(Set<String> tokens, String rawText) {
        String text = normalizeKeyword(rawText);
        if (text == null) {
            return;
        }

        String compact = text.replaceAll("\\s+", "");
        if (!compact.isBlank()) {
            tokens.add(compact);
        }

        for (var segment : SEGMENTER.process(text, JiebaSegmenter.SegMode.SEARCH)) {
            String token = segment.word == null ? "" : segment.word.trim();
            if (!token.isBlank() && ALNUM_OR_CHINESE_PATTERN.matcher(token).matches()) {
                tokens.add(token.toLowerCase(Locale.ROOT));
            }
        }
    }
}
