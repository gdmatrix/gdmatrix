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
package org.santfeliu.webapp.modules.geo.sld;

import java.util.List;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
public class SldNamedLayer extends SldNode
{
  public SldNamedLayer()
  {
  }

  public SldNamedLayer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getLayerName()
  {
    return getElementText("Name");
  }

  public void setLayerName(String name)
  {
    int index = findNode("Name", 0);
    if (index != -1)
    {
      SldNode child = getChild(index);
      child.setTextValue(name);
    }
    else
    {
      SldNode node = new SldNode(null, "Name");
      node.setTextValue(name);
      insertChild(node, 0);
    }
  }

  public List<SldUserStyle> getUserStyles()
  {
    return findNodes(SldUserStyle.class);
  }

  public SldUserStyle addUserStyle()
  {
    SldUserStyle userStyle = new SldUserStyle(null, "UserStyle");
    addChild(userStyle);
    return userStyle;
  }

  public SldUserStyle getUserStyle(String name)
  {
    boolean found = false;
    SldUserStyle userStyle = null;
    int i = 0;
    while (!found && i < getChildCount())
    {
      SldNode child = getChild(i);
      if (child instanceof SldUserStyle)
      {
        userStyle = (SldUserStyle)child;
        String styleName = userStyle.getStyleName();
        if (isBlank(name) && isBlank(styleName))
          found = true;
        else if (name != null && name.equals(styleName))
          found = true;
      }
      i++;
    }
    return found ? userStyle : null;
  }

  public SldNamedLayer duplicate()
  {
    SldRoot root = (SldRoot)getParent();
    SldNamedLayer newNode = root.addNamedLayer();
    newNode.setInnerElements(getInnerElements());
    newNode.setCustomData(getCustomData());
    return newNode;
  }
}
