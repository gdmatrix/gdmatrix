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
package org.santfeliu.doc.transform;

import org.matrix.doc.Content;
import org.matrix.doc.Document;

/**
 *
 * @author realor
 */
public class Transformation
{
  private String transformerId;
  private String name;
  private String targetFormatId;
  private String sourceContentType;
  private String sourceFormatId;
  private String targetContentType;
  private Boolean persistent;
  private String description;

  public Transformation()
  {
  }

  public Transformation(String transformerId, String name,
    String sourceContentType, String sourceFormatId,
    String targetContentType, String targetFormatId,
    Boolean persistent, String description)
  {
    this.transformerId = transformerId;
    this.name = name;
    this.sourceContentType = sourceContentType;
    this.sourceFormatId = sourceFormatId;
    this.targetContentType = targetContentType;
    this.targetFormatId = targetFormatId;
    this.persistent = persistent;
    this.description = description;
  }

  public Transformation(Document document)
  {
    this(document, null);
  }

  public Transformation(Document document, TransformationRequest request)
  {
    if (document != null)
    {
      Content content = document.getContent();
      if (content != null)
      {
        this.sourceContentType = content.getContentType();
        this.sourceFormatId = content.getFormatId();
      }
    }
    if (request != null)
    {
      this.transformerId = request.getTransformerId();
      this.name = request.getTransformationName();
      this.targetContentType = request.getTargetContentType();
      this.targetFormatId = request.getTargetFormatId();
    }
  }

  public String getTransformerId()
  {
    return transformerId;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getSourceContentType()
  {
    return sourceContentType;
  }

  public String getSourceFormatId()
  {
    return sourceFormatId;
  }

  public String getTargetContentType()
  {
    return targetContentType;
  }

  public String getTargetFormatId()
  {
    return targetFormatId;
  }

  public Boolean getPersistent()
  {
    return persistent;
  }

  public boolean isSuitableFor(Transformation other)
  {
    if (other == null) return true;

    if (other.transformerId != null && transformerId != null)
    {
      if (!other.transformerId.equals(transformerId))
        return false;
    }

    if (other.name != null && name != null)
    {
      if (!other.name.equals(name))
        return false;
    }

    if (other.sourceContentType != null && sourceContentType != null)
    {
      if (!other.sourceContentType.equals(sourceContentType))
        return false;
    }

    if (other.sourceFormatId != null && sourceFormatId != null)
    {
      if (!other.sourceFormatId.equals(sourceFormatId))
        return false;
    }

    if (other.targetContentType != null && targetContentType != null)
    {
      if (!other.targetContentType.equals(targetContentType))
        return false;
    }

    if (other.targetFormatId != null && targetFormatId != null)
    {
      if (!other.targetFormatId.equals(targetFormatId))
        return false;
    }

    if (other.persistent != null && persistent != null)
    {
      if (!other.persistent.equals(persistent))
        return false;
    }

    if (other.description != null && description != null)
    {
      if (description.indexOf(other.description) == -1)
        return false;
    }

    return true;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(transformerId == null ? "*" : transformerId);
    builder.append("/");
    builder.append(name == null ? "*" : name);
    builder.append(":");
    builder.append(description == null ? "-" : description);
    builder.append(":");
    builder.append(sourceContentType == null ? "*" : sourceContentType);
    builder.append(" -> ");
    builder.append(targetContentType == null ? "*" : targetContentType);
    return builder.toString();
  }
}
