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
package de.uni.rostock.demel.portal.download;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.digitization.Scan;
import de.uni.rostock.demel.data.service.solr.SolrScanQueryService;
import de.uni.rostock.demel.portal.util.JSONResponseService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ScanDownloadController {

    @Autowired
    private SolrScanQueryService solrScanQueryService;

    @Autowired
    JSONResponseService jsonService;

    private ObjectMapper objectMapper = new ObjectMapper()
        .addMixIn(Scan.class, ScanJsonOuptutMixIn.class)
        .enable(SerializationFeature.INDENT_OUTPUT);;

    @RequestMapping(value = "/api/data/json/scans/{id}", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showScanById(@PathVariable(value = "id", required = true) String id) {
        Scan s = solrScanQueryService.findById(id);
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                objectMapper.writeValue(outputStream, s);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(
            ContentDisposition.parse("attachment; filename=\"demel_" + s.getId() + ".json\""));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);

    }

    @RequestMapping(value = "/api/data/json/scans", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showScans(
        @RequestParam(value = "attestation", required = false, defaultValue = "") String attestation,
        UriComponentsBuilder requestUriBuilder,
        HttpServletRequest request) {

        requestUriBuilder.query(request.getQueryString());
        requestUriBuilder.replaceQueryParam("backURL"); //= remove

        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                String downloadURL = "";

                List<Scan> scans = attestation.isEmpty() ? Collections.emptyList()
                    : solrScanQueryService.findScanByAttestation(attestation);

                JSONQuery query = new JSONQuery();
                query.setStart(0);

                query.setRows(scans.size());
                query.setTotal(scans.size());

                UriComponentsBuilder jsonUriBuilder = requestUriBuilder.cloneBuilder();
                jsonUriBuilder.replacePath("api/data/json/scans");
                String contentURL = jsonUriBuilder.build().toUriString();

                jsonService.outputJSONResponse(outputStream, objectMapper, query, scans, downloadURL, contentURL);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_scans.json\""));

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "typeEnum" })
    @JsonPropertyOrder({ "id", "doctype", "status", "type" })
    public class ScanJsonOuptutMixIn {
    }

}
