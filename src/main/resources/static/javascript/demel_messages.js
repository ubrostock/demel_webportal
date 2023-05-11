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
function toggleMessageArea(button) {
  hidePopovers();

  let objId = button.dataset.objectId;
  let objType = button.dataset.objectType;
  //let tdImg = button.closest('td');
  if (button.classList.contains('active')) {
    button.classList.remove('active');
    document.querySelectorAll('#container_messages_' + objId).forEach((x) => {
      x.innerHTML = '';
    });
  } else {
    button.classList.add('active');
    createExtendedArea(objId);
    showMessages(objId, objType);
  }
}

function showMessages(objId, objType) {
  bootstrap.Collapse.getOrCreateInstance(`#${objType}_info_${objId}`).show();

  fetch(document.getElementsByName('demel-baseurl')[0].getAttribute('content') + "api/messages/" + objId + "/show")
    .then((resp) => resp.text())
    .then((data) => {
      document.querySelector('#container_messages_' + objId).innerHTML = data;
      document.querySelector("tr#tr_" + objId).scrollIntoView({ behavior: "smooth" }); //[0] gets the underlying HTML element
    });
}

function submitMessageForm(form, event) {
  let objId = form.dataset.objectId;
  let msgId = form.dataset.messageId;
  //write the editor data back to textarea (necessary, because this is a simulated submit via Javascript)
  document.querySelector("textarea#ta_messages_" + objId + "_" + msgId + "_content").value = trimTinyMCEContent(tinymce.get("ta_messages_" + objId + "_" + msgId + "_content").getContent());
  let formData = new URLSearchParams(new FormData(document.querySelector(`#form_message_${objId}_edit_${msgId}`)));

  fetch(form.getAttribute('action'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
    },
    body: formData
  })
    .then((resp) => resp.text())
    .then((data) => {
      let taId = "ta_messages_" + objId + "_" + msgId + "_content";
      document.querySelector("#container_messages_" + objId).innerHTML = data;
      document.querySelector("tr#tr_" + objId).scrollIntoView({ behavior: "smooth" });
      
      initMessageHtmlEditor(taId);
    });
  event.preventDefault();
}

function resetMessageForm(form, event) {
  let objId = form.dataset.objectId;
  showMessages(objId);
}

function toggleMessageForm(button) {
  hidePopovers();
  let objId = button.dataset.objectId;
  let msgId = button.dataset.messageId;
  if (button.classList.contains("active")) {
    button.classList.remove("active");
    if(msgId==='new') {
      document.querySelector("#container_messages_edit_"+objId+"_" + msgId).classList.add('d-none');
    } else{
      document.querySelector("#container_messages_edit_"+objId+"_" + msgId).innerHTML='';
    }
    document.querySelector("#container_messages_show_"+objId+"_" + msgId).classList.remove('d-none');
  } else {
    button.classList.add("active");
    if(msgId==="new"){
      document.querySelector("#container_messages_edit_"+objId+"_" + msgId).classList.remove('d-none');
      initMessageHtmlEditor("ta_messages_" + objId + "_" + msgId + "_content");
    } else {
      fetch(document.getElementsByName('demel-baseurl')[0].getAttribute('content') + "api/messages/" + objId + "/edit/" + msgId)
        .then((resp) => resp.text())
        .then((data) => {
          document.querySelector("#container_messages_show_" +objId+"_"+ msgId).classList.add('d-none');
          document.querySelector("#container_messages_edit_" +objId+"_" + msgId).innerHTML = data;
          let closest = document.querySelector("#container_messages_edit_"+objId+"_" + msgId).closest('.card');
          if(closest.length>0) {
            closest.scrollIntoView({ behavior: "smooth" });
          }
          initMessageHtmlEditor("ta_messages_" + objId + "_" + msgId + "_content");
        });
    }
  }
}

function createMessagesAreaToggleButton(id, type, i18nTitle, countMessagesPublished, countMessagesInreview, countMessages) {
  let out = 
    `<button type="button" class="btn btn-sm btn-outline-secondary text-nowrap"
             title="${i18nTitle}" id="button_messages_${id}" data-object-id="${id}" data-object-type="${type}"
             onclick="toggleMessageArea(this);">
       <i class="fa-regular fa-comments"></i>`

  if (countMessagesPublished != null && countMessagesPublished > 0) {
    out += `  <span> ${countMessagesPublished} </span>`;
  }
  if (Array.from(document.querySelectorAll("meta[name='demel:role']")).find(m => m.content === "ROLE_MESSAGEEDITOR")){
    if(countMessagesInreview != null && countMessagesInreview > 0) {
      out += `  <span class="text-white bg-danger px-1 ms-2"> ${countMessagesInreview} </span>`
    }
    if(countMessages != null && countMessages - countMessagesPublished - countMessagesInreview > 0) {
      out += `  <span class="text-warning ms-2"> ${countMessages - countMessagesPublished - countMessagesInreview} </span>`;
    }
  }
  out += `  </button>`;
  return out;
}

function initMessageHtmlEditor(taId){
  if (tinymce.get(taId)) {
    tinymce.get(taId).destroy();
  }
  let lang = document.getElementById('languageSelector').dataset.currentLang;
  if(lang=='en') { // default
    lang = null; 
  }
  tinymce.init({
    selector: "#"+taId,
    menubar:  false,
    language: lang,
    relative_urls : false,
    language_url : '/javascript/tinymce/languages/' + lang + '.js',
    plugins:  'link',
    height:   '16em',
    promotion: false,
    toolbar:  'insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link unlink',
    setup: (editor) => {
      editor.on('init', (e) => {
        e.target.contentAreaContainer.closest(".tox-tinymce").setAttribute("id", "tiny_"+e.target.id);
        if(document.getElementById(taId).classList.contains("is-invalid")) {
          e.target.contentAreaContainer.closest(".tox-tinymce").classList.add("form-control", "is-invalid");
        }
      });
    }
  });
}
