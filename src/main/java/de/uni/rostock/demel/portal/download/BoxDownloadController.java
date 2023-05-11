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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.digitization.Box;
import de.uni.rostock.demel.data.service.solr.SolrBoxQueryService;
import de.uni.rostock.demel.portal.util.JSONResponseService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BoxDownloadController {

    @Autowired
    private SolrBoxQueryService solrBoxQueryService;

    @Autowired
    JSONResponseService jsonService;

    private ObjectMapper objectMapper = new ObjectMapper()
        .addMixIn(Box.class, BoxJsonOuptutMixIn.class);

    @RequestMapping(value = "/api/popover/cite/box/{id}", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView popOverCite(@PathVariable(value = "id", required = true) String id,
        UriComponentsBuilder b, HttpServletRequest request) throws IOException {
        ModelAndView mav = new ModelAndView("parts/popover_cite_box");
        Box box = solrBoxQueryService.findById(id);
        if (box != null) {
            mav.addObject("box", box);
            return mav;
        } else {
            throw new IOException("Box not found");
        }
    }

    @RequestMapping(value = "/api/data/json/boxes/{id}", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showBoxesById(@PathVariable(value = "id", required = true) String id) {
        Box box = solrBoxQueryService.findById(id);
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                objectMapper.writeValue(outputStream, box);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders
            .setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_" + box.getId() + ".json\""));

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/data/json/boxes", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showBoxes(
        @RequestParam(value = "section", required = false, defaultValue = "") String section,
        UriComponentsBuilder requestUriBuilder,
        HttpServletRequest request) {

        requestUriBuilder.query(request.getQueryString());
        requestUriBuilder.replaceQueryParam("backURL"); //= remove
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                JSONQuery query = new JSONQuery();

                UriComponentsBuilder jsonUriBuilder = requestUriBuilder.cloneBuilder();
                jsonUriBuilder.replacePath("api/data/json/boxes");
                String contentURL = jsonUriBuilder.build().toUriString();

                List<Box> boxes = section.isEmpty() ? solrBoxQueryService.findAllBoxes()
                    : solrBoxQueryService.findBoxesBySection(section);
                query.setStart(0);
                query.setRows(boxes.size());
                query.setTotal(boxes.size());

                jsonService.outputJSONResponse(outputStream, objectMapper, query, boxes, "", contentURL);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_boxes.json\""));

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "sectionSort", "statusEnum" })
    @JsonPropertyOrder({ "id", "doctype", "status", "name", "name_es", "section", "countScans" })
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public class BoxJsonOuptutMixIn {
    }
}
