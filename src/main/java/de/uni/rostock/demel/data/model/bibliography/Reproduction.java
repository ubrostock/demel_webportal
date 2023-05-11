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

import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.data.model.AbstractModelObject;

public class Reproduction extends AbstractModelObject {

    public static final String DOCTYPE = "reproduction";

    public static final String DOCID_PREFIX = "r";

    public static final Pattern DOCID_PATTERN = Pattern.compile("r?(\\d{1,6})");

    public static String convertId(long id) {
        return String.format("r%06d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum Access {
        FREE, RESTRICTED, UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Access byValue(String value) {
            for (Access a : Access.values()) {
                if (a.name().equalsIgnoreCase(value)) {
                    return a;
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

    @Field("r__online_url")
    private String onlineUrl;

    @Field("r__provider")
    private String provider;

    //@Field("r__access") on setter
    private Access access;

    @Field("r__edition__id")
    private String editionId;

    @Field("r__sort")
    private int sort = 0;

    @Field("r__hints_extern")
    private String hintsExtern;

    //SETTER / GETTER

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
        this.id = convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public String getAccess() {
        return access.getValue();
    }

    public Access getAccessEnum() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    @Field("r__access")
    public void setAccess(String access) {
        this.access = Access.byValue(access);
    }

    public String getOnlineUrl() {
        return onlineUrl;
    }

    public void setOnlineUrl(String onlineUrl) {
        this.onlineUrl = onlineUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEditionId() {
        return editionId;
    }

    public void setEditionId(String editionId) {
        this.editionId = editionId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getHintsExtern() {
        return hintsExtern;
    }

    public void setHintsExtern(String hintsExtern) {
        this.hintsExtern = hintsExtern;
    }
}
