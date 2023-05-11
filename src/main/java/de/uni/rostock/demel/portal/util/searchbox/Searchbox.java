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
package de.uni.rostock.demel.portal.util.searchbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.service.solr.util.QueryUtils;
import jakarta.servlet.http.HttpServletRequest;

@SuppressFBWarnings("EI_EXPOSE_REP")
public abstract class Searchbox {
    private String name;
    private List<String> objects = new ArrayList<>();
    private List<String> terms = new ArrayList<>();

    public Searchbox(String name) {
        this.name = name;
    }

    public void handle(HttpServletRequest request, ModelAndView mav) {
        objects.clear();
        if (request.getParameterValues(name + "_object") != null) {
            objects.addAll(Arrays.asList(request.getParameterValues(name + "_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).collect(Collectors.toList()));
        }
        String addObject = request.getParameter("add_" + name + "_object");
        if (StringUtils.isNotEmpty(addObject) && !objects.contains(addObject)) {
            objects.add(addObject.trim());
        }

        terms.clear();
        if (request.getParameterValues(name + "_term") != null) {
            terms.addAll(Arrays.asList(request.getParameterValues(name + "_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).map(x -> x.trim()).collect(Collectors.toList()));
        }
        String addTerm = request.getParameter("add_" + name + "_term");
        if (StringUtils.isNotEmpty(addTerm) && !terms.contains(addTerm)) {
            terms.add(addTerm.trim());
        }

        List<String> action = request.getParameterValues("action") == null ? Collections.emptyList()
            : Arrays.asList(request.getParameterValues("action"));
        if (action != null) {
            for (String a : action) {
                if (a.startsWith("del:" + name + ":*")) {
                    terms.clear();
                    objects.clear();
                }
                if (a.startsWith("del:" + name + "_term:")) {
                    String term = a.substring(("del:" + name + "_term:").length());
                    terms.remove(term);
                }
                if (a.startsWith("del:" + name + "_object:")) {
                    String objectID = a.substring(("del:" + name + "_object:").length());
                    objects.remove(objectID);
                }
            }
        }
        updateModelAndView(mav);
    }

    public void updateModelAndView(ModelAndView mav) {
        mav.getModelMap().remove(name + "_objects");
        mav.getModelMap().remove(name + "_terms");
        mav.addObject(name + "_objects", query(objects));
        mav.addObject(name + "_terms", terms);
    }

    public abstract List<? extends SearchboxItem> query(List<String> objects);

    public List<String> getObjects() {
        return objects;
    }

    public List<String> getTerms() {
        return terms;
    }

    // Spring Data SOLR implementation for reference */
    /*
    public static Optional<Criteria> buildCriteria(String solrField4Object, List<String> objects, String solrField4Term,
        List<String> terms, boolean fulltextSearchMode) {
        Criteria c = null;
        if (objects.size() + terms.size() > 0) {
            if (!objects.isEmpty()) {
                c = Criteria.where(solrField4Object).in(objects);
            }
            if (!terms.isEmpty()) {
                if (c == null) {
                    c = Criteria.where(solrField4Term).expression(QueryUtils.modifyTerm(terms.get(0), fulltextSearchMode));
                } else {
                    c = c.or(solrField4Term).expression(QueryUtils.modifyTerm(terms.get(0), fulltextSearchMode));
                }
                for (int i = 1; i < terms.size(); i++) {
                    c = c.or(solrField4Term).expression(QueryUtils.modifyTerm(terms.get(i), fulltextSearchMode));
                }
            }
        }
        return Optional.ofNullable(c);
    }
    */

    public static Optional<Map<String, Object>> buildJsonQuery(String solrField4Object, List<String> objects,
        String solrField4Term,
        List<String> terms, boolean fulltextSearchMode) {
        if (objects.size() + terms.size() > 0) {
            List<Map<String, Object>> expressions = new ArrayList<>();
            for (String obj : objects) {
                expressions.add(Map.ofEntries(
                    Map.entry("lucene", Map.ofEntries(Map.entry("df", solrField4Object), Map.entry("query", obj)))));
            }
            for (String term : terms) {
                if (!StringUtils.isBlank(term)) {
                    expressions.add(Map.ofEntries(Map.entry("lucene", Map.ofEntries(Map.entry("df", solrField4Term),
                        Map.entry("query", QueryUtils.modifyTerm(term.trim(), fulltextSearchMode))))));
                }
            }
            return Optional.of(Map.ofEntries(Map.entry("bool", Map.ofEntries(Map.entry("should", expressions)))));
        }
        return Optional.empty();
    }
}
