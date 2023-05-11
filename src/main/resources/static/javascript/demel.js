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
function encodeHTMLEntities(rawStr) {
  return rawStr.replace(/[\u00A0-\u9999<>\&]/g, ((i) => `&#${i.charCodeAt(0)};`));
}

// input HTML string representing a single element
// returns an Element
function htmlToElement(html) {
    var template = document.createElement('template');
    template.innerHTML = html.trim();
    return template.content.firstChild;
}

//returns the form data as URL param string
//e.g.:  'lemma_object=l00038411&lemma_object=l00032381&type%5B%5D=lemma'
function retrieveFormParams(formQuerySelector) {
  let formData = new FormData(document.querySelector(formQuerySelector));
  let formParams = new URLSearchParams(formData);
  let cleanFormParams = new URLSearchParams();
  formParams.forEach((value, key) => {
    if(value!==''){
      cleanFormParams.append(key, value);
    }
  });
  return cleanFormParams.toString(); 
}

function initPopovers(node) {
  let n = node !== undefined ? node : document;
  n.querySelectorAll('[data-bs-toggle="popover-help"]').forEach((button) => {
    let p = bootstrap.Popover.getOrCreateInstance(button, createPopoverHelpOptions());
    button.addEventListener('shown.bs.popover', ()=>  {
      addCloseButton(p);
    });
  });

  n.querySelectorAll('[data-bs-toggle="popover-cite"]').forEach((button) => {
    let popover = bootstrap.Popover.getOrCreateInstance(button, createPopoverCiteOptions(button));
    button.addEventListener('shown.bs.popover', ()=>  {
      showCitePopover(button, popover);
    });
  });

  n.querySelectorAll('[data-bs-toggle="popover-info"]').forEach((button) => {
    let popover = bootstrap.Popover.getOrCreateInstance(button, createPopoverInfoOptions());
    button.addEventListener('shown.bs.popover', () =>  {
      showInfoPopover(button, popover);
    });

    button.addEventListener('hidden.bs.popover', () =>  {
      button.classList.remove("active");
    });
  });
}

function createPopoverHelpOptions() {
  return {
    template: `<div class="popover popover-help" role="popover">
                 <div class="popover-arrow"></div>
                 <h3 class="popover-header"></h3>
                 <div class="popover-body"></div>
               </div>`,
    container: 'body',
    html: true
    }
  }

function buttonCiteWithPopover(objectId, objectType, i18n) {
  return `<button type="button" id="btn_cite_${objectId}" class="btn demel-btn-cite" data-bs-toggle="popover-cite"
                  data-bs-title="${i18n.label} ${objectId}" data-object-id="${objectId}" data-object-type="${objectType}"> 
            <i class="fa-solid fa-link" title="${i18n.purl}"></i> 
          </button>`;
}

function createPopoverCiteOptions(btn) {
  return {
    template: `<div class="popover popover-cite popover-cite-${btn.dataset.objectType}" role="popover">
                 <div class="popover-arrow"></div>
                 <h3 class="popover-header"></h3>
                 <div class="popover-body"></div>
               </div>`,
    content: '&nbsp;',
    container: 'body',
    placement: 'left',
    html: true
  };
}

function showCitePopover(btn, popover) {
  fetch(`../../api/popover/cite/${btn.dataset.objectType}/${btn.dataset.objectId}`)
    .then((response) => response.text())
    .then((text) => {
      let divId = btn.getAttribute('aria-describedby');
      let popoverBody = document.getElementById(divId).querySelector(".popover-body");
      popoverBody.innerHTML = text;
      addCloseButton(popover);
    });
}

function createPopoverInfoOptions() {
  return {
    template: `<div class="popover popover-info" role="popover">
                 <div class="popover-arrow"></div>
                 <h3 class="popover-header"></h3>
                 <div class="popover-body"></div>
               </div>`,
    content: '&nbsp;',
    container: 'body',
    html: true
    }
  };

function showInfoPopover(btn, popover) {
  btn.classList.add('active');
  let objectType = btn.getAttribute('data-object-type');
  let objectId = btn.getAttribute('data-object-id');
  let infoBoxUrl = `api/infobox/${objectType}/${objectId}` ;

  fetch(document.getElementsByName('demel-baseurl')[0].getAttribute('content') + infoBoxUrl)
    .then((response) => response.text())
    .then((text) => {
      let divId = btn.getAttribute('aria-describedby');
      let popoverBody = document.getElementById(divId).querySelector(".popover-body");
      popoverBody.style.width = '525px';
      popoverBody.innerHTML = text;
      addCloseButton(popover);
    });
}

