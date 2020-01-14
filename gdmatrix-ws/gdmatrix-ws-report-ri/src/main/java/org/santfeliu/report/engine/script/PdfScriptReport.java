/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.report.engine.script;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.activation.DataSource;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author realor
 */
public class PdfScriptReport extends ScriptReport
{
  private File file;
  private PdfWriter writer;
  private Document document;

  @Override
  public String getFormat()
  {
    return "pdf";
  }

  @Override
  public String getContentType()
  {
    return "application/pdf";
  }

  @Override
  public void open()
  {
    try
    {
      file = File.createTempFile("temp", "pdf");
      document = new Document();
      writer = PdfWriter.getInstance(document, new FileOutputStream(file));
      document.open();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public void importPdf(String url)
  {
    try
    {
      PdfReader reader = new PdfReader(new URL(url));
      PdfContentByte cb = writer.getDirectContent();

      int pages = reader.getNumberOfPages();
      for (int i = 1; i <= pages; i++)
      {
        document.setPageSize(reader.getPageSize(i));
        document.newPage();
        PdfImportedPage page = writer.getImportedPage(reader, i);       
        cb.addTemplate(page, 0, 0);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);      
    }
  }  
  
  public PdfContentByte getDirectContent()
  {
    return writer.getDirectContent();
  }

  public PdfGraphics2D getGraphics()
  {
    Rectangle pageSize = document.getPageSize();
    float pageWidth = pageSize.getWidth();
    float pageHeight = pageSize.getHeight();
    return new PdfGraphics2D(getDirectContent(), pageWidth, pageHeight); 
  }

  public Document getDocument()
  {
    return document;
  }

  public void addParagraph(String text)
  {
    try
    {
      document.add(new Paragraph(text));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void setPageSize(String paper, boolean landscape)
  {
    Rectangle rect = PageSize.A4;
    if ("A0".equals(paper))
    {
      rect = PageSize.A0;
    }
    else if ("A1".equals(paper))
    {
      rect = PageSize.A1;
    }
    else if ("A2".equals(paper))
    {
      rect = PageSize.A2;
    }
    else if ("A3".equals(paper))
    {
      rect = PageSize.A3;
    }
    else if ("A4".equals(paper))
    {
      rect = PageSize.A4;
    }
    else if ("A5".equals(paper))
    {
      rect = PageSize.A5;
    }
    else if ("A6".equals(paper))
    {
      rect = PageSize.A6;
    }
    if (landscape)
    {
      rect = rect.rotate();
    }
    document.setPageSize(rect);
  }

  public void drawText(String text, int x, int y, int size)
    throws Exception
  {
    float pageHeight = getPageSize().getHeight();
    PdfContentByte cb = getOverContent();
    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
      BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    cb.saveState();
    cb.beginText();
    cb.moveText(x, pageHeight - y);
    cb.setFontAndSize(bf, size);    
    cb.showText(text);
    cb.endText();
    cb.restoreState();
  }
  
  public void drawText(String text, int alignment, int x, int y, int size, float rotation)
  {
    float pageHeight = getPageSize().getHeight();    
    Font font = new Font();
    font.setSize(size);
    Phrase phrase = new Phrase(text, font);
    ColumnText.showTextAligned(getOverContent(), alignment, phrase, x, pageHeight - y, rotation);
  }

  public void drawLine(float x1, float y1, float x2, float y2)
  {
    float pageHeight = getPageSize().getHeight();
    PdfContentByte cb = getOverContent();
    cb.moveTo(x1, pageHeight - y1);
    cb.lineTo(x2, pageHeight - y2);
    cb.stroke();    
  }  
  
  public void drawRect(float x1, float y1, float x2, float y2)
  {
    float pageHeight = getPageSize().getHeight();
    PdfContentByte cb = getOverContent();
    cb.moveTo(x1, pageHeight - y1);
    cb.lineTo(x2, pageHeight - y1);
    cb.lineTo(x2, pageHeight - y2);
    cb.lineTo(x1, pageHeight - y2);
    cb.lineTo(x1, pageHeight - y1);
    cb.stroke();
  }

  public void drawRect(float x1, float y1, float x2, float y2, int border)
  {
    float pageHeight = getPageSize().getHeight();
    Rectangle rect = new com.lowagie.text.Rectangle(x1, pageHeight - y1, x2, pageHeight - y2);
    rect.setBorder(com.lowagie.text.Rectangle.BOX);
    rect.setBorderWidth(border);
    getOverContent().rectangle(rect);    
  }  
  
  public void drawImage(String url, float x, float y, float width, float height)
    throws Exception
  {
    Image image = Image.getInstance(new URL(url));
    float pageHeight = getPageSize().getHeight();        
    
    float imageWidth = image.getWidth();
    float imageHeight = image.getHeight();    
    float imageAspect = imageWidth / imageHeight;
    float boxAspect = width / height;
    if (imageAspect < boxAspect)
    {
      width = imageAspect * height;
    }
    else
    {
      height = width / imageAspect;
    }
    image.setAbsolutePosition(x, pageHeight - y - height);
    image.scaleAbsolute(width, height);
    getOverContent().addImage(image);
  } 
  
  public void setColor(String color)
  {
    if (color != null)
    {
      if (color.startsWith("#")) color = color.substring(1);
      try
      {
        int argb = Integer.parseInt(color, 16) | 0xFF000000;
        PdfContentByte cb = getOverContent();
        cb.setColorFill(new Color(argb));
      }
      catch (NumberFormatException ex)
      {
      }
    }
  }  
  
  public void shrinkPage(float percentage, float offsetX, float offsetY)
  {
    float pageHeight = getPageSize().getHeight();    
    float height = pageHeight * percentage;
    getUnderContent().setLiteral(
      String.format("\nq %s 0 0 %s %s %s cm\nq\n", percentage, percentage, offsetX, pageHeight - offsetY - height));
    getOverContent().setLiteral("\nQ\nQ\n");    
  }
  
  public void rotatePage(int rotation)
  {
//    writer.addPageDictEntry(PdfName.ROTATE, new PdfNumber(rotation));   
    document.getPageSize().setRotation(rotation);
    document.getPageSize().rotate();
  }
  
  public void drawPageNumber(int pageNumber, int numberOfPages, String pattern, int alignment, float x, float y)
  {
    if (pattern == null)
      pattern = "Pàgina %s de %s";
    ColumnText.showTextAligned(getOverContent(), 
      alignment,
      new Phrase(java.lang.String.format(pattern, pageNumber, numberOfPages)), x, y, 0);    
  }
  
  public void drawQRCode(String url, float x1, float y1, int width, int height, float percent) 
  {
    try
    {
      float pageHeight = getPageSize().getHeight();
//      BarcodeQRCode qrcode = new BarcodeQRCode(url, width, height, null);
//      Image qrcodeImage = qrcode.getImage();

      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);

      File tempFile = File.createTempFile("qrcode", ".png");
      MatrixToImageWriter.writeToFile(bitMatrix, "PNG", tempFile);
      tempFile.deleteOnExit();
      Image qrcodeImage = Image.getInstance(tempFile.getPath());
      qrcodeImage.setAbsolutePosition(x1, pageHeight - y1 - height);
      qrcodeImage.scalePercent(percent);
      getOverContent().addImage(qrcodeImage);      
    }
    catch (Exception ex)
    {
      drawRect(x1, y1, x1 + width, y1 + height);
    }
  }
  
  public PdfPTable newTable(int numCols)
  {
    return new PdfPTable(numCols);
  }
  
  public PdfPCell newCell()
  {
    PdfPCell cell = new PdfPCell();
    return cell;
  }
  
  public void addCellText(PdfPCell cell, String text, float leading)
  {
    addCellText(cell, text, new Font(), leading);
  }
  
  public void addCellText(PdfPCell cell, String text, float size, String style, float leading)
  {
    Font font = new Font();
    if (font.getSize() != size)
      font.setSize(size);
    font.setStyle(style);
    addCellText(cell, text, font, leading);
  }
  
  private void addCellText(PdfPCell cell, String text, Font font, float leading)
  {
    Phrase ph = new Phrase(leading, text, font);
    cell.addElement(ph);    
  }
  
  public void addCell(PdfPTable table, PdfPCell cell)
  {
    table.addCell(cell);
  }
  
  public void drawTable(PdfPTable table, float x, float y)
  {
    float pageHeight = getPageSize().getHeight();
    int rowStart = 0;
    int rowEnd = table.getRows().size();
    
    table.writeSelectedRows(rowStart, rowEnd, x, pageHeight - y, getOverContent());
  }  

  
  @Override
  public void close()
  {
    try
    {
      document.close();
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public DataSource getData()
  {
    return new TemporaryDataSource(file, "application/pdf");
  }
  
  public PdfPageImporter importPages(InputStream is)
  {
    try
    {
      return new PdfPageImporter(is);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private Rectangle getPageSize()
  {
    return document.getPageSize();
  }
  
  private PdfContentByte getOverContent()
  {
    return writer.getDirectContent();
  }

  private PdfContentByte getUnderContent()
  {
    return writer.getDirectContentUnder();
  }  
  
  public class PdfPageImporter
  {
    private final PdfReader reader;
    
    public PdfPageImporter(InputStream source) throws IOException  
    {
      this.reader = new PdfReader(source);
    }
    
    public void importPage(int pageNumber) throws Exception
    {
      PdfContentByte contentByte = getDirectContent();
      document.setPageSize(reader.getPageSizeWithRotation(pageNumber));
      document.newPage();

      PdfImportedPage page = writer.getImportedPage(reader, pageNumber);
//      int rotation = page.getRotation();    
      int rotation = document.getPageSize().getRotation();
      if (rotation > 0) //If page is rotated
      {
        float angle = (float) Math.toRadians(rotation);
        float a = (float)(Math.cos(angle)); 
        float b = (float)(-Math.sin(angle)); 
        float c = (float)(Math.sin(angle));       
        float d = (float)(Math.cos(angle));
        float pageWidth = reader.getPageSizeWithRotation(pageNumber).getWidth();
        float pageHeight = reader.getPageSizeWithRotation(pageNumber).getHeight();
        float x = 0;
        float y = 0;
        switch (rotation)
        {
          //Pdf specification allows only 0, 90, 180 or 270 degrees rotation
          case 90: y = pageHeight; break;
          case 180: x = pageWidth; y = pageHeight; break;
          case 270: x = pageWidth; break;  
        }
        contentByte.addTemplate(page, a, b, c, d, x, y);
      }
      else
        contentByte.addTemplate(page, 0, 0);

      writer.freeReader(reader);
    }
    
    public int getNumOfPages()
    {
      return reader.getNumberOfPages();
    }

    public Rectangle getPageSize(int page)
    {
      return reader.getPageSizeWithRotation(page);
    }
    
    public int getPageRotation(int page)
    {
      return reader.getPageRotation(page);
    }

    public void close()
    {
      try
      {
        reader.close(); 
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);      
      }
    }
  }
  
}
