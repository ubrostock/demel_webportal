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

import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.LemmaRowMapper;
import de.uni.rostock.demel.portal.browse.admin.SolrIndexingData;

@Service
public class SolrLemmaUpdateService extends AbstractSolrUpdateService<Lemma> {

    @Autowired
    @Qualifier("indexStatusLemmas")
    SolrIndexingData indexStatusLemmas;

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SolrService solrService;

    private LemmaRowMapper rowMapper = new LemmaRowMapper();

    /**
     * copy lemmas from database into SOLR index
     */
    @Async
    @Override
    public void reindexAllObjects() {
        indexStatusLemmas.start(countDBEntrys());
        deleteAllObjects();
        ArrayList<Lemma> todo = new ArrayList<>();
        LemmaRowMapper rowMapper = new LemmaRowMapper();

        jdbcTemplate.query(LemmaRowMapper.SQL_SELECT_ALL,
            new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    if (!indexStatusLemmas.isRunning()) {
                        rs.last();
                        return;
                    }
                    Lemma l = rowMapper.mapRow(rs, 0);
                    todo.add(l);
                    indexStatusLemmas.increment();
                    if (todo.size() > 500) {
                        saveAllObjects(todo);
                        todo.clear();
                    }
                }
            });
        if (todo.size() > 0) {
            saveAllObjects(todo);
        }
    }

    @Override
    public void reindexObject(String id) {
        long theId = Long.parseLong(id.replace("l", ""));
        jdbcTemplate.query(LemmaRowMapper.SQL_SELECT_SINGLE_LEMMA, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Lemma l = rowMapper.mapRow(rs, 0);
                saveObject(l);
            }
        }, theId);
    }

    @Override
    protected void saveAllObjects(List<Lemma> todo) {
        solrService.saveAll(todo);
    }

    @Override
    public void saveObject(Lemma l) {
        solrService.save(l);
    }

    @Override
    protected void deleteAllObjects() {
        solrService.deleteByQuery("doctype:" + Lemma.DOCTYPE);
    }

    @Override
    protected int countDBEntrys() {
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dict_lemma;", Integer.class);
        return c == null ? 0 : c.intValue();
    }

}
