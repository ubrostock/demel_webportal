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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.beans.Field;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.HasAttestationsCount;
import de.uni.rostock.demel.data.model.HasMessagesCount;
import de.uni.rostock.demel.portal.util.searchbox.SearchboxItem;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class Lemma extends AbstractModelObject implements SearchboxItem, HasAttestationsCount, HasMessagesCount {

    public static final String DOCTYPE = "lemma";

    public static final String DOCID_PREFIX = "l";

    public static final Pattern DOCID_PATTERN = Pattern.compile("l?(\\d{1,8})");

    public static String convertId(long id) {
        return String.format("l%08d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum LemmaType {
        LEMMA("🄻"), LINKLEMMA("⤴");

        private String symbol;

        private LemmaType(String symbol) {
            this.symbol = symbol;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public String getSymbol() {
            return symbol;
        }

        public static LemmaType byValue(String value) {
            for (LemmaType t : LemmaType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum PartOfSpeech {
        ADJ(null, false),
        ADV(null, false),
        ART(null, false),
        GERUND(null, false),
        INTERJ(null, false),
        CONJ(null, false),
        NAME_ALL(null, true),
        NAME_ANTHROPO(NAME_ALL, false),
        NAME_TOPO(NAME_ALL, false),
        NAME_OTHER(NAME_ALL, false),
        PART(null, false),
        PREP(null, false),
        PRON(null, false),
        SUBST_ALL(null, true),
        SUBST_F(SUBST_ALL, false),
        SUBST_F_PL(SUBST_ALL, false),
        SUBST_M(SUBST_ALL, false),
        SUBST_M_PL(SUBST_ALL, false),
        SUBST(SUBST_ALL, false),
        VERB_ALL(null, true),
        VERB(VERB_ALL, false),
        VERB_PRNL(VERB_ALL, false),
        UNDOCUMENTED(null, false);

        private PartOfSpeech group;
        private boolean isGroup;

        private PartOfSpeech(PartOfSpeech group, boolean isGroup) {
            this.group = group;
            this.isGroup = isGroup;
        }

        public PartOfSpeech getGroup() {
            return group;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public boolean isGroup() {
            return isGroup;
        }

        public static PartOfSpeech byValue(String value) {
            for (PartOfSpeech p : PartOfSpeech.values()) {
                if (p.name().equalsIgnoreCase(value)) {
                    return p;
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

    private Map<String, String> i18nPartOfSpeech;

    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = Lemma.DOCTYPE;

    @Field("sorting")
    private String sorting;

    //@Field("l__type") @Field is at setter (to convert String to Enum)
    private LemmaType type;

    @Field("l__name")
    private String name;

    @Field("l__name_variants")
    private List<String> nameVariants;

    //@Field("l__part_of_speech") @Field is at setter (to convert String to Enum)
    private EnumSet<PartOfSpeech> partOfSpeechs;

    @Field("l__hints_extern")
    private String hintsExtern;

    //@Field("l__status")
    private Status status;

    private String hintsIntern;

    @Field(value = "l__prefix")
    private String prefix;

    @Field("l__lemmalink__source_names")
    private List<String> lemmalinkSourceNames;

    @Field("l__lemmalink__source_ids")
    private List<String> lemmalinkSourceIds;

    @Field("l__lemmalink__target_names")
    private List<String> lemmalinkTargetNames;

    @Field("l__lemmalink__target_ids")
    private List<String> lemmalinkTargetIds;

    @Field("l__search")
    private List<String> lemmaSearch;

    @Field("count__attestations")
    private long countAttestations = 0;

    @Field("count__attestations_primary")
    private long countAttestationsPrimary = 0;

    @Field("count__attestations_secondary")
    private long countAttestationsSecondary = 0;

    @Field("count__attestations_lemmalink")
    private long countAttestationsLemmalink = 0;

    @Field("count__attestations_undocumented")
    private long countAttestationsUndocumented = 0;

    @Field("count__messages")
    private long countMessages;

    @Field("count__messages_published")
    private long countMessagesPublished;

    @Field("count__messages_inreview")
    private long countMessagesInreview;

    @Field("is_parent")
    private boolean isParent = true;

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
        this.id = Lemma.convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public LemmaType getTypeEnum() {
        return type;
    }

    public void setTypeEnum(LemmaType type) {
        this.type = type;
    }

    public String getType() {
        return type.getValue();
    }

    @Field("l__type")
    protected void setType(String type) {
        this.type = LemmaType.byValue(type);
    }

    public String getTypeSymbol() {
        return getTypeEnum().getSymbol();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getNameVariants() {
        return nameVariants;
    }

    public void setNameVariants(List<String> nameVariants) {
        this.nameVariants = nameVariants;
    }

    public EnumSet<PartOfSpeech> getPartOfSpeechEnums() {
        return partOfSpeechs;
    }

    public List<String> getPartOfSpeechs() {
        return partOfSpeechs == null ? Collections.emptyList() : partOfSpeechs.stream().map(p -> p.getValue()).toList();
    }

    public List<String> getPartOfSpeechforDisplay() {
        if (partOfSpeechs == null) {
            return Collections.emptyList();
        }
        return getPartOfSpeechEnums().stream()
            .filter(p -> !p.isGroup())
            .map(x -> x.getValue())
            .toList();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Field("l__part_of_speech")
    public void setPartOfSpeechs(List<String> partOfSpeechs) {
        this.partOfSpeechs = EnumSet.copyOf(
            Objects.requireNonNullElse(partOfSpeechs, Collections.<String>emptyList())
                .stream().map(p -> PartOfSpeech.byValue(p)).toList());
    }

    public void setPartOfSpeechFromEnums(List<PartOfSpeech> partOfSpeechs) {
        this.partOfSpeechs = EnumSet.copyOf(Objects.requireNonNullElse(partOfSpeechs,
            Collections.<PartOfSpeech>emptyList()));
    }

    public String getHintsExtern() {
        return hintsExtern;
    }

    public void setHintsExtern(String hintsExtern) {
        this.hintsExtern = hintsExtern;
    }

    public Status getStatusEnum() {
        return status;
    }

    public void setStatusEnum(Status status) {
        this.status = status;
    }

    public String getStatus() {
        return status.getValue();
    }

    @Field("l__status")
    public void setStatus(String status) {
        this.status = Status.byValue(status);
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    public List<String> getLemmalinkSourceNames() {
        return lemmalinkSourceNames;
    }

    public List<String> getLemmalinkSourceIds() {
        return lemmalinkSourceIds;
    }

    public List<String> getLemmalinkTargetNames() {
        return lemmalinkTargetNames;
    }

    public List<String> getLemmalinkTargetIds() {
        return lemmalinkTargetIds;
    }

    @Override
    public long getCountAttestations() {
        return countAttestations;
    }

    @Override
    public void setCountAttestations(long count) {
        this.countAttestations = count;
    }

    @Override
    public long getCountAttestationsPrimary() {
        return countAttestationsPrimary;
    }

    @Override
    public void setCountAttestationsPrimary(long count) {
        this.countAttestationsPrimary = count;
    }

    @Override
    public long getCountAttestationsSecondary() {
        return countAttestationsSecondary;
    }

    @Override
    public void setCountAttestationsSecondary(long count) {
        this.countAttestationsSecondary = count;
    }

    @Override
    public long getCountAttestationsLemmalink() {
        return countAttestationsLemmalink;
    }

    @Override
    public void setCountAttestationsLemmalink(long count) {
        this.countAttestationsLemmalink = count;
    }

    @Override
    public long getCountAttestationsUndocumented() {
        return countAttestationsLemmalink;
    }

    @Override
    public void setCountAttestationsUndocumented(long count) {
        this.countAttestationsUndocumented = count;
    }

    public void setLemmalinkSourceNames(List<String> lemmalinkSourceNames) {
        this.lemmalinkSourceNames = lemmalinkSourceNames;
    }

    public void setLemmalinkSourceIds(List<String> lemmalinkSourceIds) {
        this.lemmalinkSourceIds = lemmalinkSourceIds;
    }

    public void setLemmalinkTargetNames(List<String> lemmalinkTargetNames) {
        this.lemmalinkTargetNames = lemmalinkTargetNames;
    }

    public void setLemmalinkTargetIds(List<String> lemmalinkTargetIds) {
        this.lemmalinkTargetIds = lemmalinkTargetIds;
    }

    public List<String> getLemmaSearch() {
        return lemmaSearch;
    }

    public void setLemmaSearch(List<String> lemmaSearch) {
        this.lemmaSearch = lemmaSearch;
    }

    @Override
    public String getHtmlDisplay() {
        StringBuffer sb = new StringBuffer();
        sb.append("<span style=\"display:inline-block;width:1.33em\">").append(getTypeEnum().getSymbol())
            .append("</span>");
        if (getName() != null) {
            sb.append(StringEscapeUtils.escapeHtml4(getName()));
        }
        sb.append(" <small class='text-muted'>");
        sb.append(getHtmlPartOfSpeechsWithoutUnknown());
        sb.append("</small>");

        return sb.toString();
    }

    public String getHtmlPartOfSpeechs() {
        return calcHtmlPartOfSpeechs(false);
    }

    public String getHtmlPartOfSpeechsWithoutUnknown() {
        return calcHtmlPartOfSpeechs(true);
    }

    private String calcHtmlPartOfSpeechs(boolean hideUndocumented) {
        if (i18nPartOfSpeech != null && partOfSpeechs != null) {
            EnumSet<PartOfSpeech> poss = EnumSet.copyOf(partOfSpeechs);
            if (hideUndocumented) {
                poss.remove(PartOfSpeech.UNDOCUMENTED);
            }
            return String.join(" / ",
                poss.stream().filter(p -> !p.isGroup())
                    .map(pos -> {
                        if (i18nPartOfSpeech.containsKey(pos.getValue())) {
                            return "<span>" + i18nPartOfSpeech.get(pos.getValue()) + "</span>";
                        } else {
                            return "<span class='text-danger'>" + pos.getValue() + "</span>";
                        }
                    }).toList());
        }
        return "";
    }

    public String getHtmlLemmalinks() {
        StringBuffer sb = new StringBuffer();
        if (getLemmalinkTargetNames() != null) {
            for (int i = 0; i < getLemmalinkTargetNames().size(); i++) {
                sb.append("<span class='d-inline-block text-nowrap");
                if (i != getLemmalinkTargetNames().size() - 1) {
                    sb.append(" me-4");
                }
                sb.append("'><small><i class='fa-solid fa-right-long'></i></small>&nbsp;");
                sb.append("<a style='white-space:normal' href='lemmas?lemma_object=" + getLemmalinkTargetIds().get(i)
                    + "'>" + getLemmalinkTargetNames().get(i) + "</a>");
                sb.append("</span>");
            }
        }
        return sb.toString();
    }

    public void setI18nPartOfSpeech(Map<String, String> i18nPartOfSpeech) {
        this.i18nPartOfSpeech = i18nPartOfSpeech;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public static String createSorting(Lemma l) {
        return l.getName() + "|" + l.getId();
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

}
