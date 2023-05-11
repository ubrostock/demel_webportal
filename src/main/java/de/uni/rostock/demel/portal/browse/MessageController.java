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
package de.uni.rostock.demel.portal.browse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni.rostock.demel.data.model.utility.Message;
import de.uni.rostock.demel.data.model.utility.Message.MessageStatus;
import de.uni.rostock.demel.data.service.db.MessageDBService;
import de.uni.rostock.demel.data.service.solr.SolrAttestationUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrLemmaUpdateService;
import de.uni.rostock.demel.data.service.solr.SolrSourceUpdateService;
import jakarta.validation.Valid;

@Controller
public class MessageController {

    @Autowired
    SolrAttestationUpdateService solrAttestationUpdateService;

    @Autowired
    SolrLemmaUpdateService solrLemmaUpdateService;

    @Autowired
    SolrSourceUpdateService solrSourceUpdateService;

    @Autowired
    MessageDBService messageDBService;

    @RequestMapping(value = "/api/messages/{objId}/show", method = RequestMethod.GET)
    public ModelAndView showMessages(@PathVariable(value = "objId", required = true) String objId) {
        ModelAndView mav = new ModelAndView("parts/container_messages");

        mav.addObject("message", createNewMessage(objId));
        mav.addObject("messages", messageDBService.queryMessagesByObjectId(objId));
        mav.addObject("count_public", messageDBService.countPublishedMessagesByObjectId(objId));

        return mav;
    }

    @RequestMapping(value = "/api/messages/{objid}/new", method = RequestMethod.POST)
    public ModelAndView submitNewMessageForm(@PathVariable(name = "objid", required = true) String objId,
        @Valid @ModelAttribute("message") Message msg, BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView("parts/container_messages");

        if (!messageDBService.hasObject(objId)) {
            bindingResult
                .addError(new ObjectError("message", "demel.message_form.error.global.notexists"));
        } else {
            msg.setCreatorName(StringUtils.truncate(StringUtils.trim(msg.getCreatorName()), 100));
            msg.setCreatorEmail(StringUtils.truncate(StringUtils.trim(msg.getCreatorEmail()), 100));
            msg.setContent(StringUtils.truncate(StringUtils.trim(msg.getContent()), 900));

            if (msg.getStatusEnum() == null) {
                msg.setStatusEnum(MessageStatus.INREVIEW);
            }

            if (!bindingResult.hasErrors()) {
                messageDBService.insertMessage(msg);
                updateParentObject(objId);
                String oldType = msg.getType();
                msg = createNewMessage(objId);
                msg.setType(oldType);
            }
        }
        mav.addObject("success", bindingResult.getErrorCount() == 0);
        mav.addObject("message", msg);
        mav.addObject("messages", messageDBService.queryMessagesByObjectId(objId));
        mav.addObject("count_public", messageDBService.countPublishedMessagesByObjectId(objId));
        return mav;
    }

    @RequestMapping(value = "/api/messages/{objid}/edit/{mid}",
        method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_MESSAGEEDITOR')")
    public ModelAndView showEditCommentForm(
        @PathVariable(name = "objid", required = true) String objId,
        @PathVariable(name = "mid", required = true) String msgId) {

        ModelAndView mav = new ModelAndView("parts/form_message");
        if ("new".equals(msgId)) {
            mav.addObject("message", createNewMessage(objId));
        } else {
            mav.addObject("message", messageDBService.queryObject(msgId));
        }
        return mav;
    }

    @RequestMapping(value = "/api/messages/{objid}/edit/{mid}",
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_MESSAGEEDITOR')")
    public ModelAndView submitEditMessageForm(
        @PathVariable(name = "objid", required = true) String objId,
        @PathVariable(name = "mid", required = true) String mId,
        @Valid @ModelAttribute("message") Message msg, BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView("parts/container_messages");

        if (!messageDBService.hasObject(objId)) {
            bindingResult
                .addError(new ObjectError("message", "demel.message_form.error.global.notexists"));
        } else {
            msg.setObjectIdAsString(objId);
            if (mId.equals(msg.getId()) && !bindingResult.hasErrors()) {
                messageDBService.updateMessage(msg);
                switch (msg.getObjectTypeEnum()) {
                    case LEMMA -> solrLemmaUpdateService.reindexObject(msg.getObjectIdAsString());
                    case ATTESTATION -> solrAttestationUpdateService.reindexObject(msg.getObjectIdAsString());
                    case SOURCE -> solrSourceUpdateService.reindexObject(msg.getObjectIdAsString());
                    case UNDOCUMENTED -> {
                    }
                }
            }
        }
        mav.addObject("success", bindingResult.getErrorCount() == 0);
        mav.addObject("message", msg);
        mav.addObject("messages", messageDBService.queryMessagesByObjectId(objId));
        mav.addObject("count_public", messageDBService.countPublishedMessagesByObjectId(objId));
        return mav;
    }

    private Message createNewMessage(String objectId) {
        Message msg = new Message();
        msg.setId("new");
        msg.setObjectIdAsString(objectId);
        msg.setStatusEnum(MessageStatus.INREVIEW);
        return msg;
    }

    private void updateParentObject(String objectId) {
        switch (Message.calcObjectTypeFromObjectId(objectId)) {
            case LEMMA -> solrLemmaUpdateService.reindexObject(objectId);
            case ATTESTATION -> solrAttestationUpdateService.reindexObject(objectId);
            case SOURCE -> solrSourceUpdateService.reindexObject(objectId);
            case UNDOCUMENTED -> {
            }
        }
    }
}
