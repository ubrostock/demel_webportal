-- ============================================================================
-- = DEMel Database Tables
-- ============================================================================
-- = This file is part of the DEMel web application. 
-- = 
-- = Copyright 2023
-- = DEMel project team, Rostock University, 18051 Rostock, Germany
-- = 
-- = Licensed under the Apache License, Version 2.0 (the "License");
-- = you may not use this file except in compliance with the License.
-- = You may obtain a copy of the License at
-- = 
-- = http://www.apache.org/licenses/LICENSE-2.0
-- = 
-- = Unless required by applicable law or agreed to in writing, software
-- = distributed under the License is distributed on an "AS IS" BASIS,
-- = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- = See the License for the specific language governing permissions and
-- = limitations under the License.
-- ============================================================================

-- ----------------------------------------------------------------------------
--   Dictionary
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `demel_reloaded`.`dict_lemma` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `type` ENUM('lemma', 'linklemma') NOT NULL,
  `name` VARCHAR(256) NOT NULL,
  `name_variants` VARCHAR(512) NOT NULL,
  `part_of_speech` SET('adj', 'adv', 'art', 'gerund', 'interj', 'conj', 'name_all', 'name_anthropo', 'name_topo', 'name_other', 'part', 'prep', 'pron', 'subst_all', 'subst_f', 'subst_f_pl', 'subst_m', 'subst_m_pl', 'subst', 'verb_all', 'verb', 'verb_prnl', 'undocumented') DEFAULT NULL,
  `hints_extern` VARCHAR(1024) DEFAULT NULL,
  `status` ENUM('published', 'deleted') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`dict_attestation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `type` ENUM('primary', 'secondary', 'lemmalink', 'undocumented') NOT NULL,
  `form` VARCHAR(256) DEFAULT NULL,
  `lemma_id` INT NOT NULL,
  `multiwordexpr` VARCHAR(256) DEFAULT NULL,
  `dating_origin` ENUM('scan','primary_source','secondary_source','undocumented') NOT NULL,
  `dating_display` VARCHAR(128) DEFAULT NULL,
  `dating_from` SMALLINT DEFAULT NULL,
  `dating_to` SMALLINT DEFAULT NULL,
  `source_id` INT NOT NULL,
  `lemmalink_id` INT DEFAULT NULL,
  `status` ENUM('published', 'deleted') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_lemma_id` (`lemma_id`),
  KEY `IDX_source_id` (`source_id`),
  KEY `IDX_type` (`type`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`dict_source` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `type` ENUM('primary','secondary','journal','undocumented') NOT NULL,
  `dating_display` VARCHAR(128) DEFAULT NULL,
  `dating_from` SMALLINT DEFAULT NULL,
  `dating_to` SMALLINT DEFAULT NULL,
  `dating_unique` BOOLEAN DEFAULT NULL,
  `texttypes` SET('prose','verse','undocumented') DEFAULT NULL,
  `genre` ENUM('dramatic','epic','lyric','non_fictional','undocumented') DEFAULT NULL,
  `subgenre` ENUM('scientific','historical','legal','leisure','moralizing','religious','undocumented') DEFAULT NULL,
  `languages` SET('aljamiado','castilian','galician','latin','leonese','navarro_aragonese','undocumented') DEFAULT NULL,
  `hsms_ids` VARCHAR(128) DEFAULT NULL COMMENT 'multiple values, separated by `|`',
  `beta_ids` VARCHAR(128) DEFAULT NULL COMMENT 'multiple values, separated by `|`',
  `status` ENUM('published', 'deleted') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`util_message` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `object_type` ENUM('lemma','attestation','source', 'undocumented') NOT NULL,
  `object_id` INT NOT NULL,
  `type` ENUM('official_comment', 'official_error', 'official_revision', 'user_comment', 'user_error', 'undocumented') NOT NULL,
  `created` TIMESTAMP NOT NULL,
  `creator_name` VARCHAR(128) DEFAULT NULL,
  `creator_email` VARCHAR(128) DEFAULT NULL,
  `content` VARCHAR(4096) NOT NULL DEFAULT '',
  `status` ENUM('published', 'deleted', 'invisible', 'inreview') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_objects` (`object_type`,`object_id`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`map_lemma_lemma` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `lemma_id` INT NOT NULL,
  `target_lemma_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_lemma_id` (`lemma_id`),
  KEY `IDX_target_lemma_id` (`target_lemma_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
--   Digitization
-- ----------------------------------------------------------------------------   
CREATE TABLE if NOT EXISTS `demel_reloaded`.`digi_box` (
  `id` SMALLINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(256) NOT NULL,
  `name_es` VARCHAR(256) NOT NULL,
  `section_www` VARCHAR(128) DEFAULT NULL,
  `section_www_sort` SMALLINT DEFAULT NULL,
  `mods_title` VARCHAR(256) DEFAULT NULL,
  `mods_subtitle` VARCHAR(256) DEFAULT NULL,
  `mods_abstract` VARCHAR(2048) DEFAULT NULL,
  `mods_relateditem` VARCHAR(256) DEFAULT NULL,
  `status` ENUM('included', 'excluded', 'integrateable', 'undocumented') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10001;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`digi_scan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `filename` VARCHAR(64) NOT NULL,
  `contentid` VARCHAR(64) NOT NULL,
  `box_id` SMALLINT NOT NULL,
  `sort` SMALLINT NOT NULL,
  `rotation` SMALLINT NOT NULL DEFAULT '0',
  `type` ENUM('lemmasection_title', 'attestation', 'separator', 'oversize_placeholder', 'other', 'undocumented') NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_card_filename` (`filename`),
  KEY `IDX_card_contentid` (`contentid`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`map_attestation_scan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `attestation_id` INT NOT NULL,
  `scan_id` INT NOT NULL,
  `scan_sort` SMALLINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_scan_id` (`scan_id`),
  KEY `IDX_attestation_id` (`attestation_id`),
  KEY `IDX_scan_sort` (`scan_sort`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
--   Bibliography
-- ----------------------------------------------------------------------------   

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`bibl_sigle` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `type` ENUM('main', 'variant') NOT NULL,
  `source_id` INT NOT NULL,
  `sigle_search` VARCHAR(128) NOT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `IDX_source_id` (`source_id`),
  INDEX `IDX_sigle_search` (`sigle_search`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`bibl_edition` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title_info` VARCHAR(1024) NOT NULL,
  `source_id` INT NOT NULL DEFAULT '0',
  `sort` SMALLINT NOT NULL,
  `dating_from` SMALLINT DEFAULT NULL,
  `dating_to` SMALLINT DEFAULT NULL,
  `bne_id` VARCHAR(128) DEFAULT NULL,
  `source_host_id` INT DEFAULT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`bibl_reproduction` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `online_url` VARCHAR(256) NOT NULL,
  `provider` VARCHAR(128) NOT NULL,
  `access` ENUM('free', 'restricted') DEFAULT NULL,
  `edition_id` INT NOT NULL DEFAULT '0',
  `sort` SMALLINT NOT NULL,
  `hints_extern` VARCHAR(1024) DEFAULT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`bibl_person` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name_display` VARCHAR(256) NOT NULL,
  `viaf_id` VARCHAR(128) DEFAULT NULL,
  `gnd_id` VARCHAR(128) DEFAULT NULL,
  `bne_id` VARCHAR(128) DEFAULT NULL,
  `wikidata_id` VARCHAR(128) DEFAULT NULL,
  `hints_intern` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;


-- CREATE TABLE IF NOT EXISTS `demel_reloaded`.`map_edition_person` (
--   `id` INT NOT NULL AUTO_INCREMENT,
--   `edition_id` INT NOT NULL,
--   `person_id` INT NOT NULL,
--   PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`map_source_person` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `source_id` INT NOT NULL,
  `person_id` INT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
--   Utility Tables
-- ----------------------------------------------------------------------------   
CREATE TABLE IF NOT EXISTS `demel_reloaded`.`util_webcontent` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`code` VARCHAR(64) NOT NULL,
	`lang` VARCHAR(4) NOT NULL DEFAULT 'de',
	`version` SMALLINT NOT NULL DEFAULT '0',
	`created` TIMESTAMP NOT NULL,
	`content` LONGTEXT NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `UNIQUE_lang_code_version` (`lang`, `code`, `version`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`util_history` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`object_type` ENUM('lemma','attestation') NOT NULL,
	`object_id` INT NOT NULL,
	`user` VARCHAR(64) DEFAULT NULL,
	`created` TIMESTAMP NOT NULL,
--	`field` VARCHAR(64) NOT NULL,
	`field` ENUM('attestation__form','attestation__lemma_id','attestation__multiwordexpr',
	             'attestation__dating_display','attestation__dating_from','attestation__dating_to',
	             'attestation__source_id','attestation__lemmalink_id','attestation__hints_intern',
	             'lemma__name','lemma__name_variants','lemma__part_of_speech',
	             'lemma__hints_extern','lemma__hints_intern') NOT NULL,
	`old_value` VARCHAR(512) DEFAULT NULL,
	`new_value` VARCHAR(512)  DEFAULT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `demel_reloaded`.`util_login` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`user_id` VARCHAR(64) NOT NULL,
	`user_name` VARCHAR(128) NOT NULL,
	`user_email` VARCHAR(256) NOT NULL,
	`roles` VARCHAR(256) DEFAULT NULL,
	`password_sha256` CHAR(64) NOT NULL,
	`password_reset_token` VARCHAR(128) DEFAULT NULL,
	`hints_intern` VARCHAR(1024) DEFAULT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `util_vocabulary` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`type` VARCHAR(64) NOT NULL,
	`code` VARCHAR(64) NOT NULL,
	`sort` SMALLINT NOT NULL,
	`abbr_de` VARCHAR(64) NULL DEFAULT NULL,
	`term_de` VARCHAR(256) NULL DEFAULT NULL,
	`abbr_es` VARCHAR(64) NULL DEFAULT NULL,
	`term_es` VARCHAR(256) NULL DEFAULT NULL,
	`abbr_en` VARCHAR(64) NULL DEFAULT NULL,
	`term_en` VARCHAR(256) NULL DEFAULT NULL,
	`hints_intern` VARCHAR(1024) NULL DEFAULT NULL,
	PRIMARY KEY (`id`) USING BTREE
)
ENGINE=InnoDB;
