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
package de.uni.rostock.demel.data.model.digitization;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.HasScansCount;

public class Box extends AbstractModelObject implements HasScansCount {

    public static final String DOCTYPE = "box";

    public static final String DOCID_PREFIX = "k";

    public static final Pattern DOCID_PATTERN = Pattern.compile("k?(\\d{1,4})");

    public static String convertId(long id) {
        return String.format("k%04d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum Status {
        INCLUDED,
        EXCLUDED,
        INTEGRATEABLE,
        UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Status byValue(String value) {
            for (Status s : Status.values()) {
                if (s.name().equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return null;
        }
    }

    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("k__name")
    private String name;

    @Field("k__name_es")
    private String name_es;

    @Field("k__section")
    private String section;

    @Field("k__section_sort")
    private int sectionSort;

    //field definition on setter
    private Status status;

    @Field("count__scans")
    private long countScans;

    @Field("is_parent")
    private boolean isParent = true;

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
        this.id = Box.convertId(id);
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

    public String getName_es() {
        return name_es;
    }

    public void setName_es(String name_es) {
        this.name_es = name_es;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getSectionSort() {
        return sectionSort;
    }

    public void setSectionSort(int sectionSort) {
        this.sectionSort = sectionSort;
    }

    public Status getStatusEnum() {
        return status;
    }

    public void setStatusEnum(Status status) {
        this.status = status;
    }

    public String getStatus() {
        return status.getValue();
    }

    @Field("k__status")
    public void setStatus(String status) {
        this.status = Status.byValue(status);
    }

    @Override
    public long getCountScans() {
        return countScans;
    }

    @Override
    public void setCountScans(long countScans) {
        this.countScans = countScans;
    }

}
