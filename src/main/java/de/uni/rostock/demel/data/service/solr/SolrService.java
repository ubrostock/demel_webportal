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

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;

@Service
public class SolrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrService.class);

    @Value("${solr.server.url}")
    String solrUrl;

    @Value("${solr.server.collection}")
    String solrCollection;

    private HttpSolrClient solrClient = null;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public HttpSolrClient getSolrClient() {
        return solrClient;
    }

    @PostConstruct
    private void init() {
        solrClient = new HttpSolrClient.Builder(solrUrl)
            .withConnectionTimeout(10000)
            .withSocketTimeout(60000)
            .build();
    }

    public QueryResponse querySolr(SolrQuery query) {
        try {
            QueryResponse response = solrClient.query(solrCollection, query);
            return response;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new QueryResponse(solrClient);
    }

    public QueryResponse querySolr(JsonQueryRequest request) {
        try {
            QueryResponse response = request.process(solrClient, solrCollection);
            return response;
        } catch (BaseHttpSolrClient.RemoteSolrException | SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new QueryResponse(solrClient);
    }

    public UpdateResponse deleteByQuery(String query) {
        try {
            UpdateResponse resp = solrClient.deleteByQuery(solrCollection, query);
            solrClient.commit(solrCollection);
            return resp;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new UpdateResponse();
    }

    public UpdateResponse save(Object obj) {
        try {
            UpdateResponse resp = solrClient.addBean(solrCollection, obj);
            solrClient.commit(solrCollection);
            return resp;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new UpdateResponse();
    }

    public UpdateResponse saveAll(List<? extends Object> list) {
        try {
            UpdateResponse resp = solrClient.addBeans(solrCollection, list);
            solrClient.commit(solrCollection);
            return resp;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new UpdateResponse();
    }

    public UpdateResponse saveSolrDoc(SolrInputDocument doc) {
        try {
            UpdateResponse resp = solrClient.add(solrCollection, doc);
            solrClient.commit(solrCollection);
            return resp;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new UpdateResponse();
    }

    public UpdateResponse saveSolrDocs(List<SolrInputDocument> docs) {
        try {
            UpdateResponse resp = solrClient.add(solrCollection, docs);
            solrClient.commit(solrCollection);
            return resp;
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SOLR Server error", e);
        }
        return new UpdateResponse();
    }

}
