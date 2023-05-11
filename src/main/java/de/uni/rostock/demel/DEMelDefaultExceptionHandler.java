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

import java.io.IOException;
import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DEMelDefaultExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DEMelDefaultExceptionHandler.class);

    @ExceptionHandler({ IOException.class, ClientAbortException.class })
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public void clientAbortExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        // This usually means the browser closed or disconnected or
        // something. We can't do anything. To avoid excessive stack traces
        // in log, just print a simple message and return null
        String username = "<NONE>";
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            username = principal.getName();
        }
        LOGGER.warn("ClientAbortException: username={},remoteAddr={},userAgent={},requestedURL={}", username,
            request.getRemoteAddr(), request.getHeader("User-Agent"), request.getRequestURL());

        //return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }
}
