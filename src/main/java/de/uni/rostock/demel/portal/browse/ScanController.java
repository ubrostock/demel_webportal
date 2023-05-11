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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import de.uni.rostock.demel.data.model.digitization.Scan;
import de.uni.rostock.demel.data.service.solr.SolrScanQueryService;

@Controller
public class ScanController {

    @Autowired
    private SolrScanQueryService solrScanQueryService;

    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "/browse/scans")
    public ModelAndView browseScanWithId(@RequestParam(value = "scan_object", required = true) String id) {
        Scan scan = solrScanQueryService.findById(id);
        if (scan == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("browse/scan");
        mav.addObject("scan", scan);
        return mav;
    }

    @RequestMapping(value = "/api/infobox/scan/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView lemmaInfobox(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {

        Scan scan = solrScanQueryService.findById(id);
        if (scan == null) {
            return errorNotfound(id);
        }

        ModelAndView mav = new ModelAndView("parts/infobox_scan");
        mav.addObject("scan", scan);
        return mav;
    }

    @RequestMapping(value = "/api/popover/cite/scan/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView popOverCite(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) {

        Scan scan = solrScanQueryService.findById(id);
        if (scan == null) {
            return errorNotfound(id);
        }
        ModelAndView mav = new ModelAndView("parts/popover_cite_scan");
        mav.addObject("scan", scan);
        return mav;
    }

    private ModelAndView errorNotfound(String id) {
        ModelAndView mav = new ModelAndView("error", HttpStatus.NOT_FOUND);
        String msg = messageSource.getMessage("demel.scans.error_message.404", new Object[] { id },
            LocaleContextHolder.getLocale());
        mav.addObject("error", msg);
        return mav;
    }
}
