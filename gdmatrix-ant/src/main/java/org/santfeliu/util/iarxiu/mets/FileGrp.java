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
package org.santfeliu.util.iarxiu.mets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.santfeliu.util.IOUtils;
/**
 *
 * @author blanquepa
 */
public class FileGrp
{
  String mimeType;
  List<Metadata> metadatas = new ArrayList<>();

  private String id;
  private String name;
  private String checksum;
  private String checksumType;
  private String createdDateTime;
  private String href;
  private String data;
  private File file;
  private String filePath;

  public FileGrp()
  {
  }

  public void setFile(File file)
  {
    this.file = file;
    this.name = file.getName();
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  String getChecksum()
  {
    return checksum;
  }

  void setChecksum(String checksum)
  {
    this.checksum = checksum;
  }

  String getChecksumType()
  {
    return checksumType;
  }

  void setChecksumType(String checksumType)
  {
    this.checksumType = checksumType;
  }

  String getCreatedDateTime()
  {
    return createdDateTime;
  }

  void setCreatedDateTime(String createdDateTime)
  {
    this.createdDateTime = createdDateTime;
  }

  public String getData()
  {
    return data;
  }

  void setData(String data)
  {
    this.data = data;
  }

  public String getHref()
  {
    return href;
  }

  void setHref(String href)
  {
    this.href = href;
  }

  public String getName()
  {
    return name;
  }

  void setName(String name)
  {
    this.name = name;
  }

  String getFilePath()
  {
    return filePath;
  }

  void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  //Public
  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }


  //Actions
  public Metadata addMetadata(Metadata metadata)
  {
    int index = metadatas.size();
    String mdId = normalizeId(this.name + "_AMD_" + index);
    metadata.id = mdId;

    metadatas.add(metadata);
    return metadata;
  }

  public List<Metadata> getMetadatas()
  {
    return metadatas;
  }


  public File getFile() throws Exception
    // retorn una ruta al sistema de ficheros del PC
  {
    // lazy loading
    if (isFileNull())
    {
      if (href != null)
      {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String filename = null;
        int lastIndex = href.lastIndexOf("/");
        if (lastIndex > 0)
          filename = href.substring(lastIndex);
        else
          filename = href;
        file = new File(tmpdir, filename);
      }
      else
        file = File.createTempFile("mets", ".tmp");

      InputStream is = getInputStream();
      if (is != null)
        IOUtils.writeToFile(getInputStream(), file);
    }
    return file;
  }

  public boolean isFileNull()
  {
    return file == null;
  }

  public InputStream getInputStream() throws Exception
  {
    InputStream is = null;
    if (href != null)
    {
      if (filePath.endsWith(".zip"))
      {
        ZipFile zf = new ZipFile(filePath);
        ZipEntry zipEntry = zf.getEntry(href);
        is = zf.getInputStream(zipEntry);
      }
      else
      {
        File f = new File(filePath + File.separator + href);
        is = new FileInputStream(f);
      }
    }
    else if (data != null)
    {
      is = new ByteArrayInputStream(Base64.getMimeDecoder().decode(data));
    }
    return is;
  }

  String getMetadataId()
  {
    if (metadatas.size() > 0)
    {
      StringBuilder sb = new StringBuilder();
      for (Metadata md : metadatas)
      {
        sb.append(md.id).append(" ");
      }
      sb.deleteCharAt(sb.lastIndexOf(" "));
      return sb.toString();
    }
    else
      return null;
  }

  private String normalizeId(String value)
  {
    value = value.replaceAll(" ", "_");
    return value;
  }

  @Override
  public String toString()
  {
    return id + " " + metadatas;
  }

}
