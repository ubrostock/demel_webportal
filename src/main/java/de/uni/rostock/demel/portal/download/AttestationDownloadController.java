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
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.uni.rostock.demel.data.api.JSONQuery;
import de.uni.rostock.demel.data.model.TristateBoolean;
import de.uni.rostock.demel.data.model.dictionary.Attestation;
import de.uni.rostock.demel.data.model.dictionary.Attestation.AttestationType;
import de.uni.rostock.demel.data.model.dictionary.Lemma.PartOfSpeech;
import de.uni.rostock.demel.data.model.digitization.Scan;
import de.uni.rostock.demel.data.service.solr.AbstractSolrQueryService.QueryResult;
import de.uni.rostock.demel.data.service.solr.SolrAttestationQueryService;
import de.uni.rostock.demel.data.service.solr.SolrScanQueryService;
import de.uni.rostock.demel.portal.browse.dictionary.AttestationDatatableParams;
import de.uni.rostock.demel.portal.util.JSONResponseService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AttestationDownloadController {

    private static Logger LOGGER = LoggerFactory.getLogger(AttestationDownloadController.class);

    @Autowired
    private SolrAttestationQueryService solrAttestationQueryService;

    @Autowired
    private SolrScanQueryService solrScanQueryService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JSONResponseService jsonService;

    @Value("${demel.basepurl}")
    private String basePURL;

    @Value("${doro.url}")
    private String doroURL;

    private int OFFSET = 50;

    private int MAX_DATA = 10000;

    private ObjectMapper objectMapper = new ObjectMapper()
        .addMixIn(Attestation.class, AttestationJsonOuptutMixIn.class);

    @RequestMapping(value = "/api/data/json/attestations/{id}",
        method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> showLemmaJSON(@PathVariable(value = "id", required = true) String id) {
        Attestation w = solrAttestationQueryService.findById(id);
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                objectMapper.writeValue(outputStream, w);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        String contentDisposition = "attachment; filename=\"demel_" + w.getId() + ".json\"";
        respHeaders.setContentDisposition(ContentDisposition.parse(contentDisposition));
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    // Long running requests in Spring with property: spring.mvc.async.request-timeout
    // and methods that return a Callable
    // https://www.baeldung.com/spring-rest-timeout#spring-mvc-request-timeout
    @RequestMapping(value = "/api/data/{format}/attestations", method = RequestMethod.GET)
    public Callable<ResponseEntity<StreamingResponseBody>> downloadAttestations(
        @PathVariable(name = "format") String format,
        @RequestParam(name = "backURL", defaultValue = "") String backURL,
        UriComponentsBuilder requestUriBuilder,
        HttpServletRequest request, Locale locale) {
        return () -> {
            requestUriBuilder.query(request.getQueryString());
            requestUriBuilder.replaceQueryParam("backURL"); //= remove

            AttestationDatatableParams dtParams = new AttestationDatatableParams(request.getParameterMap());
            dtParams.setDtLength(OFFSET);

            if (format.equalsIgnoreCase("csv")) {
                return returnCSV(dtParams);
            }
            if (format.equalsIgnoreCase("json")) {
                return returnJSON(dtParams, backURL, requestUriBuilder);
            }
            if (format.equalsIgnoreCase("pdf")) {
                return returnPDF(dtParams, backURL, requestUriBuilder, locale);
            }
            return new ResponseEntity<StreamingResponseBody>(HttpStatus.NOT_FOUND);
        };
    }

    private ResponseEntity<StreamingResponseBody> returnCSV(AttestationDatatableParams dtParams) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                CSVFormat.Builder csvFormat = CSVFormat.EXCEL.builder().setNullString("[NULL]")
                    .setIgnoreSurroundingSpaces(true);
                int max = dtParams.getDtStart() + MAX_DATA;
                csvFormat.setHeader("id", "purl", "status", "type", "form", "lemmaId", "lemmaName",
                    "lemmaPartOfSpeechs",
                    "isMultiwordexpr", "multiwordexpr",
                    "datingOrigin", "datingDisplay", "datingFrom", "datingTo",
                    "sourceId", "sourceName", "sourceType",
                    "lemmalinkId", "lemmalinkName",
                    "countScans", "scanIDs", "scanContentIDs", "scanRotations", "countMessagesPublished");
                try (CSVPrinter csvPrinter = new CSVPrinter(bw, csvFormat.build())) {
                    int start = dtParams.getDtStart();
                    QueryResult<Attestation> qr = null;
                    do {
                        dtParams.setDtStart(start);
                        qr = solrAttestationQueryService.queryByDatatableParams(dtParams);
                        for (Attestation a : qr.data()) {
                            csvPrinter.printRecord(
                                a.getId(),
                                basePURL + a.getId(),
                                a.getStatus(),
                                a.getType(),
                                a.getForm(),
                                a.getLemmaId(),
                                a.getLemmaName(),
                                calcPartOfSpeechsForDisplay(a.getLemmaPartOfSpeechEnums()),
                                a.getIsMultiwordexpr(),
                                a.getMultiwordexpr(),
                                a.getDatingOrigin(),
                                a.getDatingDisplaySearch(),
                                a.getDatingFromSearch(),
                                a.getDatingToSearch(),

                                a.getSourceId(),
                                a.getSourceName(),
                                a.getSourceTypeEnum().getValue(),

                                a.getLemmalinkId(),
                                a.getLemmalinkName(),

                                a.getCountScans(),
                                StringUtils.join(a.getScanIDs(), '|'),
                                StringUtils.join(a.getScanContentIDs(), '|'),
                                StringUtils.join(a.getScanRotations(), '|'),
                                a.getCountMessagesPublished());
                        }
                        start = start + OFFSET;
                    } while (start < qr.solrResponse().getResults().getNumFound() && start < max);
                }
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_attestations.csv\""));

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    private String calcPartOfSpeechsForDisplay(EnumSet<PartOfSpeech> partOfSpeechs) {
        return partOfSpeechs.stream()
            .filter(p -> !p.isGroup())
            .map(x -> x.getValue())
            .collect(Collectors.joining("|"));
    }

    private ResponseEntity<StreamingResponseBody> returnJSON(AttestationDatatableParams dtParams, String backURL,
        UriComponentsBuilder requestUriBuilder) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                UriComponentsBuilder downUriBuilder = requestUriBuilder.cloneBuilder();
                downUriBuilder.replacePath("browse/attestations");
                String downloadURL = downUriBuilder.build().toUriString();

                JSONQuery jsonQuery = new JSONQuery();
                jsonQuery.setStart(dtParams.getDtStart());

                UriComponentsBuilder jsonUriBuilder = requestUriBuilder.cloneBuilder();
                jsonUriBuilder.replacePath("api/data/json/attestations");
                String contentURL = jsonUriBuilder.build().toUriString();
                jsonQuery.setJsonQueryRequest(solrAttestationQueryService.createQuery(dtParams));

                int start = dtParams.getDtStart();
                int max = dtParams.getDtStart() + MAX_DATA;
                List<Attestation> results = new ArrayList<Attestation>();
                QueryResult<Attestation> result;
                do {
                    dtParams.setDtStart(start);
                    result = solrAttestationQueryService.queryByDatatableParams(dtParams);
                    results.addAll(result.data());
                    start = start + OFFSET;
                } while (start < result.solrResponse().getResults().getNumFound() && start < max);

                jsonQuery.setRows(Math.min(result.solrResponse().getResults().getNumFound(), MAX_DATA));
                jsonQuery.setTotal(result.solrResponse().getResults().getNumFound());

                jsonService.outputJSONResponse(outputStream, objectMapper, jsonQuery, results, downloadURL, contentURL);
            }
        };
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_JSON);
        respHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"demel_attestations.json\""));

        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({ "sigleSearch", "lemmaSearch", "sorting", "multiwordexprSorting", "htmlDisplay",
        "datingOriginEnum", "statusEnum", "sourceTypeEnum", "typeEnum", "isMultiwordexprEnum", "lemmaPartOfSpeechEnums",
        "datingDisplay", "datingFrom", "datingTo", "sourceDatingDisplay", "sourceDatingFrom", "sourceDatingTo",
        "countMessagesInreview", "countMessages" })

    @JsonPropertyOrder({ "id", "doctype", "status", "type", "typeSymbol", "form",
        "lemmaId", "lemmaName", "lemmaPartOfSpeechs", "isMultiwordexpr", "multiwordexpr",
        "datingOrigin", "datingDisplaySearch", "datingFromSearch", "datingToSearch",
        "sourceId", "sourceName", "sourceType",
        "lemmalinkId", "lemmalinkName",
        "countScans", "scanIDs", "scanContentIDs", "scanRotations",
        "countMessagesPublished" })
    public class AttestationJsonOuptutMixIn {
    }

    private ResponseEntity<StreamingResponseBody> returnPDF(AttestationDatatableParams dtParams, String backURL,
        UriComponentsBuilder requestUriBuilder, Locale locale) {
        StreamingResponseBody responseBody = new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {

                try {
                    Document doc = new Document(PageSize.A4, 36, 36, 36, 54);
                    PdfWriter writer = PdfWriter.getInstance(doc, outputStream);
                    PDFUtil.registerPageEvent(writer,
                        messageSource.getMessage("demel.attestation_download.pdf.footer.page", null, locale));
                    doc.open();
                    UriComponentsBuilder searchUriBuilder = requestUriBuilder.cloneBuilder();
                    searchUriBuilder.replacePath("browse/attestations");
                    PdfPTable tableKopf = PDFUtil.createTableHead(doc,
                        searchUriBuilder.build().toUriString(), messageSource, locale);
                    doc.add(tableKopf);
                    Paragraph p = new Paragraph(
                        messageSource.getMessage("demel.attestation_download.pdf.table.head", null, locale),
                        PDFUtil.FONT_HEAD);
                    p.setSpacingAfter(12f);
                    doc.add(p);

                    QueryResult<Attestation> result = solrAttestationQueryService.queryByDatatableParams(dtParams);
                    long anzCards = result.data().stream().collect(Collectors.summingLong(x -> x.getCountScans()));

                    long pct = (anzCards < 20) ? 66 : ((anzCards < 50) ? 50 : 33);

                    float[] columnWidth = { 50f, 50 };
                    for (Attestation a : result.data()) {
                        PdfPTable table = new PdfPTable(columnWidth);
                        table.setTotalWidth(doc.getPageSize().getWidth() - 72);
                        table.setLockedWidth(true);
                        //table.setKeepTogether(true);

                        PdfPCell cellMeta = new PdfPCell();

                        float[] columnWidthMeta = { 33f, 66f };
                        PdfPTable tableMeta = new PdfPTable(columnWidthMeta);
                        tableMeta.setWidthPercentage(100);

                        PDFUtil.addLabelCell(tableMeta,
                            messageSource.getMessage("demel.attestation_download.pdf.table.meta.id", null, locale));
                        Chunk chIDAnchor = new Chunk(" " + Character.toString((char) (65 + 164)), PDFUtil.FONT_SYMBOL);
                        chIDAnchor.setAnchor(basePURL + a.getId());
                        Phrase phrID = new Phrase();
                        phrID.setLeading(12f);
                        phrID.add(new Chunk(a.getId(), PDFUtil.FONT_NORMAL));
                        phrID.add(chIDAnchor);
                        PDFUtil.addDataCell(tableMeta, phrID);

                        PDFUtil.addLabelCell(tableMeta,
                            messageSource.getMessage("demel.attestation_download.pdf.table.meta.type", null, locale));
                        PDFUtil.addDataCell(tableMeta, new Phrase(
                            messageSource.getMessage("demel.vocabulary.attestation__type." + a.getType() + ".term",
                                null, locale),
                            PDFUtil.FONT_NORMAL));

                        if (a.getTypeEnum() == AttestationType.PRIMARY) {
                            PDFUtil.addLabelCell(tableMeta, messageSource.getMessage(
                                "demel.attestation_download.pdf.table.meta.form", null, locale));
                            PDFUtil.addDataCell(tableMeta, new Phrase(a.getForm(), PDFUtil.FONT_BOLD));
                        }

                        if (a.getTypeEnum() == AttestationType.LEMMALINK) {
                            PDFUtil.addLabelCell(tableMeta, messageSource
                                .getMessage("demel.attestation_download.pdf.table.meta.lemmalink_id", null,
                                    locale));

                            Chunk chLinkAnchor = new Chunk(" " + Character.toString((char) (65 + 164)),
                                PDFUtil.FONT_SYMBOL);
                            chLinkAnchor.setAnchor(basePURL + a.getLemmalinkId());
                            Phrase phrLink = new Phrase();
                            phrLink.setLeading(12f);
                            phrLink.add(new Chunk(a.getLemmalinkName(), PDFUtil.FONT_NORMAL));
                            phrLink.add(chLinkAnchor);

                            PDFUtil.addDataCell(tableMeta, phrLink);
                        }

                        PDFUtil.addLabelCell(tableMeta, messageSource
                            .getMessage("demel.attestation_download.pdf.table.meta.lemma", null, locale));
                        //char 65 = 'A' = ✡
                        //char 229      = ➥ //Character.toString((char)(65+164))

                        Chunk chLemmaIDAnchor = new Chunk(" " + Character.toString((char) (65 + 164)),
                            PDFUtil.FONT_SYMBOL);
                        chLemmaIDAnchor.setAnchor(basePURL + a.getLemmaId());
                        Phrase phrLemma = new Phrase();
                        phrLemma.setLeading(12f);
                        phrLemma.add(new Chunk(a.getLemmaName(), PDFUtil.FONT_NORMAL));
                        phrLemma.add(new Chunk(" (" + StringUtils.join(a.getLemmaPartOfSpeechEnums().parallelStream()
                            .filter(x -> !x.isGroup())
                            .map(x -> messageSource.getMessage("demel.vocabulary.lemma__part_of_speech."
                                + x.getValue() + ".abbr", null, locale))
                            .collect(Collectors.toList()), ", ") + ")", PDFUtil.FONT_ITALIC));
                        phrLemma.add(chLemmaIDAnchor);

                        PDFUtil.addDataCell(tableMeta, phrLemma);

                        PDFUtil.addLabelCell(tableMeta, messageSource
                            .getMessage("demel.attestation_download.pdf.table.meta.sigle", null, locale));

                        Phrase phrSigle = convertToHTML(a.getSourceName());

                        Chunk chSigleIDAnchor = new Chunk(" " + Character.toString((char) (65 + 164)),
                            PDFUtil.FONT_SYMBOL);
                        chSigleIDAnchor.setAnchor(basePURL + a.getSourceId());
                        phrSigle.add(chSigleIDAnchor);

                        PDFUtil.addDataCell(tableMeta, phrSigle);

                        PDFUtil.addLabelCell(tableMeta,
                            messageSource.getMessage("demel.attestation_download.pdf.table.meta.dating", null, locale));

                        String dating = a.getDatingDisplaySearch();
                        if (dating == null) {
                            dating = messageSource.getMessage("demel.attestations.table.dating.null", null, locale);
                        } else if (StringUtils.isEmpty(dating)) {
                            dating = messageSource.getMessage("demel.attestations.table.dating.none", null, locale);
                        }
                        Phrase phrDating = convertToHTML(dating);
                        String origin = messageSource.getMessage(
                            "demel.vocabulary.attestation__dating_origin." + a.getDatingOrigin() + ".term", null,
                            locale);
                        phrDating.add(new Chunk(" (" + origin + ")", PDFUtil.FONT_ITALIC));

                        PDFUtil.addDataCell(tableMeta, phrDating);

                        if (a.getIsMultiwordexprEnum() == TristateBoolean.TRUE) {
                            PDFUtil.addLabelCell(tableMeta, messageSource
                                .getMessage("demel.attestation_download.pdf.table.meta.mwexpr", null,
                                    locale));
                            // the API supports Stylesheet classes, but I did not get it to work
                            // StyleSheet css = new StyleSheet();
                            // css.applyStyle("seg", Map.of("text-decoration", "underline", "font-weight", "bold", "padding-bottom", "5px"));
                            Phrase phrMwexpr = convertToHTML(
                                a.getMultiwordexpr().replace("<seg>", "<u>").replace("</seg>", "</u>"));
                            PDFUtil.addDataCell(tableMeta, phrMwexpr);
                        }

                        PDFUtil.addZitierCell(a, tableMeta, basePURL);

                        cellMeta.addElement(tableMeta);
                        table.addCell(cellMeta);

                        //Images
                        PdfPCell cellImages = new PdfPCell();
                        cellImages.setPadding(4f);

                        List<Scan> scans = solrScanQueryService.findScanByAttestation(a.getId());
                        for (int i = 0; i < scans.size(); i++) {
                            Scan s = scans.get(i);
                            //id = k0021/phys_1347
                            String imgID = "demel/" + s.getContentid();
                            String imgURL = doroURL + "api/iiif/image/v2/"
                                + imgID.replace("/", "%252F") + "/full/pct:" + pct + "/" + s.getRotation()
                                + "/default.jpg";
                            try {
                                Image img = Image.getInstance(ImageIO.read(new URL(imgURL)), null);
                                float scaleImg = 100 * (doc.getPageSize().getWidth() - 72 - 16) / 2 / img.getWidth();
                                img.scalePercent(scaleImg);

                                if (i == 1 && scans.size() == 2) {
                                    table.addCell("");
                                }

                                cellImages = new PdfPCell();
                                cellImages.setPadding(4f);
                                cellImages.addElement(img);

                                table.addCell(cellImages);

                            } catch (Exception e) {
                                cellImages
                                    .addElement(new Paragraph(
                                        messageSource.getMessage("demel.attestation_download.pdf.table.scan.error",
                                            new Object[] { s.getContentid() }, locale) + "\n" + e.getMessage()));
                            }
                        }
                        //start with new metadata on the left
                        if (a.getCountScans() > 2 && a.getCountScans() % 2 == 0) {
                            table.addCell(new PdfPCell(new Phrase("")));
                        }

                        PdfPCell cellFooter = new PdfPCell(new Phrase(" ", PDFUtil.FONT_SMALL));
                        cellFooter.setColspan(2);
                        cellFooter.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.BOTTOM | PdfPCell.TOP);
                        table.addCell(cellFooter);

                        doc.add(table);
                    }

                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase(messageSource.getMessage("demel.attestation_download.pdf.footer.totalpages",
                            new Object[] { ((HeaderFooterPageEvent) writer.getPageEvent()).getCurrentPage() }, locale),
                            PDFUtil.FONT_NORMAL),
                        297, 24, 0);
                    doc.close();
                    writer.close();
                } catch (DocumentException e) {
                    LOGGER.error("Error creating download PDF", e);
                }
            }

            private Phrase convertToHTML(String htmlText) throws IOException {
                Phrase phrSigle = new Phrase();
                phrSigle.setLeading(12f);
                for (Element e : HTMLWorker.parseToList(new StringReader(htmlText), null)) {
                    if (e instanceof Paragraph p) {
                        for (Element e2 : p.getChunks()) {
                            if (e2 instanceof Chunk c2) {
                                c2.getFont().setSize(PDFUtil.FONT_NORMAL.getSize());
                            }
                        }
                    }
                    if (e instanceof Chunk c) {
                        c.getFont().setSize(PDFUtil.FONT_NORMAL.getSize());
                    }
                    phrSigle.add(e);
                }
                return phrSigle;
            }
        };

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<StreamingResponseBody>(responseBody, respHeaders, HttpStatus.OK);
    }
}
