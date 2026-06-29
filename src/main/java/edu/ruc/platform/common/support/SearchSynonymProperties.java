package edu.ruc.platform.common.support;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "platform.search")
public class SearchSynonymProperties {

    /**
     * Each inner list is a synonym group.
     * Example: [在读证明, 证明, 盖章申请]
     */
    private List<List<String>> synonyms = new ArrayList<>();

    public List<List<String>> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<List<String>> synonyms) {
        this.synonyms = synonyms == null ? new ArrayList<>() : synonyms;
    }
}
