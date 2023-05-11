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
package de.uni.rostock.demel.portal.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.portal.dao.WebsiteDAO;

@Service
public class JSONResponseService {

    @Autowired
    WebsiteDAO websiteDAO;

    public JSONResponseService() {

    }

    public void outputJSONResponse(OutputStream out, ObjectMapper objectMapper,
        JSONQuery query, List<? extends AbstractModelObject> results, String downloadUrl, String contentUrl)
        throws IOException {

        objectMapper.addMixIn(JSONQuery.class, SolrJSONQueryJsonOuptutMixIn.class);
        enhanceSolrQueryString(query);

        String metadata = websiteDAO.selectTextByLangAndCode("io", "schema.org_datadownload");
        metadata = metadata.replace("$DATE_CREATED$", Instant.now().toString());
        metadata = metadata.replace("$DOWNLOAD_URL$", downloadUrl);
        metadata = metadata.replace("$CONTENT_URL$", contentUrl);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            bw.append("{");
            bw.append("\n\"metadata\":");
            bw.append(metadata);
            bw.append(",\n\"query\":");
            bw.append(objectMapper.writeValueAsString(query));
            bw.append(",\n\"results\":");
            bw.append(objectMapper.writeValueAsString(results));
            bw.append("\n}");
        }
    }

    /**
     * convert the Solr JsonQueryRequest Object of the given JSONQuery 
     * to JSON 
     * and remove facet and offset configuration
     * 
     * @param the JSONQuery q
     */
    private void enhanceSolrQueryString(JSONQuery q) {
        if (q.getJsonQueryRequest() == null) {
            q.setSolrJsonRequest("{}");
        } else {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                q.getJsonQueryRequest().getContentWriter(null).write(baos);
                String solrQuery = new String(baos.toByteArray(), StandardCharsets.UTF_8);

                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode json;

                json = objectMapper.readValue(solrQuery, ObjectNode.class);
                json.remove("facet");
                json.remove("offset");
                StringWriter swResults = new StringWriter();
                objectMapper.writeValue(swResults, json);
                q.setSolrJsonRequest(swResults.toString());
            } catch (IOException e) {
                //does not happen on ByteArrayOUtputStream
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "jsonQueryRequest" })
    public class SolrJSONQueryJsonOuptutMixIn {
        @JsonRawValue
        String solrJsonRequest;
    }

}
