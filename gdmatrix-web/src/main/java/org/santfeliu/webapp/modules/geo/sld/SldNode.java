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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.modules.geo.io.SldReader;
import org.santfeliu.webapp.modules.geo.io.SldWriter;

/**
 *
 * @author realor
 */
public class SldNode implements Serializable
{
  private String prefix;
  private String name;
  private Map<String, String> attributes;
  private List<SldNode> children;
  private SldNode parent;
  private String textValue;
  private Object customData;

  public SldNode()
  {
  }

  public SldNode(String name)
  {
    this.name = name;
  }

  public SldNode(String prefix, String name)
  {
    this.prefix = prefix;
    this.name = name;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public String getName()
  {
    return name;
  }

  public SldNode getParent()
  {
    return parent;
  }

  public Map<String, String> getAttributes()
  {
    if (attributes == null) attributes = new HashMap<>();
    return attributes;
  }

  public int getChildCount()
  {
    return children == null ? 0 : children.size();
  }

  public int getChildIndex(SldNode child)
  {
    return children.indexOf(child);
  }

  public SldNode getChild(int index)
  {
    if (children == null) return null;
    return children.get(index);
  }

  public void addChild(SldNode node)
  {
    if (children == null) children = new ArrayList<>();
    children.add(node);
    node.parent = this;
  }

  public void insertChild(SldNode node, int index)
  {
    if (children == null) children = new ArrayList<>();
    children.add(index, node);
    node.parent = this;
  }

  public void removeChild(int index)
  {
    if (children != null)
    {
      SldNode node = children.remove(index);
      node.parent = null;
    }
  }

  public void removeChild(SldNode node)
  {
    if (children != null)
    {
      children.remove(node);
      node.parent = null;
    }
  }

  public void remove()
  {
    if (parent != null)
    {
      parent.removeChild(this);
    }
  }

  public void moveUp()
  {
    if (parent != null)
    {
      SldNode currentParent = parent;
      int index = parent.getChildIndex(this);
      if (index > 0)
      {
        remove();
        currentParent.insertChild(this, index - 1);
      }
    }
  }

  public void moveDown()
  {
    if (parent != null)
    {
      SldNode currentParent = parent;
      int index = parent.getChildIndex(this);
      if (index >= 0 && index < parent.getChildCount() - 1)
      {
        remove();
        currentParent.insertChild(this, index + 1);
      }
    }
  }

  public String getTextValue()
  {
    return textValue;
  }

  public void setTextValue(String textValue)
  {
    this.textValue = textValue;
    children = null;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(name);
    if (attributes != null)
    {
      builder.append(" ");
      builder.append(attributes.toString());
    }
    return builder.toString();
  }

  public SldNode getRoot()
  {
    SldNode node = this;
    while (node.parent != null)
    {
      node = node.parent;
    }
    return node;
  }

  public String getInnerElements()
  {
    if (!StringUtils.isBlank(getTextValue()))
    {
      return StringEscapeUtils.escapeXml(getTextValue());
    }
    else
    {
      StringWriter sw = new StringWriter();
      SldWriter writer = new SldWriter();
      writer.setPretty(false);
      try
      {
        writer.writeChildren(this, sw, 0);
        return sw.toString();
      }
      catch (IOException ex)
      {
        return null;
      }
    }
  }

  public void setInnerElements(String elements)
  {
    if (children != null) children.clear();
    if (StringUtils.isBlank(elements))
    {
      setTextValue(null);
    }
    else if (!elements.contains("<"))
    {
      setTextValue(StringEscapeUtils.unescapeXml(elements)); // elements is text
    }
    else
    {
      setTextValue(null);
      StringBuilder buffer = new StringBuilder();
      {
        buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        buffer.append("<StyledLayerDescriptor");
        SldNode node = getRoot();
        if (node instanceof SldRoot)
        {
          Map<String, String> attr = node.getAttributes();
          for (String key : attr.keySet())
          {
            String value = attr.get(key);
            buffer.append(" ");
            buffer.append(key);
            buffer.append("=\"");
            buffer.append(value);
            buffer.append("\"");
          }
        }
        buffer.append(">");
        buffer.append(elements);
        buffer.append("</StyledLayerDescriptor>");
        SldReader reader = new SldReader();
        try
        {
          byte[] data = buffer.toString().getBytes("ISO-8859-1");
          SldRoot root = reader.read(new ByteArrayInputStream(data));
          int count = root.getChildCount();
          for (int i = 0; i < count; i++)
          {
            SldNode child = root.getChild(0);
            root.removeChild(0);
            this.addChild(child);
          }
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public String getInnerElements(String name)
  {
    return getInnerElements(null, name);
  }

  public String getInnerElements(String prefix, String name)
  {
    int index = findNode(prefix, name, 0);
    if (index != -1)
    {
      SldNode node = getChild(index);
      return node.getInnerElements();
    }
    return null;
  }

  public void setInnerElements(String name, String elements)
  {
    setInnerElements(null, name, elements);
  }

  public void setInnerElements(String prefix, String name, String elements)
  {
    if (StringUtils.isBlank(elements))
    {
      int index = findNode(prefix, name, 0);
      if (index != -1)
      {
        removeChild(index);
      }
    }
    else
    {
      SldNode node = getNode(prefix, name, SldNode.class);
      node.setInnerElements(elements);
    }
  }

  public Object getCustomData()
  {
    return customData;
  }

  public void setCustomData(Object customData)
  {
    this.customData = customData;
  }

  protected String getElementText(String name)
  {
    int index = findNode(name, 0);
    return index == -1 ? null : getChild(index).getTextValue();
  }

  protected <T extends SldNode> T getNode(String name, Class<T> cls)
  {
    return getNode(null, name, cls);
  }

  protected <T extends SldNode> T getNode(String prefix, String name,
    Class<T> cls)
  {
    T node = null;
    int index = findNode(name, 0);
    if (index == -1)
    {
      try
      {
        node = cls.getConstructor().newInstance();
        node.setPrefix(prefix);
        node.setName(name);
        addChild(node);
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    else node = (T)getChild(index);
    return node;
  }

  protected int findNode(String name, int from)
  {
    return findNode(null, name, from);
  }

  protected int findNode(String prefix, String name, int from)
  {
    if (children == null) return -1;
    boolean found = false;
    int index = from;
    while (!found && index < children.size())
    {
      SldNode child = children.get(index);
      if (child.name.equals(name))
      {
        found = (prefix == null) || prefix.equals(child.getPrefix());
      }
      if (!found) index++;
    }
    return found ? index : -1;
  }

  protected List<SldNode> findNodes(String name)
  {
    List<SldNode> nodes = new ArrayList<>();
    if (children == null) return nodes;
    for (SldNode child : children)
    {
      if (child.name.equals(name)) nodes.add(child);
    }
    return nodes;
  }

  protected <T extends SldNode> List<T> findNodes(Class<T> cls)
  {
    List<T> nodes = new ArrayList<>();
    if (children == null) return nodes;
    for (SldNode child : children)
    {
      if (cls.isAssignableFrom(child.getClass())) nodes.add((T)child);
    }
    return nodes;
  }

  protected void setName(String name)
  {
    this.name = name;
  }

  protected void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }
}