function addCloseButton(popover) {
  let popoverHeader = popover.tip.querySelector('.popover-header, .card-header');
  if(popoverHeader.querySelector('.btn-close') == null) {
	popoverHeader.insertAdjacentHTML('afterbegin' , "<button type='button' class='btn-close' aria-label='Close'></button>");
    popoverHeader.querySelector('.btn-close').addEventListener("click", function() {
      popover.hide();
    });
  }
}

function initTinyMCERevisionMessage(taId) {
  if (!tinymce.get(taId)) {
    let lang = document.getElementById('languageSelector').dataset.currentLang;
    if(lang=='en') { // default
      lang = null;
    }
    tinymce.init({
      selector: 'textarea#'+taId,
      language: lang,
      language_url: '/javascript/tinymce/languages/' + lang + '.js',
      relative_urls: false,
      menubar:  false,
      plugins:  'link',
      height:   '12em',
      toolbar:  'insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link unlink',
    });
  }
}

function trimTinyMCEContent(content){
  let c = content;
  while(c.startsWith("<p>&nbsp;")){
    c = "<p>"+c.substring(9).trim();
  }
  while(c.endsWith("&nbsp;</p>")){
    c = c.substring(0, c.length-10).trim()+ "</p>";
  }
  return c;
}

// attention: one scan could be displayed multiple times, because it contains multiple attestations
function displayScans(htmlContainer, i18n, baseURL, attestationID, scanIDs) {
  fetch(baseURL+"api/data/json/scans?attestation="+attestationID)
    .then((response) => response.json())
    .then((data) => {
      let sortedCards = new Array(data.results.length);
      data.results.forEach((scan) => {
        sortedCards[scanIDs.indexOf(scan.id)] = scan;
      });

      let doroUrl = document.querySelector("meta[name='demel-dorourl']").content;
      sortedCards.forEach((scan, index) => {
	   setTimeout(function() {
		let divScan = document.createElement("div");
		divScan.setAttribute("id", `div_scan_${attestationID}_${scan.id}`);
		htmlContainer.appendChild(divScan);
		
        let imageURL = `${doroUrl}depot/demel/${scan.boxId}/images_old/${scan.filename}.jpg`;
        if(scan.contentid != null) {
          /* fallback use depot api for unrotated images, 
          //since there seems to be problems loading images via DORO IIIF-API
          imageURL = doroUrl + "depot/demel/" + scan.boxId + "/images/" + scan.contentid.substring(6) + ".jpg";
          if(scan.rotation > 0){
            var iiifId = "demel/" + scan.contentid;
            imageURL = doroUrl + "api/iiif/image/v2/demel%252F" + scan.contentid.replace("/", "%252F")+"/full/full/" + scan.rotation + "/default.jpg"
          }
          */
          imageURL = `${doroUrl}api/iiif/image/v2/demel%252F${scan.contentid.replace("/", "%252F")}/full/full/${scan.rotation}/default.jpg`;
        }

        if(index==0) {
          let btnCloseHtml = `<button class="btn btn-sm btn-secondary demel-btn-close-scans text-danger float-end ms-2 mb-2" type="button" 
	                                  onclick="toggleScans('${attestationID}')" value="${scan.filename}" 
	                                  title="${i18n.demel_attestations_scans_button_close}"><i class="fa-solid fa-xmark"></i></button>`;
          divScan.insertAdjacentHTML('beforeend', btnCloseHtml);
        }

        if (scan.attestationIds.length>1) {
          query="&filter=published";
          scan.attestationIds.forEach((a) => {
            query = query + "&att_object="+a;
          });
          let btnShowAttestationsHtml = 
            `<a class="btn btn-sm btn-secondary float-end ms-2 mb-2" href="${baseURL}browse/attestations?${query}" value="${scan.filename}" 
                title="${i18n.demel_attestations_scans_button_other_attestations}"><i class="fa-regular fa-file-lines"></i></a>`
          divScan.insertAdjacentHTML('beforeend', btnShowAttestationsHtml);
        }

        let btnOpenHtml = 
          `<a class="btn btn-sm btn-secondary float-end ms-2 mb-2" id="btn_open_${attestationID}_${scan.id}" title="${i18n.demel_attestations_scans_button_enlarge}" 
              href="${imageURL}" target="_blank"><i class="fa-solid fa-expand"></i></a>`;
        divScan.insertAdjacentHTML('beforeend', btnOpenHtml);
        if(scan.contentid) {
          let btnRotateHtml = 
            `<button class="btn btn-sm btn-secondary float-end mb-2 ms-2 demel-btn-rotate" type="button" onclick="rotateImage(this)" 
                     title="${i18n.demel_attestations_scans_button_rotate}" data-scan-id="${scan.id}" data-attestation-id="${attestationID}"><i class="fa-solid fa-arrow-rotate-right"></i></button>`;
          divScan.insertAdjacentHTML('beforeend', btnRotateHtml);
        }
        let btnCiteHtml = buttonCiteWithPopover(scan.id, 'scan', {label: i18n.demel_attestations_table_scan, purl: i18n.demel_javascript_citelink});
        //make id unique
        btnCiteHtml = btnCiteHtml.replace(`id="btn_cite_${scan.id}"`, `id="btn_cite_${attestationID}_${scan.id}"`)
        let btnCite = htmlToElement(btnCiteHtml); 
        divScan.appendChild(btnCite);
        btnCite.classList.add("float-end", "demel-btn-cite", "p-1");
        bootstrap.Popover.getOrCreateInstance(btnCite, createPopoverCiteOptions(btnCite));

        let imgHtml = `<img class="demel-image mb-2" id="img_${attestationID}_${scan.id}" src="${imageURL}" width="100%" onerror="loadImageAgain(this)" 
                            data-zoom-image="${imageURL}" data-rotation="${scan.rotation}" data-contentid="${scan.contentid}"></img>`;
        let img = htmlToElement(imgHtml);
        divScan.appendChild(img);
        initEzPlus(img);

        initPopovers(divScan);

        if(index < data.length - 1){
          htmlContainer.insertAdjacentHTML('beforeend', '<hr></hr>');
        }
       }, index * 50); //end of setTimeout
      });
    });
}

