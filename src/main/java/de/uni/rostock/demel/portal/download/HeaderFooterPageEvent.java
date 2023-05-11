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
package de.uni.rostock.demel.portal.download;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RGBColor;

import de.uni.rostock.demel.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public class HeaderFooterPageEvent extends PdfPageEventHelper {

    private Font fNormal = new Font(Font.HELVETICA, 10, Font.NORMAL, RGBColor.BLACK);

    private Date current = new Date();

    private SimpleDateFormat sdf = null;

    private int currentPage = 0;

    private String url;

    private String i18nPage;

    public HeaderFooterPageEvent(SimpleDateFormat sdf, String url, String i18nPage) {
        this.sdf = sdf;
        this.url = url;
        this.i18nPage = i18nPage;
    }

    public void onStartPage(PdfWriter writer, Document document) {
        currentPage++;
    }

    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(url, fNormal), 36, 36, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
            new Phrase(i18nPage + " " + document.getPageNumber()), 297, 36, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
            new Phrase(sdf.format(current), fNormal), 560, 36, 0);
    }

    public int getCurrentPage() {
        return currentPage;
    }

}
