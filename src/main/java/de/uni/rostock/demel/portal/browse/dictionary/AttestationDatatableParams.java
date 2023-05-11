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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.portal.util.datatable.DatatableParams;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class AttestationDatatableParams extends DatatableParams {
    private List<String> lemmaObjects = new ArrayList<>();

    private List<String> lemmaTerms = new ArrayList<>();

    private List<String> formObjects = new ArrayList<>();

    private List<String> formTerms = new ArrayList<>();

    private List<String> sourceObjects = new ArrayList<>();

    private List<String> sourceTerms = new ArrayList<>();

    private Integer datingFrom = 0;

    private Integer datingTo = 999999;

    private List<String> types = new ArrayList<>();

    private List<String> withMultiwordexprs = new ArrayList<>();

    private List<String> filter = new ArrayList<>();

    private String mwexprTerm = "";

    private boolean searchExact = false;

    public AttestationDatatableParams(Map<String, String[]> requestParams) {
        super(requestParams);
        init(requestParams);
    }

    public AttestationDatatableParams(Map<String, String[]> requestParams, int start, int length) {
        super(requestParams);
        init(requestParams);
        this.setDtStart(start);
        this.setDtLength(length);
    }

    private void init(Map<String, String[]> requestParams) {
        //lemma_object -> l00000210, l00000073
        if (requestParams.containsKey("used_lemma_object")) {
            lemmaObjects.addAll(Arrays.asList(requestParams.get("used_lemma_object")));
        }

        //lemma_term -> abc, def
        if (requestParams.containsKey("used_lemma_term")) {
            lemmaTerms.addAll(Arrays.asList(requestParams.get("used_lemma_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //attestation_object -> f00000004, f00000790
        if (requestParams.containsKey("att_object")) {
            formObjects.addAll(Arrays.asList(requestParams.get("att_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //sigle_term -> abc, def
        if (requestParams.containsKey("att_term")) {
            formTerms.addAll(Arrays.asList(requestParams.get("att_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //source_object -> b0004, b0790
        if (requestParams.containsKey("used_source_object")) {
            sourceObjects.addAll(Arrays.asList(requestParams.get("used_source_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //sigle_term -> abc, def
        if (requestParams.containsKey("used_source_term")) {
            sourceTerms.addAll(Arrays.asList(requestParams.get("used_source_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        //dating_from -> 1250
        if (requestParams.containsKey("dating_from")) {
            datingFrom = 0;
            try {
                datingFrom = Integer.parseInt(requestParams.get("dating_from")[0].trim());
            } catch (NumberFormatException e) {
                //ignore, use default
            }
        }

        //dating_to -> 1300
        if (requestParams.containsKey("dating_to")) {
            datingTo = 999999;
            try {
                datingTo = Integer.parseInt(requestParams.get("dating_to")[0].trim());
            } catch (NumberFormatException e) {
                //ignore, use default
            }
        }

        //type -> p, s, l
        if (requestParams.containsKey("type")) {
            types.clear();
            types.addAll(
                Arrays.asList(requestParams.get("type")).stream()
                    .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        // mwxp -> true, false, null
        if (requestParams.containsKey("mwexpr")) {
            withMultiwordexprs.clear();
            withMultiwordexprs.addAll(Arrays.asList(requestParams.get("mwexpr")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("mwexpr_term")) {
            mwexprTerm = Arrays.asList(requestParams.get("mwexpr_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).findFirst().orElse("");
        }

        if (requestParams.containsKey("filter")) {
            filter.clear();
            filter.addAll(Arrays.asList(requestParams.get("filter")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("search_exact")) {
            searchExact = Boolean.parseBoolean(requestParams.get("search_exact")[0]);
        }
    }

    public List<String> getLemmaObjects() {
        return lemmaObjects;
    }

    public void setLemmaObjects(List<String> lemmaObjects) {
        this.lemmaObjects = lemmaObjects;
    }

    public List<String> getLemmaTerms() {
        return lemmaTerms;
    }

    public void setLemmaTerms(List<String> lemmaTerms) {
        this.lemmaTerms = lemmaTerms;
    }

    public List<String> getSourceObjects() {
        return sourceObjects;
    }

    public void setSourceObjects(List<String> sourceObjects) {
        this.sourceObjects = sourceObjects;
    }

    public List<String> getSourceTerms() {
        return sourceTerms;
    }

    public void setSourceTerms(List<String> sourceTerms) {
        this.sourceTerms = sourceTerms;
    }

    public Integer getDatingFrom() {
        return datingFrom;
    }

    public void setDatingFrom(Integer datingFrom) {
        this.datingFrom = datingFrom;
    }

    public Integer getDatingTo() {
        return datingTo;
    }

    public void setDatingTo(Integer datingTo) {
        this.datingTo = datingTo;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getWithMultiwordexprs() {
        return withMultiwordexprs;
    }

    public void setWithMultiwordexprs(List<String> withMultiwordexprs) {
        this.withMultiwordexprs = withMultiwordexprs;
    }

    public String getMwexprTerm() {
        return mwexprTerm;
    }

    public void setMwexprTerm(String mwexprTerm) {
        this.mwexprTerm = mwexprTerm;
    }

    public List<String> getFormObjects() {
        return formObjects;
    }

    public void setFormObjects(List<String> formObjects) {
        this.formObjects = formObjects;
    }

    public List<String> getFormTerms() {
        return formTerms;
    }

    public void setFormTerms(List<String> formTerms) {
        this.formTerms = formTerms;
    }

    public boolean isSearchExact() {
        return searchExact;
    }

    public void setSearchExact(boolean searchExact) {
        this.searchExact = searchExact;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }
}
