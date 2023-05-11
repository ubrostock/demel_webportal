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

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Spring based LoginController
 * 
 * https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html
 *
 */
@Controller
public class LoginController {
    private static final String SQL_COUNT_RESET_TOKEN
        = "SELECT COUNT(*) from util_login WHERE password_reset_token = ?";

    private static final String SQL_UPDATE_RESET_TOKEN
        = "UPDATE util_login SET password_reset_token = ? WHERE user_id = ?;";

    private static final String SQL_SELECT_FOR_EMAIL
        = "SELECT user_id, user_name, user_email, password_reset_token FROM util_login WHERE user_id = ?;";

    private static final String SQL_UPATE_PASSWORD
        = "UPDATE util_login SET password_sha256 = ?, password_reset_token=null WHERE password_reset_token = ?;";

    @Autowired
    @Qualifier("jdbcDemel")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ApplicationContext context;

    @Value("${demel.mail.from}")
    private String demelMailFrom;

    @RequestMapping(value = "/login")
    public ModelAndView login() {
        return new ModelAndView("login/login");
    }

    @RequestMapping(value = "/admin/login/password", method = RequestMethod.GET)
    public String password() {
        return "login/password_request";
    }

    @RequestMapping(value = "/admin/login/password", method = RequestMethod.GET, params = "token")
    public ModelAndView passwordForm(@RequestParam(value = "userid", required = true) String userid,
        @RequestParam(value = "token", required = true) String token) {
        ModelAndView mav = new ModelAndView("login/password_form");
        mav.addObject("requestParamUserid", userid);
        mav.addObject("requestParamToken", token);

        return mav;
    }

    @RequestMapping(value = "/admin/login/password", method = RequestMethod.POST, params = "do_set_password")
    public ModelAndView resetPassword(@RequestParam(value = "password_reset_token", required = true) String token,
        @RequestParam(value = "password", required = true) String password,
        HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        if (validateToken(token, mav)) {
            jdbcTemplate.update(SQL_UPATE_PASSWORD, DEMelAuthenticationProvider.sha256(password), token);
            mav.setViewName("login/password_success");
        } else {
            mav.setViewName("login/login");
        }
        return mav;
    }

    @RequestMapping(value = "/admin/login/password", method = RequestMethod.POST, params = "do_password_reset")
    public ModelAndView passwordReset(@RequestParam(value = "userid", required = true) String userid) {
        createToken(userid);
        final AtomicBoolean found = new AtomicBoolean(false);
        jdbcTemplate.query(SQL_SELECT_FOR_EMAIL, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                sendEmail(rs.getString("user_id"), rs.getString("user_name"), rs.getString("user_email"),
                    rs.getString("password_reset_token"));
                found.set(true);
            }
        }, userid);
        if (found.get()) {
            return new ModelAndView("login/email_success");
        } else {
            ModelAndView mav = new ModelAndView("login/password_request");
            mav.addObject("errorMessage",
                context.getMessage("demel.login.error_message.unknown_user", null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    private boolean validateToken(String token, ModelAndView mav) {
        Integer x = jdbcTemplate.queryForObject(SQL_COUNT_RESET_TOKEN, Integer.class, token);
        if (x != null && x == 1 && token.contains("_")) {
            String timeString = token.substring(0, token.indexOf("_"));
            long time = Long.parseLong(timeString);
            if (time < System.currentTimeMillis()) {
                mav.addObject("errorMessage", context.getMessage("demel.password.error_message.invalid_time", null,
                    LocaleContextHolder.getLocale()));
                return false;
            } else {
                return true;
            }
        } else {
            mav.addObject("errorMessage", context.getMessage("demel.password.error_message.invalid_token", null,
                LocaleContextHolder.getLocale()));
            return false;
        }
    }

    private void sendEmail(String userid, String name, String email, String token) {
        try {
            UriComponents uriComponents = MvcUriComponentsBuilder.fromMethodName(LoginController.class, "password")
                .queryParam("userid", userid)
                .queryParam("token", token).build();
            URI uri = uriComponents.encode().toUri();
            MimeMessageHelper mailHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
            mailHelper.setFrom(demelMailFrom);
            mailHelper.setTo(email);
            mailHelper.setSubject(
                context.getMessage("demel.email.password_reset.subject", null, LocaleContextHolder.getLocale()));
            mailHelper
                .setText(context.getMessage("demel.email.password_reset.body", null, LocaleContextHolder.getLocale())
                    + "\n" + uri.toString());
            mailSender.send(mailHelper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void createToken(String username) {
        String token = Long.toString(System.currentTimeMillis() + 10 * 60 * 1000) + "_" + UUID.randomUUID().toString();
        jdbcTemplate.update(SQL_UPDATE_RESET_TOKEN, token, username);
    }
}
