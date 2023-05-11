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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Source;

@Service
public class EditionRowMapper implements RowMapper<Edition> {

    public static final String SELECT_BY_SOURCE_ID = """
        SELECT e.*,
           ( SELECT he.title_info FROM bibl_edition he
             WHERE he.source_id = e.source_host_id
                   AND he.sort = ( SELECT MIN(he.sort) FROM bibl_edition ee
                                   WHERE ee.source_id = e.source_host_id )
           ) AS he_title,
           ( SELECT hs.name FROM bibl_sigle hs
             WHERE hs.source_id = e.source_host_id AND hs.type='main'
           ) AS hs_name
        FROM bibl_edition e
        WHERE e.source_id = ?
        ORDER BY e.sort;
        """;

    @Override
    public Edition mapRow(ResultSet rs, int rowNum) throws SQLException {
        Edition e = new Edition();
        e.setId(rs.getLong("e.id"));
        e.setTitleInfo(rs.getString("e.title_info"));
        e.setSourceId(rs.getString("e.source_id"));
        e.setSort(rs.getInt("e.sort"));
        e.setDatingFrom(rs.getInt("e.dating_from"));
        e.setDatingTo(rs.getInt("e.dating_to"));
        e.setBneId(rs.getString("e.bne_id"));
        e.setSourceHostId(Source.convertId(rs.getLong("e.source_host_id")));
        if (rs.wasNull()) {
            e.setSourceHostId(null);
        }
        e.setSourceHostSigle(rs.getString("hs_name"));
        e.setSourceHostTitle(rs.getString("he_title"));
        return e;
    }
}
