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
package de.uni.rostock.demel.portal.browse.admin;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.data.service.solr.SolrService;

@Controller
public class CharStatsController {

    @Autowired
    SolrService solrService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CharStatsController.class);

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @RequestMapping(value = "/browse/admin/charstats", method = RequestMethod.GET)
    public ModelAndView get(
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "field", required = false) String field) {
        ModelAndView mav = new ModelAndView("browse/admin/characterstatistics");

        if (StringUtils.isAnyEmpty(query, field)) {
            return mav;
        }

        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(2500);
        solrQuery.setShowDebugInfo(true);
        solrQuery.setFields("id", field);
        int start = 0;

        Map<Character, Statistics> data = new TreeMap<>();
        while (true) {
            solrQuery.setStart(start);
            QueryResponse solrResponse;
            try {
                solrResponse = solrService.querySolr(solrQuery);
                if (solrResponse.getResults().size() == 0) {
                    //nichts mehr gefunden 
                    break;
                }
                for (SolrDocument doc : solrResponse.getResults()) {
                    if (doc.getFieldValues(field) != null) {
                        String id = String.valueOf(doc.getFirstValue("id"));
                        Collection<String> values = doc.getFieldValues(field).stream().map(x -> String.valueOf(x))
                            .collect(Collectors.toList());
                        process(data, id, values);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Exception in CharStatsController", e);
            }
            start = start + solrQuery.getRows();

        }
        mav.addObject("data", data);
        mav.addObject("query", query);
        mav.addObject("field", field);
        return mav;
    }

    private void process(Map<Character, Statistics> data, String id, Collection<String> values) {
        for (String v : values) {
            for (int i = 0; i < v.length(); i++) {
                char c = v.charAt(i);
                if (c >= 65 && c <= 90) { // A-Z
                    continue;
                }
                if (c >= 97 && c <= 122) { // a-z
                    continue;
                }

                Statistics stats = data.get(c);
                if (stats == null) {
                    stats = new Statistics();
                    data.put(c, stats);
                }
                stats.add(c, id, v);

            }
        }

    }

    static class Statistics {
        private Character character;

        private Set<String> first10Values = new TreeSet<String>();

        private AtomicInteger count = new AtomicInteger();

        public Set<String> getFirst10Values() {
            return first10Values;
        }

        public int getCount() {
            return count.get();
        }

        public void add(char c, String id, String value) {
            character = c;
            count.incrementAndGet();
            if (first10Values.size() < 10) {
                first10Values.add(id.split("\\-")[0] + "|" + value);
            }
        }

        public Character getCharacter() {
            return character;
        }

        public String getUnicodeName() {
            return Character.getName(character);
        }

        public String getHex() {
            return String.format("%04X", (int) character.charValue());
        }
    }

}
