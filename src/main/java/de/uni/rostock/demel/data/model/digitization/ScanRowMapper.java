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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Attestation;

@Service
public class ScanRowMapper implements RowMapper<Scan> {
    
    public static final String SQL_SELECT_ALL = """
        SELECT *,
           (SELECT GROUP_CONCAT(m.attestation_id SEPARATOR '|')
            FROM map_attestation_scan m
            WHERE m.scan_id = s.id) AS attestatation_ids
        FROM digi_scan  s
        """;

    public static final String SQL_SELECT_SINGLE_SCAN = SQL_SELECT_ALL + " WHERE s.id=?";

    @Override
    public Scan mapRow(ResultSet rs, int rowNum) throws SQLException {
        Scan s = new Scan();
        s.setId(rs.getLong("s.id"));
        s.setFilename(rs.getString("s.filename"));
        s.setContentid(rs.getString("s.contentid"));
        s.setBoxId(Box.convertId(rs.getLong("s.box_id")));
        s.setSort(rs.getInt("s.sort"));
        s.setRotation(rs.getInt("s.rotation") % 360); //modulo 360
        s.setType(rs.getString("type"));
        if (rs.getString("attestatation_ids") != null) {
            s.setAttestationIDs(Arrays.stream(rs.getString("attestatation_ids").split("\\|"))
                .map(w -> Attestation.convertId(Long.parseLong(w))).toList());
        }
        return s;
    }
}
