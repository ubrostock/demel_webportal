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

import java.util.regex.Pattern;

import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.portal.util.searchbox.SearchboxItem;

public class Person extends AbstractModelObject implements SearchboxItem {
    public static final String DOCTYPE = "person";

    public static final String DOCID_PREFIX = "p";

    public static final Pattern DOCID_PATTERN = Pattern.compile("p?(\\d{1,6})");

    public static String convertId(long id) {
        return String.format("p%06d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    /**
     * the id - "p"+123456 (6stellig)
     */
    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("is_parent")
    private boolean isParent = false;

    @Field("sorting")
    private String sorting;

    @Field("p__name_display")
    private String nameDisplay;

    @Field("p__search")
    private String personSearch;

    @Field("p__viaf_id")
    private String viafId;

    @Field("p__gnd_id")
    private String gndId;

    @Field("p__bne_id")
    private String bneId;

    @Field("p__wikidata_id")
    private String wikidatadId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = Person.convertId(id);
    }

    public String getDoctype() {
        return doctype;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    public String getNameDisplay() {
        return nameDisplay;
    }

    public void setNameDisplay(String nameDisplay) {
        this.nameDisplay = nameDisplay;
    }

    public String getViafId() {
        return viafId;
    }

    public void setViafId(String viafId) {
        this.viafId = viafId;
    }

    public String getGndId() {
        return gndId;
    }

    public void setGndId(String gndId) {
        this.gndId = gndId;
    }

    public String getBneId() {
        return bneId;
    }

    public void setBneId(String bneId) {
        this.bneId = bneId;
    }

    public String getWikidataId() {
        return wikidatadId;
    }

    public void setWikidataId(String wikidatadId) {
        this.wikidatadId = wikidatadId;
    }

    public String getPersonSearch() {
        return personSearch;
    }

    public void setPersonSearch(String personSearch) {
        this.personSearch = personSearch;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    @Override
    public String getHtmlDisplay() {
        return getNameDisplay();
    }

}
