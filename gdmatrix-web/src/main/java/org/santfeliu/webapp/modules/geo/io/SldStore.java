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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.geo.sld.SldRoot;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class SldStore
{
  static final Logger LOGGER = Logger.getLogger("SldStore");

  public static final String SLD_TYPEID = "SLD";
  public static final String SLD_PROPERTY_NAME = "sld_name";
  public static final String SLD_ENCODING = "ISO-8859-1";
  public static final String SLD_TITLE = "Styled Layer Descriptor: ";

  private DocumentManagerPort documentManagerPort;

  private static final Map<String, String> sldUrlCache =
    Collections.synchronizedMap(new HashMap<>());
  private static long lastPurgeMillis;
  private static final long SLD_CACHE_REFRESH_TIME = 300000; // 5 minutes

  public void setCredentials(String userId, String password)
  {
    this.documentManagerPort = DocModuleBean.getPort(userId, password);
  }

  public List<String> findSld(String sldName)
  {
    LOGGER.log(Level.INFO, "Finding SLD like {0}", sldName);

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SLD_TYPEID);
    filter.setTitle(SLD_TITLE + "%");
    Property property = new Property();
    property.setName(SLD_PROPERTY_NAME);
    property.getValue().add("%" + sldName + "%");
    filter.getProperty().add(property);
    filter.getOutputProperty().add(SLD_PROPERTY_NAME);
    List<Document> documents = getPort().findDocuments(filter);
    return documents.stream()
      .map(d -> DictionaryUtils.getPropertyValue(d.getProperty(), SLD_PROPERTY_NAME))
      .collect(Collectors.toList());
  }

  public SldRoot createSld(List<String> layers, List<String> styles)
    throws Exception
  {
    InputStream is = SldStore.class.getResourceAsStream("sld_template.xml");
    SldReader reader = new SldReader();
    SldRoot root = reader.read(is);
    root.addNamedLayers(layers, styles);
    return root;
  }

  public SldRoot loadSld(String sldName) throws Exception
  {
    LOGGER.log(Level.INFO, "Loading SLD {0}", sldName);

    Document document = getSldDocument(sldName, true);
    if (document == null) return null;
    InputStream is = document.getContent().getData().getInputStream();
    SldReader reader = new SldReader();
    return (SldRoot)reader.read(is);
  }

  public boolean storeSld(String sldName, SldRoot sld, boolean isNewSld)
    throws Exception
  {
    LOGGER.log(Level.INFO, "Storing SLD {0}", sldName);

    Document document = getSldDocument(sldName, true);
    if (document == null)
    {
      document = new Document();
      document.setDocTypeId(SLD_TYPEID);
      Property property = new Property();
      property.setName(SLD_PROPERTY_NAME);
      property.getValue().add(sldName);
      document.getProperty().add(property);
    }
    else
    {
      if (isNewSld) return false;
    }
    document.setTitle(SLD_TITLE + sldName);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SldWriter writer = new SldWriter();
    writer.write(sld, bos);
    byte[] bytes = bos.toByteArray();
    MemoryDataSource ds = new MemoryDataSource(bytes, "sld", "text/xml");
    DataHandler dh = new DataHandler(ds);
    Content content = new Content();
    content.setData(dh);
    content.setContentType("text/xml");
    document.setContent(content);
    getPort().storeDocument(document);

    sldUrlCache.remove(sldName);

    return true;
  }

  public Document getSldDocument(String sldName, boolean content)
  {
    Document document = null;
    if (!isBlank(sldName))
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(SLD_TYPEID);
      Property property = new Property();
      property.setName(SLD_PROPERTY_NAME);
      property.getValue().add(sldName);
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

  public String getSldUrl(String sldName)
  {
    long now = System.currentTimeMillis();
    if (now - lastPurgeMillis > SLD_CACHE_REFRESH_TIME)
    {
      lastPurgeMillis = now;
      sldUrlCache.clear();
    }

    String sldUrl = sldUrlCache.get(sldName);
    if (sldUrl == null)
    {
      sldUrl = buildSldUrl(sldName);
      sldUrlCache.put(sldName, sldUrl);
    }
    return sldUrl;
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

  private String buildSldUrl(String sldName)
  {
    LOGGER.log(Level.INFO, "Build url for SLD {0}", sldName);

    if (!isBlank(sldName))
    {
      Document document = getSldDocument(sldName, false);
      if (document != null)
      {
        String host = MatrixConfig.getProperty("org.santfeliu.web.hostname");
        if (host == null)
        {
          try
          {
            host = java.net.InetAddress.getLocalHost().getHostAddress();
          }
          catch (Exception ex)
          {
            host = "localhost";
          }
        }
        String contentId = document.getContent().getContentId();
        StringBuilder buffer = new StringBuilder();
        buffer.append("http://");
        buffer.append(host);
        buffer.append(":");
        buffer.append(MatrixConfig.getProperty("org.santfeliu.web.defaultPort"));
        buffer.append(MatrixConfig.getProperty("contextPath"));
        buffer.append("/documents/");
        buffer.append(contentId);
        buffer.append("/");
        try
        {
          buffer.append(URLEncoder.encode(sldName, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
          buffer.append(sldName);
        }
        buffer.append(".sld");
        return buffer.toString();
      }
    }
    return null;
  }
}
