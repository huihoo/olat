package org.olat.lms.search.searcher;

import java.io.Serializable;
import java.util.List;

import org.olat.data.basesecurity.Roles;

/**
 * Description:<br>
 * Encapsulates the search request input.
 * <P>
 * Initial Date: 03.06.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class SearchRequest implements Serializable {

    private String queryString;
    private List<String> condQueries;
    private Long identityId;
    private Roles roles;
    private boolean doHighlighting;
    private int firstResult;
    private int maxResults;

    public SearchRequest() {
        // default constructor
    }

    public SearchRequest(final String queryString, final List<String> condQueries, final Long identityId, final Roles roles, final int firstResult, final int maxResults,
            final boolean doHighlighting) {
        super();
        this.queryString = queryString;
        this.condQueries = condQueries;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.identityId = identityId;
        this.roles = roles;
        this.doHighlighting = doHighlighting;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(final int firstResult) {
        this.firstResult = firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(final int maxResults) {
        this.maxResults = maxResults;
    }

    public boolean isDoHighlighting() {
        return doHighlighting;
    }

    public void setDoHighlighting(final boolean doHighlighting) {
        this.doHighlighting = doHighlighting;
    }

    public Long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(final Long identityId) {
        this.identityId = identityId;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(final String queryString) {
        this.queryString = queryString;
    }

    public List<String> getCondQueries() {
        return condQueries;
    }

    public void setCondQueries(final List<String> condQueries) {
        this.condQueries = condQueries;
    }

    public Roles getRoles() {
        return roles;
    }

    public void setRoles(final Roles roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        // dummy impl
        return "queryString: " + queryString + " identityId: " + identityId + " roles: " + roles + " doHighlighting: " + doHighlighting;
    }
}
