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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.request.json.DomainMap;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.request.json.TermsFacetMap;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.bibliography.Edition;
import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.bibliography.Reproduction;
import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import de.uni.rostock.demel.portal.browse.dictionary.SourceDatatableParams;
import de.uni.rostock.demel.portal.util.I18NService;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;
import de.uni.rostock.demel.portal.util.searchbox.SelectData;

@Service
public class SolrSourceQueryService extends AbstractSolrQueryService<Source, SourceDatatableParams> {

    @Autowired
    private I18NService i18nService;

    /**
     * SOLR query syntax: 
     * https://github.com/spring-projects/spring-data-solr/blob/main/src/main/asciidoc/reference/data-solr.adoc
     */
    public List<Source> findSources(List<String> sigleIds, List<String> sigleTerms, String edition,
        String dateAfter, String dateBefore) {

        List<Map<String, Object>> expressions = new ArrayList<>();
        //Criteria c = Criteria.where("doctype").is("bibitem");
        expressions.add(QueryUtils.createLuceneQuery("doctype", "source"));
        Optional<Map<String, Object>> oc = Searchbox.buildJsonQuery("id", sigleIds,
            "b__sigle__search", sigleTerms, false);
        if (oc.isPresent()) {
            expressions.add(oc.get());
        }

        if (StringUtils.isNotEmpty(edition)) {
            expressions.add(QueryUtils.createLuceneQuery("b__edition__search", edition));
        }
        if (StringUtils.isNotEmpty(dateAfter) && !"0".equals(dateAfter)) {
            //c = c.and("b.date_to").greaterThanEqual(dateAfter);
            expressions.add(QueryUtils.createLuceneQuery("b__dating_to", "[" + dateAfter + " TO *]"));
        }
        if (StringUtils.isNotEmpty(dateBefore) && !"999999".equals(dateBefore)) {
            //c = c.and("b.date_from").lessThanEqual(dateBefore);
            expressions.add(QueryUtils.createLuceneQuery("b__dating_from", "[* TO " + dateBefore + "]"));
        }
        QueryResponse resp = querySolr(
            QueryUtils.createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions)));
        return fromSolrResponse(resp);
    }

    public List<SelectData> selectSiglesByName(String term, boolean exact, EnumSet<SolrSourceQueryFilter> filter) {
        List<SelectData> result = new ArrayList<>();

        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(QueryUtils.createLuceneQuery("doctype", Sigle.DOCTYPE));
        if (term != null) {
            String field = exact ? "s__name_exact" : "s__name";
            term = QueryUtils.modifyTerm(term) + "*";
            expressions.add(QueryUtils.createLuceneQuery(field, term));
            JsonQueryRequest query = QueryUtils
                .createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));
            query.setLimit(20);
            if (filter.contains(SolrSourceQueryFilter.ONLY_USED)) {
                query.withFilter(QueryUtils.createRangeQuery("s__source__count__attestations", 1, Long.MAX_VALUE));
            }
            if (filter.contains(SolrSourceQueryFilter.STATUS_PUBLISHED)) {
                query.withFilter(QueryUtils.createLuceneQuery("s__source__status", "published"));
            }
            query.setSort("sorting asc");
            QueryResponse resp = solrService.querySolr(query);
            for (Sigle s : resp.getBeans(Sigle.class)) {
                i18nService.enhanceWithI18nDatingNone(s);
                result.add(new SelectData(s.getSourceId(), s.getHtmlDisplay()));
            }
        }
        return result;
    }

    public SelectData selectSigleBySourceID(String sourceId) {
        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(QueryUtils.createLuceneQuery("doctype", "sigle"));
        expressions.add(QueryUtils.createLuceneQuery("s__type", "main"));
        expressions.add(QueryUtils.createLuceneQuery("s__source__id", sourceId));
        JsonQueryRequest query = QueryUtils.createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));

        QueryResponse resp = solrService.querySolr(query);
        List<Sigle> result = resp.getBeans(Sigle.class);
        if (result.size() > 0) {
            Sigle s = result.get(0);
            i18nService.enhanceWithI18nDatingNone(s);
            return new SelectData(s.getSourceId(), s.getHtmlDisplay());
        }
        return null;
    }

    public List<Source> findAllSources() {
        Map<String, Object> q = QueryUtils.createLuceneQuery("doctype", "source");
        QueryResponse resp = querySolr(QueryUtils.createJsonQueryRequest(q));
        return fromSolrResponse(resp);
    }

    @Override
    public JsonQueryRequest createQuery(SourceDatatableParams params) {

        List<Map<String, Object>> expressions = new ArrayList<>();
        expressions.add(QueryUtils.createLuceneQuery("doctype", "source"));

        Searchbox.buildJsonQuery("id", params.getSourceObjects(), "b__sigle__search",
            params.getSourceTerms(), false).ifPresent(x -> expressions.add(x));

        params.getEditionTerms().replaceAll(t -> QueryUtils.modifyTerm(t, true));
        Searchbox.buildJsonQuery("", Collections.emptyList(), "b__edition__search", params.getEditionTerms(), true)
            .ifPresent(x -> expressions.add(x));

        //c = c.and(Criteria.where("b.date_to").greaterThanEqual());
        //c = c.and(Criteria.where("b.date_from").lessThanEqual());
        if (params.getDatingFrom() != null && params.getDatingFrom() != 0) {
            expressions.add(QueryUtils.createLuceneQuery("b__dating_to", "[" + params.getDatingFrom() + " TO *]"));
        }
        if (params.getDatingTo() != null && params.getDatingTo() != 999999) {
            expressions.add(QueryUtils.createLuceneQuery("b__dating_from", "[* TO " + params.getDatingTo() + "]"));
        }

        if (params.getFilter().contains("unused")) {
            //c = c.and(Criteria.where("b.count_forms").greaterThan(0));
            expressions.add(QueryUtils.createLuceneQuery("count__attestations", "[1 TO *]"));
        }
        if (params.getFilter().contains("undated")) {
            //c = c.and(Criteria.where("b.date_from").lessThan(5000));
            expressions.add(QueryUtils.createLuceneQuery("b__dating_to", "[0 TO 5000]"));
        }

        JsonQueryRequest query = QueryUtils.createJsonQueryRequest(QueryUtils.createBooleanQuery("must", expressions));
        //Pageable p = PageRequest.of(params.getDtStart() / params.getDtLength(), params.getDtLength());
        query.setOffset(params.getDtStart());
        query.setLimit(params.getDtLength());

        if (!params.getTypes().isEmpty()) {
            //fq = new SimpleFilterQuery(Criteria.where("{!tag=fqtype}b__type").in(params.getTypes()));
            query.withFilter(QueryUtils.tagQuery("fqtype", QueryUtils.createInQuery("b__type", params.getTypes())));
        }

        Searchbox.buildJsonQuery("b__person__id", params.getPersonObjects(), "b__person__search",
            params.getPersonTerms(), false).ifPresent(x -> expressions.add(x));

        if (!params.getTypes().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqtype", QueryUtils.createInQuery("b__type", params.getTypes())));
        }
        if (!params.getTexttypes().isEmpty()) {
            query.withFilter(
                QueryUtils.tagQuery("fqtexttypes", QueryUtils.createInQuery("b__texttypes", params.getTexttypes())));
        }
        if (!params.getLanguages().isEmpty()) {
            query.withFilter(
                QueryUtils.tagQuery("fqlanguages", QueryUtils.createInQuery("b__languages", params.getLanguages())));
        }
        if (!params.getGenres().isEmpty()) {
            query.withFilter(QueryUtils.tagQuery("fqgenre", QueryUtils.createInQuery("b__genre", params.getGenres())));
        }
        if (!params.getSubgenres().isEmpty()) {
            query.withFilter(
                QueryUtils.tagQuery("fqsubgenre", QueryUtils.createInQuery("b__subgenre", params.getSubgenres())));
        }

        if (params.getFilter().contains("published")) {
            query.withFilter(QueryUtils.createLuceneQuery("b__status", "published"));
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

    public Source fromSolrDocument(SolrDocument doc) {
        DocumentObjectBinder dob = solrService.getSolrClient().getBinder();
        Source s = dob.getBean(Source.class, doc);
        if (doc.getFieldValues("b__child__editions") != null) {
            for (Object o : doc.getFieldValues("b__child__editions")) {
                SolrDocument docE = (SolrDocument) o;
                Edition e = dob.getBean(Edition.class, docE);
                if (docE.getFieldValues("e__child__reproductions") != null) {
                    for (Object objR : docE.getFieldValues("e__child__reproductions")) {
                        e.getReproductions().add(dob.getBean(Reproduction.class, (SolrDocument) objR));
                    }
                }
                s.getEditions().add(e);
            }
        }
        if (doc.getFieldValue("b__child__sigles") != null) {
            for (Object o : doc.getFieldValues("b__child__sigles")) {
                s.getSigles().add(dob.getBean(Sigle.class, (SolrDocument) o));
            }
        }
        if (doc.getFieldValue("b__child__persons") != null) {
            for (Object o : doc.getFieldValues("b__child__persons")) {
                s.getPersons().add(dob.getBean(Person.class, (SolrDocument) o));
            }
        }
        return s;
    }

    public List<Source> fromSolrResponse(QueryResponse resp) {
        ArrayList<Source> result = new ArrayList<>();
        if (resp.getResults() != null) {
            for (SolrDocument doc : resp.getResults()) {
                result.add(fromSolrDocument(doc));
            }
        }
        return result;
    }

    public QueryResponse querySolr(JsonQueryRequest jqr) {
        jqr.returnFields("*");
        jqr.returnFields("[child limit=100]");
        QueryResponse resp = solrService.querySolr(jqr);
        return resp;
    }

    @Override
    protected void enhanceWithFacetOptions(JsonQueryRequest query, int minCount, int limit) {
        query.withFacet("b__type", new TermsFacetMap("b__type").setMinCount(1).setLimit(1000)
            .withDomain(new DomainMap().withTagsToExclude("fqtype")));
        query.withFacet("b__texttypes", new TermsFacetMap("b__texttypes").setMinCount(1).setLimit(1000)
            .withDomain(new DomainMap().withTagsToExclude("fqtexttypes")));
        query.withFacet("b__languages", new TermsFacetMap("b__languages").setMinCount(1).setLimit(1000)
            .withDomain(new DomainMap().withTagsToExclude("fqlanguages")));
        query.withFacet("b__genre", new TermsFacetMap("b__genre").setMinCount(1).setLimit(1000)
            .withDomain(new DomainMap().withTagsToExclude("fqgenre")));
        query.withFacet("b__subgenre", new TermsFacetMap("b__subgenre").setMinCount(1).setLimit(1000)
            .withDomain(new DomainMap().withTagsToExclude("fqsubgenre")));

    }

    @Override
    protected Map<String, String> createColumNames2SolrFieldsMap() {
        return Map.ofEntries(
            Map.entry("id", "sorting"),
            Map.entry("type", "b__type"),
            //{ "bibliography", "{!parent which=\"doctype:bibitem\"}e.title" }
            Map.entry("bibliography", "b__edition__search"));
    }

}
