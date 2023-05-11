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
package de.uni.rostock.demel.portal.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class DEMelAuthenticationProvider implements AuthenticationProvider {
    private static final String SELECT_USER = "SELECT * FROM util_login WHERE user_id=? AND password_sha256=? LIMIT 1";

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials().toString();
        String passwordSha256 = sha256(password);
        try {
            UsernamePasswordAuthenticationToken token = jdbcTemplate.queryForObject(SELECT_USER,
                new RowMapper<UsernamePasswordAuthenticationToken>() {

                    @Override
                    public UsernamePasswordAuthenticationToken mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        String roleString = rs.getString("roles");
                        if (roleString != null) {
                            for (String r : roleString.split("\\|")) {
                                authorities.add(new SimpleGrantedAuthority(r));
                            }
                        }
                        return new UsernamePasswordAuthenticationToken(rs.getString("user_id"),
                            rs.getString("password_sha256"), authorities);
                    }
                }, username, passwordSha256);
            return token;
        } catch (DataAccessException e) {
            //no or multiple results
            throw new BadCredentialsException("authentication failed", e);
        }
    }

    public static String sha256(String password) {
        StringBuilder sb = new StringBuilder();
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
            for (byte b : hashbytes) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            // Does not happen
        }
        return sb.toString();
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }

}
