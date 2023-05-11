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
public class PersonRowMapper implements RowMapper<Person> {

    public static final String SQL_SELECT_ALL = """
        SELECT * FROM bibl_person p;
        """;
    public static final String SQL_SELECT_SINGLE_PERSON = SQL_SELECT_ALL + " WHERE p.id=?";

    public static final String SELECT_BY_SOURCE_ID = """
        SELECT p.* FROM bibl_person p
                 JOIN map_source_person m
                 ON m.person_id = p.id
        WHERE m.source_id = ?
        ORDER BY p.name_display;
        """;

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person p = new Person();
        p.setId(rs.getLong("p.id"));
        p.setNameDisplay(rs.getString("name_display"));
        p.setPersonSearch(rs.getString("name_display"));
        p.setBneId(rs.getString("p.bne_id"));
        p.setViafId(rs.getString("p.viaf_id"));
        p.setGndId(rs.getString("p.gnd_id"));
        p.setWikidataId(rs.getString("p.wikidata_id"));
        p.setSorting(rs.getString("name_display"));

        return p;
    }
}
