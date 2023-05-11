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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.uni.rostock.demel.SuppressFBWarnings;
import de.uni.rostock.demel.data.model.AbstractModelObject;
import de.uni.rostock.demel.data.model.HasAttestationsCount;
import de.uni.rostock.demel.data.model.HasMessagesCount;
import de.uni.rostock.demel.data.model.TristateBoolean;
import de.uni.rostock.demel.data.model.bibliography.Edition;
import de.uni.rostock.demel.data.model.bibliography.Person;
import de.uni.rostock.demel.data.model.bibliography.Sigle;
import de.uni.rostock.demel.portal.util.searchbox.SearchboxItem;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class Source extends AbstractModelObject implements SearchboxItem, HasAttestationsCount, HasMessagesCount {

    public static final String DOCTYPE = "source";

    public static final String DOCID_PREFIX = "b";

    public static final Pattern DOCID_PATTERN = Pattern.compile("b?(\\d{1,4})");

    public static String convertId(long id) {
        return String.format("b%04d", id);
    }

    public static Long convertId(String id) {
        return Long.parseLong(id.substring(1));
    }

    public enum SourceType {
        PRIMARY("🄿"),
        SECONDARY("🅂"),
        JOURNAL("🄹"),
        UNDOCUMENTED("🅇");

        private String symbol;

        private SourceType(String symbol) {
            this.symbol = symbol;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public String getSymbol() {
            return symbol;
        }

        public static SourceType byValue(String value) {
            for (SourceType t : SourceType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum Texttype {
        PROSE, VERSE, UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Texttype byValue(String value) {
            for (Texttype p : Texttype.values()) {
                if (p.name().equalsIgnoreCase(value)) {
                    return p;
                }
            }
            return null;
        }
    }

    // `genre` ENUM('dramatic','epic','lyric','non_fictional','undocumented') DEFAULT NULL,
    // SELECT UCASE(CODE) FROM util_vocabulary WHERE `TYPE`='source__genre' ORDER BY sort;
    public enum Genre {
        DRAMATIC,
        EPIC,
        LYRIC,
        NON_FICTIONAL,
        UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Genre byValue(String value) {
            for (Genre p : Genre.values()) {
                if (p.name().equalsIgnoreCase(value)) {
                    return p;
                }
            }
            return null;
        }
    }

    // `subgenre` ENUM('scientific','historical','legal','leisure','moralizing','religious','undocumented')
    //    DEFAULT NULL,
    //  SELECT UCASE(CODE) FROM util_vocabulary WHERE `TYPE`='source__subgenre' ORDER BY sort;
    public enum Subgenre {
        SCIENTIFIC,
        HISTORICAL,
        LEGAL,
        LEISURE,
        MORALIZING,
        RELIGIOUS,
        UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Subgenre byValue(String value) {
            for (Subgenre p : Subgenre.values()) {
                if (p.name().equalsIgnoreCase(value)) {
                    return p;
                }
            }
            return null;
        }
    }

    //  `language` SET('castilian','galician','leonese','latin','navarro_aragonese','aljamiado','undocumented') DEFAULT NULL, 
    public enum Language {
        ALJAMIADO,
        CASTILIAN,
        GALICIAN,
        LATIN,
        LEONESE,
        NAVARRO_ARAGONESE,
        UNDOCUMENTED;

        public String getValue() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Language byValue(String value) {
            for (Language l : Language.values()) {
                if (l.name().equalsIgnoreCase(value)) {
                    return l;
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

    /**
     * the id - "s"+1234 (4stellig)
     */
    @Field("id")
    private String id;

    @Field("doctype")
    private String doctype = DOCTYPE;

    @Field("sorting")
    private String sorting;

    private SourceType type;

    @Field("b__dating_display")
    private String datingDisplay;

    @Field("b__dating_from")
    private int datingFrom;

    @Field("b__dating_to")
    private int datingTo;

    //@Field("b__dating_unique") on setter
    private TristateBoolean datingUnique;

    private EnumSet<Texttype> texttypes;

    private Genre genre;

    private Subgenre subgenre;

    private EnumSet<Language> languages;

    @Field("b__hsms_ids")
    private List<String> hsmsIds = new ArrayList<>();

    @Field("b__beta_ids")
    private List<String> betaIds = new ArrayList<>();

    //@Field("b__status") on setter
    private Status status;

    private String hintsIntern;

    @Field("b__sigle__search")
    private List<String> sigleSearch = new ArrayList<>();

    @Field("b__edition__search")
    private List<String> editionSearch = new ArrayList<>();

    @Field("b__person__id")
    private List<String> personId = new ArrayList<>();

    @Field("b__person__search")
    private List<String> personSearch = new ArrayList<>();

    @Field("b__name")
    private String name;

    @Field("count__attestations")
    private long countAttestations;

    @Field("count__attestations_primary")
    private long countAttestationsPrimary;

    @Field("count__attestations_secondary")
    private long countAttestationsSecondary;

    @Field("count__attestations_lemmalink")
    private long countAttestationsLemmalink;

    @Field("count__attestations_undocumented")
    private long countAttestationsUndocumented;

    @Field("count__messages")
    private long countMessages;

    @Field("count__messages_published")
    private long countMessagesPublished;

    @Field("count__messages_inreview")
    private long countMessagesInreview;

    //error: "Source cannot have more than one Field with child=true"
    //solution : self creation of structured objects with support of annotations 
    //           (where possible)
    // @Field(value = "b__child__editions", child = true)
    private Collection<Edition> editions = new ArrayList<>();

    // @Field(value = "b__child__sigle", child = true)
    private Collection<Sigle> sigles = new ArrayList<>();

    // @Field(value = "b__child__persons", child = true)
    private Collection<Person> persons = new ArrayList<>();

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
        this.id = convertId(id);
    }

    @Override
    public String getDoctype() {
        return doctype;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getType() {
        return type == null ? null : type.getValue();
    }

    public SourceType getTypeEnum() {
        return type;
    }

    @Field("b__type")
    public void setType(String type) {
        this.type = SourceType.byValue(type);
    }

    public void setTypeEnum(SourceType type) {
        this.type = type;
    }

    public String getTypeSymbol() {
        return type.getSymbol();
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

    public String getDatingUnique() {
        return datingUnique.getValue();
    }

    //param is String, because it must match the value in Solr index
    @Field("b__dating_unique")
    public void setDatingUnique(String datingUnique) {
        this.datingUnique = TristateBoolean.byValue(datingUnique);
    }

    public TristateBoolean getDatingUniqueEnum() {
        return datingUnique;
    }

    public void setDatingUniqueEnum(TristateBoolean datingUnique) {
        this.datingUnique = datingUnique;
    }

    public List<String> getTexttypes() {
        return texttypes == null ? Collections.emptyList() : texttypes.stream().map(p -> p.getValue()).toList();
    }

    public EnumSet<Texttype> getTexttypesEnums() {
        return texttypes;
    }

    @Field("b__texttypes")
    public void setTexttypes(Collection<String> texttypes) {
        this.texttypes = EnumSet.copyOf(texttypes.stream().map(x -> Texttype.byValue(x)).toList());
    }

    public void setTexttypesFromEnums(List<Texttype> texttypes) {
        this.texttypes = EnumSet.copyOf(Objects.requireNonNullElse(texttypes, Collections.<Texttype>emptyList()));
    }

    public String getGenre() {
        return genre == null ? null : genre.getValue();
    }

    public Genre getGenreEnum() {
        return genre;
    }

    @Field("b__genre")
    public void setGenre(String genre) {
        this.genre = Genre.byValue(genre);
    }

    public void setGenreEnum(Genre genre) {
        this.genre = genre;
    }

    public String getSubgenre() {
        return subgenre == null ? null : subgenre.getValue();
    }

    public Subgenre getSubgenreEnum() {
        return subgenre;
    }

    @Field("b__subgenre")
    public void setSubgenre(String subgenre) {
        this.subgenre = Subgenre.byValue(subgenre);
    }

    public void setSubgenreEnum(Subgenre subgenre) {
        this.subgenre = subgenre;
    }

    public List<String> getLanguages() {
        return languages == null ? Collections.emptyList() : languages.stream().map(p -> p.getValue()).toList();
    }

    public EnumSet<Language> getLanguagesEnums() {
        return languages;
    }

    @Field("b__languages")
    public void setLanguages(Collection<String> languages) {
        this.languages = EnumSet.copyOf(languages.stream().map(x -> Language.byValue(x)).toList());
    }

    public void setLanguagesFromEnums(List<Language> languages) {
        this.languages = EnumSet.copyOf(Objects.requireNonNullElse(languages, Collections.<Language>emptyList()));
    }

    public List<String> getHsmsIds() {
        return hsmsIds;
    }

    public void setHsmsIds(List<String> hsmsIds) {
        this.hsmsIds.clear();
        if (hsmsIds != null) {
            List<String> l = new ArrayList<String>(hsmsIds);
            l.removeIf(x -> x.isBlank());
            this.hsmsIds.addAll(l);
        }
    }

    public void setHsmsIds(String hsmsId) {
        this.hsmsIds.clear();
        if (hsmsId != null) {
            List<String> l = new ArrayList<>(Arrays.asList(hsmsId.split("\\|")));
            l.removeIf(x -> x.isBlank());
            this.hsmsIds.addAll(l);
        }
    }

    public List<String> getBetaIds() {
        return betaIds;
    }

    public void setBetaIds(List<String> betaIds) {
        this.betaIds.clear();
        if (betaIds != null) {
            List<String> l = new ArrayList<String>(betaIds);
            l.removeIf(x -> x.isBlank());
            this.betaIds.addAll(l);
        }
    }

    public void setBetaIds(String betaId) {
        this.betaIds.clear();
        if (betaId != null) {
            List<String> l = new ArrayList<>(Arrays.asList(betaId.split("\\|")));
            l.removeIf(x -> x.isBlank());
            this.betaIds.addAll(l);
        }
    }

    public String getStatus() {
        return status == null ? null : status.getValue();
    }

    public Status getStatusEnum() {
        return status;
    }

    @Field("b__status")
    public void setStatus(String status) {
        this.status = Status.byValue(status);
    }

    public void setStatusEnum(Status status) {
        this.status = status;
    }

    public String getHintsIntern() {
        return hintsIntern;
    }

    public void setHintsIntern(String hintsIntern) {
        this.hintsIntern = hintsIntern;
    }

    @JsonIgnore
    public List<String> getSigleSearch() {
        return sigleSearch;
    }

    public void setSigleSearch(List<String> sigleSearch) {
        this.sigleSearch = sigleSearch;
    }

    @JsonIgnore
    public List<String> getEditionSearch() {
        return editionSearch;
    }

    public void setEditionSearch(List<String> editionSearch) {
        this.editionSearch = editionSearch;
    }

    public List<String> getPersonId() {
        return personId;
    }

    public void setPersonId(List<String> personId) {
        this.personId = personId;
    }

    public List<String> getPersonSearch() {
        return personSearch;
    }

    public void setPersonSearch(List<String> personSearch) {
        this.personSearch = personSearch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getCountAttestations() {
        return countAttestations;
    }

    @Override
    public void setCountAttestations(long countAttestations) {
        this.countAttestations = countAttestations;
    }

    @Override
    public long getCountAttestationsPrimary() {
        return countAttestationsPrimary;
    }

    @Override
    public void setCountAttestationsPrimary(long countAttestationsPrimary) {
        this.countAttestationsPrimary = countAttestationsPrimary;
    }

    @Override
    public long getCountAttestationsSecondary() {
        return countAttestationsSecondary;
    }

    @Override
    public void setCountAttestationsSecondary(long countAttestationsSecondary) {
        this.countAttestationsSecondary = countAttestationsSecondary;
    }

    @Override
    public long getCountAttestationsLemmalink() {
        return countAttestationsLemmalink;
    }

    @Override
    public void setCountAttestationsLemmalink(long countAttestationsLemmalink) {
        this.countAttestationsLemmalink = countAttestationsLemmalink;
    }

    @Override
    public long getCountAttestationsUndocumented() {
        return countAttestationsUndocumented;
    }

    @Override
    public void setCountAttestationsUndocumented(long countAttestationsUndocumented) {
        this.countAttestationsUndocumented = countAttestationsUndocumented;
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

    public Collection<Edition> getEditions() {
        return editions;
    }

    public void setEditions(Collection<Edition> editions) {
        this.editions = editions;
    }

    public Collection<Sigle> getSigles() {
        return sigles;
    }

    public void setSigles(Collection<Sigle> sigles) {
        this.sigles = sigles;
    }

    public Collection<Person> getPersons() {
        return persons;
    }

    public void setPersons(Collection<Person> persons) {
        this.persons = persons;
    }

    public List<String> getSigleVariationsForDisplay() {
        List<String> list = new ArrayList<String>();
        for (Sigle s : sigles) {
            if (!s.getName().equals(getName())) {
                list.add(s.getName());
            }
        }
        return list;
    }

    /**
     * replace undocumented fields with null values
     * for genre, subgenre, texttype, languages
     */
    public void hideUndocumentedFields() {
        genre = (genre == Genre.UNDOCUMENTED) ? null : genre;
        subgenre = (subgenre == Subgenre.UNDOCUMENTED) ? null : subgenre;
        if (texttypes != null) {
            texttypes.remove(Texttype.UNDOCUMENTED);
        }
        if (languages != null) {
            languages.remove(Language.UNDOCUMENTED);
        }
    }

    /**
     * returns String (may contain HTML) that will be displayed in searchbox
     * 
     */
    @Override
    public String getHtmlDisplay() {
        StringBuffer sb = new StringBuffer();
        sb.append("<span class=\"demel-search-filter-symbol\">" + type.getSymbol() + "</span>");
        sb.append("&nbsp;");
        sb.append(StringEscapeUtils.escapeHtml4(getName()));
        if (getDatingDisplay() != null) {
            sb.append(" <small class=\"text-muted\">");
            sb.append("<br />");
            sb.append(createHTMLDatingDisplay(getDatingDisplay()));
            sb.append("</small>");
        }
        return sb.toString();
    }

    public static String createHTMLDatingDisplay(String dating) {
        StringBuffer sb = new StringBuffer();
        if (dating != null) {
            if ("[none]".equals(dating)) {
                sb.append("<em>[none]</em>");
            } else {
                sb.append(dating);
            }
        } else {
            sb.append("<em></em>");
        }
        return sb.toString();
    }

    /**
     * Sortierkriterien für BibliographyEinträgen:
     * 1. nach Typ
     * 2. nach Primärsigle
     */
    public static String createSorting(Source b) {
        StringBuffer sb = new StringBuffer();
        switch (b.getTypeEnum()) {
            case PRIMARY:
                sb.append("01|");
                break;
            case SECONDARY:
                sb.append("02|");
                break;
            case JOURNAL:
                sb.append("03|");
                break;
            case UNDOCUMENTED:
                sb.append("09|");
                break;
            default:
                sb.append("99|");
        }
        sb.append(b.getName());
        sb.append("|").append(b.getId());
        return sb.toString();
    }

}
