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
package de.uni.rostock.demel.data.model.dictionary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.TristateBoolean;
import de.uni.rostock.demel.data.model.bibliography.Edition;
import de.uni.rostock.demel.data.model.bibliography.EditionRowMapper;
import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.bibliography.PersonRowMapper;
import de.uni.rostock.demel.data.model.bibliography.Reproduction;
import de.uni.rostock.demel.data.model.bibliography.ReproductionRowMapper;
import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.data.model.bibliography.SigleRowMapper;

@Service
public class SourceRowMapper implements RowMapper<Source> {

    public static final String SQL_SELECT_ALL = """
        SELECT * FROM dict_source s
          LEFT JOIN (SELECT source_id
                          , COUNT(CASE WHEN `type` =  'primary' THEN `type` END) anz_primary
                          , COUNT(CASE WHEN `type` =  'secondary' THEN `type` END) anz_secondary
                          , COUNT(CASE WHEN `type` =  'lemmalink' THEN `type` END) anz_lemmalink
                        FROM dict_attestation WHERE status='published'
                        GROUP BY source_id) anz
                     ON s.id = anz.source_id
          LEFT JOIN (SELECT object_id,
                                   SUM(1) AS anz_all,
                                   SUM(status='published') AS anz_published,
                                   SUM(status='inreview') AS anz_inreview
                        FROM util_message WHERE object_type='source' GROUP BY object_id) AS messages
                     ON s.id = messages.object_id
         """;

    public static final String SQL_SELECT_SINGLE_SOURCE = SQL_SELECT_ALL + " WHERE s.id=?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EditionRowMapper editionRowMapper;

    @Autowired
    SigleRowMapper sigleRowMapper;

    @Autowired
    PersonRowMapper personRowMapper;

    @Autowired
    ReproductionRowMapper reproductionRowMapper;

    @Override
    public Source mapRow(ResultSet rs, int rowNum) throws SQLException {
        Source s = new Source();
        long sourceId = rs.getLong("s.id");
        s.setId(sourceId);
        s.setType(rs.getString("s.type"));
        s.setDatingDisplay(rs.getString("s.dating_display"));
        s.setDatingFrom(rs.getInt("s.dating_from"));
        s.setDatingTo(rs.getInt("s.dating_to"));

        boolean datingUnique = rs.getBoolean("s.dating_unique");
        if (rs.wasNull()) {
            s.setDatingUniqueEnum(TristateBoolean.NULL);
        } else if (datingUnique) {
            s.setDatingUniqueEnum(TristateBoolean.TRUE);
        } else {
            s.setDatingUniqueEnum(TristateBoolean.FALSE);
        }

        String texttypes = rs.getString("s.texttypes");
        if (texttypes != null) {
            s.setTexttypes(Arrays.asList(texttypes.split(",")));
        }
        s.setGenre(rs.getString("s.genre"));
        s.setSubgenre(rs.getString("s.subgenre"));
        String langs = rs.getString("s.languages");
        if (langs != null) {
            s.setLanguages(Arrays.asList(langs.split(",")));
        }
        s.setHsmsIds(rs.getString("s.hsms_ids"));
        s.setBetaIds(rs.getString("s.beta_ids"));
        s.setStatus(rs.getString("s.status"));
        s.setHintsIntern(rs.getString("s.hints_intern"));
        s.setCountAttestationsPrimary(rs.getLong("anz_primary"));
        s.setCountAttestationsSecondary(rs.getLong("anz_secondary"));
        s.setCountAttestationsLemmalink(rs.getLong("anz_lemmalink"));
        s.setCountAttestations(
            s.getCountAttestationsPrimary() + s.getCountAttestationsSecondary() + s.getCountAttestationsLemmalink());

        s.setCountMessages(rs.getLong("messages.anz_all"));
        s.setCountMessagesPublished(rs.getLong("messages.anz_published"));
        s.setCountMessagesInreview(rs.getLong("messages.anz_inreview"));

        s.setEditions(jdbcTemplate.query(EditionRowMapper.SELECT_BY_SOURCE_ID, editionRowMapper, sourceId));
        for (Edition e : s.getEditions()) {
            String eId = e.getId();
            e.setId(s.getId() + "-" + eId);
            s.getEditionSearch().add(e.getTitleInfo());

            e.setReproductions(jdbcTemplate.query(ReproductionRowMapper.SELECT_BY_EDITION_ID, reproductionRowMapper,
                Edition.convertId(eId)));
            for (Reproduction r : e.getReproductions()) {
                r.setId(e.getId() + "-" + r.getId());
            }
        }

        s.setSigles(jdbcTemplate.query(SigleRowMapper.SELECT_BY_SOURCE_ID, sigleRowMapper, sourceId));
        for (Sigle si : s.getSigles()) {
            si.setId(s.getId() + "-" + si.getId());
            si.setSourceId(s.getId());
            si.setSourceName(s.getName());
            si.setSourceDating(s.getDatingDisplay());
            si.setSourceTypeEnum(s.getTypeEnum());
            si.setSourceStatusEnum(s.getStatusEnum());
            si.setSourceCountAttestations(s.getCountAttestations());
            s.getSigleSearch().add(si.getSorting());

            if (si.getTypeEnum() == Sigle.Type.MAIN) {
                s.setName(si.getName());
            }
        }

        s.setPersons(jdbcTemplate.query(PersonRowMapper.SELECT_BY_SOURCE_ID, personRowMapper, sourceId));
        for (Person p : s.getPersons()) {
            s.getPersonId().add(p.getId());
            s.getPersonSearch().add(p.getNameDisplay());

            p.setId(s.getId() + "-" + p.getId());
        }

        s.setSorting(Source.createSorting(s));
        return s;
    }
}
