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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.request.json.DomainMap;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.request.json.TermsFacetMap;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.browse.dictionary.AttestationDatatableParams;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;

@Service
public class SolrAttestationQueryService extends AbstractSolrQueryService<Attestation, AttestationDatatableParams> {

    //for conversion of Spring Data Criteria API to SOLR queries:
    //see https://docs.spring.io/spring-data/solr/docs/current/reference/html/#solr.query-methods
    @Override
    public JsonQueryRequest createQuery(AttestationDatatableParams params) {
        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(QueryUtils.createLuceneQuery("doctype", "attestation"));
        Optional<Map<String, Object>> oc = null;
        String field = params.isSearchExact() ? "d__form_exact" : "d__form";
        oc = Searchbox.buildJsonQuery("id", params.getFormObjects(), field, params.getFormTerms(), false);
        if (oc.isPresent()) {
            expressions.add(oc.get());
        }

        field = params.isSearchExact() ? "d__lemma__search_exact" : "d__lemma__search";
        oc = Searchbox.buildJsonQuery("d__lemma__id", params.getLemmaObjects(), field, params.getLemmaTerms(), false);
        if (oc.isPresent()) {
            expressions.add(oc.get());
        }

        field = params.isSearchExact() ? "d__sigle__search_exact" : "d__sigle__search";
        oc = Searchbox.buildJsonQuery("d__source__id", params.getSourceObjects(), field, params.getSourceTerms(),
            false);
        if (oc.isPresent()) {
            expressions.add(oc.get());
        }

        field = params.isSearchExact() ? "d__multiwordexpr_search_exact" : "d__multiwordexpr_search";
        if (!StringUtils.isBlank(params.getMwexprTerm())) {
            oc = Searchbox.buildJsonQuery(null, Collections.emptyList(), field, List.of(params.getMwexprTerm()), false);
            if (oc.isPresent()) {
                expressions.add(oc.get());
            }
        }

        if (params.getDatingFrom() != null && params.getDatingFrom() != 0) {
            //Criteria c1 = Criteria.where("d.bibitem.date_to").greaterThanEqual(params.getDateFrom());
            expressions
                .add(QueryUtils.createLuceneQuery("d__dating_to_search", "[" + params.getDatingFrom() + " TO *]"));
        }
        if (params.getDatingTo() != null && params.getDatingTo() != 999999) {
            //Criteria c1 = Criteria.where("d.bibitem.date_from").lessThanEqual(params.getDateTo());
            expressions
                .add(QueryUtils.createLuceneQuery("d__dating_from_search", "[* TO " + params.getDatingTo() + "]"));
        }

        if (params.getFilter().contains("undated")) {
            //c = c.and(Criteria.where("d.bibitem.date_from").lessThan(5000));
            expressions.add(QueryUtils.createLuceneQuery("d__dating_from_search", "{0 TO 5000}"));
        }

        //Pageable p = PageRequest.of(params.getDtStart() / params.getDtLength(), params.getDtLength());
        //SimpleFacetQuery q = new SimpleFacetQuery(c, p);
        JsonQueryRequest query = QueryUtils.createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));
        //Pageable p = PageRequest.of(params.getDtStart() / params.getDtLength(), params.getDtLength());
        query.setOffset(params.getDtStart());
        query.setLimit(params.getDtLength());

        if (!params.getTypes().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqtype", QueryUtils.createInQuery("d__type", params.getTypes())));
        }

        if (!params.getWithMultiwordexprs().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqmwexpr",
                QueryUtils.createInQuery("d__is_multiwordexpr", params.getWithMultiwordexprs())));
        }

        if (params.getFilter().contains("published")) {
            query.withFilter(QueryUtils.createLuceneQuery("d__status", "published"));
        }

        if (params.getFilter().contains("messages_published")) {
            //c = c.and(Criteria.where("d.count_usercomments_public").greaterThan(0));
            query.withFilter(QueryUtils.createLuceneQuery("count__messages_published", "{0 TO *]"));
        }
        if (params.getFilter().contains("messages_inreview")) {
            //c = c.and(Criteria.where("d.count_usercomments_inreview").greaterThan(0));
            query.withFilter(QueryUtils.createLuceneQuery("count__messages_inreview", "{0 TO *]"));
        }
        if (params.getFilter().contains("messages_all")) {
            //c = c.and(Criteria.where("d.count_usercomments_all").greaterThan(0));
            query.withFilter(QueryUtils.createLuceneQuery("count__messages", "{0 TO *]"));
        }

        return enhanceQuery(query, params);
    }

    @Override
    protected void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit) {
        query.withFacet("d__type", new TermsFacetMap("d__type").setMinCount(minCount).setLimit(limit)
            .withDomain(new DomainMap().withTagsToExclude("fqtype")));
        query.withFacet("d__is_multiwordexpr",
            new TermsFacetMap("d__is_multiwordexpr").setMinCount(minCount).setLimit(limit)
                .withDomain(new DomainMap().withTagsToExclude("fqmwexpr")));
    }

    @Override
    protected List<Attestation> fromSolrResponse(QueryResponse response) {
        return response.getResults() == null ? List.of() : response.getBeans(Attestation.class);
    }

    @Override
    protected Map<String, String> createColumNames2SolrFieldsMap() {
        return Map.ofEntries(
            Map.entry("id", "sorting"),
            Map.entry("lemma", "d__lemma__name_sorting"),
            Map.entry("type", "d__type"),
            Map.entry("form", "sorting"),
            Map.entry("source", "d__source__name_sorting"),
            Map.entry("dating", "d__dating_from_search"),
            Map.entry("multiwordexpr", "d__multiwordexpr_sorting"),
            Map.entry("scans", "count__scans"));
    }

}
