/*
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.request.json.DomainMap;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.request.json.TermsFacetMap;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.browse.dictionary.LemmaDatatableParams;
import de.uni.rostock.demel.portal.util.I18NService;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;

@Service
public class SolrLemmaQueryService extends AbstractSolrQueryService<Lemma, LemmaDatatableParams> {

    @Autowired
    I18NService i18nService;

    @Override
    public List<Lemma> findByIds(List<String> ids) {
        return i18nService.enhanceWithI18nPartOfSpeech(super.findByIds(ids));
    }

    @Override
    public Lemma findById(String id) {
        return i18nService.enhanceWithI18nPartOfSpeech(super.findById(id));
    }

    @Override
    public QueryResult<Lemma> queryByDatatableParams(LemmaDatatableParams dtParams) {
        QueryResult<Lemma> qr = super.queryByDatatableParams(dtParams);
        return new QueryResult<>(i18nService.enhanceWithI18nPartOfSpeech(qr.data()), qr.solrResponse());
    }

    public List<Lemma> findLemmasByNameVariant(String term, boolean exact,
        EnumSet<SolrLemmaQueryFilter> lemmaQueryFilter) {
        List<Map<String, Object>> expr = new ArrayList<>();
        expr.add(QueryUtils.createLuceneQuery("doctype", "lemma"));

        String field = exact ? "l__search_exact" : "l__search";
        if (StringUtils.isNotEmpty(term)) {
            if (term.endsWith(".")) {
                term = QueryUtils.modifyTerm(term.substring(0, term.length() - 1));
            } else {
                term = QueryUtils.modifyTerm(term) + "*";
            }
            expr.add(QueryUtils.createLuceneQuery(field, term));
        }
        Map<String, Object> q = QueryUtils.createBooleanQuery("must", expr);
        JsonQueryRequest jqr = QueryUtils.createJsonQueryRequest(q);
        if (lemmaQueryFilter.contains(SolrLemmaQueryFilter.TYPE_LEMMA)) {
            jqr.withFilter(QueryUtils.createLuceneQuery("l__type", "lemma"));
        }
        if (lemmaQueryFilter.contains(SolrLemmaQueryFilter.ONLY_USED)) {
            jqr.withFilter(QueryUtils.createRangeQuery("count__attestations", 1, Long.MAX_VALUE));
        }
        if (lemmaQueryFilter.contains(SolrLemmaQueryFilter.STATUS_PUBLISHED)) {
            jqr.withFilter(QueryUtils.createLuceneQuery("l__status", "published"));
        }
        jqr.setSort("sorting asc");

        QueryResponse resp = solrService.querySolr(jqr);
        List<Lemma> result = fromSolrResponse(resp);
        i18nService.enhanceWithI18nPartOfSpeech(result);
        return result;
    }

    @Override
    public JsonQueryRequest createQuery(LemmaDatatableParams params) {
        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(
            Map.ofEntries(Map.entry("lucene", Map.ofEntries(Map.entry("df", "doctype"), Map.entry("query", "lemma")))));

        Optional<Map<String, Object>> oc = Searchbox.buildJsonQuery("id", params.getLemmaObjects(), "l__search",
            params.getLemmaTerms(), false);
        if (oc.isPresent()) {
            expressions.add(oc.get());
        }

        JsonQueryRequest query = QueryUtils.createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));
        query.setOffset(params.getDtStart());
        query.setLimit(params.getDtLength());
        query.setSort("sorting ASC");

        if (!params.getPrefixes().isEmpty()) {
            query.withFilter(
                QueryUtils.tagQuery("fqprefix", QueryUtils.createInQuery("l__prefix", params.getPrefixes())));
        }
        if (!params.getPartOfSpeechs().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqpos",
                QueryUtils.createInQuery("l__part_of_speech", params.getPartOfSpeechs())));
        }
        if (!params.getTypes().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqtype", QueryUtils.createInQuery("l__type", params.getTypes())));
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

        if (params.getFilter().contains("published")) {
            query.withFilter(QueryUtils.createLuceneQuery("l__status", "published"));
        }
        return enhanceQuery(query, params);
    }

    @Override
    protected List<Lemma> fromSolrResponse(QueryResponse response) {
        return response.getResults() == null ? List.of() : response.getBeans(Lemma.class);
    }

    @Override
    protected void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit) {
        query.withFacet("l__prefix", new TermsFacetMap("l__prefix").setMinCount(minCount).setLimit(limit)
            .withDomain(new DomainMap().withTagsToExclude("fqprefix")));
        query.withFacet("l__type", new TermsFacetMap("l__type").setMinCount(minCount).setLimit(limit)
            .withDomain(new DomainMap().withTagsToExclude("fqtype")));
        query.withFacet("l__part_of_speech",
            new TermsFacetMap("l__part_of_speech").setMinCount(minCount).setLimit(limit)
                .withDomain(new DomainMap().withTagsToExclude("fqpos")));
        query.withFacet("selected_grammar_info",
            new TermsFacetMap("l__part_of_speech").setMinCount(minCount).setLimit(limit));
    }

    @Override
    protected Map<String, String> createColumNames2SolrFieldsMap() {
        return Map.of();
    }
}
