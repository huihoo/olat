package org.olat.connectors.rest.support.vo.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Description:<br>
 * task course node configuration
 * <P>
 * Initial Date: 27.07.2010 <br>
 * 
 * @author skoeber
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "taskConfigVO")
public class TaskConfigVO {

    private Boolean isAssignmentEnabled;
    private String taskAssignmentType;
    private String taskAssignmentText;
    private Boolean isTaskPreviewEnabled;
    private Boolean isTaskDeselectEnabled;
    private Boolean onlyOneUserPerTask;

    private Boolean isDropboxEnabled;
    private Boolean isDropboxConfirmationMailEnabled;
    private String dropboxConfirmationText;

    private Boolean isReturnboxEnabled;

    private Boolean isScoringEnabled;
    private Boolean isScoringGranted;
    private Float minScore;
    private Float maxScore;
    private Boolean isPassingGranted;
    private Float passingScoreThreshold;
    private Boolean hasCommentField;
    private String commentForUser;
    private String commentForCoaches;

    private Boolean isSolutionEnabled;

    private String conditionTask;
    private String conditionDropbox;
    private String conditionReturnbox;
    private String conditionScoring;
    private String conditionSolution;

    public TaskConfigVO() {
        // make JAXB happy
    }

    public Boolean getIsAssignmentEnabled() {
        return isAssignmentEnabled;
    }

    public void setIsAssignmentEnabled(final Boolean isAssignmentEnabled) {
        this.isAssignmentEnabled = isAssignmentEnabled;
    }

    public String getTaskAssignmentType() {
        return taskAssignmentType;
    }

    public void setTaskAssignmentType(final String taskAssignmentType) {
        this.taskAssignmentType = taskAssignmentType;
    }

    public String getTaskAssignmentText() {
        return taskAssignmentText;
    }

    public void setTaskAssignmentText(final String taskAssignmentText) {
        this.taskAssignmentText = taskAssignmentText;
    }

    public Boolean getIsTaskPreviewEnabled() {
        return isTaskPreviewEnabled;
    }

    public void setIsTaskPreviewEnabled(final Boolean isTaskPreviewEnabled) {
        this.isTaskPreviewEnabled = isTaskPreviewEnabled;
    }

    public Boolean getIsTaskDeselectEnabled() {
        return isTaskDeselectEnabled;
    }

    public void setIsTaskDeselectEnabled(final Boolean isTaskDeselectEnabled) {
        this.isTaskDeselectEnabled = isTaskDeselectEnabled;
    }

    public Boolean getOnlyOneUserPerTask() {
        return onlyOneUserPerTask;
    }

    public void setOnlyOneUserPerTask(final Boolean onlyOneUserPerTask) {
        this.onlyOneUserPerTask = onlyOneUserPerTask;
    }

    public Boolean getIsDropboxEnabled() {
        return isDropboxEnabled;
    }

    public void setIsDropboxEnabled(final Boolean isDropboxEnabled) {
        this.isDropboxEnabled = isDropboxEnabled;
    }

    public Boolean getIsDropboxConfirmationMailEnabled() {
        return isDropboxConfirmationMailEnabled;
    }

    public void setIsDropboxConfirmationMailEnabled(final Boolean isDropboxConfirmationMailEnabled) {
        this.isDropboxConfirmationMailEnabled = isDropboxConfirmationMailEnabled;
    }

    public Boolean getIsReturnboxEnabled() {
        return isReturnboxEnabled;
    }

    public void setIsReturnboxEnabled(final Boolean isReturnboxEnabled) {
        this.isReturnboxEnabled = isReturnboxEnabled;
    }

    public Boolean getIsScoringEnabled() {
        return isScoringEnabled;
    }

    public void setIsScoringEnabled(final Boolean isScoringEnabled) {
        this.isScoringEnabled = isScoringEnabled;
    }

    public Boolean getIsScoringGranted() {
        return isScoringGranted;
    }

    public void setIsScoringGranted(final Boolean isScoringGranted) {
        this.isScoringGranted = isScoringGranted;
    }

    public Float getMinScore() {
        return minScore;
    }

    public void setMinScore(final Float minScore) {
        this.minScore = minScore;
    }

    public Float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(final Float maxScore) {
        this.maxScore = maxScore;
    }

    public Boolean getIsPassingGranted() {
        return isPassingGranted;
    }

    public void setIsPassingGranted(final Boolean isPassingGranted) {
        this.isPassingGranted = isPassingGranted;
    }

    public Float getPassingScoreThreshold() {
        return passingScoreThreshold;
    }

    public void setPassingScoreThreshold(final Float passingScoreThreshold) {
        this.passingScoreThreshold = passingScoreThreshold;
    }

    public Boolean getHasCommentField() {
        return hasCommentField;
    }

    public void setHasCommentField(final Boolean hasCommentField) {
        this.hasCommentField = hasCommentField;
    }

    public String getCommentForUser() {
        return commentForUser;
    }

    public void setCommentForUser(final String commentForUser) {
        this.commentForUser = commentForUser;
    }

    public String getCommentForCoaches() {
        return commentForCoaches;
    }

    public void setCommentForCoaches(final String commentForCoaches) {
        this.commentForCoaches = commentForCoaches;
    }

    public Boolean getIsSolutionEnabled() {
        return isSolutionEnabled;
    }

    public void setIsSolutionEnabled(final Boolean isSolutionEnabled) {
        this.isSolutionEnabled = isSolutionEnabled;
    }

    public String getConditionTask() {
        return conditionTask;
    }

    public void setConditionTask(final String conditionTask) {
        this.conditionTask = conditionTask;
    }

    public String getConditionDropbox() {
        return conditionDropbox;
    }

    public void setConditionDropbox(final String conditionDropbox) {
        this.conditionDropbox = conditionDropbox;
    }

    public String getConditionReturnbox() {
        return conditionReturnbox;
    }

    public void setConditionReturnbox(final String conditionReturnbox) {
        this.conditionReturnbox = conditionReturnbox;
    }

    public String getConditionScoring() {
        return conditionScoring;
    }

    public void setConditionScoring(final String conditionScoring) {
        this.conditionScoring = conditionScoring;
    }

    public String getConditionSolution() {
        return conditionSolution;
    }

    public void setConditionSolution(final String conditionSolution) {
        this.conditionSolution = conditionSolution;
    }

}
