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
package de.uni.rostock.demel.portal.browse.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.util.FacetOutput;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;

public class _BaseController {

    protected Map<String, Object> createDatatableResponse(DatatableParams dtParams,
        QueryResult<?> queryResult, Map<String, String> facetSearchfieldMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", queryResult.data()); //List of SolrAttestation
        result.put("draw", dtParams.getDtDraw()); //int .. running number of datatable request to preserve order in asynchronous environment
        if (queryResult.solrResponse().getResults() != null) {
            result.put("recordsTotal", queryResult.solrResponse().getResults().getNumFound());
            result.put("recordsFiltered", queryResult.solrResponse().getResults().getNumFound());
        } else {
            result.put("recordsTotal", 0L);
            result.put("recordsFiltered", 0L);
        }
        if (queryResult.solrResponse().getResults() != null) {
            for (Entry<String, String> fe : facetSearchfieldMap.entrySet()) {

                if (queryResult.solrResponse().getJsonFacetingResponse().getBucketBasedFacets(fe.getValue()) != null) {
                    result.put(fe.getKey(),
                        queryResult.solrResponse().getJsonFacetingResponse().getBucketBasedFacets(fe.getValue())
                            .getBuckets().stream()
                            .map(x -> new FacetOutput(x.getVal().toString(), x.getCount())).toList());
                }
            }
        } else {
            for (String facet : facetSearchfieldMap.keySet()) {
                result.put(facet, Collections.emptyList());
            }
        }
        return result;
    }
}
