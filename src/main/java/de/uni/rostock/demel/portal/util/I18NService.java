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
package de.uni.rostock.demel.portal.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Lemma.PartOfSpeech;

@Service
public class I18NService {

    @Autowired
    MessageSource messageSource;

    public Map<String, String> retrievePartOfSpeechAbbrMessages() {
        HashMap<String, String> result = new HashMap<>();
        for (PartOfSpeech pos : Lemma.PartOfSpeech.values()) {
            result.put(pos.getValue(),
                messageSource.getMessage("demel.vocabulary.lemma__part_of_speech." + pos.getValue() + ".abbr", null,
                    LocaleContextHolder.getLocale()));
        }
        return result;
    }

    public List<Lemma> enhanceWithI18nPartOfSpeech(List<Lemma> sls) {
        Map<String, String> msgs = retrievePartOfSpeechAbbrMessages();
        for (Lemma sl : sls) {
            sl.setI18nPartOfSpeech(msgs);
        }
        return sls;
    }

    public Lemma enhanceWithI18nPartOfSpeech(Lemma sl) {
        if (sl == null) {
            return null;
        }
        Map<String, String> msgs = retrievePartOfSpeechAbbrMessages();
        sl.setI18nPartOfSpeech(msgs);
        return sl;
    }

    public List<Sigle> enhanceWithI18nDatingNone(List<Sigle> sigles) {
        for (Sigle s : sigles) {
            enhanceWithI18nDatingNone(s);
        }
        return sigles;
    }

    public Sigle enhanceWithI18nDatingNone(Sigle sigle) {
        if (sigle == null) {
            return null;
        }
        if ("[none]".equals(sigle.getSourceDating())) {
            sigle.setSourceDating("<em>" + messageSource.getMessage("demel.attestations.table.dating.none", null,
                LocaleContextHolder.getLocale()) + "</em>");
        }
        return sigle;
    }
}
