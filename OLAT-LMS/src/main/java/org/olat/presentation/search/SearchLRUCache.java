package org.olat.presentation.search;

import org.apache.commons.collections.map.LRUMap;
import org.olat.lms.search.SearchResults;

class SearchLRUCache extends LRUMap {

    public SearchLRUCache() {
        super(5);
    }

    @Override
    public SearchResults get(final Object key) {
        final SearchEntry searchEntry = (SearchEntry) super.get(key);
        if (searchEntry != null) {
            if (searchEntry.isUpToDate()) {
                return searchEntry.getSearchResults();
            }
            remove(key);
        }
        return null;
    }

    @Override
    public Object put(final Object key, final Object value) {
        return super.put(key, new SearchEntry((SearchResults) value));
    }

    public class SearchEntry {
        private final SearchResults results;
        private final long timestamp = System.currentTimeMillis();

        public SearchEntry(final SearchResults results) {
            this.results = results;
        }

        public SearchResults getSearchResults() {
            return results;
        }

        public boolean isUpToDate() {
            return System.currentTimeMillis() - timestamp < 300000;
        }
    }
}
