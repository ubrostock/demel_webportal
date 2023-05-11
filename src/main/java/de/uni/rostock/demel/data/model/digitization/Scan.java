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

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class Scan extends AbstractModelObject {

    public static final String DOCTYPE = "scan";

    public static final String DOCID_PREFIX = "c";

    public static final Pattern DOCID_PATTERN = Pattern.compile("c?(\\d{1,8})");

    public static String convertId(long id) {
        return String.format("c%08d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum Type {
        LEMMASECTION_TITLE,
        ATTESTATION,
        SEPARATOR,
        OVERSIZE_PLACEHOLDER,
        OTHER,
        UNDOCUMENTED;

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

    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("c__filename")
    private String filename;

    @Field("c__contentid")
    private String contentid;

    @Field("c__box__id")
    private String boxId;

    @Field("c__sort")
    private int sort;

    @Field("c__rotation")
    private int rotation;

    //@Field("c__type") on setter
    private Type type;

    @Field("c__attestation__ids")
    private List<String> attestationIds;

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
        this.id = Scan.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentid() {
        return contentid;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Type getTypeEnum() {
        return type;
    }

    public void setTypeEnum(Type type) {
        this.type = type;
    }

    public String getType() {
        return type.getValue();
    }

    @Field("c__type")
    public void setType(String type) {
        this.type = Type.byValue(type);
    }

    public List<String> getAttestationIds() {
        return attestationIds;
    }

    public void setAttestationIDs(List<String> attestationIds) {
        this.attestationIds = attestationIds;
    }

}
