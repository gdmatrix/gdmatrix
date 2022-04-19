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
package org.matrix.pf.web;

import java.io.Serializable;
import javax.faces.context.FacesContext;

/**
 *
 * @author blanquepa
 */
public class Tab implements Serializable
{
  private String label;
  private Integer index;
  private String action;
  private String typeId;
  
  public Tab(Integer index, String label, String typeId, String action)
  {
    this.label = label;
    this.index = index;
    this.action = action;
    this.typeId = typeId;
  }
  
  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Integer getIndex()
  {
    return index;
  }

  public void setIndex(Integer index)
  {
    this.index = index;
  }

  public String executeAction()
  {
    if (action.startsWith("#{") && action.endsWith("}"))
    {  
      FacesContext facesContext = FacesContext.getCurrentInstance();
      String value = facesContext.getApplication().evaluateExpressionGet(
        facesContext, action, String.class);   
      return value;
    }
    else
      return action;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }
  
  
}
