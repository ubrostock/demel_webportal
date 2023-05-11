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
package de.uni.rostock.demel.data.service.solr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.digitization.Box;
import de.uni.rostock.demel.data.model.digitization.BoxRowMapper;
import de.uni.rostock.demel.portal.browse.admin.SolrIndexingData;

@Service
public class SolrBoxUpdateService extends AbstractSolrUpdateService<Box> {
    @Autowired
    @Qualifier("indexStatusBoxes")
    SolrIndexingData indexStatusBoxes;

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SolrService solrService;

    private BoxRowMapper rowMapper = new BoxRowMapper();

    @Async
    @Override
    public void reindexAllObjects() {
        indexStatusBoxes.start(countDBEntrys());
        deleteAllObjects();
        ArrayList<Box> todo = new ArrayList<>();
        jdbcTemplate.query(BoxRowMapper.SQL_SELECT_ALL,
            new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    if (!indexStatusBoxes.isRunning()) {
                        rs.last();
                        return;
                    }
                    Box b = rowMapper.mapRow(rs, 0);
                    todo.add(b);
                    indexStatusBoxes.increment();
                }
            });

        if (todo.size() > 0) {
            saveAllObjects(todo);
        }
    }

    @Override
    public void reindexObject(String id) {
        long theId = Long.parseLong(id.substring(1));
        jdbcTemplate.query(BoxRowMapper.SQL_SELECT_SINGLE_BOX, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Box b = rowMapper.mapRow(rs, 0);
                saveObject(b);
            }
        }, theId);

    }

    @Override
    protected void saveAllObjects(List<Box> todo) {
        solrService.saveAll(todo);
    }

    @Override
    public void saveObject(Box b) {
        solrService.save(b);
    }

    @Override
    protected void deleteAllObjects() {
        solrService.deleteByQuery("doctype:" + Box.DOCTYPE);
    }

    @Override
    protected int countDBEntrys() {
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM digi_box;", Integer.class);
        return c == null ? 0 : c.intValue();
    }

}
