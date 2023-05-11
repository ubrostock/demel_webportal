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
package de.uni.rostock.demel.data.model.utility;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageRowMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        Message u = new Message();
        u.setId(rs.getLong("id"));
        u.setObjectType(rs.getString("object_type"));
        u.setObjectId(rs.getLong("object_id"));
        u.setType(rs.getString("type"));
        u.setCreated(rs.getTimestamp("created").toInstant());
        u.setCreatorName(rs.getString("creator_name"));
        u.setCreatorEmail(rs.getString("creator_email"));
        u.setContent(rs.getString("content"));
        u.setStatus(rs.getString("status"));
        u.setHintsIntern(rs.getString("hints_intern"));

        return u;
    }

}
