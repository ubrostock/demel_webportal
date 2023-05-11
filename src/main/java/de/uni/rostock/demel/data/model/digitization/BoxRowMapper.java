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

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BoxRowMapper implements RowMapper<Box> {

    public static final String SQL_SELECT_ALL = """
        SELECT * FROM digi_box b
            LEFT JOIN  (SELECT box_id, COUNT(*) AS anz_scans FROM digi_scan GROUP BY box_id) AS c
                 ON b.id = c.box_id
        ORDER BY section_www_sort, id;

        """;

    public static final String SQL_SELECT_SINGLE_BOX = SQL_SELECT_ALL + " WHERE b.id=?";

    @Override
    public Box mapRow(ResultSet rs, int rowNum) throws SQLException {
        Box b = new Box();
        b.setId(rs.getLong("b.id"));
        b.setName(rs.getString("name"));
        b.setName_es(rs.getString("name_es"));
        b.setSection(rs.getString("section_www"));
        b.setSectionSort(rs.getInt("section_www_sort"));
        b.setStatus(rs.getString("status"));
        b.setCountScans(rs.getLong("anz_scans"));
        return b;
    }
}
