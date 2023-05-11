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
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;
import de.uni.rostock.demel.portal.util.searchbox.SelectData;

@Service
public class SolrPersonQueryService extends AbstractSolrQueryService<Person, DatatableParams> {

    public List<Person> findAllPersons() {
        Map<String, Object> q = QueryUtils.createLuceneQuery("doctype", Person.DOCTYPE);
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        QueryResponse resp = querySolr(jqr);
        return fromSolrResponse(resp);
    }

    private QueryResponse querySolr(JsonQueryRequest jqr) {
        jqr.setSort("k__section asc, k__section_sort asc");
        jqr.withFilter("is_parent:true");
        jqr.setLimit(1000);
        return solrService.querySolr(jqr);
    }

    public SelectData selectPersonByID(String personId) {
        Person p = findById(personId);
        if (p != null) {
            return new SelectData(p.getId(), p.getNameDisplay());
        }
        return null;
    }

    public List<SelectData> selectPersonsByName(String term, boolean exact) {
        List<SelectData> result = new ArrayList<>();
        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(QueryUtils.createLuceneQuery("doctype", Person.DOCTYPE));
        if (term != null) {
            String field = exact ? "p__search_exact" : "p__search";
            if (term.endsWith(".")) {
                term = QueryUtils.modifyTerm(term.substring(0, term.length() - 1));
            } else {
                term = QueryUtils.modifyTerm(term) + "*";
            }
            expressions.add(QueryUtils.createLuceneQuery(field, term));
            JsonQueryRequest query = QueryUtils
                .createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));
            query.setLimit(20);
            query.setSort("sorting asc");
            query.withFilter("is_parent:true");
            QueryResponse resp = solrService.querySolr(query);
            for (Person p : resp.getBeans(Person.class)) {
                result.add(new SelectData(p.getId(), p.getNameDisplay()));
            }
        }
        return result;
    }

    @Override
    public JsonQueryRequest createQuery(DatatableParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Person> fromSolrResponse(QueryResponse response) {
        return response.getResults() == null ? List.of() : response.getBeans(Person.class);
    }

    @Override
    protected void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit) {
        //do nothing
    }

    @Override
    protected Map<String, String> createColumNames2SolrFieldsMap() {
        return Map.of();
    }

}
