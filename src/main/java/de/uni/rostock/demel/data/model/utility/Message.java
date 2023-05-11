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
package de.uni.rostock.demel.data.model.utility;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Source;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class Message extends AbstractModelObject {

    public static final String DOCTYPE = "message";

    public static final String DOCID_PREFIX = "m";

    public static final Pattern DOCID_PATTERN = Pattern.compile("m?(\\d{1,6})");

    public static String convertId(long id) {
        return String.format("m%06d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum ObjectType {
        ATTESTATION, LEMMA, SOURCE, UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static ObjectType byValue(String value) {
            for (ObjectType t : ObjectType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum MessageType {
        OFFICIAL_COMMENT, OFFICIAL_ERROR, OFFICIAL_REVISION, USER_COMMENT, USER_ERROR, UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static MessageType byValue(String value) {
            for (MessageType t : MessageType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum MessageStatus {
        PUBLISHED("fa-solid fa-bullhorn text-info"),
        DELETED("fa-solid fa-ban text-warning"),
        INVISIBLE("fa-solid fa-lock text-warning"),
        INREVIEW("fa-solid fa-eye text-danger");

        private String symbol;

        private MessageStatus(String symbol) {
            this.symbol = symbol;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public String getSymbol() {
            return symbol;
        }

        public static MessageStatus byValue(String value) {
            for (MessageStatus s : MessageStatus.values()) {
                if (s.name().equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return null;
        }
    }

    private String id;

    private String doctype = DOCTYPE;

    private ObjectType objectType;

    private long objectId;

    private MessageType type;

    private Instant created;

    @Size(max = 100, message = "demel.message_form.error.creator_name.size")
    private String creatorName;

    //not indexed in Solr!!!
    @Email(message = "demel.message_form.error.creator_email.format")
    @Size(max = 100, message = "demel.message_form.error.creator_email.size")
    private String creatorEmail;

    @NotEmpty(message = "demel.message_form.error.content.notempty")
    @Size(max = 4000, message = "demel.message_form.error.content.size")
    private String content;

    private MessageStatus status;

    //not indexed in Solr!!!
    private String hintsIntern;

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
        this.id = Message.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public ObjectType getObjectTypeEnum() {
        return objectType;
    }

    public void setObjectTypeEnum(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getObjectType() {
        return objectType == null ? null : objectType.getValue();
    }

    public void setObjectType(String type) {
        this.objectType = ObjectType.byValue(type);
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public void setObjectIdAsString(String objectId) {
        this.objectId = Long.parseLong(objectId.substring(1));
        setObjectTypeEnum(calcObjectTypeFromObjectId(objectId));
    }

    public String getObjectIdAsString() {
        return switch (objectType) {
            case LEMMA -> Lemma.convertId(objectId);
            case ATTESTATION -> Attestation.convertId(objectId);
            case SOURCE -> Source.convertId(objectId);
            default -> String.valueOf(objectId);
        };
    }

    public MessageType getTypeEnum() {
        return type;
    }

    public void setTypeEnum(MessageType type) {
        this.type = type;
    }

    public String getType() {
        return type == null ? null : type.getValue();
    }

    public void setType(String type) {
        this.type = MessageType.byValue(type);
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(MessageStatus status) {
        this.status = status;
    }

    public String getStatus() {
        return status == null ? null : status.getValue();
    }

    public void setStatus(String status) {
        this.status = MessageStatus.byValue(status);
    }

    public String getStatusSymbol() {
        return status == null ? null : status.getSymbol();
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    public boolean isParent() {
        return isParent;
    }

    public static ObjectType calcObjectTypeFromObjectId(String objectId) {
        if (objectId.startsWith(Attestation.DOCID_PREFIX)) {
            return ObjectType.byValue(Attestation.DOCTYPE);
        }
        if (objectId.startsWith(Lemma.DOCID_PREFIX)) {
            return ObjectType.byValue(Lemma.DOCTYPE);
        }
        if (objectId.startsWith(Source.DOCID_PREFIX)) {
            return ObjectType.byValue(Source.DOCTYPE);
        }
        return ObjectType.UNDOCUMENTED;
    }
}
