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
import java.util.EnumSet;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import de.uni.rostock.demel.data.model.TristateBoolean;
import de.uni.rostock.demel.data.model.dictionary.Lemma.PartOfSpeech;
import de.uni.rostock.demel.data.model.digitization.Scan;

public class AttestationRowMapper implements RowMapper<Attestation> {

    public static final String SQL_SELECT_ALL_ATTESTATIONS = """
            SELECT a.*, l.*, s.*,  ll.id, ll.name, messages.*,
              (SELECT bs.name
               FROM bibl_sigle bs WHERE bs.source_id=a.source_id AND bs.type='main' LIMIT 1) AS sigle_main,
              (SELECT GROUP_CONCAT(bs.name SEPARATOR '|')
               FROM bibl_sigle bs WHERE bs.source_id=a.source_id
               GROUP BY bs.source_id) AS sigle_search,
              (SELECT GROUP_CONCAT(m.scan_id ORDER BY m.scan_sort SEPARATOR '|')
               FROM map_attestation_scan m WHERE m.attestation_id=a.id) AS scan_ids,
              (SELECT GROUP_CONCAT(s.contentid SEPARATOR '|')
               FROM map_attestation_scan m JOIN digi_scan s ON m.scan_id = s.id WHERE m.attestation_id=a.id
               ORDER BY m.scan_sort ) AS scan_contentids,
              (SELECT GROUP_CONCAT(s.rotation SEPARATOR '|')
               FROM map_attestation_scan m JOIN digi_scan s ON m.scan_id = s.id WHERE m.attestation_id=a.id
               ORDER BY m.scan_sort ) AS scan_rotations
             FROM dict_attestation a
             LEFT JOIN dict_lemma l ON a.lemma_id = l.id
             LEFT JOIN dict_source s ON a.source_id = s.id
             LEFT JOIN dict_lemma ll ON a.lemmalink_id = ll.id
             LEFT JOIN (SELECT object_id,
                                   SUM(1) AS anz_all,
                                   SUM(status='published') AS anz_published,
                                   SUM(status='inreview') AS anz_inreview
                        FROM util_message WHERE object_type='attestation' GROUP BY object_id) AS messages
                        ON a.id = messages.object_id
        """;

    public static final String SQL_SELECT_SINGLE_ATTESTATION = SQL_SELECT_ALL_ATTESTATIONS + " WHERE a.id = ?";

    @Override
    public Attestation mapRow(ResultSet rs, int rowNum) throws SQLException {
        Attestation a = new Attestation();
        a.setId(rs.getLong("a.id"));
        a.setType(rs.getString("a.type"));
        a.setForm(rs.getString("a.form"));

        a.setLemmaId(Lemma.convertId(rs.getLong("a.lemma_id")));
        a.setLemmaName(rs.getString("l.name"));

        String pos = rs.getString("l.part_of_speech");
        if (pos != null) {
            a.setLemmaPartOfSpeechsEnum(EnumSet.copyOf(
                Arrays.asList(pos.split(",")).stream().map(p -> PartOfSpeech.byValue(p)).toList()));
        }
        LemmaRowMapper.normalizePartOfSpeech(a.getLemmaPartOfSpeechEnums());

        String lemmaNameVariants = rs.getString("l.name_variants");
        if (lemmaNameVariants != null) {
            a.setLemmaSearch(Arrays.asList(lemmaNameVariants.split("\\|")));
        }

        a.setSourceId(Source.convertId(rs.getLong("a.source_id")));
        a.setSourceName(rs.getString("sigle_main"));
        a.setSourceType(rs.getString("s.type"));
        a.setSigleSearch(Arrays.asList(rs.getString("sigle_search").split("\\|")));
        a.setSourceDatingDisplay(rs.getString("s.dating_display"));
        a.setSourceDatingFrom(rs.getInt("s.dating_from"));
        a.setSourceDatingTo(rs.getInt("s.dating_to"));
        long lemmalinkId = rs.getLong("ll.id");
        if (!rs.wasNull()) {
            a.setLemmalinkId(Lemma.convertId(lemmalinkId));
            a.setLemmalinkName(rs.getString("ll.name"));
        }

        String mwe = rs.getString("a.multiwordexpr");
        a.setMultiwordexpr(mwe);
        //We could not use numbers as prefix, 
        //because they are ignored by our collation algorithmn in Solr
        if (rs.wasNull()) {
            a.setIsMultiwordexprEnum(TristateBoolean.NULL);
            a.setMultiwordexprSorting("zz_null");
        } else if (mwe.isEmpty()) {
            a.setIsMultiwordexprEnum(TristateBoolean.FALSE);
            a.setMultiwordexprSorting("yy_empty");
        } else {
            a.setIsMultiwordexprEnum(TristateBoolean.TRUE);
            a.setMultiwordexprSorting("aa_" + mwe.replace("<seg>", "").replace("</seg>", ""));
        }

        a.setDatingDisplay(rs.getString("a.dating_display"));
        a.setDatingFrom(rs.getInt("a.dating_from"));
        a.setDatingTo(rs.getInt("a.dating_to"));
        a.setDatingOrigin(rs.getString("a.dating_origin"));

        a.setDatingDisplaySearch(
            switch (a.getDatingOriginEnum()) {
                case SCAN -> a.getDatingDisplay();
                case PRIMARY_SOURCE, SECONDARY_SOURCE -> a.getSourceDatingDisplay();
                default -> "";
            });

        a.setDatingFromSearch(
            switch (a.getDatingOriginEnum()) {
                case SCAN -> a.getDatingFrom();
                case PRIMARY_SOURCE, SECONDARY_SOURCE -> a.getSourceDatingFrom();
                default -> 9999;
            });

        a.setDatingToSearch(
            switch (a.getDatingOriginEnum()) {
                case SCAN -> a.getDatingTo();
                case PRIMARY_SOURCE, SECONDARY_SOURCE -> a.getSourceDatingTo();
                default -> 9999;
            });

        String scans = rs.getString("scan_ids");
        if (scans != null) {
            List<String> l = Arrays.stream(scans.split("\\|"))
                .map(c -> Scan.convertId(Long.parseLong(c))).toList();
            a.setCountScans(l.size());
            a.setScanIDs(l);
        } else {
            a.setCountScans(0);
        }
        String scanContentIDs = rs.getString("scan_contentids");
        if (scanContentIDs != null) {
            List<String> l = Arrays.asList(scanContentIDs.split("\\|"));
            a.setScanContentIDs(l);
        }

        String scanRotations = rs.getString("scan_rotations");
        if (scanRotations != null) {
            List<Integer> l = Arrays.asList(scanRotations.split("\\|")).stream().map(Integer::parseInt).toList();
            a.setScanRotations(l);
        }

        a.setStatus(rs.getString("a.status"));
        a.setHintsIntern(rs.getString("a.hints_intern"));

        a.setSorting(Attestation.createSorting(a));

        a.setCountMessages(rs.getLong("messages.anz_all"));
        a.setCountMessagesPublished(rs.getLong("messages.anz_published"));
        a.setCountMessagesInreview(rs.getLong("messages.anz_inreview"));

        return a;
    }

}
