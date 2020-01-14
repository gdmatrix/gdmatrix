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
package org.santfeliu.form.type.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.santfeliu.form.View;

/**
 *
 * @author realor
 */
public class HtmlView implements View, Cloneable
{
  String id;
  String reference;
  String viewType;
  HtmlView parent;
  List<View> children = new ChildrenList();
  Map<String, String> properties = new HashMap();
  String tag;

  public HtmlView()
  {
  }

  public HtmlView(String tag)
  {
    this.tag = tag;
  }

  public HtmlView(String tag, String viewType)
  {
    this.tag = tag;
    this.viewType = viewType;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setReference(String reference)
  {
    this.reference = reference;
  }

  public String getReference()
  {
    return reference;
  }

  public void setViewType(String viewType)
  {
    this.viewType = viewType;
  }

  public String getViewType()
  {
    return viewType;
  }

  public HtmlView getParent()
  {
    return parent;
  }

  public List<View> getChildren()
  {
    return children;
  }

  public void setProperty(String name, String value)
  {
    properties.put(name, value);
  }

  public String getProperty(String name)
  {
    return properties.get(name);
  }

  public Collection<String> getPropertyNames()
  {
    return properties.keySet();
  }

  public String getNativeViewType()
  {
    return tag;
  }

  public void setNativeViewType(String tag)
  {
    this.tag = tag;
  }

  // clones View fields except parent and children
  @Override
  public HtmlView clone()
  {
    try
    {
      HtmlView newView = getClass().newInstance();
      newView.id = id;
      newView.reference = reference;
      newView.properties.putAll(properties);
      newView.tag = tag;
      newView.viewType = viewType;
      return newView;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  class ChildrenList extends ArrayList<View>
  {
    @Override
    public boolean add(View view)
    {
      HtmlView htmlView = (HtmlView)view;
      super.add(htmlView);
      htmlView.parent = HtmlView.this;
      return true;
    }

    @Override
    public boolean remove(Object o)
    {
      if (o instanceof HtmlView)
      {
        HtmlView htmlView = (HtmlView)o;
        boolean removed = super.remove(htmlView);
        if (removed) htmlView.parent = null;
        return removed;
      }
      return false;
    }
  }
}
