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
package cat.aoc.iarxiu.mets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author blanquepa
 */
public class Mets
{
  Map<String, Metadata> dmdMetadatas = new HashMap<>();
  Map<String, Metadata> amdMetadatas = new HashMap<>();
  Map<String, FileGrp> fileGrps = new HashMap<>();
  Div struct = new Div();

  Map<String, XSDInfo> regSchemas = new HashMap<>();
  private String filePath;
  private String type;

  public void load(File inFile) throws Exception
  {
    InputStream is = null;
    try
    {
      filePath = inFile.getParent();
      if (inFile != null)
      {
        is = new FileInputStream(inFile);
      }

      MetsLoader loader = new MetsLoader(this);
      loader.load(is);
    }
    finally
    {
      is.close();
    }
  }

  public void saveAs(File outFile) throws Exception
  {
    FileOutputStream fos = new FileOutputStream(outFile);
    try
    {
      MetsWriter writer = new MetsWriter(this);
      writer.save(fos);
    }
    finally
    {
      fos.close();
    }
  }

  public void registerSchema(String urn, File xsdFile) throws Exception
  {
    if (xsdFile != null)
    {
      FileInputStream fis = new FileInputStream(xsdFile);
      registerSchema(urn, fis);
    }
  }

  public void registerSchema(String urn, InputStream xsdStream) throws Exception
  {
    if (xsdStream != null)
    {
      try
      {
        XSDParser parser = new XSDParser();
        XSDInfo xsdData = parser.parse(xsdStream);
        regSchemas.put(urn, xsdData);
      }
      finally
      {
        xsdStream.close();
      }
    }
  }


  public Metadata newDmdMetadata(String id, String type, String urn)
    throws Exception
  {
    if (id == null)
      id = UUID.randomUUID().toString();
    else
      id = normalizeId(id);

    Metadata metadata = newMetadata(id, type, urn);
    dmdMetadatas.put(id, metadata);
    return metadata;
  }

  public Metadata newAmdMetadata(String id, String type, String urn)
    throws Exception
  {
    if (id == null)
      id = UUID.randomUUID().toString();
    else
      id = normalizeId(id);

    Metadata metadata = newMetadata(id, type, urn);
    amdMetadatas.put(id, metadata);
    return metadata;
  }

  public FileGrp newFileGrp(String id, String filename)
  {
    FileGrp fileGrp = new FileGrp();
    fileGrp.setId(id);
    File file = new File(filename);
    fileGrp.setFile(file);
    fileGrp.setHref(file.getName());
    fileGrp.setFilePath(file.getParent());

    fileGrps.put(id, fileGrp);

    return fileGrp;
  }

  public Collection<Metadata> getDmdMetadatas()
  {
    return Collections.unmodifiableCollection(dmdMetadatas.values());
  }

  public Metadata getDmdMetadata(String mdId)
  {
    return dmdMetadatas.get(mdId);
  }

  public Collection<Metadata> getAmdMetadatas()
  {
    return Collections.unmodifiableCollection(amdMetadatas.values());
  }

  public Metadata getAmdMetadata(String mdId)
  {
    return amdMetadatas.get(mdId);
  }

  public Collection<FileGrp> getFileGrp()
  {
    return Collections.unmodifiableCollection(fileGrps.values());
  }

  public Div getStruct()
  {
    return struct;
  }

  public void setStruct(Div struct)
  {
    this.struct = struct;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }


  private Metadata newMetadata(String id, String type, String urn)
    throws Exception
  {
    if (type == null)
      throw new Exception("TYPE_NULL");

    Metadata metadata = null;
    if (type != null)
    {
      if (type.equals(MetsConstants.OTHER_MDTYPE_VALUE) && urn != null)
      {
        XSDInfo xsdInfo = regSchemas.get(urn);
        OtherMetadata md = new OtherMetadata();
        md.setNames(xsdInfo.getPropertyNames());
        md.setNamespace(xsdInfo.getNamespace());
        md.setUrn(urn);
        metadata = md;
      }
      else if (type.equals(MetsConstants.DC_MDTYPE_VALUE))
      {
        DCMetadata md = new DCMetadata();
        metadata = md;
      }
      else if (type.equals(MetsConstants.PREMIS_MDTYPE_VALUE))
      {
        //TODO: specific Metadata impl.
        GenericXmlMetadata md = new GenericXmlMetadata();
        md.setType("PREMIS");
        metadata = md;
      }
      else
      {
        GenericXmlMetadata md = new GenericXmlMetadata();
        md.setType(type);
        metadata = md;
      }
      metadata.setId(id);
    }

    return metadata;
  }

  String getFilePath()
  {
    return filePath;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  private String normalizeId(String value)
  {
    value = value.replaceAll(" ", "_");
    return value;
  }
}
