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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;
import de.uni.rostock.demel.portal.util.datatable.DatatableParamsOrder;

public abstract class AbstractSolrQueryService<O extends AbstractModelObject, D extends DatatableParams> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSolrQueryService.class);

    @Autowired
    protected SolrService solrService;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public record QueryResult<X>(List<X> data, QueryResponse solrResponse) {
    }

    public List<O> findByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            Map<String, Object> q = QueryUtils.createInQuery("id", ids);
            JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
            jqr.setLimit(Math.min(1000, ids.size()));
            QueryResponse resp = solrService.querySolr(enhanceQuery(jqr, null));
            return fromSolrResponse(resp);
        } else {
            return Collections.emptyList();
        }
    }

    public O findById(String id) {
        Map<String, Object> q = QueryUtils.createLuceneQuery("id", id);
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        QueryResponse resp = solrService.querySolr(enhanceQuery(jqr, null));
        List<O> result = fromSolrResponse(resp);
        return result.size() == 1 ? result.get(0) : null;
    }

    //add default return fields
    //subclasses may override or extend
    protected JsonQueryRequest enhanceQuery(JsonQueryRequest jqr, DatatableParams dtparams) {
        jqr.setSort(createSortOptions(dtparams));
        jqr.returnFields("*");
        jqr.returnFields("[child limit=100]");

        enhanceWithFacetOptions(jqr, 1, 1000);
        return jqr;
    }

    private String createSortOptions(DatatableParams dtparams) {
        List<String> sort = new ArrayList<>();
        Map<String, String> columNames2SolrFieldsMap = createColumNames2SolrFieldsMap();
        if (dtparams != null && columNames2SolrFieldsMap != null) {
            for (DatatableParamsOrder o : dtparams.getDtOrders()) {
                try {
                    String columnName = dtparams.getDtColumns().get(o.getColumn()).getName();
                    if (columNames2SolrFieldsMap.containsKey(columnName)
                        && columNames2SolrFieldsMap.get(columnName).length() > 0) {
                        sort.add(columNames2SolrFieldsMap.get(columnName) + " " + o.getDir());
                    }
                } catch (Exception e) {
                    LOGGER.error("Could not create sort options", e);
                }
            }
        }
        //add default sort field as last option
        sort.add("sorting asc");
        return String.join(", ", sort);
    }

    public QueryResult<O> queryByDatatableParams(D dtParams) {
        JsonQueryRequest q = createQuery(dtParams);
        QueryResponse resp = solrService.querySolr(q);
        List<O> result = fromSolrResponse(resp);
        return new QueryResult<O>(result, resp);
    }

    public abstract JsonQueryRequest createQuery(D params);

    protected abstract void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit);

    protected abstract Map<String, String> createColumNames2SolrFieldsMap();

    protected abstract List<O> fromSolrResponse(QueryResponse resp);

}
