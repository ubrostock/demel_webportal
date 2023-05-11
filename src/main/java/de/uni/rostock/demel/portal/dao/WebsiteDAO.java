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
package de.uni.rostock.demel.portal.dao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteDAO {

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    public String selectTextByLangAndCode(String lang, String code) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT content FROM util_webcontent WHERE lang=? AND code=? AND version = 0;", String.class, lang,
                code);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Erhöht die Version um 1, schreibt den neuen Text als Version 0 und löscht alle Texte mit Version größer 5.
     * Der Text (HTML) wird mit JSOUP formatiert
     */
    public void saveText(String code, String lang, String text) {
        Document doc = Jsoup.parseBodyFragment(text);
        doc.outputSettings().indentAmount(2);
        doc.outputSettings().prettyPrint(true);

        jdbcTemplate.update(
            "UPDATE util_webcontent SET version = version + 1 WHERE lang=? AND code = ? ORDER BY version DESC;", lang,
            code);
        jdbcTemplate.update("DELETE FROM util_webcontent WHERE version > 5;");
        jdbcTemplate.update("INSERT INTO util_webcontent VALUES (NULL, ?, ?, 0, NOW(), ?);", code, lang,
            doc.getElementsByTag("body").html());

    }
}
