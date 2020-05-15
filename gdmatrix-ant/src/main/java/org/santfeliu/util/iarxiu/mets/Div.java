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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class Div
{
  private List<Div> divs = new ArrayList<Div>();
  private List<Metadata> dmdMetadatas = new ArrayList<Metadata>();
  private List<Metadata> amdMetadatas = new ArrayList<Metadata>();
  private FileGrp fileGrp;
  private String label;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public void setFileGrp(FileGrp fileGrp)
  {
    this.fileGrp = fileGrp;
  }

  public FileGrp getFileGrp()
  {
    return fileGrp;
  }

  public void setDivs(List<Div> divs)
  {
    this.divs = divs;
  }

  public List<Div> getDivs()
  {
    return divs;
  }

  public void setAmdMetadatas(List<Metadata> amdMetadatas)
  {
    this.amdMetadatas = amdMetadatas;
  }

  public void setDmdMetadatas(List<Metadata> dmdMetadatas)
  {
    this.dmdMetadatas = dmdMetadatas;
  }

  public List<Metadata> getDmdMetadatas()
  {
    return dmdMetadatas;
  }

  public List<Metadata> getAmdMetadatas()
  {
    return amdMetadatas;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<div>");
    sb.append(label + " " + dmdMetadatas + "," + amdMetadatas);
    for (Div div : divs)
    {
      sb.append("\n"+div);
    }
    if (fileGrp != null)
      sb.append("*" +  fileGrp);
    sb.append("</div>");
    return sb.toString();
  }

  String getAmdMetadataIds()
  {
    return getMetadataIds(amdMetadatas);
  }

  String getDmdMetadataIds()
  {
    return getMetadataIds(dmdMetadatas);
  }

  private String getMetadataIds(List<Metadata> metadatas)
  {
    if (metadatas != null && metadatas.size() > 0)
    {
      StringBuffer sb = new StringBuffer();
      for (Metadata md : metadatas)
      {
        sb.append(normalizeId(md.getId()) + " ");
      }
      int lastIndex = sb.lastIndexOf(" ");
      
      if (lastIndex > 0)
        sb.deleteCharAt(lastIndex);
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
}
