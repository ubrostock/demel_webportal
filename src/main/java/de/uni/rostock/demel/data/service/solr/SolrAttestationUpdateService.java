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

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.AttestationRowMapper;
import de.uni.rostock.demel.portal.browse.admin.SolrIndexingData;

@Service
public class SolrAttestationUpdateService extends AbstractSolrUpdateService<Attestation> {

    @Autowired
    @Qualifier("indexStatusAttestations")
    SolrIndexingData indexStatusAttestations;

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SolrService solrService;

    private AttestationRowMapper rowMapper = new AttestationRowMapper();

    /**
     * copy attestation from database into SOLR index
     */
    @Async
    @Override
    public void reindexAllObjects() {
        indexStatusAttestations.start(countDBEntrys());
        deleteAllObjects();
        ArrayList<Attestation> todo = new ArrayList<>();
        AttestationRowMapper rowMapper = new AttestationRowMapper();
        jdbcTemplate.query(AttestationRowMapper.SQL_SELECT_ALL_ATTESTATIONS,
            new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    if (!indexStatusAttestations.isRunning()) {
                        rs.last();
                        return;
                    }
                    Attestation w = rowMapper.mapRow(rs, 0);
                    todo.add(w);
                    indexStatusAttestations.increment();
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

    // id = "d"+[integer]
    @Override
    public void reindexObject(String id) {
        long theId = Long.parseLong(id.replace("d", ""));

        jdbcTemplate.query(AttestationRowMapper.SQL_SELECT_SINGLE_ATTESTATION, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Attestation l = rowMapper.mapRow(rs, 0);
                saveObject(l);
            }
        }, theId);
    }

    @Override
    protected void saveAllObjects(List<Attestation> todo) {
        solrService.saveAll(todo);
    }

    @Override
    public void saveObject(Attestation a) {
        solrService.save(a);
    }

    @Override
    protected void deleteAllObjects() {
        solrService.deleteByQuery("doctype:" + Attestation.DOCTYPE);
    }

    @Override
    protected int countDBEntrys() {
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dict_attestation", Integer.class);
        return c == null ? 0 : c.intValue();
    }

}