function rotateImage(src) {
  let scanId = src.dataset.scanId;
  let attId = src.dataset.attestationId;
  let img = document.getElementById(`img_${attId}_${scanId}`);
  let r = (parseInt(img.dataset.rotation) + 90) % 360;
  let doroUrl = document.querySelector("meta[name='demel-dorourl']").content;
  let imgURL = `${doroUrl}api/iiif/image/v2/demel%252F${img.dataset.contentid.replace("/", "%252F")}/full/full/${r}/default.jpg`;
  document.getElementById(`btn_open_${attId}_${scanId}`)?.setAttribute("href", imgURL);
  img.setAttribute("data-rotation", r);
  img.setAttribute("data-zoom-image", imgURL);
  if($(img).data('ezPlus') != null) {
    $(img).data('ezPlus').closeAll();
  }
  img.setAttribute("src", imgURL);
  initEzPlus(img);
}

function loadImageAgain(img) {
  if(img.src != window.location.href){
    //we have a real image
    console.log("error loading image, will try again for: " + img.src);
    let c = img.dataset.retryCount | 0;
    if(c<4){
      // delay reload time + add random ms to reduce parallel image loading
      let wait_in_ms = (c+1)* 500 + Math.floor(Math.random() * 500); 
      setTimeout(function(){
        img.setAttribute("data-retry-count", c++);
        let source = img.src;
        img.src = source;
        initEzPlus(img);
      }, wait_in_ms);
    }
  }
}

function initEzPlus(img) {
  //wait until the image is fully loaded
  img.decode().then(() => {
    let w = document.querySelector('table#datatable_attestations th:nth-child(3)')?.offsetWidth
          + document.querySelector('table#datatable_attestations th:nth-child(4)')?.offsetWidth
          + document.querySelector('table#datatable_attestations th:nth-child(5)')?.offsetWidth
          + document.querySelector('table#datatable_attestations th:nth-child(6)')?.offsetWidth
          + document.querySelector('table#datatable_attestations th:nth-child(7)')?.offsetWidth
    if (isNaN(w)) {
      //table not found on page
      w = img.clientWidth;
    }
    $(img).ezPlus({
      borderColour: "#004A99",
      // borderColour: "#445052",
      borderSize:1,
      zoomWindowPosition:11,
      zoomWindowWidth: w,
      zoomWindowHeight: img.clientHeight,
      zoomWindowOffsetX: -15,
      zoomLevel: 1.5,
      scrollZoom:true,
      responsive:true
    });
  });
}

function demelEncodeURIComponentTerm(str) {
  return encodeURIComponent(str).replace(/[!'()\[\]*]/g, function(c) {
    return '%' + c.charCodeAt(0).toString(16);
  });
}

