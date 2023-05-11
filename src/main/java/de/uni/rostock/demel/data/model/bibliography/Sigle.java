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
package de.uni.rostock.demel.data.model.bibliography;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.portal.util.searchbox.SearchboxItem;

public class Sigle extends AbstractModelObject implements SearchboxItem {

    public static final String DOCTYPE = "sigle";

    public static final String DOCID_PREFIX = "s";

    public static final Pattern DOCID_PATTERN = Pattern.compile("s?(\\d{1,4})");

    public static String convertId(long id) {
        return String.format("s%04d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum Type {
        MAIN, VARIANT;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Type byValue(String value) {
            for (Type t : Type.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * the id - "s"+1234 (4stellig)
     */
    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("sorting")
    private String sorting;

    @Field("s__name")
    private String name;

    private Type type;

    @Field("s__source__id")
    private String sourceId;

    @Field("s__source__name")
    private String sourceName;

    private Source.SourceType sourceType;

    private Source.Status sourceStatus;

    @Field("s__source__dating")
    private String sourceDating;

    @Field("s__source__count__attestations")
    private long sourceCountAttestations;

    @Field("is_parent")
    private boolean isParent = false;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setId(long id) {
        this.id = Sigle.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Type getTypeEnum() {
        return type;
    }

    public void setTypeEnum(Type type) {
        this.type = type;
    }

    public String getType() {
        return type == null ? null : type.getValue();
    }

    @Field("s__type")
    public void setType(String type) {
        this.type = Type.byValue(type);
    }

    public Source.SourceType getSourceTypeEnum() {
        return sourceType;
    }

    public void setSourceTypeEnum(Source.SourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Field("s__source__type")
    public void setSourceType(String sourceType) {
        this.sourceType = Source.SourceType.byValue(sourceType);
    }

    public String getSourceType() {
        return sourceType.getValue();
    }

    public Source.Status getSourceStatusEnum() {
        return sourceStatus;
    }

    public void setSourceStatusEnum(Source.Status sourceStatus) {
        this.sourceStatus = sourceStatus;
    }

    @Field("s__source__status")
    public void setSourceStatus(String sourceStatus) {
        this.sourceStatus = Source.Status.byValue(sourceStatus);
    }

    public String getSourceStatus() {
        return sourceStatus.getValue();
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceDating() {
        return sourceDating;
    }

    public void setSourceDating(String sourceDating) {
        this.sourceDating = sourceDating;
    }

    public long getSourceCountAttestations() {
        return sourceCountAttestations;
    }

    public void setSourceCountAttestations(long sourceCountAttestations) {
        this.sourceCountAttestations = sourceCountAttestations;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getHtmlDisplay() {
        StringBuffer sb = new StringBuffer();
        sb.append("<span class=\"demel-search-filter-symbol\">" + getSourceTypeEnum().getSymbol() + "</span>");
        sb.append("&nbsp;");
        sb.append(StringEscapeUtils.escapeHtml4(getName()));
        sb.append(" <small class=\"text-muted\">");
        sb.append("<br />");
        if (getSourceName() != null && !getName().equals(getSourceName())) {
            sb.append(" {=" + getSourceName() + "}");
        }
        if (getSourceDating() != null) {
            sb.append(" " + Source.createHTMLDatingDisplay(getSourceDating()));
        }
        sb.append("</small>");
        return sb.toString();
    }
}
