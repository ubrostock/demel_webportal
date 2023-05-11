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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.model.dictionary.SourceRowMapper;

@Service
public class SourceDBService {
    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SourceRowMapper rowMapper;

    public Source querySource(String idParam) {
        int id = Integer.parseInt(idParam.replace(Source.DOCID_PREFIX, ""));
        Source s = jdbcTemplate.queryForObject(SourceRowMapper.SQL_SELECT_SINGLE_SOURCE,
            rowMapper, id);
        return s;
    }

    public List<Integer> querySourceIds() {
        return jdbcTemplate.queryForList("SELECT id FROM dict_source ORDER BY id", Integer.class);
    }
}