function initObjectInfo(objectType) {
  document.querySelectorAll(`.${objectType}-info`).forEach((x) => {
    x.addEventListener('show.bs.collapse', event => {
      hidePopovers();
      let oid = x.dataset[`${objectType}Id`];
      if(document.querySelector(`#${objectType}_line_${oid}`)){
        bootstrap.Collapse.getOrCreateInstance(`#${objectType}_line_${oid}`).hide();
      }
      createExtendedArea(oid);
      if(!document.getElementById(`${objectType}_info_${oid}`).querySelectorAll(".card").length) {
        fetch(`${document.getElementsByName('demel-baseurl')[0].getAttribute('content')}api/infobox/${objectType}/${oid}`)
          .then((response) => response.text())
          .then((text) => {
            let node = document.getElementById(`${objectType}_info_${oid}`);
            node.innerHTML = text;
            initPopovers(node);
          });
      }
    });

    x.addEventListener('shown.bs.collapse', event => {
      let oid = x.dataset[`${objectType}Id`];
      document.querySelector("#toggle_button_" + oid + " > i")?.classList.remove("fa-eye");
      document.querySelector("#toggle_button_" + oid + " > i")?.classList.add("fa-eye-slash", "text-danger");
    });
    
    x.addEventListener('hidden.bs.collapse', event => {
      let oid = x.dataset[`${objectType}Id`];
      document.querySelector("#toggle_button_" + oid + " > i")?.classList.remove("fa-eye-slash", "text-danger");
      document.querySelector("#toggle_button_" + oid + " > i")?.classList.add("fa-eye");

      let btnMessages = document.querySelector('#button_messages_' + oid);
      if(btnMessages && btnMessages.classList.contains("active")) {
        toggleMessageArea(btnMessages);
      }
      bootstrap.Collapse.getInstance(`#${objectType}_line_${oid}`)?.show();
    });
  });

  if(document.querySelectorAll(`.${objectType}-info`).length == 1) {
    document.querySelectorAll(`.${objectType}-info`).forEach((x) => {
      bootstrap.Collapse.getOrCreateInstance(x).show();
      let oid = x.dataset[`${objectType}Id`];
      bootstrap.Collapse.getInstance(`#${objectType}_line_${oid}`)?.hide();
    });
  }
}

function hidePopovers() {
  document.querySelectorAll('[data-bs-toggle="popover-info"]').forEach((x) => {
    bootstrap.Popover.getInstance(x)?.hide();
  });
  document.querySelectorAll('[data-bs-toggle="popover-cite"]').forEach((x) => {
    bootstrap.Popover.getInstance(x)?.hide();
  });
  document.querySelectorAll('[data-bs-toggle="popover-help"]').forEach((x) => {
    bootstrap.Popover.getInstance(x)?.hide();
  });
}

function createButtonsShowAllCloseAll(div, i18n, faIconOpen, faIconClose) {
  if(div.querySelector('#demel-btn-show-all') == undefined) {
    return `
      <button id="demel-btn-show-all" type="button" title="${i18n.btn_title_show_all}"
                  class="btn btn-secondary ms-3" onclick="showAllDetails();" style="height:2.2em; width:4.5em; margin-top: -0.25em; white-space:nowrap">
        <i class="${faIconOpen}"></i> <i class="${faIconOpen}"></i>
      </button>
      <button id="demel-btn-close-all" type="button" title="${i18n.btn_title_close_all}"
              class="d-none btn btn-secondary text-danger ms-3 px-2" onclick="closeAllDetails();" style="height: 2.2em; width:4.5em; margin-top: -0.25em; white-space:nowrap">
         <i class="${faIconClose}"></i>&nbsp;&nbsp; <i class="${faIconClose}"></i>
       </button>`;
  } else {
     return "";
  } 
}

function showAllDetails() {
   //aria-expanded = 'false' or not available
   document.getElementById("demel-btn-show-all")?.classList.add("d-none");
   document.getElementById("demel-btn-close-all")?.classList.remove("d-none");
   document.querySelectorAll(".demel-btn-show-detail:not([aria-expanded='true'])").forEach((btn, index) => {
     setTimeout(function(){
       if(document.getElementById("demel-btn-show-all")?.classList.contains("d-none")) {
         btn.click();
       };
     }, index * 50);
   });
}

function closeAllDetails() {
  document.getElementById("demel-btn-close-all")?.classList.add("d-none");
  document.getElementById("demel-btn-show-all")?.classList.remove("d-none");
  document.querySelectorAll(".demel-btn-show-detail[aria-expanded='true']").forEach((btn, index) => {
    // delay the closing to let running open actions get finished first
    setTimeout(function(){
      if(document.getElementById("demel-btn-close-all")?.classList.contains("d-none")) {
        btn.click();
      };
    }, 200 + index * 5);
  });
}
