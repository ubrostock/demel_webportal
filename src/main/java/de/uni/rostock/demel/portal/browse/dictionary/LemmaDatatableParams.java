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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class LemmaDatatableParams extends DatatableParams {

    public LemmaDatatableParams(List<String> lemmaObjects, List<String> lemmaTerms,
        List<String> partOfSpeechs, List<String> prefixes) {
        super(Collections.emptyMap());
        this.lemmaObjects = lemmaObjects;
        this.lemmaTerms = lemmaTerms;
        this.partOfSpeechs = partOfSpeechs;
        this.prefixes = prefixes;
    }

    private List<String> lemmaObjects = new ArrayList<>();

    private List<String> lemmaTerms = new ArrayList<>();

    private List<String> partOfSpeechs = new ArrayList<>();

    private List<String> prefixes = new ArrayList<>();

    private List<String> types = new ArrayList<>();

    private List<String> filter = new ArrayList<>();

    public LemmaDatatableParams(Map<String, String[]> requestParams) {
        super(requestParams);

        //lemma_object -> l00000210, l00000073
        if (requestParams.containsKey("lemma_object")) {
            lemmaObjects.addAll(Arrays.asList(requestParams.get("lemma_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //lemma_term -> abc, def
        if (requestParams.containsKey("lemma_term")) {
            lemmaTerms.addAll(Arrays.asList(requestParams.get("lemma_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        if (requestParams.containsKey("part_of_speech")) {
            partOfSpeechs.addAll(Arrays.asList(requestParams.get("part_of_speech")).stream()
                .filter(x -> StringUtils.isNotEmpty(x) && !x.endsWith("_all")).toList());
        }

        if (requestParams.containsKey("prefix")) {
            if (!Arrays.asList(requestParams.get("prefix")).contains("[clear]")) {
                prefixes.addAll(Arrays.asList(requestParams.get("prefix")).stream()
                    .filter(x -> StringUtils.isNotEmpty(x)).toList());
            }
        }
        if (requestParams.containsKey("type")) {
            types.addAll(Arrays.asList(requestParams.get("type")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("filter")) {
            filter.addAll(Arrays.asList(requestParams.get("filter")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
    }

    public List<String> getLemmaObjects() {
        return lemmaObjects;
    }

    public List<String> getLemmaTerms() {
        return lemmaTerms;
    }

    public List<String> getPartOfSpeechs() {
        return partOfSpeechs;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getFilter() {
        return filter;
    }
}
