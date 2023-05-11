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
package de.uni.rostock.demel.data.service.db;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.portal.edit.model.AbstractEditObject;

public abstract class AbstractDBService<O extends AbstractModelObject, E extends AbstractEditObject> {

    public record ChangeRecord(Field field, String oldValue, String newValue) {
        public static ChangeRecord calcDelta(Field field, Object oldValue, Object newValue) {
            if ((oldValue == null && newValue == null)
                || (oldValue != null && newValue != null && oldValue.equals(newValue))) {
                return null;
            }
            return new ChangeRecord(field,
                oldValue == null ? null : StringUtils.abbreviate(oldValue.toString(), 400),
                newValue == null ? null : StringUtils.abbreviate(newValue.toString(), 400));
        }
    };

    public enum Field {
        ATTESTATION__FORM, ATTESTATION__LEMMA_ID, ATTESTATION__MULTIWORDEXPR, ATTESTATION__DATING_DISPLAY,
        ATTESTATION__DATING_FROM, ATTESTATION__DATING_TO, ATTESTATION__SOURCE_ID,
        ATTESTATION__LEMMALINK_ID, ATTESTATION__HINTS_INTERN, LEMMA__NAME, LEMMA__NAME_VARIANTS, LEMMA__PART_OF_SPEECH,
        LEMMA__HINTS_EXTERN, LEMMA__HINTS_INTERN;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Field byValue(String value) {
            for (Field f : Field.values()) {
                if (f.name().equalsIgnoreCase(value)) {
                    return f;
                }
            }
            return null;
        }
    }

    private static final String SQL_INSERT_HISTORY = "INSERT INTO util_history VALUES (?,?,?,?,?,  ?,?,?)";

    protected void saveChanges(E edit, String currentUser, List<ChangeRecord> changes, JdbcTemplate jdbcTemplate) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (ChangeRecord change : changes) {
            if (change != null) {
                jdbcTemplate.update(SQL_INSERT_HISTORY,
                    new SqlParameterValue(Types.INTEGER, null),
                    new SqlParameterValue(Types.VARCHAR, edit.getObjectType()),
                    new SqlParameterValue(Types.INTEGER, calculateIntegerId(edit.getId())),
                    new SqlParameterValue(Types.VARCHAR, currentUser),
                    new SqlParameterValue(Types.TIMESTAMP, now),
                    new SqlParameterValue(Types.VARCHAR, change.field().getValue()),
                    new SqlParameterValue(Types.VARCHAR, change.oldValue()),
                    new SqlParameterValue(Types.VARCHAR, change.newValue()));
            }
        }
    }

    protected abstract O queryObject(String idParam);

    protected abstract boolean updateObject(E edit, String currentUser);

    protected Integer calculateIntegerId(String idString) {
        return idString == null ? null : Integer.parseInt(idString.substring(1));
    }
}
