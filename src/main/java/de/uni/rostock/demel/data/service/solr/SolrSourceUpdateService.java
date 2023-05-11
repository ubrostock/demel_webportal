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

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.bibliography.Edition;
import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.bibliography.Reproduction;
import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.model.dictionary.SourceRowMapper;
import de.uni.rostock.demel.portal.browse.admin.SolrIndexingData;

@Service
public class SolrSourceUpdateService extends AbstractSolrUpdateService<Source> {

    @Autowired
    @Qualifier("indexStatusSources")
    SolrIndexingData indexStatusSources;

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SolrService solrService;

    @Autowired
    private SourceRowMapper rowMapper;

    @Autowired
    SolrPersonUpdateService solrPersonUpdateService;

    @Async
    @Override
    public void reindexAllObjects() {
        indexStatusSources.start(countDBEntrys());
        deleteAllObjects();
        ArrayList<Source> todo = new ArrayList<>();
        jdbcTemplate.query(SourceRowMapper.SQL_SELECT_ALL, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                if (!indexStatusSources.isRunning()) {
                    rs.last();
                    return;
                }
                Source b = rowMapper.mapRow(rs, 0);
                if (b != null) {
                    todo.add(b);
                }
                indexStatusSources.increment();
                if (todo.size() > 200) {
                    saveAllObjects(todo);
                    todo.clear();
                }
            }
        });
        if (todo.size() > 0) {
            saveAllObjects(todo);
        }

        //we do not provide a separate interface to update person objects
        //so we call it here
        solrPersonUpdateService.reindexAllObjects();
    }

    @Override
    public void reindexObject(String id) {
        long theId = Source.convertId(id);
        jdbcTemplate.query(SourceRowMapper.SQL_SELECT_SINGLE_SOURCE, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Source s = rowMapper.mapRow(rs, 0);
                if (s != null) {
                    saveObject(s);
                }
            }
        }, theId);

    }

    @Override
    public void saveAllObjects(List<Source> todo) {
        ArrayList<SolrInputDocument> docs = new ArrayList<>(todo.size());
        for (Source s : todo) {
            docs.add(toSolrInputDocument(s));
        }
        solrService.saveSolrDocs(docs);
    }

    @Override
    public void saveObject(Source s) {
        solrService.saveSolrDoc(toSolrInputDocument(s));
    }

    @Override
    protected void deleteAllObjects() {
        solrService.deleteByQuery("doctype:" + Source.DOCTYPE);

        //additionally delete child documents in SOLR 7 / should not be necessary in SOLR 8
        solrService.deleteByQuery("doctype:" + Edition.DOCTYPE);
        solrService.deleteByQuery("doctype:" + Sigle.DOCTYPE);
        solrService.deleteByQuery("doctype:" + Reproduction.DOCTYPE);

        //delete root-Documents of type person
        solrService.deleteByQuery("doctype:" + Person.DOCTYPE);
    }

    @Override
    protected int countDBEntrys() {
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dict_source;", Integer.class);
        return c == null ? 0 : c.intValue();
    }

    private SolrInputDocument toSolrInputDocument(Source s) {
        DocumentObjectBinder binder = solrService.getSolrClient().getBinder();
        SolrInputDocument input = binder.toSolrInputDocument(s);
        for (Edition e : s.getEditions()) {
            SolrInputDocument inputEdition = binder.toSolrInputDocument(e);
            for (Reproduction r : e.getReproductions()) {
                inputEdition.addField("e__child__reproductions", binder.toSolrInputDocument(r));
            }
            input.addField("b__child__editions", inputEdition);
        }
        for (Sigle si : s.getSigles()) {
            input.addField("b__child__sigles", binder.toSolrInputDocument(si));
        }
        for (Person p : s.getPersons()) {
            input.addField("b__child__persons", binder.toSolrInputDocument(p));
        }
        return input;
    }

}
