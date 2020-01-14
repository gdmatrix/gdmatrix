package org.santfeliu.misc.mapviewer.sld;

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
import org.santfeliu.misc.mapviewer.io.SLDReader;
import org.santfeliu.misc.mapviewer.io.SLDWriter;

/**
 *
 * @author realor
 */
public class SLDNode implements Serializable
{
  private String prefix;
  private String name;
  private Map<String, String> attributes;
  private List<SLDNode> children;
  private SLDNode parent;
  private String textValue;
  private Object customData;

  public SLDNode()
  {
  }

  public SLDNode(String name)
  {
    this.name = name;
  }

  public SLDNode(String prefix, String name)
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
  
  public SLDNode getParent()
  {
    return parent;
  }
  
  public Map<String, String> getAttributes()
  {
    if (attributes == null) attributes = new HashMap<String, String>();
    return attributes;
  }
  
  public int getChildCount()
  {
    return children == null ? 0 : children.size();
  }
  
  public SLDNode getChild(int index)
  {
    if (children == null) return null;
    return children.get(index);
  }

  public void addChild(SLDNode node)
  {
    if (children == null) children = new ArrayList<SLDNode>();
    children.add(node);
    node.parent = this;
  }

  public void insertChild(SLDNode node, int index)
  {
    if (children == null) children = new ArrayList<SLDNode>();
    children.add(index, node);
    node.parent = this;
  }

  public void removeChild(int index)
  {
    if (children != null)
    {
      SLDNode node = children.remove(index);
      node.parent = null;
    }
  }
  
  public void removeChild(SLDNode node)
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

  public String getTextValue()
  {
    return textValue;
  }

  public void setTextValue(String textValue)
  {
    this.textValue = textValue;
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

  public SLDNode getRoot()
  {
    SLDNode node = this;
    while (node.parent != null)
    {
      node = node.parent;
    }
    return node;
  }

  public String getInnerElements()
  {
    if (getChildCount() == 0 && getTextValue() != null)
    {
      return StringEscapeUtils.escapeXml(getTextValue());
    }
    else
    {
      StringWriter sw = new StringWriter();
      SLDWriter writer = new SLDWriter();
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
    else if (elements.indexOf("<") == -1)
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
        SLDNode node = getRoot();
        if (node instanceof SLDRoot)
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
        SLDReader reader = new SLDReader();
        try
        {
          byte[] data = buffer.toString().getBytes("ISO-8859-1");
          SLDRoot root = reader.read(new ByteArrayInputStream(data));
          int count = root.getChildCount();
          for (int i = 0; i < count; i++)
          {
            SLDNode child = root.getChild(0);
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
      SLDNode node = getChild(index);
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
      SLDNode node = getNode(prefix, name, SLDNode.class);
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

  protected <T extends SLDNode> T getNode(String name, Class<T> cls)
  {
    return getNode(null, name, cls);
  }

  protected <T extends SLDNode> T getNode(String prefix, String name,
    Class<T> cls)
  {
    T node = null;
    int index = findNode(name, 0);
    if (index == -1)
    {
      try
      {
        node = cls.newInstance();
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
      SLDNode child = children.get(index);
      if (child.name.equals(name))
      {        
        found = (prefix == null) || prefix.equals(child.getPrefix());
      }
      if (!found) index++;
    }
    return found ? index : -1;
  }
  
  protected List<SLDNode> findNodes(String name)
  {
    List<SLDNode> nodes = new ArrayList<SLDNode>();
    if (children == null) return nodes;
    for (SLDNode child : children)
    {
      if (child.name.equals(name)) nodes.add(child);
    } 
    return nodes;
  }

  protected <T extends SLDNode> List<T> findNodes(Class<T> cls)
  {
    List<T> nodes = new ArrayList<T>();
    if (children == null) return nodes;
    for (SLDNode child : children)
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
