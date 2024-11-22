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
package org.santfeliu.doc.client;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import javax.activation.DataHandler;

import org.matrix.doc.Content;

import org.santfeliu.util.FileProvider;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author unknown
 */
public class UUIDFileProvider extends FileProvider
{
  private String wsdlLocation;
  private URL wsDirectoryURL;

  public UUIDFileProvider()
  {
  }

  @Deprecated
  public UUIDFileProvider(String wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  public UUIDFileProvider(URL wsDirectoryURL)
  {
    this.wsDirectoryURL = wsDirectoryURL;
  }

  public URL getWsDirectoryURL()
  {
    return wsDirectoryURL;
  }

  public void setWsDirectoryURL(URL wsDirectoryURL)
  {
    this.wsDirectoryURL = wsDirectoryURL;
  }

  @Deprecated
  public void setWsdlLocation(String wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  @Deprecated
  public String getWsdlLocation()
  {
    return wsdlLocation;
  }

  @Override
  public File loadFile(String UUID) throws Exception
  {
    File file = null;

    DocumentManagerClient client = null;
    if (wsDirectoryURL != null)
      client = new DocumentManagerClient(wsDirectoryURL);
    else
      client = new DocumentManagerClient(new URL(wsdlLocation));

    Content content = client.loadContent(UUID);
    DataHandler dh = content.getData();

    MimeTypeMap mimeMap = MimeTypeMap.getMimeTypeMap();
    String extension = mimeMap.getExtension(content.getContentType());
    if (extension == null) extension = "tmp";
    extension = "." + extension;
    file = File.createTempFile("file", extension);
    FileOutputStream fos = new FileOutputStream(file);
    try
    {
      dh.writeTo(fos);
    }
    finally
    {
      fos.close();
    }
    System.out.println("File " + file + " created.");
    return file;
  }

  @Override
  public String selectFile()
  {
    return null; // TODO
  }
}
