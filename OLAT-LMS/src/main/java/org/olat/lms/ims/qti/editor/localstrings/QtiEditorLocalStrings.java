/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.lms.ims.qti.editor.localstrings;

/**
 * Initial Date: 11.10.2011 <br>
 * Serves as parameter object for QTIEditHelper class methods. Implemented via Builder pattern
 * 
 * @author Branislav Balaz
 */
public class QtiEditorLocalStrings {

    private final String newSection;
    private final String newQuestion;
    private final String newQuestionText;
    private final String newResponseText;
    private final String newTextElement;

    private QtiEditorLocalStrings(Builder builder) {
        newSection = setCheckNullValue(builder.newSection);
        newQuestion = setCheckNullValue(builder.newQuestion);
        newQuestionText = setCheckNullValue(builder.newQuestionText);
        newResponseText = setCheckNullValue(builder.newResponseText);
        newTextElement = setCheckNullValue(builder.newTextElement);
    }

    private String setCheckNullValue(String value) {
        return value == null ? "" : value;
    }

    public String getNewSection() {
        return newSection;
    }

    public String getNewQuestion() {
        return newQuestion;
    }

    public String getNewQuestionText() {
        return newQuestionText;
    }

    public String getNewResponseText() {
        return newResponseText;
    }

    public String getNewTextElement() {
        return newTextElement;
    }

    public static class Builder {

        private String newSection;
        private String newQuestion;
        private String newQuestionText;
        private String newResponseText;
        private String newTextElement;

        public Builder newSection(String value) {
            newSection = value;
            return this;
        }

        public Builder newQuestion(String value) {
            newQuestion = value;
            return this;
        }

        public Builder newQuestionText(String value) {
            newQuestionText = value;
            return this;
        }

        public Builder newResponseText(String value) {
            newResponseText = value;
            return this;
        }

        public Builder newTextElement(String value) {
            newTextElement = value;
            return this;
        }

        public QtiEditorLocalStrings build() {
            return new QtiEditorLocalStrings(this);
        }

    }

}
