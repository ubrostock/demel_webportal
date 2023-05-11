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

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CollatorPlayground {

    public static void main(String[] args) {
        String simple = ",'(',')',° < 1 < 2 < a < b  < c < '('<')' < d < e  & ° = '(' = ')' ";

        List<String> data = Arrays.asList("abba2", "a(b)ba1", "abba 5");
        try {
            RuleBasedCollator mySimple = new RuleBasedCollator(simple);
            Collections.sort(data, mySimple);
            System.out.println(StringUtils.join(data, ", "));
        } catch (ParseException e) {

            e.printStackTrace();
        }

    }

}
