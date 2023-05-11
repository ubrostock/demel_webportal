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
package de.uni.rostock.demel.portal.browse.admin;

import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class IPCheckController {

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @RequestMapping(value = "/browse/admin/ipcheck", method = RequestMethod.GET)
    @ResponseBody
    public String get(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        sb.append(
            "<!DOCTYPE html>"
                + "\n  <html lang=\"en\">"
                + "\n    <head>"
                + "\n    <meta charset=\"utf-8\">"
                + "\n    <title>DEMel - IPCheck</title>"
                + "\n  </head>"
                + "\n  <body>");
        sb.append("\n    <h1>DEMel: IP-Informationen</h1>");

        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();

        if (details instanceof WebAuthenticationDetails wa) {
            wa = (WebAuthenticationDetails) details;
            sb.append("<h2>WebAuthenicationDetails</h2>");
            sb.append("<table><tr><th>RemoteIP:</th><td>");
            sb.append(wa.getRemoteAddress());
            sb.append("</td></tr></table>");
        }
        sb.append("<h2>RequestInformation</h2>");
        sb.append("<table>");
        sb.append("<tr><th>RemoteAddr:</th><td>").append(request.getRemoteAddr()).append("</td></tr>");
        sb.append("<tr><th>RemoteHost:</th><td>").append(request.getRemoteHost()).append("</td></tr>");
        sb.append("<tr><th>RemotePort:</th><td>").append(request.getRemotePort()).append("</td></tr>");
        sb.append("<tr><th>LocalAddr:</th><td>").append(request.getLocalAddr()).append("</td></tr>");
        sb.append("<tr><th>LocalName:</th><td>").append(request.getLocalName()).append("</td></tr>");
        sb.append("<tr><th>LocalPort:</th><td>").append(request.getLocalPort()).append("</td></tr>");

        sb.append("<tr><th>ContextPath:</th><td>").append(request.getContextPath()).append("</td></tr>");
        sb.append("</table>");

        sb.append("<h2>RequestHeader</h2>");
        sb.append("<table>");
        Enumeration<String> enumHeaders = request.getHeaderNames();
        while (enumHeaders.hasMoreElements()) {
            String h = enumHeaders.nextElement();
            sb.append("<tr><th>").append(h).append(": </th><td>")
                .append(StringUtils.join(request.getHeader(h), "<br />")).append("</td></tr>");

        }
        sb.append("</table>");
        sb.append("\n  </body>\n</html>");

        return sb.toString();
    }

}
