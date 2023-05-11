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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.model.digitization.Box;
import de.uni.rostock.demel.data.model.digitization.Scan;
import de.uni.rostock.demel.data.service.solr.SolrScanQueryService;

/**
 * resolves local identifier and redirects to their responding view pages
 * 
 * This controller is registered at the purl server
 * 
 * @author Robert Stephan
 * 
 */
@Controller
public class ResolvingController {

    @Autowired
    SolrScanQueryService solrScanQueryService;

    @Value("${doro.url}")
    private String doroURL;

    @RequestMapping(value = { "/resolve/{id}", "/resolve/{id}/{subid}" }, method = RequestMethod.GET)
    public ResponseEntity<String> resolve(@PathVariable("id") Optional<String> id,
        @PathVariable("subid") Optional<String> subid) {
        if (id.isPresent()) {
            String theId = id.get();

            String location = switch (theId.substring(0, 1)) {
                case Lemma.DOCID_PREFIX -> "/browse/lemmas?lemma_object=" + theId;
                case Attestation.DOCID_PREFIX -> "/browse/attestations?att_object=" + theId;
                case Source.DOCID_PREFIX -> "/browse/bibliography?source_object=" + theId;
                case Scan.DOCID_PREFIX -> "/browse/scans?scan_object=" + theId;
                case Box.DOCID_PREFIX -> {
                    if (subid.isPresent()) {
                        String theSubid = subid.get();
                        Scan solrCard = solrScanQueryService.findScanByContentid(theId + "/" + theSubid);
                        if (solrCard != null) {
                            yield "/browse/scans?scan_object=" + solrCard.getId();
                        } else {
                            //fallback to mycore viewer (should not happen) - this was the previous code
                            yield doroURL + "mcrviewer/recordIdentifier/demel_" + theId
                                + "/iview2/" + theSubid + ".iview2";
                        }
                    } else {
                        yield doroURL + "mcrviewer/recordIdentifier/demel_" + theId;
                    }
                }
                default -> null;
            };

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", location);
            return new ResponseEntity<String>(headers, HttpStatus.FOUND);
        }
        return new ResponseEntity<String>("Unknown parameter", HttpStatus.NOT_FOUND);
    }
}
