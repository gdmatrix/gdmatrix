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
package org.santfeliu.misc.mapviewer.sld;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class SLDCssNode extends SLDNode
{
  public SLDCssNode()
  {
  }

  public SLDCssNode(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getCssParameter(String parameter)
  {
    String value = null;
    boolean found = false;
    int index = 0;
    while (!found && index < getChildCount())
    {
      SLDNode child = getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName))
      {
        found = true;
        value = child.getTextValue();
      }
      else index++;
    }
    return found ? value : null;
  }

  public void setCssParameter(String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    boolean found = false;
    int index = 0;
    SLDNode child = null;
    while (!found && index < getChildCount())
    {
      child = getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName)) found = true;
      else index++;
    }
    if (value == null)
    {
      if (found)
      {
        removeChild(index);
      }
    }
    else // value != null
    {
      if (found)
      {
        child.setTextValue(value);
      }
      else
      {
        SLDNode cssNode = new SLDNode(null, "CssParameter");
        cssNode.getAttributes().put("name", parameter);
        cssNode.setTextValue(value);
        addChild(cssNode);
      }
    }
  }
}
