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
package org.santfeliu.webapp.modules.geo.io;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class SvgStore
{
  public static final String SVG_TYPEID = "SVGTEMPLATE";
  public static final String SVG_PROPERTY_NAME = "report";
  public static final String SVG_MIMETYPE = "image/svg+xml";

  private DocumentManagerPort documentManagerPort;

  final private Map<String, String> reportUrlCache = new HashMap<>();

  public void setCredentials(String userId, String password)
  {
    this.documentManagerPort = DocModuleBean.getPort(userId, password);
  }

  public List<String> findSvg(String svgName)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SVG_TYPEID);
    Property property = new Property();
    property.setName(SVG_PROPERTY_NAME);
    property.getValue().add("%" + svgName + "%");
    filter.getProperty().add(property);
    filter.getOutputProperty().add(SVG_PROPERTY_NAME);
    List<Document> documents = getPort().findDocuments(filter);
    return documents.stream()
      .map(d -> DictionaryUtils.getPropertyValue(d.getProperty(), SVG_PROPERTY_NAME))
      .collect(Collectors.toList());
  }

  public void storeSvg(String svgName, File svgFile)
  {
    Document document = getSvgDocument(svgName, true);
    if (document == null)
    {
      document = new Document();
      document.setDocTypeId(SVG_TYPEID);
      Property property = new Property();
      property.setName(SVG_PROPERTY_NAME);
      property.getValue().add(svgName);
      document.getProperty().add(property);
      document.setTitle("SVG TEMPLATE: " + svgName);
    }
    FileDataSource ds = new FileDataSource(svgFile, SVG_MIMETYPE);
    DataHandler dh = new DataHandler(ds);
    Content content = new Content();
    content.setData(dh);
    content.setContentType(SVG_MIMETYPE);
    document.setContent(content);
    getPort().storeDocument(document);

    reportUrlCache.remove(svgName);
  }

  public Document getSvgDocument(String svgName, boolean content)
  {
    return getReportDocument(svgName, SVG_TYPEID, content);
  }

  public Document getReportDocument(String reportName, String type,
    boolean content)
  {
    Document document = null;
    if (!StringUtils.isBlank(reportName))
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(type);
      Property property = new Property();
      property.setName(SVG_PROPERTY_NAME);
      property.getValue().add(reportName);
      filter.getProperty().add(property);
      List<Document> documents = getPort().findDocuments(filter);
      if (!documents.isEmpty())
      {
        document = documents.get(0);
        if (content)
        {
          document = getPort().loadDocument(document.getDocId(), 0,
            ContentInfo.ALL);
        }
      }
    }
    return document;
  }

  public String getReportUrl(String reportName)
  {
    String svgUrl = reportUrlCache.get(reportName);
    if (svgUrl == null)
    {
      svgUrl = buildReportUrl(reportName);
      reportUrlCache.put(reportName, svgUrl);
    }
    return svgUrl;
  }

  public void setPort(DocumentManagerPort port)
  {
    this.documentManagerPort = port;
  }

  public DocumentManagerPort getPort()
  {
    if (documentManagerPort == null)
    {
      documentManagerPort = DocModuleBean.getPort("anonymous", null);
    }
    return documentManagerPort;
  }

  private String buildReportUrl(String reportName)
  {
    if (!StringUtils.isBlank(reportName))
    {
      Document document = getReportDocument(reportName, null, false);
      if (document != null)
      {
        Content content = document.getContent();
        if (content != null)
        {
          String url = "/documents/" + content.getContentId() + "/" + reportName;
          if (document.getDocTypeId().endsWith(SVG_TYPEID))
          {
            return url + ".svg";
          }
          else
          {
            return url;
          }
        }
      }
    }
    return "#";
  }
}
