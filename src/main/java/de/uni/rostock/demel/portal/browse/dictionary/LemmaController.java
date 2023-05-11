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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
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

import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryFilter;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryService;
import de.uni.rostock.demel.portal.util.I18NService;
import de.uni.rostock.demel.portal.util.checkboxtree.CheckboxTree;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;
import de.uni.rostock.demel.portal.util.searchbox.SelectData;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LemmaController extends _BaseController {

    @Autowired
    private I18NService i18nService;

    @Autowired
    private SolrLemmaQueryService solrLemmaQueryService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    private Environment env;

    @RequestMapping(value = "/browse/lemmas", method = RequestMethod.GET)
    public ModelAndView browse(@RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "action", required = false) List<String> action,
        @RequestParam(value = "term", required = false) String term,
        @RequestParam(value = "prefix", required = false, defaultValue = "") String prefix,
        @RequestParam(value = "part_of_speech", required = false, defaultValue = "") List<String> partOfSpeech,
        @RequestParam(value = "last_part_of_speech", required = false, defaultValue = "") String lastPartOfSpeech,
        @RequestParam(value = "filter", required = false, defaultValue = "") List<String> filter,
        @RequestParam(value = "type", required = false, defaultValue = "") List<String> lemmatype,
        HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("browse/lemmas");

        boolean isShowDeleted = request.getParameterMap().keySet().size() == 1
            && request.getParameterMap().containsKey("lemma_object");
        mav.addObject("showDeleted", isShowDeleted);

        Searchbox sbLemma = new Searchbox("lemma") {
            @Override
            public List<Lemma> query(List<String> objectIDs) {
                return solrLemmaQueryService.findByIds(objectIDs);
            }
        };
        sbLemma.handle(request, mav);

        //clear prefix selection, if a search was executed
        if (StringUtils.isNotEmpty(request.getParameter("add_lemma_term"))
            || StringUtils.isNotEmpty(request.getParameter("add_lemma_object"))) {
            prefix = "";
        } else {
            //clear search if a prefix is selected
            if (StringUtils.isNotEmpty(prefix)) {
                sbLemma.getTerms().clear();
                sbLemma.getObjects().clear();
                sbLemma.updateModelAndView(mav);
            }
        }
        if (StringUtils.isNotEmpty(request.getParameter("add_lemma_object"))) {
            partOfSpeech.clear();
        }

        if (action != null) {
            for (String a : action) {
                if (a.equals("del:type:*")) {
                    lemmatype.clear();
                }
                if (a.equals("del:part_of_speech:*")) {
                    partOfSpeech.clear();
                }
                if (a.equals("del:prefix:*")) {
                    prefix = "";
                }
            }
        }

        String sPartOfSpeech = env.getProperty("demel.checkboxtree.part-of-speech");
        CheckboxTree cbtPartOfSpeech = new CheckboxTree("cbtPartOfSpeech", sPartOfSpeech);
        cbtPartOfSpeech.initSelect(partOfSpeech, lastPartOfSpeech);
        mav.addObject("cbtPartOfSpeech", cbtPartOfSpeech);

        mav.addObject("prefix", prefix);
        mav.addObject("filter", filter == null ? Collections.emptyList() : filter);
        mav.addObject("type", lemmatype == null ? Collections.emptyList() : lemmatype);
        mav.addObject("vocabulary", i18nService.retrievePartOfSpeechAbbrMessages());
        return mav;
    }

    @RequestMapping(value = "/api/searchbox/lemma", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectLemmasByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact,
        Principal principal) {

        EnumSet<SolrLemmaQueryFilter> filter = principal == null ? EnumSet.of(SolrLemmaQueryFilter.STATUS_PUBLISHED)
            : EnumSet.noneOf(SolrLemmaQueryFilter.class);
        return _selectLemmasByTerm(term, exact, filter);
    }

    @RequestMapping(value = "/api/searchbox/used_lemma", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectUsedLemmasByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact,
        Principal principal) {
        EnumSet<SolrLemmaQueryFilter> filter = principal == null
            ? EnumSet.of(SolrLemmaQueryFilter.STATUS_PUBLISHED, SolrLemmaQueryFilter.ONLY_USED)
            : EnumSet.of(SolrLemmaQueryFilter.ONLY_USED);
        return _selectLemmasByTerm(term, exact, filter);
    }

    @RequestMapping(value = "/api/searchbox/lemmas_in_atts_editor",
        method = RequestMethod.GET,
        produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectLemmasForAttsEditorByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        Principal principal) {
        EnumSet<SolrLemmaQueryFilter> filter = EnumSet.of(SolrLemmaQueryFilter.TYPE_LEMMA);
        return _selectLemmasByTerm(term, false, filter);
    }

    private Map<String, List<SelectData>> _selectLemmasByTerm(String term, boolean exact,
        EnumSet<SolrLemmaQueryFilter> filter) {
        List<SelectData> data = new ArrayList<SelectData>();
        Map<String, String> vocabPartOfSpeech = i18nService.retrievePartOfSpeechAbbrMessages();

        List<Lemma> lemmas = new ArrayList<>();
        if (term.startsWith("id:")) {
            Matcher m = Lemma.DOCID_PATTERN.matcher(term.substring(3).trim());
            if (m.matches()) {
                Lemma l = solrLemmaQueryService.findById(Lemma.convertId(Long.parseLong(m.group(1))));
                if (l != null) {
                    lemmas.add(l);
                }
            }
        } else {
            lemmas.addAll(solrLemmaQueryService.findLemmasByNameVariant(term, exact, filter));
            if (term.length() > 0) {
                data.add(new SelectData("",
                    "<em class=\"text-muted\">" + messageSource.getMessage("demel.searchbox.apply_term",
                        new Object[] {}, LocaleContextHolder.getLocale()) + "</em>"));
            }
        }

        for (Lemma lemma : lemmas) {
            StringBuffer sb = new StringBuffer();
            sb.append("<span style=\"display:inline-block;width:1.33em\">").append(lemma.getTypeEnum().getSymbol())
                .append("</span>");
            sb.append(StringEscapeUtils.escapeHtml4(lemma.getName()));
            if (lemma.getPartOfSpeechs() != null) {
                List<String> giFiltered = lemma.getPartOfSpeechs().stream()
                    .filter(gi -> !gi.endsWith("_all"))
                    .filter(gi -> !gi.equals("undocumented"))
                    .collect(Collectors.toList());
                if (!giFiltered.isEmpty()) {
                    sb.append(" (")
                        .append(giFiltered.stream()
                            .map(gi -> vocabPartOfSpeech.containsKey(gi) ? vocabPartOfSpeech.get(gi) : "?" + gi + "?")
                            .collect(Collectors.joining(", ")))
                        .append(")");
                }
            }
            data.add(new SelectData(lemma.getId(), sb.toString()));
        }
        return Collections.singletonMap("response", data);
    }

    @RequestMapping(value = "/api/infobox/lemma/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView lemmaInfobox(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {
        Lemma lma = solrLemmaQueryService.findById(id);
        if (lma == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/infobox_lemma");
        mav.addObject("lemma", lma);
        mav.addObject("vocabulary", i18nService.retrievePartOfSpeechAbbrMessages());
        return mav;
    }

    @RequestMapping(value = "/api/popover/cite/lemma/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView popOverCite(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {
        Lemma lma = solrLemmaQueryService.findById(id);
        if (lma == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/popover_cite_lemma");
        mav.addObject("lemma", lma);
        return mav;
    }

    @RequestMapping(value = "/api/datatable/lemmas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> findDatatableLemmas(HttpServletRequest request) {
        LemmaDatatableParams dtParams = new LemmaDatatableParams(request.getParameterMap());
        QueryResult<Lemma> queryResult = solrLemmaQueryService.queryByDatatableParams(dtParams);
        Map<String, String> facetSearchfieldMap = Map.ofEntries(
            Map.entry("facetPrefix", "l__prefix"),
            Map.entry("facetGrammarInfo", "l__part_of_speech"),
            Map.entry("facetSelectedGrammarInfo", "selected_grammar_info"),
            Map.entry("facetType", "l__type"));
        return createDatatableResponse(dtParams, queryResult, facetSearchfieldMap);
    }

    private ModelAndView errorNotfound(String id) {
        ModelAndView mav = new ModelAndView("error", HttpStatus.NOT_FOUND);
        String msg = messageSource.getMessage("demel.lemmas.error_message.404", new Object[] { id },
            LocaleContextHolder.getLocale());
        mav.addObject("error", msg);
        return mav;
    }
}
