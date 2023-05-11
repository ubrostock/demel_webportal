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

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.AttestationRowMapper;
import de.uni.rostock.demel.portal.edit.model.AttestationEditObject;

@Service
public class AttestationDBService extends AbstractDBService<Attestation, AttestationEditObject> {
    private static Logger LOGGER = LogManager.getLogger(AttestationDBService.class);
    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_UPDATE_ATTESTATION = """
            UPDATE dict_attestation SET form=?, lemma_id=?, multiwordexpr=?,
                   dating_display=?, dating_from=?, dating_to=?,
                   source_id=?, lemmalink_id=?, hints_intern=?
            WHERE id = ?;
        """;

    @Override
    public Attestation queryObject(String idParam) {
        int id = Integer.parseInt(idParam.replace(Attestation.DOCID_PREFIX, ""));
        Attestation att = jdbcTemplate.queryForObject(AttestationRowMapper.SQL_SELECT_SINGLE_ATTESTATION,
            new AttestationRowMapper(), id);
        return att;
    }

    @Override
    public boolean updateObject(AttestationEditObject attEdit, String currentUser) {
        try {
            List<ChangeRecord> changes = retrieveChanges(attEdit);
            jdbcTemplate.update(SQL_UPDATE_ATTESTATION,
                new SqlParameterValue(Types.VARCHAR, attEdit.getForm()),
                new SqlParameterValue(Types.INTEGER, calculateIntegerId(attEdit.getLemmaId())),
                new SqlParameterValue(Types.VARCHAR, attEdit.getMultiwordexpr()),
                new SqlParameterValue(Types.VARCHAR, attEdit.getDatingDisplay()),
                new SqlParameterValue(Types.INTEGER, attEdit.getDatingFrom()),
                new SqlParameterValue(Types.INTEGER, attEdit.getDatingTo()),
                new SqlParameterValue(Types.INTEGER, calculateIntegerId(attEdit.getSourceId())),
                new SqlParameterValue(Types.INTEGER,
                    attEdit.getLemmalinkId() == null ? null
                        : calculateIntegerId(attEdit.getLemmalinkId().substring(1))),
                new SqlParameterValue(Types.VARCHAR, attEdit.getHintsIntern()),

                Integer.parseInt(attEdit.getId().substring(1)));

            saveChanges(attEdit, currentUser, changes, jdbcTemplate);
        } catch (DataAccessException e) {
            LOGGER.error("Could not save change record in database", e);
            return false;
        }
        return true;
    }

    private List<ChangeRecord> retrieveChanges(AttestationEditObject attEdit) {
        ArrayList<ChangeRecord> result = new ArrayList<>();
        Attestation attOrig = queryObject(attEdit.getId());
        //check values against field ENUM in util_history table;
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__FORM, attOrig.getForm(), attEdit.getForm()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__LEMMA_ID, attOrig.getLemmaId(), attEdit.getLemmaId()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__MULTIWORDEXPR, attOrig.getMultiwordexpr(),
            attEdit.getMultiwordexpr()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__DATING_DISPLAY, attOrig.getDatingDisplay(),
            attEdit.getDatingDisplay()));
        result.add(
            ChangeRecord.calcDelta(Field.ATTESTATION__DATING_FROM, attOrig.getDatingFrom(), attEdit.getDatingFrom()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__DATING_TO, attOrig.getDatingTo(), attEdit.getDatingTo()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__SOURCE_ID, attOrig.getSourceId(), attEdit.getSourceId()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__LEMMALINK_ID, attOrig.getLemmalinkId(),
            attEdit.getLemmalinkId()));
        result.add(ChangeRecord.calcDelta(Field.ATTESTATION__HINTS_INTERN, attOrig.getHintsIntern(),
            attEdit.getHintsIntern()));

        //remove ALL null values
        do {
            /* nothing */} while (result.remove(null));
        return result;
    }

}
