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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import de.uni.rostock.demel.SuppressFBWarnings;

/**
 * Rule-Syntax: https://docs.oracle.com/javase/tutorial/i18n/text/rule.html
 * 
 */
@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
public class TraditionalSpanishCollationRules {
    public static void main(String... args) throws ParseException, IOException {
        System.out.println("Start");

        RuleBasedCollator baseCollator = (RuleBasedCollator) Collator.getInstance(new Locale("es", "ES"));
        baseCollator.setStrength(RuleBasedCollator.PRIMARY);

        // define some tailorings, to make it DIN 5007-2 sorting.
        // For example, this makes ö equivalent to oe
        String traditionalSpanish_tailorings = ""
            + " & c,C < ch, cH, Ch, CH < ç, Ç "
            + " & l,L < ll, lL, Ll, LL "
            + " & n,N < ñ, Ñ "
            + " & '-' ; '(' ; ')'; '['; ']'; '{'; '}'; '<' ; '>' ; '*' ; '^' " // '-' ist eines der Zeichen, die bei der Sortierung ignoriert werden 
            //   steht vor erstem "<" in der Regel
            //   -> Klammern wollen wir auch ausschließen.
            + " & z,Z  < '#'"
            + " & y,Y ; ӯ,Ӯ"

            + " & − < '|' < ' '"; // Unicode Character 'MINUS SIGN' (U+2212) = 
                                                                     // letztes Nicht-sortier-Zeichen (vor 1. "<") = Einfügen an Anfang
                                                                     // concatenate the default rules to the tailorings, and dump it to a String
        RuleBasedCollator tailoredCollator
            = new RuleBasedCollator(baseCollator.getRules() + traditionalSpanish_tailorings);
        String tailoredRules = tailoredCollator.getRules();

        // write these to a file, be sure to use UTF-8 encoding!!!
        try (BufferedWriter bw
            = Files.newBufferedWriter(Paths.get("C:\\Temp\\demel_traditionSpanishCollationRules.dat"))) {
            bw.write(tailoredRules);
        }

        outputSample(tailoredCollator, Arrays.asList("abc", "abc de", "ab-cde", "abcha", "a b c", "az", "añ", "an",
            "ao", "alm", "ama", "all", "alla"));
        //> abc, a b c, abc de, ab-cde, abcha, alm, all, alla, ama, añ, az

        outputSample(tailoredCollator, Arrays.asList("abc", "ab", "a", "aa"));
        //> a, aa, ab, abc
        outputSample(tailoredCollator, Arrays.asList("alçandola", "alçando"));
        outputSample(tailoredCollator, Arrays.asList("01|alçandol||", "01|alçandolo||"));
        outputSample(tailoredCollator, Arrays.asList("01 alf|123", "01|alf|123", "01|alfons"));
        outputSample(tailoredCollator, Arrays.asList("01|alf|123", "01|alf_|123"));

        outputSample(tailoredCollator, Arrays.asList("aczack", "ac#ack", "acZack"));

        outputSample(tailoredCollator, Arrays.asList("andora", "aNdora", "aÑdora"));
        outputSample(tailoredCollator, Arrays.asList("alfons", "aLMa", "aller"));
        outputSample(tailoredCollator, Arrays.asList("aӮ", "aY", "ay", "aӯ"));
        outputSample(tailoredCollator, Arrays.asList("a<b>c", "abba", "aba", "abcd"));
        outputSample(tailoredCollator, Arrays.asList("^aaa", "a^aa", "aa^a", "aaa^", "z^aa"));

    }

    private static void outputSample(Collator collator, List<String> sample) {
        Collections.sort(sample, collator);
        System.out.println(StringUtils.join(sample, ", "));

    }
}
