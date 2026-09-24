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
package org.santfeliu.webapp.modules.geo.ogc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class FeatureDescription implements Serializable
{
  String elementFormDefault;
  String targetNamespace;
  String targetPrefix;
  List<FeatureType> featureTypes = new ArrayList<>();

  public String getElementFormDefault()
  {
    return elementFormDefault;
  }

  public void setElementFormDefault(String elementFormDefault)
  {
    this.elementFormDefault = elementFormDefault;
  }

  public String getTargetNamespace()
  {
    return targetNamespace;
  }

  public void setTargetNamespace(String targetNamespace)
  {
    this.targetNamespace = targetNamespace;
  }

  public String getTargetPrefix()
  {
    return targetPrefix;
  }

  public void setTargetPrefix(String targetPrefix)
  {
    this.targetPrefix = targetPrefix;
  }

  public List<FeatureType> getFeatureTypes()
  {
    return featureTypes;
  }

  public void setFeatureTypes(List<FeatureType> featureTypes)
  {
    this.featureTypes = featureTypes;
  }
  
  public static class FeatureType
  {
    String typeName;
    List<Property> properties = new ArrayList<>();

    public String getTypeName()
    {
      return typeName;
    }

    public void setTypeName(String typeName)
    {
      this.typeName = typeName;
    }

    public List<Property> getProperties()
    {
      return properties;
    }

    public void setProperties(List<Property> properties)
    {
      this.properties = properties;
    }
  }
  
  public static class Property
  {
    String name;
    int maxOccurs;
    int minOccurs;
    boolean nillable;
    String type;
    String localType;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public int getMaxOccurs()
    {
      return maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs)
    {
      this.maxOccurs = maxOccurs;
    }

    public int getMinOccurs()
    {
      return minOccurs;
    }

    public void setMinOccurs(int minOccurs)
    {
      this.minOccurs = minOccurs;
    }

    public boolean isNillable()
    {
      return nillable;
    }

    public void setNillable(boolean nillable)
    {
      this.nillable = nillable;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public String getLocalType()
    {
      return localType;
    }

    public void setLocalType(String localType)
    {
      this.localType = localType;
    }
  }
}
