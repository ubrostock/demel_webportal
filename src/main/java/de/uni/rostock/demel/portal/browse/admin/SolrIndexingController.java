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
package de.uni.rostock.demel.portal.browse.admin;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.db.AttestationDBService;
import de.uni.rostock.demel.data.service.db.LemmaDBService;
import de.uni.rostock.demel.data.service.db.SourceDBService;
import de.uni.rostock.demel.data.service.solr.SolrAttestationUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrBoxUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrScanUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrService;
import de.uni.rostock.demel.data.service.solr.SolrSourceUpdateService;

@Controller
public class SolrIndexingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrIndexingController.class);

    @Autowired
    private SolrService solrService;

    @Autowired
    @Qualifier("indexStatusLemmas")
    SolrIndexingData indexStatusLemmas;

    @Autowired
    @Qualifier("indexStatusAttestations")
    SolrIndexingData indexStatusAttestations;

    @Autowired
    @Qualifier("indexStatusSources")
    SolrIndexingData indexStatusSources;

    @Autowired
    @Qualifier("indexStatusBoxes")
    SolrIndexingData indexStatusBoxes;

    @Autowired
    @Qualifier("indexStatusScans")
    SolrIndexingData indexStatusScans;

    @Autowired
    private SolrLemmaUpdateService solrLemmaUpdateService;

    @Autowired
    private SolrAttestationUpdateService solrAttestationUpdateService;

    @Autowired
    private AttestationDBService dbAttestationService;

    @Autowired
    private LemmaDBService dbLemmaService;

    @Autowired
    private SourceDBService dbSourceService;

    @Autowired
    private SolrSourceUpdateService solrSourceUpdateService;

    @Autowired
    private SolrBoxUpdateService solrBoxUpdateService;

    @Autowired
    private SolrScanUpdateService solrScanUpdateService;

    @RequestMapping(value = "/browse/admin/indexing")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView browse() {
        ModelAndView mav = new ModelAndView("browse/admin/indexing");
        return mav;
    }

    @RequestMapping(value = "/api/indexing/sources/start", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView startReindexSources() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        solrSourceUpdateService.reindexAllObjects();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/sources/stop", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView stopReindexSources() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        indexStatusSources.stop();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/sources/status",
        method = RequestMethod.GET,
        produces = "application/json")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public SolrIndexingData indexStatusSources() {
        return indexStatusSources;
    }

    @RequestMapping(value = "/api/indexing/attestations/start", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView startReindexAttestations() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        solrAttestationUpdateService.reindexAllObjects();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/attestations/stop", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView stopReindexAttestation() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        indexStatusAttestations.stop();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/attestations/reindex/{attid}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView reindexSingleAttestation(@PathVariable String attid) {
        ModelAndView mav = new ModelAndView("redirect:/browse/attestations?atts_object=" + attid);
        Attestation att = dbAttestationService.queryObject(attid);
        solrAttestationUpdateService.saveObject(att);
        if (att.getLemmaId() != null) {
            Lemma l = dbLemmaService.queryObject(att.getLemmaId());
            solrLemmaUpdateService.saveObject(l);
        }
        if (att.getSourceId() != null) {
            Source s = dbSourceService.querySource(att.getSourceId());
            solrSourceUpdateService.saveObject(s);
        }
        return mav;
    }

    @RequestMapping(value = "/api/indexing/attestations/status", method = RequestMethod.GET,
        produces = "application/json")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public SolrIndexingData indexStatusAttestations() {
        return indexStatusAttestations;
    }

    @RequestMapping(value = "/api/indexing/lemmas/start", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView startReindexLemmas() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        solrLemmaUpdateService.reindexAllObjects();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/lemmas/stop", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView stopReindexLemmas() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        indexStatusLemmas.stop();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/lemmas/status", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public SolrIndexingData indexStatusLemmas() {
        return indexStatusLemmas;
    }

    @RequestMapping(value = "/api/indexing/lemmas/reindex/{lemmaId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView reindexSingleLemma(@PathVariable String lemmaId) {
        ModelAndView mav = new ModelAndView("redirect:/browse/lemmas?lemma_object=" + lemmaId);
        Lemma lemma = dbLemmaService.queryObject(lemmaId);
        solrLemmaUpdateService.saveObject(lemma);
        return mav;
    }

    @RequestMapping(value = "/api/indexing/boxes/start", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView startReindexBoxes() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        solrBoxUpdateService.reindexAllObjects();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/boxes/stop", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView stopReindexBoxes() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        indexStatusBoxes.stop();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/boxes/status", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public SolrIndexingData indexStatusBoxes() {
        return indexStatusBoxes;
    }

    @RequestMapping(value = "/api/indexing/scans/start", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView startReindexScans() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        solrScanUpdateService.reindexAllObjects();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/scans/stop", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ModelAndView stopReindexScans() {
        ModelAndView mav = new ModelAndView("redirect:/browse/admin/indexing");
        indexStatusScans.stop();
        return mav;
    }

    @RequestMapping(value = "/api/indexing/scans/status", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public SolrIndexingData indexStatusScans() {
        return indexStatusScans;
    }

    /**
     * List ids of all sources, that are in the database, but not in SOLR index
     * 
     * @return list of source ids
     */
    @RequestMapping(value = "/api/indexing/check/attestations", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseBody
    public List<Integer> checkAttestations() {
        List<Integer> data = dbSourceService.querySourceIds();

        SolrQuery solrQuery = new SolrQuery("doctype:source");
        solrQuery.setRows(1000);
        solrQuery.setFields("id");
        int start = 0;

        while (true) {
            solrQuery.setStart(start);
            QueryResponse solrResponse;
            try {
                solrResponse = solrService.querySolr(solrQuery);
                if (solrResponse.getResults().size() == 0) {
                    //nichts mehr gefunden 
                    break;
                }
                for (SolrDocument doc : solrResponse.getResults()) {
                    String idValue = String.valueOf(doc.getFirstValue("id"));
                    int id = Integer.parseInt(idValue.substring(1));
                    data.remove(id);
                }

            } catch (Exception e) {
                LOGGER.debug("Error in checkAttestations", e);
            }
            start = start + solrQuery.getRows();
        }
        return data;
    }
}
