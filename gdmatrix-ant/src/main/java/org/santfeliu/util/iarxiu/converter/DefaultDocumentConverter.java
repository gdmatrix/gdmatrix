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
package org.santfeliu.util.iarxiu.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.security.AccessControl;
import org.matrix.doc.Document;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.iarxiu.mets.Metadata;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.iarxiu.pit.PITContent;

/**
 *
 * @author blanquepa
 * @author realor
 */
public class DefaultDocumentConverter implements DocumentConverter
{
  private String docServletPath = "http://localhost/documents"; //Default
  private HashMap<String, String> properties = new HashMap<String, String>();

  public String getDocServletPath()
  {
    return docServletPath;
  }

  public void setDocServletPath(String docServletPath)
  {
    this.docServletPath = docServletPath;
  }

  public void setProperty(String name, String value)
  {
    properties.put(name, value);
  }

  public String getProperty(String name)
  {
    return properties.get(name);
  }

  @Override
  public void convert(Object src, Metadata md)
    throws Exception
  {
    Document doc = ((Document)src);

    //Mandatory
    md.setProperty("codi_referencia", doc.getDocId());

    String classId = "UNDEFINED";
//    List<String> classIdList = doc.getClassId();
//    if (classIdList != null && classIdList.size() > 0)
//      classId = classIdList.get(0);
//    md.setProperty("codi_classificacio", classId);
//    md.setProperty("titol_serie_documental",
//      DocumentUtils.getPropertyValue(doc, "classTitle"));
//    String classTitle = DocumentUtils.getPropertyValue(doc, "classTitle");
//    md.setProperty("titol_serie_documental",
//      classTitle != null ? classTitle : classId);


    md.setProperty("numero_document", doc.getDocId());
    md.setProperty("titol", doc.getTitle());

    String creationDate = doc.getCreationDate() != null ?
      doc.getCreationDate() : doc.getCaptureDateTime();
    String formattedCreationDate = TextUtils.formatDateAsISO8601String(
      TextUtils.parseInternalDate(creationDate));
    md.setProperty("data_creacio", formattedCreationDate);

    md.setProperty("nivell_descripcio", "Unitat documental simple");
    md.setProperty("suport", "electrònic");
//    md.setProperty("nom_productor",);
//    md.setProperty("unitat_productora",);
//    md.setProperty("descripcio", );
//    md.setProperty("descriptors", );
//    md.setProperty("tipus_document", doc.getDocTypeId());
    md.setProperty("classificacio_seguretat_acces",
      isPublic(doc) ? "Accés públic" : "Accés restringit");
//    md.setProperty("sensibilitat_dades_LOPD",);
//    md.setProperty("nivell_classificacio_evidencial",);
//    md.setProperty("document_essencial",);

    for (String propertyName : properties.keySet())
    {
      String propertyValue = properties.get(propertyName);
      md.setProperty(propertyName, propertyValue);
    }
  }

  @Override
  public void convert(Object src, PITContent pitContent) throws Exception
  {
    if (!(src instanceof Document))
      throw new Exception("Invalid source class: " + src.getClass());

    Document document = (Document)src;

    Content content = document.getContent();
    String contentType = content.getContentType();
    String filename = DocumentUtils.getFilename(document.getTitle());
    String extension = MimeTypeMap.getMimeTypeMap().getExtension(contentType);

    File file = getFile(document);

    if (file != null)
      pitContent.setFile(file, contentType, filename + "." + extension);
  }

  protected boolean isPublic(org.matrix.doc.Document document)
  {
    List<AccessControl> acl = document.getAccessControl();
    for(AccessControl ac : acl)
    {
      String action = ac.getAction();
      String roleId = ac.getRoleId();
      if ("Read".equals(action) && "EVERYONE".equals(roleId))
        return true;
    }

    return false;
  }

  protected File getFile(Document document) throws IOException, Exception
  {
    File file = null;

    Content content = document.getContent();
    String contentType = content.getContentType();
    String extension =  MimeTypeMap.getMimeTypeMap().getExtension(contentType);
    String filename = DocumentUtils.getFilename(document.getTitle());

    DataHandler dataHandler = content.getData();
    if (dataHandler != null)
    {
      file = File.createTempFile(filename + "_", "." + extension);
      IOUtils.writeToFile(dataHandler, file);
      file.deleteOnExit();
    }
    else
    {
      String urlString = docServletPath + "/" + content.getContentId();
      URL url = new URL(urlString);
      URLConnection connection = url.openConnection();
      connection.connect();

      // Cast to a HttpURLConnection
      if (connection instanceof HttpURLConnection)
      {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        int code = httpConnection.getResponseCode();
        if (code == 200)
        {
          InputStream is = httpConnection.getInputStream();
          try
          {
            file = File.createTempFile(filename + "_", "." + extension);
            IOUtils.writeToFile(is, file);
            file.deleteOnExit();
          }
          finally
          {
            is.close();
          }
        }
        else
          throw new Exception("Connection to  " +
            urlString + " failed. " + httpConnection.getResponseMessage());
      }
      else
      {
         throw new Exception("Not HTTP request");
      }
    }
    return file;
  }

}
