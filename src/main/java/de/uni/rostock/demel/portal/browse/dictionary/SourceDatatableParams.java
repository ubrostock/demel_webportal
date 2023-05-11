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
public class SourceDatatableParams extends DatatableParams {
    private List<String> sourceObjects = new ArrayList<>();

    private List<String> sourceTerms = new ArrayList<>();

    private List<String> editionTerms = new ArrayList<>();

    private Integer datingFrom = 0;

    private Integer datingTo = 999999;

    private List<String> types = new ArrayList<>();

    private List<String> filter = new ArrayList<>();

    private List<String> personObjects = new ArrayList<>();

    private List<String> personTerms = new ArrayList<>();

    private List<String> texttypes = new ArrayList<>();

    private List<String> languages = new ArrayList<>();

    private List<String> genres = new ArrayList<>();

    private List<String> subgenres = new ArrayList<>();

    public SourceDatatableParams(Map<String, String[]> requestParams) {
        super(requestParams);

        if (requestParams.containsKey("source_object")) {
            sourceObjects.addAll(Arrays.asList(requestParams.get("source_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("source_term")) {
            sourceTerms.addAll(Arrays.asList(requestParams.get("source_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("edition_term")) {
            editionTerms.addAll(Arrays.asList(requestParams.get("edition_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("type")) {
            types.addAll(Arrays.asList(requestParams.get("type")).stream()
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

        if (requestParams.containsKey("filter")) {
            filter.addAll(Arrays.asList(requestParams.get("filter")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        if (requestParams.containsKey("person_object")) {
            personObjects.addAll(Arrays.asList(requestParams.get("person_object")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("person_term")) {
            personTerms.addAll(Arrays.asList(requestParams.get("person_term")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

        if (requestParams.containsKey("texttype")) {
            texttypes.addAll(Arrays.asList(requestParams.get("texttype")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("languages")) {
            languages.addAll(Arrays.asList(requestParams.get("languages")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("genre")) {
            genres.addAll(Arrays.asList(requestParams.get("genre")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }
        if (requestParams.containsKey("subgenre")) {
            subgenres.addAll(Arrays.asList(requestParams.get("subgenre")).stream()
                .filter(x -> StringUtils.isNotEmpty(x)).toList());
        }

    }

    public List<String> getSourceObjects() {
        return sourceObjects;
    }

    public List<String> getSourceTerms() {
        return sourceTerms;
    }

    public List<String> getEditionTerms() {
        return editionTerms;
    }

    public Integer getDatingFrom() {
        return datingFrom;
    }

    public Integer getDatingTo() {
        return datingTo;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getFilter() {
        return filter;
    }

    public List<String> getPersonObjects() {
        return personObjects;
    }

    public void setPersonObjects(List<String> personObjects) {
        this.personObjects = personObjects;
    }

    public List<String> getPersonTerms() {
        return personTerms;
    }

    public void setPersonTerms(List<String> personTerms) {
        this.personTerms = personTerms;
    }

    public List<String> getTexttypes() {
        return texttypes;
    }

    public void setTexttypes(List<String> texttypes) {
        this.texttypes = texttypes;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getSubgenres() {
        return subgenres;
    }

    public void setSubgenres(List<String> subgenres) {
        this.subgenres = subgenres;
    }

}
