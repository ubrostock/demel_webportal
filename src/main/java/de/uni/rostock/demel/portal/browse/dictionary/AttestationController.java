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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
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

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrAttestationQueryService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryService;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryService;
import de.uni.rostock.demel.portal.util.I18NService;
import de.uni.rostock.demel.portal.util.searchbox.Searchbox;
import de.uni.rostock.demel.portal.util.searchbox.SelectData;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AttestationController extends _BaseController {

    @Autowired
    private I18NService i18nService;

    @Autowired
    private SolrSourceQueryService solrSourceQS;

    @Autowired
    private SolrLemmaQueryService solrLemmaQS;

    @Autowired
    private SolrAttestationQueryService solrAttestationQS;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/browse/attestations", method = RequestMethod.GET)
    public ModelAndView browse(@RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "search_exact", required = false, defaultValue = "false") boolean searchExact,
        @RequestParam(value = "type", required = false) List<String> type,
        @RequestParam(value = "mwexpr", required = false) List<String> mwexpr,
        @RequestParam(value = "mwexpr_term", required = false) String mwexprTerm,
        @RequestParam(value = "action", required = false) List<String> action,
        @RequestParam(value = "dating_from", required = false, defaultValue = "") String datingFrom,
        @RequestParam(value = "dating_to", required = false, defaultValue = "") String datingTo,
        @RequestParam(value = "start", required = false, defaultValue = "") List<String> start,
        @RequestParam(value = "part_of_speech", required = false, defaultValue = "") List<String> partOfSpeech,
        @RequestParam(value = "filter", required = false) List<String> filter,
        @RequestParam(value = "size", required = false, defaultValue = "100") int size,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        HttpServletRequest request) {

        ModelAndView mav = new ModelAndView("browse/attestations");
        mav.addObject("search_exact", searchExact);

        boolean isShowDeleted = request.getParameterMap().keySet().size() == 1
            && request.getParameterMap().containsKey("att_object");
        mav.addObject("showDeleted", isShowDeleted);

        Searchbox sbLemma = new Searchbox("used_lemma") {
            @Override
            public List<Lemma> query(List<String> objectIDs) {
                return solrLemmaQS.findByIds(objectIDs);
            }
        };
        sbLemma.handle(request, mav);

        Searchbox sbForms = new Searchbox("att") {
            @Override
            public List<Attestation> query(List<String> objectIDs) {
                return solrAttestationQS.findByIds(objectIDs);
            }
        };
        sbForms.handle(request, mav);

        Searchbox sbSources = new Searchbox("used_source") {
            @Override
            public List<Source> query(List<String> objectIDs) {
                return solrSourceQS.findByIds(objectIDs);
            }
        };
        sbSources.handle(request, mav);

        if (type == null) {
            type = new ArrayList<String>();
        }
        mav.addObject("type", type);

        if (mwexpr == null) {
            mwexpr = new ArrayList<String>();
        }
        mav.addObject("mwexpr", mwexpr);

        if (filter == null) {
            filter = new ArrayList<String>();
        }
        mav.addObject("filter", filter);

        if (action != null) {
            for (String a : action) {
                if (a.equals("del:type:*")) {
                    type.clear();
                }
                if (a.equals("del:mwexpr:*")) {
                    mwexpr.clear();
                    mwexprTerm = "";
                }
                if (a.equals("del:dating:*")) {
                    datingFrom = "";
                    datingTo = "";
                    filter.remove("undated");
                }
            }
        }

        if (datingFrom != null) {
            mav.addObject("dating_from", datingFrom.trim());
        }
        if (datingTo != null) {
            mav.addObject("dating_to", datingTo.trim());
        }

        if (mwexprTerm == null) {
            mwexprTerm = "";
        }
        mav.addObject("mwexpr_term", mwexprTerm);

        mav.addObject("vocabulary", i18nService.retrievePartOfSpeechAbbrMessages());
        return mav;
    }

    @RequestMapping(value = "/api/infobox/attestation/{id}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public String attestationInfobox(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {
        //not specified - return empty string
        return "";
    }

    @RequestMapping(value = "/api/popover/cite/attestation/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView popOverCite(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {
        Attestation a = solrAttestationQS.findById(id);
        if (a == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/popover_cite_attestation");
        mav.addObject("attestation", a);
        return mav;
    }

    @RequestMapping(value = "/api/datable/attestations",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> findAttestations(HttpServletRequest request) {
        AttestationDatatableParams dtParams = new AttestationDatatableParams(request.getParameterMap());
        QueryResult<Attestation> queryResult = solrAttestationQS.queryByDatatableParams(dtParams);
        Map<String, String> facetSearchfieldMap = Map.ofEntries(
            Map.entry("facetType", "d__type"),
            Map.entry("facetMultiwordexpr", "d__is_multiwordexpr"));
        return createDatatableResponse(dtParams, queryResult, facetSearchfieldMap);
    }

    @RequestMapping(value = "/api/searchbox/att", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<SelectData>> selectAttestationByTerm(
        @RequestParam(value = "term", defaultValue = "") String term,
        @RequestParam(value = "exact", defaultValue = "false") boolean exact) {
        List<SelectData> data = new ArrayList<SelectData>();

        if (term.startsWith("id:")) {
            Matcher m = Attestation.DOCID_PATTERN.matcher(term.substring(3).trim());
            if (m.matches()) {
                Attestation a = solrAttestationQS.findById(Attestation.convertId(Long.parseLong(m.group(1))));
                if (a != null) {
                    if (a.getForm() == null) {
                        data.add(new SelectData(a.getId(), messageSource
                            .getMessage("demel.attestations.table.form.null", null, LocaleContextHolder.getLocale()))); //[nicht erfasst]
                    } else if ("".equals(a.getForm())) {
                        data.add(new SelectData(a.getId(), messageSource
                            .getMessage("demel.attestations.table.form.none", null, LocaleContextHolder.getLocale()))); //[ohne]
                    } else {
                        data.add(new SelectData(a.getId(), StringEscapeUtils
                            .escapeHtml4(StringUtils.defaultIfEmpty(a.getForm(), a.getLemmalinkName()))));
                    }
                }
            }
        } else {
            if (term.length() > 0) {
                data.add(new SelectData("",
                    "<em class=\"text-muted\">" + messageSource.getMessage("demel.searchbox.apply_term",
                        new Object[] {}, LocaleContextHolder.getLocale()) + "</em>"));
            }
        }
        return Collections.singletonMap("response", data);
    }

    private ModelAndView errorNotfound(String id) {
        ModelAndView mav = new ModelAndView("error", HttpStatus.NOT_FOUND);
        String msg = messageSource.getMessage("demel.attestations.error_message.404", new Object[] { id },
            LocaleContextHolder.getLocale());
        mav.addObject("error", msg);
        return mav;
    }
}
