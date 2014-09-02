package org.olat.connectors.rest.support.vo.elements;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Description:<br>
 * test course node configuration
 * <P>
 * Initial Date: 27.07.2010 <br>
 * 
 * @author skoeber
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testConfigVO")
public class TestConfigVO {

    private Boolean showNavigation;
    private Boolean allowNavigation;
    private Boolean showSectionsOnly;
    private String sequencePresentation;
    private Boolean allowCancel;
    private Boolean allowSuspend;
    private Boolean showQuestionTitle;
    private String summeryPresentation;
    private Integer numAttempts;
    private Boolean showQuestionProgress;
    private Boolean showScoreProgress;
    private Boolean showScoreInfo;
    private Boolean showResultsAfterFinish;
    private Boolean showResultsOnHomepage;
    private Boolean showResultsDependendOnDate;
    private Date showResultsStartDate;
    private Date showResultsEndDate;

    public TestConfigVO() {
        // make JAXB happy
    }

    public Boolean getShowNavigation() {
        return showNavigation;
    }

    public void setShowNavigation(final Boolean showNavigation) {
        this.showNavigation = showNavigation;
    }

    public Boolean getAllowNavigation() {
        return allowNavigation;
    }

    public void setAllowNavigation(final Boolean allowNavigation) {
        this.allowNavigation = allowNavigation;
    }

    public Boolean getShowSectionsOnly() {
        return showSectionsOnly;
    }

    public void setShowSectionsOnly(final Boolean showSectionsOnly) {
        this.showSectionsOnly = showSectionsOnly;
    }

    public String getSequencePresentation() {
        return sequencePresentation;
    }

    public void setSequencePresentation(final String sequencePresentation) {
        this.sequencePresentation = sequencePresentation;
    }

    public Boolean getAllowCancel() {
        return allowCancel;
    }

    public void setAllowCancel(final Boolean allowCancel) {
        this.allowCancel = allowCancel;
    }

    public Boolean getAllowSuspend() {
        return allowSuspend;
    }

    public void setAllowSuspend(final Boolean allowSuspend) {
        this.allowSuspend = allowSuspend;
    }

    public Boolean getShowQuestionTitle() {
        return showQuestionTitle;
    }

    public void setShowQuestionTitle(final Boolean showQuestionTitle) {
        this.showQuestionTitle = showQuestionTitle;
    }

    public String getSummeryPresentation() {
        return summeryPresentation;
    }

    public void setSummeryPresentation(final String summeryPresentation) {
        this.summeryPresentation = summeryPresentation;
    }

    public Integer getNumAttempts() {
        return numAttempts;
    }

    public void setNumAttempts(final Integer numAttempts) {
        this.numAttempts = numAttempts;
    }

    public Boolean getShowQuestionProgress() {
        return showQuestionProgress;
    }

    public void setShowQuestionProgress(final Boolean showQuestionProgress) {
        this.showQuestionProgress = showQuestionProgress;
    }

    public Boolean getShowScoreProgress() {
        return showScoreProgress;
    }

    public void setShowScoreProgress(final Boolean showScoreProgress) {
        this.showScoreProgress = showScoreProgress;
    }

    public Boolean getShowScoreInfo() {
        return showScoreInfo;
    }

    public void setShowScoreInfo(final Boolean showScoreInfo) {
        this.showScoreInfo = showScoreInfo;
    }

    public Boolean getShowResultsAfterFinish() {
        return showResultsAfterFinish;
    }

    public void setShowResultsAfterFinish(final Boolean showResultsAfterFinish) {
        this.showResultsAfterFinish = showResultsAfterFinish;
    }

    public Boolean getShowResultsOnHomepage() {
        return showResultsOnHomepage;
    }

    public void setShowResultsOnHomepage(final Boolean showResultsOnHomepage) {
        this.showResultsOnHomepage = showResultsOnHomepage;
    }

    public Boolean getShowResultsDependendOnDate() {
        return showResultsDependendOnDate;
    }

    public void setShowResultsDependendOnDate(final Boolean showResultsDependendOnDate) {
        this.showResultsDependendOnDate = showResultsDependendOnDate;
    }

    public Date getShowResultsStartDate() {
        return showResultsStartDate;
    }

    public void setShowResultsStartDate(final Date showResultsStartDate) {
        this.showResultsStartDate = showResultsStartDate;
    }

    public Date getShowResultsEndDate() {
        return showResultsEndDate;
    }

    public void setShowResultsEndDate(final Date showResultsEndDate) {
        this.showResultsEndDate = showResultsEndDate;
    }
}
