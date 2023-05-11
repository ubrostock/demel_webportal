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
package de.uni.rostock.demel;

import java.util.Locale;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import de.uni.rostock.demel.portal.browse.admin.SolrIndexingData;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Main class for DEMel Portal
 * 
 * @author Robert Stephan
 *
 */
@SpringBootApplication
@ServletComponentScan
@EnableAsync
public class DEMelWebApp implements WebMvcConfigurer {

    private static Locale GERMAN_LOCALE = new Locale("de");
    private static Locale SPANISH_LOCALE = new Locale("es");
    private static Locale ENGLISH_LOCALE = new Locale("en");

    @Bean(name = "dsDemel")
    @Primary
    @ConfigurationProperties("demel.datasource")
    public DataSource demelDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jdbcDemel")
    public JdbcTemplate demelJdbcTemplate(@Qualifier("dsDemel") DataSource dsDEMel) {
        return new JdbcTemplate(dsDEMel);
    }

    @Bean(name = "jdbcDemelNamedParam")
    public NamedParameterJdbcTemplate namedParamdemelJdbcTemplate(@Qualifier("dsDemel") DataSource dsDEMel) {
        return new NamedParameterJdbcTemplate(dsDEMel);
    }

    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        /*
         * https://tomcat.apache.org/tomcat-9.0-doc/config/http.html#Standard_Implementation
         * relaxedQueryChars
         * -----------------
         * The HTTP/1.1 specification requires that certain characters are %nn encoded 
         * when used in URI query strings. Unfortunately, many user agents 
         * including all the major browsers are not compliant with this specification 
         * and use these characters in unencoded form. To prevent Tomcat rejecting such requests, 
         * this attribute may be used to specify the additional characters to allow. 
         * If not specified, no additional characters will be allowed. 
         * The value may be any combination of the following characters: 
         * " < > [ \ ] ^ ` { | } .
         */
        return connector -> connector.setProperty("relaxedQueryChars", "<>[]^`{|}");
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new DEMelSessionLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultLocale(SPANISH_LOCALE);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Value("${solr.server.url}")
    String solrURL;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder().withBaseSolrUrl(solrURL).build();
    }

    @Bean(name = "indexStatusSources")
    SolrIndexingData indexStatusSources() {
        return new SolrIndexingData();
    };

    @Bean(name = "indexStatusLemmas")
    SolrIndexingData indexStatusLemmas() {
        return new SolrIndexingData();
    }

    @Bean(name = "indexStatusAttestations")
    SolrIndexingData indexStatusAttestations() {
        return new SolrIndexingData();
    }

    @Bean(name = "indexStatusBoxes")
    SolrIndexingData indexStatusBoxes() {
        return new SolrIndexingData();
    }

    @Bean(name = "indexStatusScans")
    SolrIndexingData indexStatusScans() {
        return new SolrIndexingData();
    }

    //currently there is no UI for this one / it is called with Solr update for Source.
    @Bean(name = "indexStatusPersons")
    SolrIndexingData indexStatusPersons() {
        return new SolrIndexingData();
    };

    public static void main(String[] args) {
        SpringApplication.run(DEMelWebApp.class, args);
    }

    public static class DEMelSessionLocaleResolver extends SessionLocaleResolver {

        private Function<HttpServletRequest, Locale> defaultLocaleFunction = request -> {
            Locale defaultLocale = getDefaultLocale();
            Locale locale = (defaultLocale != null ? defaultLocale : request.getLocale());
            if (locale != null && locale.getLanguage().equals(GERMAN_LOCALE.getLanguage())) {
                return GERMAN_LOCALE;
            }
            if (locale != null && locale.getLanguage().equals(ENGLISH_LOCALE.getLanguage())) {
                return ENGLISH_LOCALE;
            }
            return SPANISH_LOCALE;
        };

        public DEMelSessionLocaleResolver() {
            super();
            this.setDefaultLocaleFunction(defaultLocaleFunction);
        }
    }
}
