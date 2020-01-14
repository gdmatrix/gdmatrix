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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author realor
 */
public class TransformationRequest implements Serializable
{
  protected String transformerId;
  protected String transformationName;
  protected String targetContentType;
  protected String targetFormatId;
  protected Map options;

  public TransformationRequest()
  {
  }

  public String getTransformerId()
  {
    return transformerId;
  }

  public void setTransformerId(String transformerId)
  {
    this.transformerId = transformerId;
  }

  public String getTransformationName()
  {
    return transformationName;
  }

  public void setTransformationName(String transformationName)
  {
    this.transformationName = transformationName;
  }

  public String getTargetContentType()
  {
    return targetContentType;
  }

  public void setTargetContentType(String targetContentType)
  {
    this.targetContentType = targetContentType;
  }

  public String getTargetFormatId()
  {
    return targetFormatId;
  }

  public void setTargetFormatId(String targetFormatId)
  {
    this.targetFormatId = targetFormatId;
  }

  public Map getOptions()
  {
    return options;
  }

  public void setOptions(Map options)
  {
    this.options = options;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    if (transformerId != null)
    {
      builder.append("id=");
      builder.append(transformerId);
      builder.append(";");
    }
    if (transformationName != null)
    {
      builder.append("name=");
      builder.append(transformationName);
      builder.append(";");
    }
    if (targetContentType != null)
    {
      builder.append("contentType=");
      builder.append(targetContentType);
      builder.append(";");
    }
    if (targetFormatId != null)
    {
      builder.append("formatId=");
      builder.append(targetFormatId);
      builder.append(";");
    }
    if (options != null && !options.isEmpty())
    {
      Object keys[] = options.keySet().toArray();
      Arrays.sort(keys);
      for (int i = 0; i < keys.length; i++)
      {
        String key = String.valueOf(keys[i]);
        String value = String.valueOf(options.get(key));
        builder.append(key);
        builder.append("=");
        builder.append(value);
        builder.append(";");
      }
    }
    return builder.toString();
  }
}
