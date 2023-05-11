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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.bibliography.Edition;
import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.bibliography.Reproduction;
import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryService;
import de.uni.rostock.demel.portal.browse.dictionary.SourceDatatableParams;
import de.uni.rostock.demel.portal.util.JSONResponseService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BibliographyDownloadController {

    @Autowired
    private SolrSourceQueryService solrSourceQS;

    @Autowired
    JSONResponseService jsonService;

    @Value("${demel.basepurl}")
    private String basePURL;

    private int OFFSET = 50;

    private ObjectMapper objectMapper = new ObjectMapper()
        .addMixIn(Source.class, SourceJsonOuptutMixIn.class)
        .addMixIn(Sigle.class, SourceSigleJsonOuptutMixIn.class)
        .addMixIn(Edition.class, SourceEditionJsonOuptutMixIn.class)
        .addMixIn(Person.class, SourcePersonJsonOuptutMixIn.class)
        .addMixIn(Reproduction.class, SourceEditionReproductionJsonOuptutMixIn.class);

    @RequestMapping(value = "/api/data/json/bibliography/{id}",
        method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showLemmaJSON(@PathVariable(value = "id", required = true) String id) {
        Source b = solrSourceQS.findById(id);
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                objectMapper.writeValue(outputStream, b);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        String contentDisposition = "attachment; filename=\"demel_" + b.getId() + ".json\"";
        respHeaders.setContentDisposition(ContentDisposition.parse(contentDisposition));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/data/{format}/bibliography", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> downloadLemmas(
        @PathVariable(name = "format") String format,
        @RequestParam(name = "backURL", defaultValue = "") String backURL,
        UriComponentsBuilder requestUriBuilder,
        HttpServletRequest request, Locale locale) {
        requestUriBuilder.query(request.getQueryString());
        requestUriBuilder.replaceQueryParam("backURL"); //= remove

        SourceDatatableParams dtParams = new SourceDatatableParams(request.getParameterMap());
        dtParams.setDtLength(OFFSET);

        if (format.equalsIgnoreCase("csv")) {
            return returnCSV(dtParams);
        }
        if (format.equalsIgnoreCase("json")) {
            return returnJSON(dtParams, backURL, requestUriBuilder);
        }

        return new ResponseEntity<StreamingResponseBody>(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<StreamingResponseBody> returnCSV(SourceDatatableParams dtParams) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                CSVFormat.Builder csvFormat = CSVFormat.EXCEL.builder().setNullString("[NULL]")
                    .setIgnoreSurroundingSpaces(true);
                csvFormat.setHeader("id", "purl", "status", "type", "sigle_main",
                    "dating", "datingFrom", "datingTo",
                    "texttypes", "genre", "subgenre", "languages",
                    "hsms_ids", "beta_ids",
                    "editions", "editions_reproductions_provider", "editions_reproductions_url",
                    "countAttestations", "countAttestationsPrimary", "countAttestationsSecondary",
                    "countAttestationsLemmalink", "countAttestationsUndocumented", "countMessagesPublished");
                try (CSVPrinter csvPrinter = new CSVPrinter(bw, csvFormat.build())) {
                    int start = 0;
                    QueryResult<Source> result = null;
                    do {
                        dtParams.setDtStart(start);
                        result = solrSourceQS.queryByDatatableParams(dtParams);
                        for (Source source : result.data()) {

                            csvPrinter.printRecord(
                                source.getId(),
                                basePURL + source.getId(),
                                source.getStatus(),
                                source.getType(),
                                source.getName(),
                                source.getDatingDisplay(),
                                source.getDatingFrom(),
                                source.getDatingTo(),
                                source.getTexttypes().stream().collect(Collectors.joining("|")),
                                StringUtils.defaultIfEmpty(source.getGenre(), ""),
                                StringUtils.defaultIfEmpty(source.getSubgenre(), ""),
                                source.getLanguages().stream().collect(Collectors.joining("|")),
                                source.getHsmsIds().stream().collect(Collectors.joining("|")),
                                source.getBetaIds().stream().collect(Collectors.joining("|")),
                                source.getEditions().stream()
                                    .map(e -> e.getTitleInfo().replaceAll("\\s+", " ")) //normalize whitespace
                                    .collect(Collectors.joining("|")),
                                source.getEditions().stream()
                                    .map(e -> e.getReproductions().stream()
                                        .map(r -> r.getProvider())
                                        .collect(Collectors.joining(",")))
                                    .collect(Collectors.joining("|")),
                                source.getEditions().stream()
                                    .map(e -> e.getReproductions().stream()
                                        .map(r -> r.getOnlineUrl())
                                        .collect(Collectors.joining(",")))
                                    .collect(Collectors.joining("|")),
                                source.getCountAttestations(),
                                source.getCountAttestationsPrimary(),
                                source.getCountAttestationsSecondary(),
                                source.getCountAttestationsLemmalink(),
                                source.getCountAttestationsUndocumented(),
                                source.getCountMessagesPublished());
                        }
                        start = start + OFFSET;
                    } while (start < result.solrResponse().getResults().getNumFound());
                }
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        respHeaders.setContentDisposition(
            ContentDisposition.parse("attachment; filename=\"demel_sources.csv\""));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    private ResponseEntity<StreamingResponseBody> returnJSON(SourceDatatableParams dtParams, String backURL,
        UriComponentsBuilder requestUriBuilder) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                UriComponentsBuilder downUriBuilder = requestUriBuilder.cloneBuilder();
                downUriBuilder.replacePath("browse/bibliography");
                String downloadURL = downUriBuilder.build().toUriString();

                JSONQuery query = new JSONQuery();
                query.setStart(0);

                UriComponentsBuilder jsonUriBuilder = requestUriBuilder.cloneBuilder();
                jsonUriBuilder.replacePath("api/data/json/bibliography");
                String contentURL = jsonUriBuilder.build().toUriString();

                query.setJsonQueryRequest(solrSourceQS.createQuery(dtParams));
                int start = 0;
                QueryResult<Source> result;
                List<Source> results = new ArrayList<Source>();

                do {
                    dtParams.setDtStart(start);
                    result = solrSourceQS.queryByDatatableParams(dtParams);
                    results.addAll(result.data());
                    start = start + OFFSET;
                } while (start < result.solrResponse().getResults().getNumFound());
                query.setRows(result.solrResponse().getResults().getNumFound());
                query.setTotal(result.solrResponse().getResults().getNumFound());

                jsonService.outputJSONResponse(outputStream, objectMapper, query, results, downloadURL, contentURL);
            }
        };

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(
            ContentDisposition.parse("attachment; filename=\"demel_bibliography.json\""));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "pos", "sorting", "htmlDisplay", "sigleVariationsForDisplay",
        "datingUniqueEnum", "typeEnum", "statusEnum", "texttypesEnums", "genreEnum", "subgenreEnum", "languagesEnums",
        "personId", "personSearch", "countMessages", "countMessagesInreview", "countAttestationsUndocumented" })

    @JsonPropertyOrder({ "id", "doctype", "status", "type", "typeSymbol",
        "name", "datingDisplay", "datingFrom", "datingTo", "datingUnique",
        "texttypes", "genre", "subgenre", "languages", "hsmsIds", "betaIds",
        "sigles", "editions", "persons",
        "countAttestations", "countAttestationsPrimary", "countAttestationsSecondary",
        "countAttestationsLemmalink", "countMessagesPublished" })
    public class SourceJsonOuptutMixIn {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "sourceId", "sourceName", "sourceStatus", "sourceType", "sourceDating",
        "sourceCountAttestations", "sorting",
        "htmlDisplay", "sourceStatusEnum", "sourceTypeEnum", "typeEnum" })
    @JsonPropertyOrder({ "id", "doctype", "status", "type", "name" })
    public class SourceSigleJsonOuptutMixIn {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "pos" })
    @JsonPropertyOrder({ "id", "doctype", "status" })
    public class SourceEditionJsonOuptutMixIn {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "pos", "sort", "accessEnum" })
    @JsonPropertyOrder({ "id", "doctype", "status", "provider", "onlineUrl", "access", "editionId", "hintsExtern" })
    public class SourceEditionReproductionJsonOuptutMixIn {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "pos", "sorting", "parent", "htmlDisplay" })
    @JsonPropertyOrder({ "id", "doctype", "status", "nameDisplay" })
    public class SourcePersonJsonOuptutMixIn {
    }

}
