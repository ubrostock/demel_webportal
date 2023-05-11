/*
 * This file is part of the DEMel web application.
 * 
 * Copyright 2023
 * DEMel project team, Rostock University, 18051 Rostock, Germany
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni.rostock.demel.portal.edit.model;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AttestationEditObject extends AbstractEditObject {

    private static final String OBJECT_TYPE = "attestation";

    //id like in SOLR with prefix "d" and leading zeros
    //e.g. "a00009378"
    private String id;

    @Range(min = 0, max = 9999, message = "demel.attestation_editor.error.dating_from.range")
    private Integer datingFrom;

    @Range(min = 0, max = 9999, message = "demel.attestation_editor.error.dating_to.range")
    private Integer datingTo;

    @Size(max = 128, message = "demel.attestation_editor.error.dating_display.size")
    private String datingDisplay;

    @Size(max = 256, message = "demel.attestation_editor.error.multiwordexpr.size")
    private String multiwordexpr;

    @Size(max = 1024, message = "demel.attestation_editor.error.hints_intern.size")
    private String hintsIntern;

    @Size(max = 256, message = "demel.attestation_editor.error.form.size")
    private String form;

    @NotEmpty(message = "demel.attestation_editor.error.lemma_id.notempty")
    private String lemmaId;

    private String lemmalinkId;

    @NotEmpty(message = "demel.attestation_editor.error.source_id.notempty")
    private String sourceId;

    public Integer getDatingFrom() {
        return datingFrom;
    }

    public void setDatingFrom(Integer datingFrom) {
        this.datingFrom = datingFrom;
    }

    public Integer getDatingTo() {
        return datingTo;
    }

    public void setDatingTo(Integer datingTo) {
        this.datingTo = datingTo;
    }

    public String getDatingDisplay() {
        return datingDisplay;
    }

    public void setDatingDisplay(String datingDisplay) {
        this.datingDisplay = datingDisplay;
    }

    public String getMultiwordexpr() {
        return multiwordexpr;
    }

    public void setMultiwordexpr(String multiwordexpr) {
        this.multiwordexpr = multiwordexpr;
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(String lemmaId) {
        this.lemmaId = lemmaId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getLemmalinkId() {
        return lemmalinkId;
    }

    public void setLemmalinkId(String lemmalinkId) {
        this.lemmalinkId = lemmalinkId;
    }

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }

}
