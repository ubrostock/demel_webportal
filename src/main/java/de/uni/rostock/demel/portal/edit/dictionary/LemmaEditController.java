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
package de.uni.rostock.demel.portal.edit.dictionary;

import java.security.Principal;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Lemma.LemmaType;
import de.uni.rostock.demel.data.service.db.LemmaDBService;
import de.uni.rostock.demel.data.service.db.MessageDBService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaUpdateService;
import de.uni.rostock.demel.portal.edit.model.LemmaEditObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class LemmaEditController {

    public enum ViewArea {
        NAME, NAME_VARIANTS, PART_OF_SPEECH, HINTS_EXTERN, HINTS_INTERN;
    }

    private static final String MODEL_OBJECT_NAME = "lemma_edit";

    @Autowired
    private LemmaDBService lemmaDBService;

    @Autowired
    private SolrLemmaQueryService lemmaSolrService;

    @Autowired
    private SolrLemmaUpdateService lemmaSolrUpdateService;

    @Autowired
    private MessageDBService messageDBService;

    @RequestMapping(value = "/edit/lemma/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ModelAndView editAttestation(@PathVariable String id,
        @RequestParam(value = "return_url", required = false, defaultValue = "") String returnUrl,
        HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("edit/edit_lemma");
        LemmaEditObject lemmaEdit = buildEditObject(lemmaDBService.queryObject(id));
        mav.addObject(MODEL_OBJECT_NAME, lemmaEdit);
        Lemma lemmaOrig = lemmaSolrService.findById(lemmaEdit.getId());
        mav.addObject("lemma_orig", lemmaOrig);
        mav.addObject("view_area",
            calculateViewAreas(lemmaOrig).stream().map(x -> x.name()).collect(Collectors.toSet()));
        mav.addObject("return_url", returnUrl);
        mav.addObject("revision_message", "");
        return mav;
    }

    /*
     * ATTENTION: BindingResult parameter must immediately follow ModelAttribute !!!!
     */
    @RequestMapping(value = "/edit/lemma", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ModelAndView saveLemma(
        @RequestParam(value = "return_url", required = false, defaultValue = "") String returnUrl,
        @RequestParam(value = "revision_message", required = false, defaultValue = "") String revisionMessage,
        @Valid @ModelAttribute(MODEL_OBJECT_NAME) LemmaEditObject lemmaEdit, BindingResult bindingResult,
        HttpServletRequest request, Principal principal) {
        Lemma lemmaOrig = lemmaSolrService.findById(lemmaEdit.getId());
        normalize(lemmaEdit, lemmaOrig);
        validate(lemmaEdit, lemmaOrig, bindingResult);
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("edit/edit_lemma");
            mav.addObject("lemma_orig", lemmaOrig);
            mav.addObject(MODEL_OBJECT_NAME, lemmaEdit);
            mav.addObject("view_area",
                calculateViewAreas(lemmaOrig).stream().map(x -> x.name()).collect(Collectors.toSet()));
            mav.addObject("revision_message", revisionMessage);
            return mav;
        } else {
            lemmaDBService.updateObject(lemmaEdit, principal.getName());
            messageDBService.createRevisionMessage(lemmaEdit.getId(), revisionMessage);

            Lemma l = lemmaDBService.queryObject(lemmaEdit.getId());
            lemmaSolrUpdateService.saveObject(l);
            if (StringUtils.isBlank(returnUrl)) {
                return new ModelAndView("redirect:/browse/lemmas?lemma_object=" + lemmaEdit.getId());
            } else {
                return new ModelAndView("redirect:" + returnUrl);
            }
        }
    }

    private LemmaEditObject buildEditObject(Lemma l) {
        LemmaEditObject lEdit = new LemmaEditObject();
        lEdit.setId(l.getId());
        lEdit.setName(l.getName());
        lEdit.setNameVariants(l.getNameVariants() == null ? "" : String.join("|", l.getNameVariants()));
        if (l.getPartOfSpeechs() != null) {
            lEdit.getPartOfSpeech().addAll(l.getPartOfSpeechs());
        }
        lEdit.setHintsExtern(l.getHintsExtern());
        lEdit.setHintsIntern(l.getHintsIntern());
        return lEdit;
    }

    private EnumSet<ViewArea> calculateViewAreas(Lemma lemmaOrig) {
        EnumSet<ViewArea> viewArea = EnumSet.noneOf(ViewArea.class);
        viewArea.add(ViewArea.NAME);
        viewArea.add(ViewArea.HINTS_EXTERN);
        viewArea.add(ViewArea.HINTS_INTERN);
        if (lemmaOrig.getTypeEnum() == LemmaType.LEMMA) {
            viewArea.add(ViewArea.NAME_VARIANTS);
            viewArea.add(ViewArea.PART_OF_SPEECH);
        }
        if (lemmaOrig.getTypeEnum() == LemmaType.LINKLEMMA) {
            viewArea.add(ViewArea.NAME_VARIANTS);
        }
        return viewArea;
    }

    private void normalize(LemmaEditObject lemmaEdit, Lemma lemmaOrig) {
        if (StringUtils.isBlank(lemmaEdit.getName())) {
            lemmaEdit.setName("");
        } else {
            lemmaEdit.setName(lemmaEdit.getName().trim());
        }
        if (StringUtils.isBlank(lemmaEdit.getNameVariants())) {
            lemmaEdit.setNameVariants("");
        } else {
            lemmaEdit.setNameVariants(lemmaEdit.getNameVariants().trim());
        }
        if (StringUtils.isBlank(lemmaEdit.getHintsExtern())) {
            lemmaEdit.setHintsExtern(null);
        } else {
            lemmaEdit.setHintsExtern(lemmaEdit.getHintsExtern().trim());
        }
        if (StringUtils.isBlank(lemmaEdit.getHintsIntern())) {
            lemmaEdit.setHintsIntern(null);
        } else {
            lemmaEdit.setHintsIntern(lemmaEdit.getHintsIntern().trim());
        }
    }

    private void validate(LemmaEditObject lemmaEdit, Lemma lemmaOrig, BindingResult binding) {
        if (lemmaEdit.getId() == null) {
            binding.addError(new FieldError(MODEL_OBJECT_NAME, "id", "demel.lemma_editor.error.id_unknown"));
            return;
        } else {
            Lemma l = lemmaDBService.queryObject(lemmaEdit.getId());
            if (l == null) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "id", "demel.lemma_editor.error.id_unknown"));
            }
            if (StringUtils.isBlank(lemmaEdit.getName())) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "name", "demel.lemma_editor.error.name.empty"));
            }
            if (lemmaOrig.getTypeEnum() == Lemma.LemmaType.LEMMA) {
                if (lemmaEdit.getPartOfSpeech().size() == 0) {
                    binding.addError(new FieldError(MODEL_OBJECT_NAME, "partOfSpeech",
                        "demel.lemma_editor.error.part_of_speech.empty"));
                }
            }
        }
    }
}
