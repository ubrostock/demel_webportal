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
package de.uni.rostock.demel.portal.edit.model;

import java.util.ArrayList;
import java.util.List;

import de.uni.rostock.demel.SuppressFBWarnings;
import jakarta.validation.constraints.Size;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class LemmaEditObject extends AbstractEditObject {

    private static final String OBJECT_TYPE = "lemma";

    private String id;

    @Size(max = 256, message = "demel.lemma_editor.error.name.size")
    private String name;

    @Size(max = 512, message = "demel.lemma_editor.error.name_variants.size")
    private String nameVariants;

    @Size(max = 1024, message = "demel.lemma_editor.error.hints_intern.size")
    private String hintsIntern;

    @Size(max = 1024, message = "demel.lemma_editor.error.hints_extern.size")
    private String hintsExtern;

    private List<String> partOfSpeech = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    public String getHintsExtern() {
        return hintsExtern;
    }

    public void setHintsExtern(String hintsExtern) {
        this.hintsExtern = hintsExtern;
    }

    public List<String> getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getNameVariants() {
        return nameVariants;
    }

    public void setNameVariants(String nameVariants) {
        this.nameVariants = nameVariants;
    }

    public void setPartOfSpeech(List<String> partOfSpeech) {
        this.partOfSpeech.clear();
        this.partOfSpeech.addAll(partOfSpeech);
    }

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }
}
