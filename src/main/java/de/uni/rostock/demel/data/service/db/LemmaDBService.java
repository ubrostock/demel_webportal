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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.LemmaRowMapper;
import de.uni.rostock.demel.portal.edit.model.LemmaEditObject;

@Service
public class LemmaDBService extends AbstractDBService<Lemma, LemmaEditObject> {
    private static Logger LOGGER = LogManager.getLogger(LemmaDBService.class);

    private static final String SQL_UPDATE_LEMMA = """
            UPDATE dict_lemma SET name=?, name_variants=?, part_of_speech=?,
                   hints_extern=?, hints_intern=?
            WHERE id = ?;
        """;

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LemmaRowMapper rowMapper;

    @Override
    public Lemma queryObject(String idParam) {
        int id = Integer.parseInt(idParam.replace(Lemma.DOCID_PREFIX, ""));
        Lemma l = jdbcTemplate.queryForObject(LemmaRowMapper.SQL_SELECT_SINGLE_LEMMA,
            rowMapper, id);
        return l;
    }

    @Override
    public boolean updateObject(LemmaEditObject lemmaEdit, String currentUser) {
        try {
            Lemma lemmaOrig = queryObject(lemmaEdit.getId());
            List<ChangeRecord> changes = retrieveChanges(lemmaEdit, lemmaOrig);
            jdbcTemplate.update(SQL_UPDATE_LEMMA,
                new SqlParameterValue(Types.VARCHAR, lemmaEdit.getName()),
                new SqlParameterValue(Types.VARCHAR, lemmaEdit.getNameVariants()),
                new SqlParameterValue(Types.VARCHAR,
                    lemmaOrig.getTypeEnum() == Lemma.LemmaType.LINKLEMMA ? null
                        : String.join(",", lemmaEdit.getPartOfSpeech())),
                new SqlParameterValue(Types.VARCHAR,
                    StringUtils.isEmpty(lemmaEdit.getHintsExtern()) ? null : lemmaEdit.getHintsExtern()),
                new SqlParameterValue(Types.VARCHAR,
                    StringUtils.isEmpty(lemmaEdit.getHintsIntern()) ? null : lemmaEdit.getHintsIntern()),

                Integer.parseInt(lemmaEdit.getId().substring(1)));

            saveChanges(lemmaEdit, currentUser, changes, jdbcTemplate);
        } catch (DataAccessException e) {
            LOGGER.error("Could not save change record in database", e);
            return false;
        }
        return true;
    }

    private List<ChangeRecord> retrieveChanges(LemmaEditObject lemmaEdit, Lemma lemmaOrig) {
        ArrayList<ChangeRecord> result = new ArrayList<>();
        //check values against field ENUM in util_history table;
        result.add(ChangeRecord.calcDelta(Field.LEMMA__NAME, lemmaOrig.getName(), lemmaEdit.getName()));
        result.add(ChangeRecord.calcDelta(Field.LEMMA__NAME_VARIANTS,
            (lemmaOrig.getNameVariants() == null ? "" : String.join("|", lemmaOrig.getNameVariants())),
            StringUtils.defaultString(lemmaEdit.getNameVariants())));
        result.add(ChangeRecord.calcDelta(Field.LEMMA__PART_OF_SPEECH,
            (lemmaOrig.getPartOfSpeechs() == null ? "" : String.join(",", lemmaOrig.getPartOfSpeechs())),
            (lemmaEdit.getPartOfSpeech() == null ? "" : String.join(",", lemmaEdit.getPartOfSpeech()))));
        result.add(
            ChangeRecord.calcDelta(Field.LEMMA__HINTS_EXTERN, lemmaOrig.getHintsExtern(), lemmaEdit.getHintsExtern()));
        result.add(
            ChangeRecord.calcDelta(Field.LEMMA__HINTS_INTERN, lemmaOrig.getHintsIntern(), lemmaEdit.getHintsIntern()));

        //remove ALL null values
        do {
            /* nothing */} while (result.remove(null));
        return result;
    }
}
