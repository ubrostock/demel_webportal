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
package de.uni.rostock.demel.data.service.solr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.util.ClientUtils;

public class QueryUtils {
    public static String modifyTerm(String term, boolean fulltextSearchMode) {
        String s = term.trim();
        if (fulltextSearchMode) {
            if (s.contains("\"") || (s.startsWith("(") && s.endsWith(")"))) {
                return s;
            }
            if (s.contains(" ")) {
                return Arrays.asList(s.split("\\s+"))
                    .stream()
                    .map(x -> modifyTerm(x))
                    .collect(Collectors.joining(" AND ", "(", ")"));
            }
        }
        //return ClientUtils.escapeQueryChars(s);
        return modifyTerm(s);
    }

    public static String modifyTerm(String s) {
        String result = ClientUtils.escapeQueryChars(s);
        result = result.replace("\\*", "*").replace("\\?", "?").replace("\\~", "~").replace("\\-", "-");
        return result;
    }

    public static Map<String, Object> createLuceneQuery(String df, String query) {
        return Map.of("lucene",
            Map.ofEntries(Map.entry("df", df), Map.entry("query", query)));
    }

    public static Map<String, Object> createRangeQuery(String field, long lower, long upper) {
        Map<String, Object> frange = new HashMap<>();
        frange.put("query", field);
        if (lower == Long.MIN_VALUE) {
            //do not set the 'l' field
            //frange.put("l", "*"); --- leads to parsing error
        } else {
            frange.put("l", lower);
        }
        if (upper == Long.MAX_VALUE) {
            //do not set the 'u' field
            //frange.put("u", "*"); --- leads to parsing error
        } else {
            frange.put("u", upper);
        }
        return Map.ofEntries(Map.entry("frange", frange));
    }

    public static Map<String, Object> createInQuery(String field, List<String> values) {
        List<Map<String, Object>> expressions = new ArrayList<>();
        for (String v : values) {
            expressions.add(Map.ofEntries(
                Map.entry("lucene", Map.ofEntries(Map.entry("df", field), Map.entry("query", v)))));
        }
        return Map.ofEntries(Map.entry("bool", Map.ofEntries((Map.entry("should", expressions)))));
    }

    public static Map<String, Object> tagQuery(String tag, Map<String, Object> query) {
        return Map.ofEntries(Map.entry(StringUtils.prependIfMissing(tag, "#"), query));
    }

    public static final String BOOLEAN_QUERY_OPERATOR_MUST = "must";

    public static final String BOOLEAN_QUERY_OPERATOR_SHOULD = "should";

    public static Map<String, Object> createBooleanQuery(String operator, List<Map<String, Object>> expressions) {
        return Map.ofEntries(Map.entry("bool", Map.ofEntries(Map.entry(operator, expressions))));
    }

    public static JsonQueryRequest createJsonQueryRequest(Map<String, Object> query) {
        final JsonQueryRequest queryR = new JsonQueryRequest();
        queryR.setQuery(query);
        return queryR;
    }
}
