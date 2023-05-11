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
package de.uni.rostock.demel.portal.browse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.portal.dao.WebsiteDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MainController {
    @Value("${doro.url}")
    String doroURL;

    @Value("#{'${demel.webpages.show_toc}'.split(',')}")
    List<String> showToc;

    @Autowired
    WebsiteDAO websiteDAO;

    @Autowired
    private Environment env;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("browse/home");
        mav.addObject("demel_schema_org", websiteDAO.selectTextByLangAndCode("io", "schema.org_dataset_demel"));
        // info from Thymeleaf: The 'request','session','servletContext' and 'response' expression utility objects 
        // are no longer available by default for template expressions and their use is not recommended. 
        //In cases where they are really needed, they should be manually added as context variables.
        mav.addObject("requestURI", request.getRequestURI());
        return mav;
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String error() {
        return "error";
    }

    @RequestMapping(value = "/site/{site_code}", method = RequestMethod.GET)
    public ModelAndView loadWebsite(@PathVariable(name = "site_code", required = true) String code) {
        String text = websiteDAO.selectTextByLangAndCode(LocaleContextHolder.getLocale().getLanguage(), code);
        if (text != null) {
            ModelAndView mav = new ModelAndView("site");
            mav.addObject("site_code", code);
            mav.addObject("site_lang", LocaleContextHolder.getLocale().getLanguage());
            mav.addObject("show_toc", showToc.contains(code));

            mav.addObject("site_text", StringUtils.defaultString(text));

            List<String> preNav = new ArrayList<String>();
            List<String> postNav = new ArrayList<String>();

            String navEntriesString = env.getProperty("demel.webpages.navigation." + code);
            List<String> navEntries
                = navEntriesString == null ? new ArrayList<String>() : Arrays.asList(navEntriesString.split(","));

            if (navEntries.contains(code)) {
                for (String e : navEntries) {
                    if (navEntries.indexOf(e) < navEntries.indexOf(code)) {
                        preNav.add(e);
                    }
                    if (navEntries.indexOf(e) > navEntries.indexOf(code)) {
                        postNav.add(e);
                    }
                }
            } else {
                preNav.addAll(navEntries);
            }

            mav.addObject("pre_nav", preNav);
            mav.addObject("post_nav", postNav);

            return mav;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A webpage for '" + code + "' does not exist.");
    }

    @RequestMapping(value = "/site/{site_code}", method = RequestMethod.POST)
    public ModelAndView saveWebsite(@PathVariable(name = "site_code") String code,
        @RequestParam String lang,
        @RequestParam String text,
        @RequestParam(required = false) String save,
        HttpServletRequest request) {
        if (request.isUserInRole("ROLE_WEBEDITOR") && save != null) {
            websiteDAO.saveText(code, lang, text);
        }
        return loadWebsite(code);
    }

    @RequestMapping(value = "/browse/search", method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView browse(@RequestParam(value = "term", required = false) String term,
        @RequestParam(value = "option", required = false) String option,
        Model model) {
        return switch (option) {
            case Lemma.DOCTYPE -> new ModelAndView(
                "redirect:/browse/lemmas?add_lemma_term=" + URLEncoder.encode(term, StandardCharsets.UTF_8));
            case Attestation.DOCTYPE -> new ModelAndView(
                "redirect:/browse/attestations?add_att_term=" + URLEncoder.encode(term, StandardCharsets.UTF_8));
            default -> new ModelAndView("redirect:/");
        };
    }

    @GetMapping(value = "/demel_datamodel.svg")
    public ResponseEntity<StreamingResponseBody> download(final HttpServletResponse response) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try (InputStream is = getClass().getResourceAsStream("/demel_datamodel.drawio.svg")) {
                    IOUtils.copy(is, outputStream);
                }
            }
        };

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(new MediaType("image", "svg+xml", StandardCharsets.UTF_8));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

}
