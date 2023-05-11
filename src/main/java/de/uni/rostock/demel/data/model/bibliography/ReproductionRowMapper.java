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

@Service
public class ReproductionRowMapper implements RowMapper<Reproduction> {

    public static final String SELECT_BY_EDITION_ID = """
        SELECT r.* FROM bibl_reproduction r
        WHERE r.edition_id = ?
        ORDER BY r.sort ASC;
        """;

    @Override
    public Reproduction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Reproduction r = new Reproduction();
        r.setId(rs.getLong("r.id"));
        r.setOnlineUrl(rs.getString("r.online_url"));
        r.setProvider(rs.getString("r.provider"));
        r.setAccess(rs.getString("r.access"));
        r.setEditionId(Edition.convertId(rs.getInt("r.edition_id")));
        r.setSort(rs.getInt("sort"));
        r.setHintsExtern(rs.getString("hints_extern"));

        return r;
    }
}
