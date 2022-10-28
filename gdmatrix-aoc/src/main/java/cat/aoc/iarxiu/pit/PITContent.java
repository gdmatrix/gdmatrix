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
package cat.aoc.iarxiu.pit;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import cat.aoc.iarxiu.mets.Div;
import cat.aoc.iarxiu.mets.FileGrp;
import cat.aoc.iarxiu.mets.Metadata;
import cat.aoc.iarxiu.mets.MetsConstants;
import org.santfeliu.util.IOUtils;

/**
 *
 * @author blanquepa
 */
public class PITContent extends PITObject
{
//  private File file;

  PITContent(PIT pit)
  {
    this.div = new Div();
    this.div.setFileGrp(new FileGrp());
    this.pit = pit;
  }

  public String getName()
  {
    return div.getLabel();
  }

  public void setName(String name)
  {
    div.setLabel(name);
  }

  public void setId(String id)
  {
    FileGrp fileGrp = div.getFileGrp();
    if (fileGrp != null)
      fileGrp.setId(id);
  }

  public String getId()
  {
    FileGrp fileGrp = div.getFileGrp();
    if (fileGrp != null)
      return fileGrp.getId();
    else
      return null;
  }

  void setMimeType(String mimeType)
  {
    div.getFileGrp().setMimeType(mimeType);
  }

  String getMimeType()
  {
    return div.getFileGrp().getMimeType();
  }


  public File getFile() throws Exception
  {
    if (div.getFileGrp().isFileNull())
    {
      File file = File.createTempFile("pit", ".tmp");
      InputStream is = getInputStream();
      if (is != null)
        IOUtils.writeToFile(getInputStream(), file);
      div.getFileGrp().setFile(file);
    }

    return div.getFileGrp().getFile();
  }

  public InputStream getInputStream() throws Exception
  {
    InputStream is = null;
    FileGrp fileGrp = div.getFileGrp();
    if (pit.getZipFile() == null)
    {
      if (fileGrp != null) is = fileGrp.getInputStream();
    }
    else
    {
      ZipFile zipFile = new ZipFile(pit.getZipFile());
      ZipEntry zipEntry = zipFile.getEntry(fileGrp.getHref());
      is = zipFile.getInputStream(zipEntry);
    }

    return is;
  }

  void copyFrom(Div div)
  {
    this.div.setLabel(div.getLabel());
    this.div.setAmdMetadatas(div.getAmdMetadatas());
    this.div.setFileGrp(div.getFileGrp());
  }

  public Metadata newAmdMetadata() throws Exception
  {
    return newAmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.SIGNATURE_URN);
  }

  @Override
  public Metadata newAmdMetadata(String type, String urn) throws Exception
  {
    int count = getAmdMetadatas().size();
    String id = div.getFileGrp().getId() + "_AMD_" + String.valueOf(count);
    Metadata metadata = pit.getMets().newAmdMetadata(id, type, urn);
    div.getAmdMetadatas().add(metadata);

    return metadata;
//    return super.newAmdMetadata(type, urn);
  }

  @Override
  public List<Metadata> getAmdMetadatas()
  {
    List<Metadata> result = new ArrayList<>();
    result.addAll(super.getAmdMetadatas());
    result.addAll(div.getFileGrp().getMetadatas());
    return result;
  }

  public void setFile(File file, String mimeType)
  {
    setFile(file, mimeType, file.getName());
  }

  public void setFile(File file, String mimeType, String name)
  {
    FileGrp fileGrp =
      pit.getMets().newFileGrp(getName(), file.getAbsolutePath());
    fileGrp.setMimeType(mimeType);
    div.setLabel(name);
    div.setFileGrp(fileGrp);
  }

}
