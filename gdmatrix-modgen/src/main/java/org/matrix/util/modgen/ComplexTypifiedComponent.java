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
package org.matrix.util.modgen;

/**
 *
 * @author realor
 */
public abstract class ComplexTypifiedComponent extends TypifiedComponent
{
  private String minOccurs;
  private String maxOccurs;
  private String references;
  private boolean nillable;
  private boolean readOnly;
  private String expectedContentTypes;

  public String getExpectedContentTypes()
  {
    return expectedContentTypes;
  }

  public void setExpectedContentTypes(String contentType)
  {
    this.expectedContentTypes = contentType;
  }

  public String getMinOccurs()
  {
    return minOccurs;
  }

  public void setMinOccurs(String minOccurs)
  {
    this.minOccurs = minOccurs;
  }

  public String getMaxOccurs()
  {
    return maxOccurs;
  }

  public void setMaxOccurs(String maxOccurs)
  {
    this.maxOccurs = maxOccurs;
  }

  public boolean isNillable()
  {
    return nillable;
  }

  public void setNillable(boolean nillable)
  {
    this.nillable = nillable;
  }

  public boolean isReadOnly()
  {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  public String getReferences()
  {
    return references;
  }

  public void setReferences(String references)
  {
    this.references = references;
  }
}
