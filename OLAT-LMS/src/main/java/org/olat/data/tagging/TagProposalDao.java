package org.olat.data.tagging;

import java.util.List;

public interface TagProposalDao {

    /**
     * get some good tags for the given text
     * 
     * @param referenceText
     * @param onlyExisting
     *            if true, returns only such tags, that yet exist
     * @return
     */
    public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting);

}
