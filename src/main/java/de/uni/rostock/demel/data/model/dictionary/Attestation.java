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
package de.uni.rostock.demel.data.model.dictionary;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.context.i18n.LocaleContextHolder;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.HasMessagesCount;
import de.uni.rostock.demel.data.model.HasScansCount;
import de.uni.rostock.demel.data.model.TristateBoolean;
import de.uni.rostock.demel.portal.util.searchbox.SearchboxItem;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class Attestation extends AbstractModelObject implements SearchboxItem, HasScansCount, HasMessagesCount {

    public static final String DOCTYPE = "attestation";

    public static final String DOCID_PREFIX = "d";

    public static final Pattern DOCID_PATTERN = Pattern.compile("d?(\\d{1,8})");

    public static String convertId(long id) {
        return String.format("d%08d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public static String stripXMLTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "");
    }

    public enum AttestationType {
        PRIMARY("Ⓟ"),
        SECONDARY("Ⓢ"),
        LEMMALINK("Ⓛ"),
        UNDOCUMENTED("Ⓧ");

        private String symbol;

        private AttestationType(String symbol) {
            this.symbol = symbol;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public String getSymbol() {
            return symbol;
        }

        public static AttestationType byValue(String value) {
            for (AttestationType t : AttestationType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum Status {
        PUBLISHED, DELETED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Status byValue(String value) {
            for (Status s : Status.values()) {
                if (s.name().equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum DatingOrigin {
        SCAN, PRIMARY_SOURCE, SECONDARY_SOURCE, UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static DatingOrigin byValue(String value) {
            for (DatingOrigin d : DatingOrigin.values()) {
                if (d.name().equalsIgnoreCase(value)) {
                    return d;
                }
            }
            return null;
        }
    }

    @Field("id")
    private String id;

    //@Field annotation on setter
    private AttestationType type;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("sorting")
    private String sorting;

    @Field("is_parent")
    private boolean isParent = true;

    @Field("d__form")
    private String form;

    @Field("d__lemma__id")
    private String lemmaId;

    //sortfield as copyfield: d__lemma__name_sorting
    @Field("d__lemma__name")
    private String lemmaName;

    private EnumSet<Lemma.PartOfSpeech> lemmaPartOfSpeechs = EnumSet.noneOf(Lemma.PartOfSpeech.class);

    @Field("d__lemma__search")
    private List<String> lemmaSearch;

    private TristateBoolean isMultiwordexpr;

    @Field("d__multiwordexpr")
    private String multiwordexpr;

    @Field("d__multiwordexpr_search")
    private String multiwordexprSearch;

    @Field("d__multiwordexpr_sorting")
    private String multiwordexprSorting;

    @Field("d__dating_display")
    private String datingDisplay;

    @Field("d__dating_from")
    private int datingFrom;

    @Field("d__dating_to")
    private int datingTo;

    @Field("d__source__dating_display")
    private String sourceDatingDisplay;

    @Field("d__source__dating_from")
    private int sourceDatingFrom;

    @Field("d__source__dating_to")
    private int sourceDatingTo;

    //on setter: @Field("d__dating_origin")
    private DatingOrigin datingOrigin;

    @Field("d__dating_display_search")
    private String datingDisplaySearch;

    @Field("d__dating_from_search")
    private int datingFromSearch;

    @Field("d__dating_to_search")
    private int datingToSearch;

    @Field("d__source__id")
    private String sourceId;

    //sortfield as copyfield: d__source__name_sorting
    @Field("d__source__name")
    private String sourceName;

    private Source.SourceType sourceType;

    @Field("d__lemmalink__id")
    private String lemmalinkId;

    @Field("d__lemmalink__name")
    private String lemmalinkName;

    //not indexed in Solr!!!
    private String hintsIntern;

    //@Field("d__status")
    private Status status;

    @Field("d__sigle__search") //copy field: d__sigle__search_exact
    private List<String> sigleSearch;

    @Field("count__scans")
    private long scanCount;

    @Field("d__scan__ids")
    private List<String> scanIDs;

    @Field("d__scan__contentids")
    private List<String> scanContentIDs;

    @Field("d__scan__rotations")
    private List<Integer> scanRotations;

    @Field("count__messages")
    private long countMessages;

    @Field("count__messages_published")
    private long countMessagesPublished;

    @Field("count__messages_inreview")
    private long countMessagesInreview;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setId(long id) {
        this.id = Attestation.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public AttestationType getTypeEnum() {
        return type;
    }

    public void setType(AttestationType type) {
        this.type = type;
    }

    @Field("d__type")
    protected void setType(String type) {
        this.type = AttestationType.byValue(type);
    }

    public String getType() {
        return type.getValue();
    }

    public String getTypeSymbol() {
        return getTypeEnum().getSymbol();
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(String lemmaId) {
        this.lemmaId = lemmaId;
    }

    public String getLemmaName() {
        return lemmaName;
    }

    public void setLemmaName(String lemmaName) {
        this.lemmaName = lemmaName;
    }

    public EnumSet<Lemma.PartOfSpeech> getLemmaPartOfSpeechEnums() {
        return lemmaPartOfSpeechs;
    }

    public void setLemmaPartOfSpeechsEnum(EnumSet<Lemma.PartOfSpeech> lemmaPartOfSpeechs) {
        this.lemmaPartOfSpeechs = lemmaPartOfSpeechs;
    }

    @Field("d__lemma__part_of_speech")
    public void setLemmaPartOfSpeechs(List<String> lemmaPartOfSpeechs) {
        this.lemmaPartOfSpeechs = EnumSet
            .copyOf(lemmaPartOfSpeechs.stream().map(s -> Lemma.PartOfSpeech.byValue(s)).toList());
    }

    public List<String> getLemmaPartOfSpeechs() {
        return lemmaPartOfSpeechs.stream().map(p -> p.getValue()).toList();
    }

    public List<String> getLemmaSearch() {
        return lemmaSearch;
    }

    public void setLemmaSearch(List<String> lemmaSearch) {
        this.lemmaSearch = lemmaSearch;
    }

    public String getIsMultiwordexpr() {
        return isMultiwordexpr.getValue();
    }

    @Field("d__is_multiwordexpr")
    public void setIsMultiwordexpr(String isMultiwordexpr) {
        this.isMultiwordexpr = TristateBoolean.byValue(isMultiwordexpr);
    }

    public TristateBoolean getIsMultiwordexprEnum() {
        return isMultiwordexpr;
    }

    public void setIsMultiwordexprEnum(TristateBoolean isMultiwordexpr) {
        this.isMultiwordexpr = isMultiwordexpr;
    }

    public String getMultiwordexpr() {
        return multiwordexpr;
    }

    public void setMultiwordexpr(String multiwordexpr) {
        this.multiwordexpr = multiwordexpr;
        this.multiwordexprSearch = stripXMLTags(multiwordexpr);
    }

    public String getMultiwordexprSearch() {
        return multiwordexprSearch;
    }

    public void setMultiwordexprSearch(String multiwordexprSearch) {
        this.multiwordexprSearch = multiwordexprSearch;
    }

    public String getMultiwordexprSorting() {
        return multiwordexprSorting;
    }

    public void setMultiwordexprSorting(String multiwordexprSorting) {
        this.multiwordexprSorting = multiwordexprSorting;
    }

    public String getDatingDisplay() {
        return datingDisplay;
    }

    public void setDatingDisplay(String datingDisplay) {
        this.datingDisplay = datingDisplay;
    }

    public int getDatingFrom() {
        return datingFrom;
    }

    public void setDatingFrom(int datingFrom) {
        this.datingFrom = datingFrom;
    }

    public int getDatingTo() {
        return datingTo;
    }

    public void setDatingTo(int datingTo) {
        this.datingTo = datingTo;
    }

    public String getSourceDatingDisplay() {
        return sourceDatingDisplay;
    }

    public void setSourceDatingDisplay(String sourceDatingDisplay) {
        this.sourceDatingDisplay = sourceDatingDisplay;
    }

    public int getSourceDatingFrom() {
        return sourceDatingFrom;
    }

    public void setSourceDatingFrom(int sourceDatingFrom) {
        this.sourceDatingFrom = sourceDatingFrom;
    }

    public int getSourceDatingTo() {
        return sourceDatingTo;
    }

    public void setSourceDatingTo(int sourceDatingTo) {
        this.sourceDatingTo = sourceDatingTo;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Source.SourceType getSourceTypeEnum() {
        return sourceType;
    }

    public void setSourceTypeEnum(Source.SourceType s) {
        this.sourceType = s;
    }

    public String getSourceType() {
        return sourceType == null ? null : sourceType.getValue();
    }

    @Field("d__source__type")
    public void setSourceType(String s) {
        this.sourceType = Source.SourceType.byValue(s);
    }

    public String getLemmalinkId() {
        return lemmalinkId;
    }

    public void setLemmalinkId(String lemmalinkId) {
        this.lemmalinkId = lemmalinkId;
    }

    public String getLemmalinkName() {
        return lemmalinkName;
    }

    public void setLemmalinkName(String lemmalinkName) {
        this.lemmalinkName = lemmalinkName;
    }

    public Status getStatusEnum() {
        return status;
    }

    public void setStatusEnum(Status status) {
        this.status = status;
    }

    public String getStatus() {
        return status == null ? null : status.getValue();
    }

    @Field("d__status")
    public void setStatus(String status) {
        this.status = Status.byValue(status);
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    public List<String> getSigleSearch() {
        return sigleSearch;
    }

    public void setSigleSearch(List<String> sigleSearch) {
        this.sigleSearch = sigleSearch;
    }

    @Override
    public long getCountScans() {
        return scanCount;
    }

    @Override
    public void setCountScans(long scanCount) {
        this.scanCount = scanCount;
    }

    public List<String> getScanIDs() {
        return scanIDs;
    }

    public void setScanIDs(List<String> scanIDs) {
        this.scanIDs = scanIDs;
    }

    public List<String> getScanContentIDs() {
        return scanContentIDs;
    }

    public void setScanContentIDs(List<String> scanContentIDs) {
        this.scanContentIDs = scanContentIDs;
    }

    public List<Integer> getScanRotations() {
        return scanRotations;
    }

    public void setScanRotations(List<Integer> scanRotations) {
        this.scanRotations = scanRotations;
    }

    @Override
    public String getHtmlDisplay() {
        if (type == AttestationType.LEMMALINK) {
            return "<em>[s.v. " + lemmalinkName + "]</em>";
        }
        //TODO I18N
        if (type == AttestationType.SECONDARY) {
            if (LocaleContextHolder.getLocale().getLanguage().equalsIgnoreCase("es")) {
                return "<em>[no registrada]</em>";
            }
            return "<em>[nicht erfasst]</em>";
        }
        if (StringUtils.isEmpty(form)) {
            if (LocaleContextHolder.getLocale().getLanguage().equalsIgnoreCase("es")) {
                return "<em>[sin]</em>";
            }
            return "<em>[ohne]</em>";
        }
        // type == AttestationType.PRIMARY
        return StringEscapeUtils.escapeHtml4(getForm());
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    /**
     * Sortierkriterien für Belegte Formen:
    1. Primärbelege Belegte Form A-Z (ohne Klammern, Großbuchstaben, Sonderzeichen, Accente ignorieren, außer "enje" ...)
    2. Primärbelege "ohne" -> nach Lemma
    3. Sekundärbelege nach Lemma
    4. Verweise nach Verweiswort (Lemmalink, Normalisierung s.o.)
    */
    public static String createSorting(Attestation w) {
        StringBuffer sb = new StringBuffer();
        if (w.getTypeEnum() == AttestationType.PRIMARY) {
            if (StringUtils.isEmpty(w.getForm())) {
                sb.append("02||" + w.getLemmaName() + "|");
            } else {
                sb.append("01|" + w.getForm() + "||");
            }
        } else if (w.getTypeEnum() == AttestationType.SECONDARY) {
            sb.append("03||" + w.getLemmaName() + "|");

        } else if (w.getTypeEnum() == AttestationType.LEMMALINK) {
            sb.append("04|||" + w.getLemmalinkName());
        } else {
            //should not happen
            sb.append("99|" + w.getForm() + "|" + w.getLemmaName() + "|" + String.valueOf(w.getLemmalinkName()));
        }

        //finally add id to sort string
        sb.append("|").append(w.getId());

        String result = sb.toString().replace("[", "").replace("]", "").replace("?", "_");
        return result;
    }

    @Override
    public long getCountMessages() {
        return countMessages;
    }

    @Override
    public void setCountMessages(long countMessages) {
        this.countMessages = countMessages;
    }

    @Override
    public long getCountMessagesPublished() {
        return countMessagesPublished;
    }

    @Override
    public void setCountMessagesPublished(long countMessagesPublished) {
        this.countMessagesPublished = countMessagesPublished;
    }

    @Override
    public long getCountMessagesInreview() {
        return countMessagesInreview;
    }

    @Override
    public void setCountMessagesInreview(long countMessagesInreview) {
        this.countMessagesInreview = countMessagesInreview;
    }

    public DatingOrigin getDatingOriginEnum() {
        return datingOrigin;
    }

    public void setDatingOriginEnum(DatingOrigin datingOrigin) {
        this.datingOrigin = datingOrigin;
    }

    @Field("d__dating_origin")
    public void setDatingOrigin(String datingOrigin) {
        this.datingOrigin = DatingOrigin.byValue(datingOrigin);
    }

    public String getDatingOrigin() {
        return datingOrigin.getValue();
    }

    public String getDatingDisplaySearch() {
        return datingDisplaySearch;
    }

    public void setDatingDisplaySearch(String datingDisplaySearch) {
        this.datingDisplaySearch = datingDisplaySearch;
    }

    public int getDatingFromSearch() {
        return datingFromSearch;
    }

    public void setDatingFromSearch(int datingFromSearch) {
        this.datingFromSearch = datingFromSearch;
    }

    public int getDatingToSearch() {
        return datingToSearch;
    }

    public void setDatingToSearch(int datingToSearch) {
        this.datingToSearch = datingToSearch;
    }
}
