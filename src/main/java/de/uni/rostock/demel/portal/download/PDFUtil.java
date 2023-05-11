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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.springframework.context.MessageSource;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RGBColor;

import de.uni.rostock.demel.data.model.dictionary.Attestation;

public class PDFUtil {

    public static final Font FONT_HEAD = new Font(Font.HELVETICA, 20, Font.BOLD, RGBColor.BLACK);

    public static final Font FONT_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD, RGBColor.BLACK);

    public static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL, RGBColor.BLACK);

    public static final Font FONT_SMALL = new Font(Font.HELVETICA, 7, Font.NORMAL, RGBColor.BLACK);

    public static final Font FONT_SMALL_BOLD = new Font(Font.HELVETICA, 7, Font.BOLD, RGBColor.BLACK);

    public static final Font FONT_ITALIC = new Font(Font.HELVETICA, 10, Font.ITALIC, RGBColor.BLACK);

    public static final Font FONT_SYMBOL = new Font(Font.ZAPFDINGBATS, 10, Font.NORMAL, RGBColor.BLACK);

    public static void registerPageEvent(PdfWriter writer, String i18nPage) {
        HeaderFooterPageEvent hfpEvent = new HeaderFooterPageEvent(
            new SimpleDateFormat("dd.MM.yyyy / HH:mm:ss"), "https://demel.uni-rostock.de", i18nPage);
        writer.setPageEvent(hfpEvent);
    }

    public static PdfPTable createTableHead(Document doc, String searchURL, MessageSource messageSource,
        Locale locale) {
        float[] columnWidthKopf = { 50f, 18f, 32f };
        PdfPTable tableKopf = new PdfPTable(columnWidthKopf);
        tableKopf.setTotalWidth(doc.getPageSize().getWidth() - 72);
        tableKopf.setLockedWidth(true);

        //table.setKeepTogether(true);
        tableKopf.addCell(createHeaderLeft(doc));
        tableKopf.addCell(createHeaderQR(doc, searchURL));
        tableKopf.addCell(createHeaderURL(searchURL, messageSource, locale));
        tableKopf.addCell(createHeaderCopyright(doc, messageSource, locale));
        return tableKopf;
    }

    private static PdfPCell createHeaderLeft(Document doc) {
        //table.setKeepTogether(true);
        PdfPCell cellKopfLeft = new PdfPCell();
        cellKopfLeft.setPadding(4f);

        try {
            Image logo = Image.getInstance(
                ImageIO.read(PDFUtil.class.getClassLoader().getResource("static/images/demel_logo.png")), null);
            float scale = 100 * (doc.getPageSize().getWidth() - 72 - 32) / 2 / logo.getWidth();
            logo.scalePercent(scale);
            cellKopfLeft.addElement(logo);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

        Paragraph p = new Paragraph("https://demel.uni-rostock.de");
        p.setSpacingAfter(4f);
        cellKopfLeft.addElement(p);

        return cellKopfLeft;

    }

    private static PdfPCell createHeaderQR(Document doc, String searchURL) {

        PdfPCell cellKopfQR = new PdfPCell();
        cellKopfQR.setPadding(1f);
        cellKopfQR.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cellKopfQR.disableBorderSide(PdfPCell.RIGHT);
        cellKopfQR.setRowspan(2);
        createBarcode(searchURL, cellKopfQR, 100f);
        return cellKopfQR;
    }

    private static PdfPCell createHeaderURL(String searchURL, MessageSource messageSource, Locale locale) {
        Chunk chDownload = new Chunk(searchURL.replace("?", "\n?"), PDFUtil.FONT_SMALL);
        chDownload.setAnchor(searchURL);
        PdfPCell cellKopfRight = new PdfPCell();
        cellKopfRight.setPadding(4f);
        cellKopfRight.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cellKopfRight.disableBorderSide(PdfPCell.LEFT);
        cellKopfRight.setRowspan(2);

        Paragraph p = new Paragraph();
        p.add(new Chunk(
            messageSource.getMessage("demel.lemma_download.pdf.table.head.right", null, locale),
            PDFUtil.FONT_SMALL_BOLD));
        cellKopfRight.addElement(p);
        p = new Paragraph("", PDFUtil.FONT_SMALL);
        p.add(chDownload);
        cellKopfRight.addElement(p);

        cellKopfRight.enableBorderSide(0);
        return cellKopfRight;
    }

    private static PdfPCell createHeaderCopyright(Document doc, MessageSource messageSource, Locale locale) {
        PdfPCell cellCopyright = new PdfPCell();
        cellCopyright.setPadding(4f);
        cellCopyright.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        Paragraph p = new Paragraph();
        p.setMultipliedLeading(1.33f);
        p.add(new Chunk(
            messageSource.getMessage("demel.popover.cite.citation", null, locale) + ": ",
            PDFUtil.FONT_SMALL_BOLD));
        p.add(new Chunk(
            messageSource.getMessage("demel.copyright_notice", null, locale),
            PDFUtil.FONT_SMALL));
        cellCopyright.addElement(p);
        return cellCopyright;
    }

    public static void createBarcode(String downloadURL, PdfPCell cellKopfRight, Float widthPercentage) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                downloadURL,
                BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);
            Image imgBarcode = Image.getInstance(bi, null);
            imgBarcode.setWidthPercentage(widthPercentage);
            cellKopfRight.addElement(imgBarcode);
        } catch (IOException | WriterException e) {
            // do nothing
        }
    }

    public static void addZitierCell(Attestation wf, PdfPTable tableMeta, String basePURL) {
        PdfPCell cell = new PdfPCell();
        cell.setColspan(2);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);

        Paragraph phrZitier = new Paragraph(
            new Chunk(basePURL + wf.getId(), PDFUtil.FONT_ITALIC));
        phrZitier.setAlignment(Paragraph.ALIGN_RIGHT);
        cell.addElement(phrZitier);
        cell.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.BOTTOM);

        cell.setPadding(4f);
        cell.setPaddingTop(0f);
        tableMeta.addCell(cell);
    }

    public static void addDataCell(PdfPTable tableMeta, Phrase content) {
        PdfPCell cell;
        cell = new PdfPCell(content);
        cell.disableBorderSide(PdfPCell.RIGHT | PdfPCell.TOP);
        cell.setPadding(4f);
        //increase padding bottom to see underline in multiwordexpressions
        cell.setPaddingBottom(6f);
        tableMeta.addCell(cell);
    }

    public static void addLabelCell(PdfPTable tableMeta, String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, PDFUtil.FONT_BOLD));
        cell.setPadding(4f);
        cell.disableBorderSide(PdfPCell.TOP | PdfPCell.LEFT);
        tableMeta.addCell(cell);
    }
}
