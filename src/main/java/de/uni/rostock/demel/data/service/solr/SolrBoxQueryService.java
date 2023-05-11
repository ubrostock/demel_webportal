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

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.digitization.Box;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;

@Service
public class SolrBoxQueryService extends AbstractSolrQueryService<Box, DatatableParams> {

    public List<Box> findAllBoxes() {
        Map<String, Object> q = QueryUtils.createLuceneQuery("doctype", Box.DOCTYPE);
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        QueryResponse resp = querySolr(jqr);
        return fromSolrResponse(resp);
    }

    public List<Box> findBoxesBySection(String section) {
        Map<String, Object> q = QueryUtils.createBooleanQuery(QueryUtils.BOOLEAN_QUERY_OPERATOR_MUST,
            List.of(QueryUtils.createLuceneQuery("k__section", section),
                QueryUtils.createLuceneQuery("doctype", Box.DOCTYPE)));
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        QueryResponse resp = querySolr(jqr);
        return fromSolrResponse(resp);
    }

    private QueryResponse querySolr(JsonQueryRequest jqr) {
        jqr.setSort("k__section asc, k__section_sort asc");
        jqr.setLimit(1000);
        return solrService.querySolr(jqr);
    }

    @Override
    public JsonQueryRequest createQuery(DatatableParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Box> fromSolrResponse(QueryResponse response) {
        return response.getResults() == null ? List.of() : response.getBeans(Box.class);
    }

    @Override
    protected void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit) {
        // do nothing
    }

    @Override
    protected Map<String, String> createColumNames2SolrFieldsMap() {
        return Map.of();
    }

}
