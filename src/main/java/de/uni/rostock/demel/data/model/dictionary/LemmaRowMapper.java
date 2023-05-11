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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Lemma.LemmaType;
import de.uni.rostock.demel.data.model.dictionary.Lemma.PartOfSpeech;

@Service
public class LemmaRowMapper implements RowMapper<Lemma> {

    public static final String SQL_SELECT_ALL
        = """
                 SELECT l.*, anz.*, link.*, link3.*, messages.*
                 FROM dict_lemma l
                 LEFT JOIN (SELECT wf.lemma_id,
                                   SUM(1) anz_all,
                                   SUM(`type` =  'primary') anz_primary,
                                   SUM(`type` =  'secondary') anz_secondary,
                                   SUM(`type` =  'lemmalink') anz_lemmalink

                            FROM dict_attestation wf WHERE wf.status='published' GROUP BY wf.lemma_id) AS anz
                      ON l.id = anz.lemma_id

                 LEFT JOIN (SELECT ll.lemma_id ,
                                   GROUP_CONCAT(IFNULL((SELECT l2.name FROM dict_lemma l2 WHERE l2.id = ll.target_lemma_id), '#') SEPARATOR '|') AS link_target_name,
                                   GROUP_CONCAT(IFNULL(target_lemma_id,   '#') SEPARATOR '|') AS link_target_id
                            FROM map_lemma_lemma ll GROUP BY ll.lemma_id) AS link
                      ON l.id = link.lemma_id

                 LEFT JOIN (SELECT ll3.target_lemma_id ,
                                   GROUP_CONCAT(IFNULL((SELECT l3.name FROM dict_lemma l3 WHERE l3.id = ll3.lemma_id), '#') SEPARATOR '|') AS link_source_name,
                                   GROUP_CONCAT(IFNULL(ll3.lemma_id,   '#') SEPARATOR '|') AS link_source_id
                            FROM map_lemma_lemma ll3 GROUP BY ll3.target_lemma_id) AS link3
                      ON l.id = link3.target_lemma_id

                 LEFT JOIN (SELECT object_id,
                                   SUM(1) AS anz_all,
                                   SUM(status='published') AS anz_published,
                                   SUM(status='inreview') AS anz_inreview
                            FROM util_message WHERE object_type='lemma' GROUP BY object_id) AS messages
                      ON l.id = messages.object_id
            """;

    public static final String SQL_SELECT_SINGLE_LEMMA = SQL_SELECT_ALL + " WHERE id = ?";

    public static List<PartOfSpeech> POS_SORTED = Arrays.asList(PartOfSpeech.values());

    @Override
    public Lemma mapRow(ResultSet rs, int rowNum) throws SQLException {
        Lemma l = new Lemma();
        long lemmaId = rs.getLong("l.id");
        l.setId(lemmaId);

        l.setLemmaSearch(new ArrayList<>());

        l.setName(rs.getString("l.name"));
        l.setPrefix(calcPrefix(rs.getString("l.name")));
        l.getLemmaSearch().add(l.getName());

        l.setTypeEnum(LemmaType.byValue(rs.getString("l.type")));

        if (!StringUtils.isEmpty(rs.getString("name_variants"))) {
            String[] nameVariants = rs.getString("name_variants").split("\\|");
            l.setNameVariants(new ArrayList<>(Arrays.asList(nameVariants)));
            l.getNameVariants().remove(l.getName());
            l.getLemmaSearch().addAll(l.getNameVariants());
        } else {
            l.setNameVariants(new ArrayList<>());
        }

        if (!StringUtils.isEmpty(rs.getString("part_of_speech"))) {
            String pos = rs.getString("part_of_speech");
            l.setPartOfSpeechs(Arrays.asList(pos.split(",")));
        }
        if (!StringUtils.isEmpty(rs.getString("hints_extern"))) {
            l.setHintsExtern(rs.getString("hints_extern"));
        }
        l.setStatus(rs.getString("l.status"));
        if (!StringUtils.isEmpty(rs.getString("hints_intern"))) {
            l.setHintsIntern(rs.getString("hints_intern"));
        }

        if (!StringUtils.isEmpty(rs.getString("link_source_name"))) {
            l.setLemmalinkSourceNames(Arrays.asList(rs.getString("link_source_name").split("\\|")));
        }
        if (!StringUtils.isEmpty(rs.getString("link_source_id"))) {
            l.setLemmalinkSourceIds(Arrays.stream(rs.getString("link_source_id").split("\\|"))
                .map(x -> x.equals("#") ? "#" : Lemma.convertId(Long.parseLong(x)))
                .collect(Collectors.toList()));
        }
        if (!StringUtils.isEmpty(rs.getString("link_target_name"))) {
            l.setLemmalinkTargetNames(Arrays.asList(rs.getString("link_target_name").split("\\|")));
        }
        if (!StringUtils.isEmpty(rs.getString("link_target_id"))) {
            l.setLemmalinkTargetIds(Arrays.stream(rs.getString("link_target_id").split("\\|"))
                .map(x -> x.equals("#") ? "#" : Lemma.convertId(Long.parseLong(x)))
                .collect(Collectors.toList()));
        }
        l.setSorting(Lemma.createSorting(l));

        l.setCountAttestations(rs.getLong("anz_all"));
        l.setCountAttestationsPrimary(rs.getLong("anz_primary"));
        l.setCountAttestationsSecondary(rs.getLong("anz_secondary"));
        l.setCountAttestationsLemmalink(rs.getLong("anz_lemmalink"));

        l.setCountMessages(rs.getLong("messages.anz_all"));
        l.setCountMessagesPublished(rs.getInt("messages.anz_published"));
        l.setCountMessagesInreview(rs.getInt("messages.anz_inreview"));

        //add grouping elements subst_all, verb_all, name_all if applicable
        normalizePartOfSpeech(l.getPartOfSpeechEnums());

        return l;
    }

    private String calcPrefix(String name) {
        name = name.toLowerCase();
        while (name.length() > 0) {
            if (name.startsWith("[") || name.startsWith("(") || name.startsWith("-") || name.startsWith("¡")
                || name.startsWith(" ")) {
                name = name.substring(1);
                continue;
            }
            if (name.startsWith("ch")) {
                return "ch";
            }
            if (name.startsWith("ll")) {
                return "ll";
            }
            if (name.startsWith("ñ")) {
                return "ñ";
            }
            if (name.startsWith("ç")) {
                return "ç";
            }
            return StringUtils.stripAccents(name.substring(0, 1));
        }
        return "";
    }

    /**
     * Hinzufügen der gruppierenden Elemente: subst_all, verb_all, name_all
     */
    public static void normalizePartOfSpeech(EnumSet<PartOfSpeech> pos) {
        if (pos == null) {
            return;
        }
        List<PartOfSpeech> grouping = new ArrayList<>();
        for (PartOfSpeech p : pos) {
            if (p.getGroup() != null) {
                grouping.add(p.getGroup());
            }
        }
        pos.addAll(grouping);
    }

}
