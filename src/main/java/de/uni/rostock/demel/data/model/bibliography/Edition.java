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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public class Edition extends AbstractModelObject {
    public static final String DOCTYPE = "edition";

    public static final String DOCID_PREFIX = "e";

    public static final Pattern DOCID_PATTERN = Pattern.compile("e?(\\d{1,6})");

    public static String convertId(long id) {
        return String.format("e%06d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    /**
     * the id - "e"+123456 (6stellig)
     */
    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("is_parent")
    private boolean isParent = false;

    @Field("e__title_info")
    private String titleInfo;

    @Field("e__source__id")
    private String sourceId;

    //Sort position - may be obsolete, when sorting works properly
    @JsonIgnore
    @Field("e__sort")
    private int sort;

    //dating_display: enthalten in title_info

    @Field("e__dating_from")
    private int datingFrom;

    @Field("e__dating_to")
    private int datingTo;

    @Field("e__bne_id")
    private String bneId;

    @Field("e__source_host__id")
    private String sourceHostId;

    @Field("e__source_host__sigle")
    private String sourceHostSigle;

    @Field("e__source_host__title")
    private String sourceHostTitle;

    //as childdoc: e__child__reproductions
    private List<Reproduction> reproductions = new ArrayList<>();

    // SETTER and GETTER

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
        this.id = Edition.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public String getTitleInfo() {
        return titleInfo;
    }

    public void setTitleInfo(String titleInfo) {
        this.titleInfo = titleInfo;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getDatingFrom() {
        return datingFrom;
    }

    public void setDatingFrom(int datingFrom) {
        this.datingFrom = datingFrom;
    }

    public int getDatingTo() {
        return datingTo;
    }

    public void setDatingTo(int datingTo) {
        this.datingTo = datingTo;
    }

    public String getBneId() {
        return bneId;
    }

    public void setBneId(String bneId) {
        this.bneId = bneId;
    }

    public String getSourceHostId() {
        return sourceHostId;
    }

    public void setSourceHostId(String sourceHostId) {
        this.sourceHostId = sourceHostId;
    }

    public String getSourceHostSigle() {
        return sourceHostSigle;
    }

    public void setSourceHostSigle(String sourceHostSigle) {
        this.sourceHostSigle = sourceHostSigle;
    }

    public String getSourceHostTitle() {
        return sourceHostTitle;
    }

    public void setSourceHostTitle(String sourceHostTitle) {
        this.sourceHostTitle = sourceHostTitle;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<Reproduction> getReproductions() {
        return reproductions;
    }

    public void setReproductions(List<Reproduction> reproductions) {
        this.reproductions = reproductions;
    }

}
