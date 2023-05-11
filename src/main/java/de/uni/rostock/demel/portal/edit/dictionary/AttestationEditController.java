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
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Attestation.AttestationType;
import de.uni.rostock.demel.data.model.dictionary.Attestation.DatingOrigin;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.model.dictionary.Source.SourceType;
import de.uni.rostock.demel.data.service.db.AttestationDBService;
import de.uni.rostock.demel.data.service.db.LemmaDBService;
import de.uni.rostock.demel.data.service.db.MessageDBService;
import de.uni.rostock.demel.data.service.db.SourceDBService;
import de.uni.rostock.demel.data.service.solr.SolrAttestationQueryService;
import de.uni.rostock.demel.data.service.solr.SolrAttestationUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryService;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryService;
import de.uni.rostock.demel.portal.edit.model.AttestationEditObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class AttestationEditController {

    public enum ViewArea {
        FORM, LEMMA, SOURCE, DATING, MULTIWORDEXPR, LEMMALINK, HINTS_INTERN;
    }

    private static final String MODEL_OBJECT_NAME = "att_edit";

    @Autowired
    private AttestationDBService attestationDBService;

    @Autowired
    private LemmaDBService lemmaDBService;

    @Autowired
    private SourceDBService sourceDBService;

    @Autowired
    private SolrAttestationQueryService attestationSolrService;

    @Autowired
    private SolrSourceQueryService sourceSolrService;

    @Autowired
    private SolrLemmaQueryService lemmaSolrService;

    @Autowired
    private SolrAttestationUpdateService attestationSolrUpdateService;

    @Autowired
    private MessageDBService messageDBService;

    @InitBinder
    public void configureModelAttributeBinding(WebDataBinder binder) {
        // tell spring to convert empty values to NULL
        // binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @RequestMapping(value = "/edit/attestations/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ModelAndView editAttestation(@PathVariable String id,
        @RequestParam(value = "return_url", required = false, defaultValue = "") String returnUrl,
        HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("edit/edit_attestations");
        AttestationEditObject attEdit = buildEditObject(attestationDBService.queryObject(id));
        mav.addObject(MODEL_OBJECT_NAME, attEdit);
        Attestation attOrig = attestationSolrService.findById(attEdit.getId());
        mav.addObject("att_orig", attOrig);
        initModel(mav.getModelMap(), attEdit, attOrig);
        mav.addObject("return_url", returnUrl);
        mav.addObject("revision_message", "");
        return mav;
    }

    /*
     * ATTENTION: BindingResult parameter must immediately follow ModelAttribute !!!!
     */
    @RequestMapping(value = "/edit/attestation", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ModelAndView saveAttestation(
        @RequestParam(value = "return_url", required = false, defaultValue = "") String returnUrl,
        @Valid @ModelAttribute(MODEL_OBJECT_NAME) AttestationEditObject attEdit, BindingResult bindingResult,
        @RequestParam(value = "revision_message", required = false, defaultValue = "") String revisionMessage,
        HttpServletRequest request, Principal principal) {
        Attestation attOrig = attestationSolrService.findById(attEdit.getId());
        normalize(attEdit, attOrig);
        revisionMessage = StringUtils.trim(revisionMessage);
        validate(attEdit, attOrig, bindingResult);
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("edit/edit_attestations");
            mav.addObject("att_orig", attOrig);
            mav.addObject(MODEL_OBJECT_NAME, attEdit);
            initModel(mav.getModelMap(), attEdit, attOrig);
            mav.addObject("revision_message", revisionMessage);
            return mav;
        } else {
            attestationDBService.updateObject(attEdit, principal.getName());
            messageDBService.createRevisionMessage(attEdit.getId(), revisionMessage);

            Attestation a = attestationDBService.queryObject(attEdit.getId());
            attestationSolrUpdateService.saveObject(a);
            if ("".equals(returnUrl)) {
                return new ModelAndView("redirect:/browse/attestations?att_object=" + attEdit.getId());
            } else {
                return new ModelAndView("redirect:" + returnUrl);
            }
        }
    }

    private void initModel(ModelMap model, AttestationEditObject attEdit, Attestation attOrig) {
        if (attOrig.getSourceId() != null) {
            model.addAttribute("source_html_orig", sourceSolrService.findById(attOrig.getSourceId()).getHtmlDisplay());
        }
        if (attOrig.getLemmaId() != null) {
            model.addAttribute("lemma_html_orig", lemmaSolrService.findById(attOrig.getLemmaId()).getHtmlDisplay());
        }
        if (attOrig.getLemmalinkId() != null) {
            model.addAttribute("lemmalink_html_orig",
                lemmaSolrService.findById(attOrig.getLemmalinkId()).getHtmlDisplay());
        }

        if (attEdit.getSourceId() != null) {
            model.addAttribute("source_html_edit", sourceSolrService.findById(attEdit.getSourceId()).getHtmlDisplay());
        }
        if (attEdit.getLemmaId() != null) {
            model.addAttribute("lemma_html_edit", lemmaSolrService.findById(attEdit.getLemmaId()).getHtmlDisplay());
        }
        if (attEdit.getLemmalinkId() != null) {
            model.addAttribute("lemmalink_html_edit",
                lemmaSolrService.findById(attEdit.getLemmalinkId()).getHtmlDisplay());
        }
        model.addAttribute("view_area",
            calculateViewAreas(attOrig).stream().map(x -> x.name()).collect(Collectors.toSet()));
    }

    private EnumSet<ViewArea> calculateViewAreas(Attestation attOrig) {
        EnumSet<ViewArea> viewArea = EnumSet.allOf(ViewArea.class);
        if (attOrig.getTypeEnum() == AttestationType.SECONDARY || attOrig.getTypeEnum() == AttestationType.LEMMALINK) {
            viewArea.remove(ViewArea.FORM);
        }
        if (attOrig.getTypeEnum() == AttestationType.SECONDARY
            || (attOrig.getTypeEnum() == AttestationType.LEMMALINK
                && attOrig.getSourceTypeEnum() == SourceType.SECONDARY)) {
            viewArea.remove(ViewArea.MULTIWORDEXPR);
            viewArea.remove(ViewArea.DATING);
        }
        if (attOrig.getDatingOriginEnum() == DatingOrigin.PRIMARY_SOURCE
            || attOrig.getDatingOriginEnum() == DatingOrigin.SECONDARY_SOURCE) {
            viewArea.remove(ViewArea.DATING);
        }
        if (attOrig.getTypeEnum() != AttestationType.LEMMALINK) {
            viewArea.remove(ViewArea.LEMMALINK);
        }
        return viewArea;
    }

    private AttestationEditObject buildEditObject(Attestation a) {
        AttestationEditObject aEdit = new AttestationEditObject();
        aEdit.setId(a.getId());
        aEdit.setForm(a.getForm());
        aEdit.setLemmaId(a.getLemmaId());
        aEdit.setMultiwordexpr(a.getMultiwordexpr());
        aEdit.setDatingDisplay(a.getDatingDisplay());
        aEdit.setDatingFrom(a.getDatingFrom());
        aEdit.setDatingTo(a.getDatingTo());
        aEdit.setSourceId(a.getSourceId());
        aEdit.setLemmalinkId(a.getLemmalinkId());
        aEdit.setHintsIntern(a.getHintsIntern());
        return aEdit;
    }

    private void normalize(AttestationEditObject attEdit, Attestation attOrig) {
        if (attOrig.getTypeEnum() == AttestationType.SECONDARY || attOrig.getTypeEnum() == AttestationType.LEMMALINK) {
            attEdit.setForm(null);
        } else {
            attEdit.setForm(StringUtils.trim(attEdit.getForm()));
        }
        if (attOrig.getTypeEnum() == AttestationType.SECONDARY
            || (attOrig.getTypeEnum() == AttestationType.LEMMALINK
                && attOrig.getSourceTypeEnum() == SourceType.SECONDARY)) {
            attEdit.setMultiwordexpr(null);
        } else {
            attEdit.setMultiwordexpr(StringUtils.trim(attEdit.getMultiwordexpr()));
        }

        if (attOrig.getDatingOriginEnum() == DatingOrigin.SCAN
            || attOrig.getDatingOriginEnum() == DatingOrigin.UNDOCUMENTED) {
            if (attOrig.getDatingOriginEnum() == DatingOrigin.UNDOCUMENTED
                && StringUtils.isBlank(attEdit.getDatingDisplay())) {
                attEdit.setDatingDisplay(null);
                attEdit.setDatingFrom(null);
                attEdit.setDatingTo(null);
            } else {
                attEdit.setDatingDisplay(StringUtils.trim(attEdit.getDatingDisplay()));
            }
        } else {
            attEdit.setDatingDisplay(null);
            attEdit.setDatingFrom(null);
            attEdit.setDatingTo(null);
        }

        if (attOrig.getTypeEnum() != AttestationType.LEMMALINK) {
            attEdit.setLemmalinkId(null);
        }
        if (StringUtils.isBlank(attEdit.getHintsIntern())) {
            attEdit.setHintsIntern(null);
        } else {
            attEdit.setHintsIntern(StringUtils.trim(attEdit.getHintsIntern()));
        }
    }

    private void validate(AttestationEditObject attEdit, Attestation attOrig, BindingResult binding) {
        if (attEdit.getLemmaId() != null) {
            Lemma l = lemmaDBService.queryObject(attEdit.getLemmaId());
            if (l == null) {
                binding.addError(
                    new FieldError(MODEL_OBJECT_NAME, "lemmaId", "demel.attestation_editor.error.lemma_id_unknown"));
            }
        } else {
            binding.addError(
                new FieldError(MODEL_OBJECT_NAME, "lemmaId", "demel.attestation_editor.error.lemma_id_unknown"));
        }
        if (attOrig.getDatingOriginEnum() == DatingOrigin.SCAN) {
            if (StringUtils.isEmpty(attEdit.getDatingDisplay())) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "datingDisplay",
                    "demel.attestation_editor.error.dating_display.notempty"));
            }
            if (attEdit.getDatingFrom() == null) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "datingFrom",
                    "demel.attestation_editor.error.dating_from.range"));
            } else {
                if ("".equals(attEdit.getDatingDisplay()) && attEdit.getDatingFrom() != 9999) {
                    binding.addError(new FieldError(MODEL_OBJECT_NAME, "datingFrom",
                        "demel.attestation_editor.error.dating_from.9999"));
                }
            }
            if (attEdit.getDatingTo() == null) {
                binding.addError(
                    new FieldError(MODEL_OBJECT_NAME, "datingTo", "demel.attestation_editor.error.dating_to.range"));
            } else {
                if ("".equals(attEdit.getDatingDisplay()) && attEdit.getDatingTo() != 9999) {
                    binding.addError(
                        new FieldError(MODEL_OBJECT_NAME, "datingTo", "demel.attestation_editor.error.dating_to.9999"));
                }
            }
        }

        if (attEdit.getSourceId() != null) {
            Source s = sourceDBService.querySource(attEdit.getSourceId());
            if (s == null) {
                binding.addError(
                    new FieldError(MODEL_OBJECT_NAME, "sourceId", "demel.attestation_editor.error.source_id_unknown"));
            }
        } else {
            binding.addError(
                new FieldError(MODEL_OBJECT_NAME, "sourceId", "demel.attestation_editor.error.source_id_unknown"));
        }

        if (attOrig.getTypeEnum() == AttestationType.LEMMALINK) {
            if (StringUtils.isEmpty(attOrig.getLemmalinkId())) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "lemmalinkId",
                    "demel.attestation_editor.error.lemmalink_id.notempty"));
            }
        }
        if (attEdit.getLemmalinkId() != null) {
            Lemma l = lemmaDBService.queryObject(attEdit.getLemmalinkId());
            if (l == null) {
                binding.addError(new FieldError(MODEL_OBJECT_NAME, "lemmalinkId",
                    "demel.attestation_editor.error.lemmalink_id_unknown"));
            }
        }
    }
}
