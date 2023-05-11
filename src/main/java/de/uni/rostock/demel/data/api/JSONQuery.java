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
package de.uni.rostock.demel.data.api;

import org.apache.solr.client.solrj.request.json.JsonQueryRequest;

import de.uni.rostock.demel.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public class JSONQuery {

    private long start;

    private long rows;

    private long total;

    private JsonQueryRequest jsonRequest;

    private String solrJsonRequest;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getRows() {
        return rows;
    }

    public void setRows(long rows) {
        this.rows = rows;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public JsonQueryRequest getJsonQueryRequest() {
        return jsonRequest;
    }

    public void setJsonQueryRequest(JsonQueryRequest req) {
        this.jsonRequest = req;
    }

    public String getSolrJsonRequest() {
        return solrJsonRequest;
    }

    public void setSolrJsonRequest(String solrJsonRequest) {
        this.solrJsonRequest = solrJsonRequest;
    }
}
