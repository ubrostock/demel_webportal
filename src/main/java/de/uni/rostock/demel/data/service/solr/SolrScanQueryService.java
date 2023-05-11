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

import de.uni.rostock.demel.data.model.digitization.Scan;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;

@Service
public class SolrScanQueryService extends AbstractSolrQueryService<Scan, DatatableParams> {

    public Scan findScanByContentid(String contentid) {
        Map<String, Object> q = QueryUtils.createBooleanQuery(QueryUtils.BOOLEAN_QUERY_OPERATOR_MUST,
            List.of(QueryUtils.createLuceneQuery("c__contentid", contentid),
                QueryUtils.createLuceneQuery("doctype", Scan.DOCTYPE)));
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        jqr.setSort("c__sort asc");
        QueryResponse resp = solrService.querySolr(jqr);
        List<Scan> result = fromSolrResponse(resp);
        return result.size() == 1 ? result.get(0) : null;
    }

    public List<Scan> findScanByAttestation(String attestationId) {
        Map<String, Object> q = QueryUtils.createBooleanQuery(QueryUtils.BOOLEAN_QUERY_OPERATOR_MUST,
            List.of(QueryUtils.createLuceneQuery("c__attestation__ids", attestationId),
                QueryUtils.createLuceneQuery("doctype", Scan.DOCTYPE)));
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        jqr.setSort("c__sort asc");
        QueryResponse resp = solrService.querySolr(jqr);
        return fromSolrResponse(resp);
    }

    @Override
    public JsonQueryRequest createQuery(DatatableParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Scan> fromSolrResponse(QueryResponse response) {
        return response.getResults() == null ? List.of() : response.getBeans(Scan.class);
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
