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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author real
 */
public class SLDUserStyle extends SLDNode
{
  public SLDUserStyle()
  {
  }

  public SLDUserStyle(String prefix, String name)
  {
    super(prefix, name);
  }
  
  public String getStyleName()
  {
    return getElementText("Name");
  }  

  public void setStyleName(String name)
  {
    int index = findNode("Name", 0);
    if (index != -1)
    {
      SLDNode child = getChild(index);
      child.setTextValue(name);
    }
    else
    {
      SLDNode node = new SLDNode(null, "Name");
      node.setTextValue(name);
      insertChild(node, 0);
    }
  }

  public boolean isDefaultStyle()
  {
    String value = getElementText("IsDefault");
    return "1".equals(value);
  }

  public void setDefaultStyle(boolean def)
  {
    SLDNode node = getNode("IsDefault", SLDNode.class);
    node.setTextValue(def ? "1" : "0");
  }
  
  public List<SLDRule> getRules()
  {
    int index = findNode("FeatureTypeStyle", 0);
    if (index == -1) return Collections.EMPTY_LIST;
    SLDNode featureStyle = (SLDNode)getChild(index);
    return featureStyle.findNodes(SLDRule.class);
  }

  public SLDRule addRule()
  {
    SLDNode node = getNode("FeatureTypeStyle", SLDNode.class);
    SLDRule rule = new SLDRule(null, "Rule");
    node.addChild(rule);
    return rule;
  }
}
