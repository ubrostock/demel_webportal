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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.dictionary.Lemma;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrLemmaQueryService;
import de.uni.rostock.demel.portal.browse.dictionary.LemmaDatatableParams;
import de.uni.rostock.demel.portal.util.JSONResponseService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LemmaDownloadController {

    private static Logger LOGGER = LoggerFactory.getLogger(LemmaDownloadController.class);

    @Autowired
    private SolrLemmaQueryService solrLemmaQueryService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    JSONResponseService jsonService;

    @Value("${demel.basepurl}")
    private String basePURL;

    private int OFFSET = 50;

    private ObjectMapper objectMapper = new ObjectMapper()
        .addMixIn(Lemma.class, LemmaJsonOuptutMixIn.class)
        .enable(SerializationFeature.INDENT_OUTPUT);

    @RequestMapping(value = "/api/data/json/lemmas/{id}",
        method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showLemmaJSON(@PathVariable(value = "id", required = true) String id) {
        Lemma l = solrLemmaQueryService.findById(id);

        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                objectMapper.writeValue(outputStream, l);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        String contentDisposition = "attachment; filename=\"demel_" + l.getId() + ".json\"";
        respHeaders.setContentDisposition(ContentDisposition.parse(contentDisposition));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/data/{format}/lemmas", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> downloadLemmas(
        @PathVariable(name = "format") String format,
        UriComponentsBuilder requestUriBuilder,
        HttpServletRequest request, Locale locale) {

        requestUriBuilder.query(request.getQueryString());

        LemmaDatatableParams dtParams = new LemmaDatatableParams(request.getParameterMap());
        dtParams.setDtLength(OFFSET);

        if (format.equalsIgnoreCase("csv")) {
            return returnCSV(dtParams);
        }

        if (format.equalsIgnoreCase("pdf")) {
            return returnPDF(dtParams, requestUriBuilder, locale);
        }

        if (format.equalsIgnoreCase("json")) {
            return returnJSON(dtParams, requestUriBuilder);
        }

        return new ResponseEntity<StreamingResponseBody>(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<StreamingResponseBody> returnCSV(LemmaDatatableParams dtParams) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                CSVFormat.Builder csvFormat = CSVFormat.EXCEL.builder().setNullString("[NULL]")
                    .setIgnoreSurroundingSpaces(true);
                csvFormat.setHeader("id", "purl", "status", "type", "name", "prefix", "partOfSpeech",
                    "lemmalinkTargetIds", "lemmalinkTargetNames", "lemmalinkSourceIds", "lemmalinkSourceNames",
                    "hintsExtern",
                    "countAttestations", "countAttestationsPrimary", "countAttestationsSecondary",
                    "countAttestationsLemmalink",
                    "countMessagesPublished");
                try (CSVPrinter csvPrinter = new CSVPrinter(bw, csvFormat.build())) {
                    int start = 0;
                    QueryResult<Lemma> qr = null;
                    do {
                        dtParams.setDtStart(start);
                        qr = solrLemmaQueryService.queryByDatatableParams(dtParams);
                        for (Lemma lemma : qr.data()) {
                            csvPrinter.printRecord(
                                lemma.getId(),
                                basePURL + lemma.getId(),
                                lemma.getStatus(),
                                lemma.getType(),
                                lemma.getName(),
                                lemma.getPrefix(),
                                StringUtils.join(lemma.getPartOfSpeechforDisplay(), '|'),
                                StringUtils.join(lemma.getLemmalinkTargetIds(), '|'),
                                StringUtils.join(lemma.getLemmalinkTargetNames(), '|'),
                                StringUtils.join(lemma.getLemmalinkSourceIds(), '|'),
                                StringUtils.join(lemma.getLemmalinkSourceNames(), '|'),
                                lemma.getHintsExtern(),
                                lemma.getCountAttestations(),
                                lemma.getCountAttestationsPrimary(),
                                lemma.getCountAttestationsSecondary(),
                                lemma.getCountAttestationsLemmalink(),
                                lemma.getCountMessagesPublished());
                        }
                        start = start + OFFSET;
                    } while (start < qr.solrResponse().getResults().getNumFound());
                }
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_lemmas.csv\""));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    private ResponseEntity<StreamingResponseBody> returnJSON(LemmaDatatableParams dtParams,
        UriComponentsBuilder requestUriBuilder) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                UriComponentsBuilder downUriBuilder = requestUriBuilder.cloneBuilder();
                downUriBuilder.replacePath("browse/lemmas");
                String downloadURL = downUriBuilder.build().toUriString();

                JSONQuery query = new JSONQuery();
                query.setStart(0);
                query.setJsonQueryRequest(solrLemmaQueryService.createQuery(dtParams));
                UriComponentsBuilder jsonUriBuilder = requestUriBuilder.cloneBuilder();
                jsonUriBuilder.replacePath("api/data/json/lemmas");
                String contentUrl = jsonUriBuilder.build().toUriString();

                List<Lemma> results = new ArrayList<Lemma>();
                int start = 0;
                QueryResult<Lemma> qr = null;
                do {
                    dtParams.setDtStart(start);
                    qr = solrLemmaQueryService.queryByDatatableParams(dtParams);
                    results.addAll(qr.data());

                    start = start + OFFSET;
                } while (start < qr.solrResponse().getResults().getNumFound());

                query.setRows(qr.solrResponse().getResults().getNumFound());
                query.setTotal(qr.solrResponse().getResults().getNumFound());

                jsonService.outputJSONResponse(outputStream, objectMapper, query, results, downloadURL, contentUrl);

            }
        };

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_lemmas.json\""));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "lemmalink", "lemmaSearch", "sorting", "parent",
        "htmlDisplay", "htmlPartOfSpeechs", "partOfSpeechforDisplay",
        "htmlLemmalinks", "htmlPartOfSpeechsWithoutUnknown",
        "countMessagesInreview", "countMessages", "typeEnum", "partOfSpeechEnums", "statusEnum" })
    @JsonPropertyOrder({ "id", "doctype", "status", "type", "typeSymbol",
        "name", "nameVariants", "prefix", "partOfSpeechs",
        "lemmalinkTargetIds", "lemmalinkTargetNames", "lemmalinkSourceIds", "lemmalinkSourceNames",
        "hintsExtern",
        "countAttestations", "countAttestationsPrimary", "countAttestationsSecondary",
        "countAttestationsLemmalink", "countAttestationsUndocumented", "countMessagesPublished" })
    public class LemmaJsonOuptutMixIn {
    }

    private ResponseEntity<StreamingResponseBody> returnPDF(LemmaDatatableParams dtParams,
        UriComponentsBuilder requestUriBuilder, Locale locale) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    Document doc = new Document(PageSize.A4, 36, 36, 36, 54);
                    PdfWriter writer = PdfWriter.getInstance(doc, outputStream);
                    PDFUtil.registerPageEvent(writer,
                        messageSource.getMessage("demel.lemma_download.pdf.footer.page", null, locale));
                    doc.open();
                    UriComponentsBuilder searchUriBuilder = requestUriBuilder.cloneBuilder();
                    searchUriBuilder.replacePath("browse/lemma");
                    PdfPTable tableKopf = PDFUtil.createTableHead(doc, searchUriBuilder.build().toUriString(),
                        messageSource, locale);
                    doc.add(tableKopf);
                    Paragraph p = new Paragraph(
                        messageSource.getMessage("demel.lemma_download.pdf.table.head", null, locale),
                        PDFUtil.FONT_HEAD);
                    p.setSpacingAfter(12f);
                    doc.add(p);

                    float[] columnWidth = { 13f, 17f, 21f, 12f, 29f, 8f };
                    PdfPTable table = new PdfPTable(columnWidth);
                    table.setTotalWidth(doc.getPageSize().getWidth() - 72);
                    table.setLockedWidth(true);

                    PdfPCell cell;
                    List<String> columnNames = Arrays.asList(
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.id", null, locale),
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.type", null, locale),
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.name", null, locale),
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.part_of_speech", null,
                            locale),
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.lemmalink_id", null, locale),
                        messageSource.getMessage("demel.lemma_download.pdf.table.column.attestation", null, locale));
                    createTableHeads(table, columnNames);

                    int start = 0;
                    QueryResult<Lemma> qr = null;
                    do {
                        dtParams.setDtStart(start);
                        qr = solrLemmaQueryService.queryByDatatableParams(dtParams);
                        for (Lemma lemma : qr.data()) {
                            Chunk chLemmaIDAnchor = new Chunk(" " + Character.toString((char) (65 + 164)),
                                PDFUtil.FONT_SYMBOL);
                            //Property!!!
                            chLemmaIDAnchor.setAnchor(basePURL + lemma.getId());
                            Phrase phrID = new Phrase();
                            phrID.setLeading(12f);
                            phrID.add(new Chunk(lemma.getId(), PDFUtil.FONT_NORMAL));
                            phrID.add(chLemmaIDAnchor);

                            cell = new PdfPCell(phrID);
                            cell.setPadding(4f);
                            table.addCell(cell);
                            cell = new PdfPCell(new Phrase(messageSource.getMessage(
                                "demel.vocabulary.lemma__type." + lemma.getType() + ".term",
                                null, locale), PDFUtil.FONT_NORMAL));
                            cell.setPadding(4f);
                            table.addCell(cell);

                            cell = new PdfPCell(new Phrase(lemma.getName(), PDFUtil.FONT_BOLD));
                            cell.setPadding(4f);
                            table.addCell(cell);

                            //Wortart
                            String partOfSpeech = StringUtils.join(lemma
                                .getPartOfSpeechforDisplay().stream()
                                .map(x -> messageSource
                                    .getMessage("demel.vocabulary.lemma__part_of_speech." + x + ".abbr", null,
                                        "??demel.vocabulary.lemma__part_of_speech." + x + ".abbr??", locale))
                                .map(x -> x.replace(" ", "\u00a0"))
                                .collect(Collectors.toList()), ", ");
                            cell = new PdfPCell(new Phrase(partOfSpeech, PDFUtil.FONT_NORMAL));
                            cell.setPadding(4f);
                            table.addCell(cell);

                            //Verweise
                            Phrase phrLemmaLink = new Phrase("", PDFUtil.FONT_NORMAL);
                            phrLemmaLink.setLeading(12f);
                            if (lemma.getLemmalinkTargetIds() != null) {
                                for (int i = 0; i < lemma.getLemmalinkTargetIds().size(); i++) {
                                    if (i > 0) {
                                        phrLemmaLink.add(" ");
                                    }
                                    Chunk chLemmaLinkIDAnchor = new Chunk(Character.toString((char) (65 + 164)),
                                        PDFUtil.FONT_SYMBOL);
                                    chLemmaLinkIDAnchor.setAnchor(
                                        basePURL + lemma.getLemmalinkTargetIds().get(i));
                                    phrLemmaLink.add(chLemmaLinkIDAnchor);
                                    phrLemmaLink
                                        .add(new Chunk(
                                            "\u00a0" + lemma.getLemmalinkTargetNames().get(i).replace(" ", "\u00a0"),
                                            PDFUtil.FONT_NORMAL));
                                }
                            }

                            cell = new PdfPCell(phrLemmaLink);
                            cell.setPadding(4f);
                            table.addCell(cell);

                            cell = new PdfPCell(
                                new Phrase(Long.toString(lemma.getCountAttestations()), PDFUtil.FONT_NORMAL));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                            cell.setPadding(4f);
                            table.addCell(cell);
                        }
                        start = start + OFFSET;
                    } while (start < qr.solrResponse().getResults().getNumFound());

                    doc.add(table);

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase(messageSource.getMessage("demel.lemma_download.pdf.footer.totalpages",
                            new Object[] { ((HeaderFooterPageEvent) writer.getPageEvent()).getCurrentPage() }, locale),
                            PDFUtil.FONT_NORMAL),
                        297, 24, 0);

                    doc.close();
                    writer.close();
                } catch (DocumentException e) {
                    LOGGER.error("Error creating PDF", e);
                }
            }

            private void createTableHeads(PdfPTable table, List<String> columnNames) {
                for (String s : columnNames) {
                    PdfPCell cell = new PdfPCell(new Phrase(s, PDFUtil.FONT_BOLD));
                    cell.setPadding(4f);
                    table.addCell(cell);
                }
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }
}
