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
package org.santfeliu.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.enc.HtmlEncoder;

/**
 *
 * @author realor
 */
public class HtmlFormatter extends Formatter
{
  private String title;
  private String style;
  private String dateFormat = "yyyy-MM-dd HH:mm:ss";
  private boolean showLevel = true;
  private SimpleDateFormat df;

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  public boolean isShowLevel()
  {
    return showLevel;
  }

  public void setShowLevel(boolean showLevel)
  {
    this.showLevel = showLevel;
  }

  @Override
  public String format(LogRecord record)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"");
    sb.append(record.getLevel().getName());
    sb.append("\">");
    if (!StringUtils.isBlank(dateFormat))
    {
      if (df == null) df = new SimpleDateFormat(dateFormat);
      sb.append("<span class=\"date\">");
      sb.append(df.format(new Date(record.getMillis())));
      sb.append("</span> ");
    }
    if (showLevel)
    {
      sb.append("<span class=\"level\">");
      String level = record.getLevel().toString();
      int pad = 7 - level.length();
      for (int i = 0; i < pad; i++)
      {
        level += "&nbsp;";
      }
      sb.append(level);
      sb.append("</span> ");
    }
    sb.append("<span class=\"message\">");
    String message = formatMessage(record);
    sb.append(HtmlEncoder.encode(message));
    sb.append("</span>");
    sb.append("</div>\n");

    return sb.toString();
  }

  @Override
  public String getHead(Handler h)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<!DOCTYPE html>\n");
    sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    sb.append("<head>\n");
    if (!StringUtils.isBlank(title))
    {
      sb.append("<title>");
      sb.append(title);
      sb.append("</title>\n");
    }
    sb.append("<style>\n");
    sb.append("body { font-family: Monospace, Courier New; font-size: 14px; }\n");
    sb.append("div.INFO { color: black; }\n");
    sb.append("div.WARNING { color: #a0a000; }\n");
    sb.append("div.SEVERE { color: red; }\n");
    sb.append("div.FINE { color: #808080; }\n");
    sb.append("div.FINER { color: #a0a0a0; }\n");
    sb.append("div.FINEST { color: #b0b0b0; }\n");
    sb.append("h1 { font-size: 16px; }\n");

    if (!StringUtils.isBlank(style))
    {
      sb.append(style);
      sb.append("\n");
    }
    sb.append("</style>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    if (!StringUtils.isBlank(title))
    {
      sb.append("<h1>");
      sb.append(HtmlEncoder.encode(title));
      sb.append("</h1>\n");
    }
    return sb.toString();
  }

  @Override
  public String getTail(Handler h)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("</body>\n");
    sb.append("</html>\n");
    return sb.toString();
  }
}
