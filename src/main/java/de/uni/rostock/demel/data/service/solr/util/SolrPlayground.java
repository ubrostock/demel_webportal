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
package de.uni.rostock.demel.data.service.solr.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import de.uni.rostock.demel.data.model.dictionary.Lemma;

public class SolrPlayground {

    static final String SOLR_URL = "http://localhost:8983/solr";

    static final String SOLR_COLLECTION = "demel";

    HttpSolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL)
        .withConnectionTimeout(10000)
        .withSocketTimeout(60000)
        .build();

    public void queryLemma() {
        /*
        final Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("q", "*:*");
        queryParamMap.put("fl", "id, name");
        queryParamMap.put("sort", "id asc");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);
        */
        final SolrQuery query = new SolrQuery("l__prefix:q");
        query.addField("*");
        query.setSort("l__name", ORDER.asc);
        query.setRows(10);

        try {
            QueryResponse response = solrClient.query(SOLR_COLLECTION, query);
            final SolrDocumentList documents = response.getResults();

            System.out.println("Found " + documents.getNumFound() + " documents");
            for (SolrDocument document : documents) {
                final String id = (String) document.getFirstValue("id");
                final String name = (String) document.getFirstValue("l__name");

                System.out.println("id: " + id + "; name: " + name);
            }

            for (Lemma l : response.getBeans(Lemma.class)) {
                System.out
                    .println("id: " + l.getId() + "; name: " + l.getName() + "; part_of_speech: "
                        + String.join(", ", l.getPartOfSpeechforDisplay()));
            }

        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

    }

    public void queryByJSON() {
        /* with Jakarta: 
          JsonObject json = Json.createObjectBuilder()
            .add("name", "Duke")
            .add("age", 42)
            .add("skills",
                Json.createArrayBuilder()
                .add("Java SE")
                .add("Java EE").build())
            .add("address",
                Json.createObjectBuilder()
                .add("street", "Mainstreet")
                .add("city", "Jakarta")
                .build())
            .build();
        
        JsonObject json = Json.createObjectBuilder()
            .add("bool", Json.createObjectBuilder()
                .add("must", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                        .add("lucene", Json.createObjectBuilder()
                            .add("df", "l__prefix")
                            .add("query", "q")
                            .build())))
                .build())
            .build();
        
        System.out.println(json.toString());
        // This could create a Solr-compatible MapOfObjects
        // Gson gson = new Gson();
        // gson.fromJson(json.toString(), com.google.gson.JsonObject.class);
        // Map map = (Map) gson.fromJson(json.toString(), Map.class);
        */

        Map<String, Object> map = Map.ofEntries(Map.entry("bool", Map.ofEntries(
            Map.entry("must", List.of(Map.ofEntries(
                Map.entry("lucene", Map.ofEntries(Map.entry("df", "l__prefix"), Map.entry("query", "q")))))))));
        final JsonQueryRequest query = new JsonQueryRequest().setQuery(map);

        try {
            QueryResponse response = query.process(solrClient, SOLR_COLLECTION);
            for (SolrDocument document : response.getResults()) {
                final String id = (String) document.getFirstValue("id");
                final String name = (String) document.getFirstValue("l__name");

                System.out.println("id: " + id + "; name: " + name);
            }

        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SolrPlayground app = new SolrPlayground();
        //app.queryLemma();
        app.queryByJSON();

    }

}
