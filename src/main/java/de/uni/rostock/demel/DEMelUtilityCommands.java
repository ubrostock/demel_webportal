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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import de.uni.rostock.demel.data.model.dictionary.Source;
import de.uni.rostock.demel.data.service.solr.SolrAttestationUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrSourceQueryService;
import de.uni.rostock.demel.data.service.solr.SolrSourceUpdateService;

//@SpringBootApplication
/**
 * Application should be run with property as parameter: --spring.main.web-application-type=NONE
 * 
 * @author Robert Stephan
 *
 */
public class DEMelUtilityCommands
    implements CommandLineRunner {

    @Autowired
    SolrAttestationUpdateService attUpdService;

    @Autowired
    SolrSourceUpdateService sourceUpdService;

    @Autowired
    SolrSourceQueryService sourceQueryService;

    private static Logger LOG = LoggerFactory
        .getLogger(DEMelUtilityCommands.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(DEMelUtilityCommands.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");
        //   sourceUpdService.reindexAllBibitems();
        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }

        Source s = sourceQueryService.findById("b0011");
        System.out.println(s);
    }
}
