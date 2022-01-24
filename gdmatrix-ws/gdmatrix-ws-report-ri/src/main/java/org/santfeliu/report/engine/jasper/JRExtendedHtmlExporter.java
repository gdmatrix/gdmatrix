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
package org.santfeliu.report.engine.jasper;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.util.JRStringUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.type.RunDirectionEnum;
import net.sf.jasperreports.engine.type.LineSpacingEnum;
import net.sf.jasperreports.engine.xml.JRPrintImageSourceObject;
import net.sf.jasperreports.renderers.ResourceRenderer;

/**
 *
 * @author realor
 */
public class JRExtendedHtmlExporter extends JRHtmlExporter
{

  @Override
  protected void exportImage(JRPrintImage image, JRExporterGridCell gridCell)
    throws JRException, IOException
  {
    try
    {
      String imageSrc = null;
      if (image.getRenderer() != null)
      {
        if (image.getRenderer() instanceof ResourceRenderer)
        {
          imageSrc = ((ResourceRenderer)image.getRenderer()).
            getResourceLocation();
        }
        else
        {
          byte[] source = image.getRenderable().getImageData();
          if (source != null && source.length > 0)
          {
            String encodedSource = Base64.getMimeEncoder().
              encodeToString(source);
            imageSrc = "data:image/png;base64," + encodedSource;
          }
        }
      }
      if (imageSrc != null)
      {
        JRPrintImageSourceObject imageSource =
          new JRPrintImageSourceObject(true);
        imageSource.setEmbedded(false);
        imageSource.setPrintImage(image);
        imageSource.setImageSource(imageSrc);
        super.exportImage(image, gridCell);
      }
    }
    catch (Exception ex)
    {
    }
  }

  protected void exportText(JRPrintText text, JRExporterGridCell gridCell)
          throws IOException
  {
    JRStyledText styledText = getStyledText(text);

    int textLength = 0;

    if (styledText != null)
    {
      textLength = styledText.length();
    }

    //FIXME why dealing with cell style if no text to print (textLength == 0)?
    writeCellStart(gridCell);

    String verticalAlignment = HTML_VERTICAL_ALIGN_TOP;

    switch (text.getVerticalAlignmentValue())
    {
      case BOTTOM:
      {
        verticalAlignment = HTML_VERTICAL_ALIGN_BOTTOM;
        break;
      }
      case MIDDLE:
      {
        verticalAlignment = HTML_VERTICAL_ALIGN_MIDDLE;
        break;
      }
      case TOP:
      default:
      {
        verticalAlignment = HTML_VERTICAL_ALIGN_TOP;
      }
    }

    if (!verticalAlignment.equals(HTML_VERTICAL_ALIGN_TOP))
    {
      writer.write(" valign=\"");
      writer.write(verticalAlignment);
      writer.write("\"");
    }

    if (text.getRunDirectionValue() == RunDirectionEnum.RTL)
    {
      writer.write(" dir=\"rtl\"");
    }

    String styleClass = null;
    JRStyle style = text.getStyle();
    if (style != null)
    {
      styleClass = style.getName();
      writer.write(" class=\"" + styleClass + "\"");
    }

    StringBuilder styleBuffer = new StringBuilder();
    appendBackcolorStyle(gridCell, styleBuffer);
    appendBorderStyle(gridCell.getBox(), styleBuffer);

    String horizontalAlignment = CSS_TEXT_ALIGN_LEFT;

    if (textLength > 0)
    {
      switch (text.getHorizontalAlignmentValue())
      {
        case RIGHT:
        {
          horizontalAlignment = CSS_TEXT_ALIGN_RIGHT;
          break;
        }
        case CENTER:
        {
          horizontalAlignment = CSS_TEXT_ALIGN_CENTER;
          break;
        }
        case JUSTIFIED:
        {
          horizontalAlignment = CSS_TEXT_ALIGN_JUSTIFY;
          break;
        }
        case LEFT:
        default:
        {
          horizontalAlignment = CSS_TEXT_ALIGN_LEFT;
        }
      }

      if ((text.getRunDirectionValue() == RunDirectionEnum.LTR &&
          !horizontalAlignment.equals(CSS_TEXT_ALIGN_LEFT)) ||
          (text.getRunDirectionValue() == RunDirectionEnum.RTL &&
          !horizontalAlignment.equals(CSS_TEXT_ALIGN_RIGHT)))
      {
        styleBuffer.append("text-align: ");
        styleBuffer.append(horizontalAlignment);
        styleBuffer.append(";");
      }
    }

    if (getCurrentItemConfiguration().isWrapBreakWord())
    {
      styleBuffer.append("width: " + gridCell.getWidth() + getCurrentItemConfiguration().getSizeUnit() + "; ");
      styleBuffer.append("word-wrap: break-word; ");
    }

    if (text.getLineSpacingValue() != LineSpacingEnum.SINGLE)
    {
      styleBuffer.append("line-height: " + text.getLineSpacingFactor() + "; ");
    }

    if (text.getLineBreakOffsets() != null)
    {
      //if we have line breaks saved in the text, set nowrap so that
      //the text only wraps at the explicit positions
      styleBuffer.append("white-space: nowrap; ");
    }

    if (styleBuffer.length() > 0)
    {
      writer.write(" style=\"");
      writer.write(styleBuffer.toString());
      writer.write("\"");
    }

    writer.write(">");

    if (text.getAnchorName() != null)
    {
      writer.write("<a name=\"");
      writer.write(text.getAnchorName());
      writer.write("\"/>");
    }

    startHyperlink(text);

    if (textLength > 0)
    {
      //only use text tooltip when no hyperlink present
      String textTooltip = hyperlinkStarted ? null : text.getHyperlinkTooltip();
      if (styleClass == null)
      {
        exportStyledText(text, styledText, textTooltip);
      }
      else
      {
        exportPlainText(styledText.getText(), textTooltip, getTextLocale(text));
      }
    }
    else
    {
      writer.write(emptyCellStringProvider.getStringForEmptyTD());
    }

    endHyperlink();

    writer.write("</td>\n");
  }

  protected void exportPlainText(
          String text,
          String tooltip,
          Locale locale) throws IOException
  {
    if (tooltip != null)
    {
      writer.write("<span title=\"");
      writer.write(JRStringUtil.xmlEncode(tooltip));
      writer.write("\">");
    }
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>text:" + text);
    writer.write(JRStringUtil.htmlEncode(text));

    if (tooltip != null)
    {
      writer.write("</span>");
    }
  }
}
