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
public class SigleRowMapper implements RowMapper<Sigle> {

    public static final String SELECT_BY_SOURCE_ID
        = "SELECT * FROM bibl_sigle si WHERE si.source_id = ? ORDER by type,sigle_search;";

    @Override
    public Sigle mapRow(ResultSet rs, int rowNum) throws SQLException {
        Sigle s = new Sigle();
        s.setId(rs.getInt("si.id"));
        s.setName(rs.getString("si.name"));
        s.setType(rs.getString("si.type"));
        s.setSourceId(Source.convertId(rs.getLong("si.source_id")));
        s.setSorting(rs.getString("si.sigle_search"));

        return s;
    }
}
