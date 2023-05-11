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
function createBoxDetails(i18n) {
  fetch(document.getElementsByName('demel-baseurl')[0].getAttribute('content') + "api/data/json/boxes")
    .then((response) => response.json())
    .then((data) => {
      //document.getElementsByClassName("slide") would  return a nodelist, which has no .forEach()
      document.querySelectorAll('.demel-section').forEach(function(div) {
	    div.innerHTML="";
        let section = div.dataset.section;
        let found = false;
        let i = 0;
        let cardDeck = null;
          data.results.forEach(function(item) {
            if(item.section == section) {
            found = true;
            if(i % 5 == 0) {
              cardDeck = document.createElement("div");
              cardDeck.classList.add("row", "my-3");
              div.append(cardDeck);
            }
            cardDeck.insertAdjacentHTML("beforeEnd",
              `<div class="col px-0">
                 <div id="${item.id}" class="card border mx-2" style="min-height:6em">
                   ${createCardContent(item, i18n)}
                 </div>
                </div>`
            );
            i++;
          }
        });
        if(i % 5 != 0) {
          for(j = i % 5; j < 5; j++ ) {
            cardDeck.insertAdjacentHTML("beforeEnd", `<div class="col px-0"></div>`);
          }
        }

        if(!found) {
          div.insertAdjacentHTML("beforeEnd", `<div class="border border-danger text-center py-3">Section: ${section}</div>`);
        }
      });

      document.querySelectorAll('.demel-box').forEach(function(div) {
	    div.innerHTML = "";
        let box = div.dataset.box;
        let found = false;
        data.results.forEach(function(item) {
          if(item.id == box) {
            found = true;
            div.insertAdjacentHTML("beforeEnd",
              `<div id="${item.id}" class="card border" style="min-height:6em">
                 ${createCardContent(item, i18n)}
               </div>`
            );
          }
        });
        if(!found) {
          div.insertAdjacentHTML("beforeEnd", 
            `<div class="border border-danger text-center py-3">Box: ${box}</div>`);
        }
      });
      initPopovers();
    });
}

function createCardContent(item, i18n) {
  let lang = document.querySelector("#languageSelector").dataset.currentLang;
  let name = (lang.startsWith("es")) ? item.name_es : item.name;
  let doroUrl = document.querySelector("meta[name='demel-dorourl']").content;
  return `
    <div class='card-header p-0 pb-1 bg-white text-center'>
      <a class="btn btn-sm btn-outline-secondary border-light float-end"
         href="https://dfg-viewer.de/show/?tx_dlf[id]=${doroUrl}depot/demel_${item.id}/demel_${item.id}.dv.mets.xml"
         title="${i18n.demel_fichero_dfgviewer_tooltip}">
         DFG
      </a>
      <a class="btn btn-sm btn-outline-secondary border-light float-start"
         href="${doroUrl}mcrviewer/recordIdentifier/demel_${item.id}"
         title="${i18n.demel_fichero_doro_tooltip}">
         <i class="fa-regular fa-eye"></i>
      </a>
      <span class="small text-muted"> ${item.id}</span>
    </div>
    <div class="card-body p-1 bg-white text-center" style="position:relative;">
      ${name.replace("DEM: ", "")}
      <div style="position:absolute; bottom:0;right:0; transform:scale(.8) translate(.90em, .33em)">
        ${buttonCiteWithPopover(item.id, 'box', {
            label: i18n.demel_vocabulary_box,
            purl: i18n.demel_citelink})
        }
      </div>
    </div> `;
}

