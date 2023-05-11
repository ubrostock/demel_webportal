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
package de.uni.rostock.demel.data.service.db;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

import de.uni.rostock.demel.data.model.utility.Message;
import de.uni.rostock.demel.data.model.utility.Message.ObjectType;
import de.uni.rostock.demel.data.model.utility.MessageRowMapper;

@Service
public class MessageDBService {
    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MessageRowMapper rowMapper;

    private static Map<Message.ObjectType, String> TABLES = Map.ofEntries(
        Map.entry(Message.ObjectType.ATTESTATION, "dict_attestation"),
        Map.entry(Message.ObjectType.LEMMA, "dict_lemma"),
        Map.entry(Message.ObjectType.SOURCE, "dict_source"));

    private static final String SQL_INSERT_REVISION_MESSSAGE = "INSERT INTO util_message VALUES (?,?,?,?,?, ?,?,?,?,?)";

    public Message queryObject(String idParam) {
        int id = Integer.parseInt(idParam.replace(Message.DOCID_PREFIX, ""));
        Message m = jdbcTemplate.queryForObject("SELECT * FROM util_message WHERE id =?",
            rowMapper, id);
        return m;
    }

    public void createRevisionMessage(String objectId, String content) {
        String messageContent = StringUtils.abbreviate(content.trim(), 4096);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (StringUtils.isNotBlank(messageContent)) {
            jdbcTemplate.update(SQL_INSERT_REVISION_MESSSAGE,
                new SqlParameterValue(Types.INTEGER, null),
                new SqlParameterValue(Types.VARCHAR, Message.calcObjectTypeFromObjectId(objectId).getValue()),
                new SqlParameterValue(Types.INTEGER, Long.parseLong(objectId.substring(1))),
                new SqlParameterValue(Types.VARCHAR, Message.MessageType.OFFICIAL_REVISION.getValue()),
                new SqlParameterValue(Types.TIMESTAMP, now),
                new SqlParameterValue(Types.VARCHAR, "Equipo del DEMel"),
                new SqlParameterValue(Types.VARCHAR, ""),
                new SqlParameterValue(Types.VARCHAR, messageContent),
                new SqlParameterValue(Types.VARCHAR, "published"),
                new SqlParameterValue(Types.VARCHAR, null));
        }
    }

    public void insertMessage(Message msg) {
        jdbcTemplate.update("INSERT INTO util_message VALUES (NULL, ?, ?, ?, NOW(), ?, ?, ?, ?, NULL);",
            msg.getObjectType(), msg.getObjectId(), msg.getType(), msg.getCreatorName(),
            msg.getCreatorEmail(),
            msg.getContent(), msg.getStatus());
    }

    public void updateMessage(Message msg) {
        Long msgId = Long.parseLong(msg.getId().replace(Message.DOCID_PREFIX, ""));
        jdbcTemplate.update(
            "UPDATE util_message SET type=?, creator_name=?, creator_email=?, content=?, status=? WHERE id=?",
            msg.getType(), msg.getCreatorName(), msg.getCreatorEmail(), msg.getContent(), msg.getStatus(),
            msgId);
    }

    public List<Message> queryMessagesByObjectId(String objId) {
        String objectType = Message.calcObjectTypeFromObjectId(objId).getValue();
        long objectID = Long.parseLong(objId.substring(1));
        List<Message> messages = jdbcTemplate.query(
            "SELECT * FROM util_message WHERE object_type=? and object_id=? ORDER BY created DESC",
            new MessageRowMapper(), objectType, objectID);
        return messages;
    }

    public long countPublishedMessagesByObjectId(String id) {
        String objectType = Message.calcObjectTypeFromObjectId(id).getValue();
        long objectID = Long.parseLong(id.substring(1));

        Long count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM util_message WHERE status='published' AND object_type=? AND object_id=?",
            Long.class,
            objectType, objectID);
        return count == null ? 0 : count.longValue();
    }

    public boolean hasObject(String objId) {
        ObjectType objectType = Message.calcObjectTypeFromObjectId(objId);
        long objectId = Long.parseLong(objId.substring(1));
        Boolean b = jdbcTemplate.queryForObject(
            "SELECT count(*) = 1 FROM " + TABLES.get(objectType) + " WHERE id=?",
            Boolean.class, objectId);
        return b == null ? false : b.booleanValue();
    }
}
