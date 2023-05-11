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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrPersonQueryService;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryFilter;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryService;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;
import de.uni.rostock.demel.portal.util.searchbox.SelectData;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SourceController extends _BaseController {

    @Autowired
    private SolrSourceQueryService solrSourceQueryService;

    @Autowired
    private SolrPersonQueryService solrPersonQueryService;

    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "/browse/bibliography", method = RequestMethod.GET)
    public ModelAndView browse(
        @RequestParam(value = "type", required = false) List<String> param_type,
        @RequestParam(value = "dating_from", required = false) String dating_from,
        @RequestParam(value = "dating_to", required = false) String dating_to,
        @RequestParam(value = "action", required = false) List<String> actions,
        @RequestParam(value = "texttype", required = false) List<String> param_texttype,
        @RequestParam(value = "languages", required = false) List<String> param_language,
        @RequestParam(value = "genre", required = false) List<String> param_genre,
        @RequestParam(value = "subgenre", required = false) List<String> param_subgenre,
        @RequestParam(value = "filter", required = false) List<String> param_filter,
        HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("browse/bibliography");

        List<String> type = param_type == null ? new ArrayList<>() : param_type;
        List<String> texttype = param_texttype == null ? new ArrayList<>() : param_texttype;
        List<String> language = param_language == null ? new ArrayList<>() : param_language;
        List<String> genre = param_genre == null ? new ArrayList<>() : param_genre;
        List<String> subgenre = param_subgenre == null ? new ArrayList<>() : param_subgenre;
        List<String> filter = param_filter == null ? new ArrayList<>() : param_filter;

        boolean isShowDeleted = request.getParameterMap().keySet().size() == 1
            && request.getParameterMap().containsKey("source_object");
        mav.addObject("showDeleted", isShowDeleted);

        Searchbox sbSigles = new Searchbox("source") {
            @Override
            public List<Source> query(List<String> objects) {
                return solrSourceQueryService.findByIds(objects);
            }
        };
        sbSigles.handle(request, mav);

        Searchbox sbEditions = new Searchbox("edition") {
            @Override
            public List<Source> query(List<String> objects) {
                return Collections.emptyList();
            }
        };
        sbEditions.handle(request, mav);

        if (actions != null) {
            for (String a : actions) {
                if (a.equals("del:type:*")) {
                    type.clear();
                }
                if (a.equals("del:dating:*")) {
                    dating_from = "";
                    dating_to = "";
                    filter.remove("undated");
                }
                if (a.equals("del:texttype_language:*")) {
                    texttype.clear();
                    language.clear();
                }
                if (a.equals("del:genre_subgenre:*")) {
                    genre.clear();
                    subgenre.clear();
                }
            }
        }

        Searchbox sbPersons = new Searchbox("person") {
            @Override
            public List<Person> query(List<String> objectIDs) {
                return solrPersonQueryService.findByIds(objectIDs);
            }
        };
        sbPersons.handle(request, mav);

        mav.addObject("type", type);
        mav.addObject("dating_from", dating_from != null ? dating_from.trim() : null);
        mav.addObject("dating_to", dating_to != null ? dating_to.trim() : null);
        mav.addObject("filter", filter);

        mav.addObject("texttype", texttype);
        mav.addObject("languages", language);
        mav.addObject("genre", genre);
        mav.addObject("subgenre", subgenre);

        return mav;
    }

    @RequestMapping(value = "/api/searchbox/source", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectSiglesByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact,
        Principal principal) {
        EnumSet<SolrSourceQueryFilter> filter = principal == null ? EnumSet.of(SolrSourceQueryFilter.STATUS_PUBLISHED)
            : EnumSet.noneOf(SolrSourceQueryFilter.class);
        return _selectSiglesByTerm(term, exact, filter);
    }

    @RequestMapping(value = "/api/searchbox/used_source", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectUsedSigleByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact,
        Principal principal) {
        EnumSet<SolrSourceQueryFilter> filter = principal == null
            ? EnumSet.of(SolrSourceQueryFilter.STATUS_PUBLISHED, SolrSourceQueryFilter.ONLY_USED)
            : EnumSet.of(SolrSourceQueryFilter.ONLY_USED);
        return _selectSiglesByTerm(term, exact, filter);
    }

    @RequestMapping(value = "/api/searchbox/sources_in_atts_editor",
        method = RequestMethod.GET,
        produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectSourcesForAttsEditorByTerm(
        @RequestParam(value = "term", defaultValue = "") String term) {
        EnumSet<SolrSourceQueryFilter> filter = EnumSet.noneOf(SolrSourceQueryFilter.class);
        return _selectSiglesByTerm(term, false, filter);
    }

    private Map<String, List<SelectData>> _selectSiglesByTerm(String term, boolean exact,
        EnumSet<SolrSourceQueryFilter> filter) {
        List<SelectData> data = new ArrayList<>();
        if (term.startsWith("id:")) {
            Matcher m = Source.DOCID_PATTERN.matcher(term.substring(3).trim());
            if (m.matches()) {
                SelectData sd = solrSourceQueryService
                    .selectSigleBySourceID(Source.convertId(Long.parseLong(m.group(1))));
                if (sd != null) {
                    data.add(sd);
                }

            }
        } else {
            if (term.length() > 0) {
                data.add(new SelectData("",
                    "<em class=\"text-muted\">" + messageSource.getMessage("demel.searchbox.apply_term",
                        new Object[] {}, LocaleContextHolder.getLocale()) + "</em>"));
            }
            data.addAll(solrSourceQueryService.selectSiglesByName(term, exact, filter));
        }
        return Collections.singletonMap("response", data);
    }

    /**
     * return empty list for searchbox
     * @param term
     * @param exact
     * @return
     */
    @RequestMapping(value = "/api/searchbox/edition", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectEdition(@RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact) {
        List<SelectData> data = new ArrayList<>();
        data.add(new SelectData("", "<em class=\"text-muted\">"
            + messageSource.getMessage("demel.searchbox.apply_term", new Object[] {}, LocaleContextHolder.getLocale())
            + "</em>"));
        return Collections.singletonMap("response", data);
    }

    @RequestMapping(value = "/api/searchbox/person", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectPerson(@RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact) {
        return _selectPersonsByTerm(term, exact);
    }

    private Map<String, List<SelectData>> _selectPersonsByTerm(String term, boolean exact) {
        List<SelectData> data = new ArrayList<>();
        if (term.startsWith("id:")) {
            Matcher m = Person.DOCID_PATTERN.matcher(term.substring(3).trim());
            if (m.matches()) {
                SelectData sd = solrPersonQueryService.selectPersonByID(Person.convertId(Long.parseLong(m.group(1))));
                if (sd != null) {
                    data.add(sd);
                }
            }
        } else {
            if (term.length() > 0) {
                data.add(new SelectData("",
                    "<em class=\"text-muted\">" + messageSource.getMessage("demel.searchbox.apply_term",
                        new Object[] {}, LocaleContextHolder.getLocale()) + "</em>"));
            }
            data.addAll(solrPersonQueryService.selectPersonsByName(term, exact));
        }
        return Collections.singletonMap("response", data);
    }

    @RequestMapping(value = "/api/infobox/source/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView bibliographyInfobox(@PathVariable(value = "id", required = true) String id,
        HttpServletRequest request) {
        Source source = solrSourceQueryService.findById(id);
        if (source == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/infobox_source");
        source.hideUndocumentedFields();
        mav.addObject("source", source);
        return mav;
    }

    @RequestMapping(value = "/api/popover/cite/source/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView popOverCite(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {
        Source source = solrSourceQueryService.findById(id);
        if (source == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/popover_cite_source");
        mav.addObject("source", source);
        return mav;
    }

    @RequestMapping(value = "/api/datatable/bibliography",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> findDatatableBibliography(HttpServletRequest request) {
        SourceDatatableParams dtParams = new SourceDatatableParams(request.getParameterMap());
        QueryResult<Source> queryResult = solrSourceQueryService.queryByDatatableParams(dtParams);
        Map<String, String> facetSearchfieldMap = Map.ofEntries(
            Map.entry("facetType", "b__type"),
            Map.entry("facetTexttypes", "b__texttypes"),
            Map.entry("facetLanguages", "b__languages"),
            Map.entry("facetGenre", "b__genre"),
            Map.entry("facetSubgenre", "b__subgenre"));
        return createDatatableResponse(dtParams, queryResult, facetSearchfieldMap);
    }

    private ModelAndView errorNotfound(String id) {
        ModelAndView mav = new ModelAndView("error", HttpStatus.NOT_FOUND);
        String msg = messageSource.getMessage("demel.biblio.error_message.404", new Object[] { id },
            LocaleContextHolder.getLocale());
        mav.addObject("error", msg);
        return mav;
    }
}
