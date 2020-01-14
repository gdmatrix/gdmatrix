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
package org.santfeliu.web.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
public abstract class GroupablePageBean extends PageBean 
{
  public static final String ALL_ROWS_NAME = "ALL_ROWS";
  public static final String NULL_GROUP_NAME = ";"; 
  
  protected Group NULL_GROUP = new Group(NULL_GROUP_NAME, "");
  protected Group ALL_ROWS_GROUP = new Group(ALL_ROWS_NAME, "Mostrar tot");  
  
  //Node properties
  @CMSProperty
  public static final String GROUPS_PAGE_SIZE_PROPERTY = "groupsPageSize";

  public static final String NONE_SELECTION_MODE = "none";
  public static final String BUTTON_SELECTION_MODE = "button";
  public static final String BAR_SELECTION_MODE = "bar";

  protected String groupBy;
  protected TreeMap<Group, List> groupedRows; //Contains all groups
  protected List<Group> renderGroups = new ArrayList(); //Contains the groups to be rendered
  protected String groupSelectionMode = NONE_SELECTION_MODE;  

  protected List<String> orderBy;
  
  public abstract GroupExtractor getGroupExtractor();   
  
  public Set<Group> getGroups()
  {
    if (groupedRows != null)
    {
      return groupedRows.keySet();
    }
    else
      return null;
  }

  public List getRows()
  {
    Group group = (Group)getValue("#{group}");
    if (groupedRows != null)
    {
      List rows = groupedRows.get(group != null ? group : new Group(NULL_GROUP_NAME, ""));
      if (orderBy != null && !orderBy.isEmpty())
        Collections.sort(rows, getPropertyComparator());
      return rows;
    }
    else
      return null;
  }
  
  public Comparator getPropertyComparator()
  {
    return new PropertyComparator();
  }
  
  public boolean isRowsEmpty()
  {
    Group group = (Group)getValue("#{group}");
    if (groupedRows != null)
    {
      List rows = groupedRows.get(group);
      return rows == null || rows.isEmpty();
    }
    return true;
  }

  public int getFirstRowIndex()
  {
    Group group = (Group)getValue("#{group}");
    if (group != null)
      return group.getFirst();
    else
      return 0;
  }

  public void setFirstRowIndex(int index)
  {
    Group group = (Group)getValue("#{group}");
    if (group != null)
      group.setFirst(index);
  }

  public void resetFirstRowIndexes()
  {
    Set<Group> groups = groupedRows.keySet();
    for (Group group : groups)
    {
      group.setFirst(0);
    }
  }
  
  protected void setGroups(List rows, GroupExtractor extractor)
  {
    groupedRows = new TreeMap<Group, List>();
    if (!rows.isEmpty()) //there are some rows to group
    {
      if (extractor != null)
      {
        for (Object row : rows)
        {
          Group key = extractor.getGroup(row);
          if (key == null) key = NULL_GROUP;

          List list = groupedRows.get(key);
          if (list == null)
            list = new ArrayList();
          list.add(row);
          groupedRows.put(key, list);
        }
        if (BUTTON_SELECTION_MODE.equals(groupSelectionMode))
        {
          if (groupedRows.size() > 1) //There is more than one group
          {
            groupedRows.put(ALL_ROWS_GROUP, new ArrayList());
            groupedRows.get(ALL_ROWS_GROUP).addAll(rows);
          }
          if (renderGroups.isEmpty() || groupedRows.get(renderGroups.get(0)) == null)
          {
            renderGroups.clear();
            renderGroups.add(groupedRows.firstKey());
          }
        }
        else
        {
          renderGroups.addAll(groupedRows.keySet());
        }              
      }
      else
        groupedRows.put(NULL_GROUP, rows);      
    }
    else //there is not any row to group
    {
      renderGroups.clear();
    }
  }

  public boolean isSingleGroup()
  {
    return (groupedRows == null || groupedRows.size() <= 1 || isRenderGroupButtonMode());
  }
  
  public int getGroupsPageSize()
  {
    if (isRenderGroupButtonMode()) return Integer.MAX_VALUE;
    
    String pageSize = getSelectedMenuItem().getProperty(GROUPS_PAGE_SIZE_PROPERTY);
    if (pageSize != null)
      return Integer.valueOf(pageSize);
    else
      return PAGE_SIZE;
  }

  public String showGroup()
  {
    Group group = (Group)getValue("#{group}");

    if (isRenderGroupBarMode())
    {
      if (renderGroups.contains(group))
        renderGroups.remove(group);
      else
        renderGroups.add(group);
    }
    else if (isRenderGroupButtonMode())
    {
      renderGroups.clear();
      renderGroups.add(group);
    }

    return null;
  }

  public boolean isRenderGroup()
  {
    Group group = (Group)getValue("#{group}");
    return
      groupBy == null || (renderGroups != null && renderGroups.contains(group));
  }

  public Group getRenderedGroup()
  {
    return renderGroups != null ? renderGroups.get(0) : null;
  }

  public String getGroupSelectionMode()
  {
    return groupSelectionMode;
  }

  public void setGroupSelectionMode(String groupSelectionMode)
  {
    this.groupSelectionMode = groupSelectionMode;
  }

  public boolean isRenderGroupBarMode()
  {
    return BAR_SELECTION_MODE.equals(groupSelectionMode);
  }

  public boolean isRenderGroupButtonMode()
  {
    return BUTTON_SELECTION_MODE.equals(groupSelectionMode);
  }

  public boolean isRenderGroupSelection()
  {
    return (groupSelectionMode != null && !NONE_SELECTION_MODE.equals(groupSelectionMode));
  }  

  public String expandAllGroups()
  {
    if (groupedRows != null && !groupedRows.isEmpty() && renderGroups != null)
    {
      renderGroups.addAll(this.groupedRows.keySet());
    }

    return null;
  }

  public String collapseAllGroups()
  {
    if (renderGroups != null)
      renderGroups.clear();

    return null;
  }
  
  public class Group implements Comparable, Serializable
  {
    private String name;
    private String description;
    private int first = 0;

    public Group(String name, String description)
    {
      this.name = name;
      this.description = description;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public int getFirst()
    {
      return first;
    }

    public void setFirst(int first)
    {
      this.first = first;
    }

    @Override
    public int compareTo(Object o)
    {
      if (this.isAllRowsGroup() && ((Group)o).isAllRowsGroup())
        return 0;
      else if (this.isAllRowsGroup() && !((Group)o).isAllRowsGroup())
        return -1;
      else if (!this.isAllRowsGroup() && ((Group)o).isAllRowsGroup())
        return 1;
      else 
        return description.compareTo(((Group)o).getDescription());
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final Group other = (Group) obj;
      if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
    
    private boolean isAllRowsGroup()
    {
      return ALL_ROWS_NAME.equals(name);
    }
    
    
  }  
  
  public abstract class GroupExtractor
  {
    public abstract Group getGroup(Object view);    
  }

  public class DefaultGroupExtractor extends GroupExtractor
  {
    protected String propertyName;

    public DefaultGroupExtractor(String propertyName)
    {
      this.propertyName = propertyName;
    }

    @Override
    public Group getGroup(Object view)
    {
      String name = getName(view);
      if (name != null)
      {
        String description = getDescription(name);
        return new Group(name, description);
      }
      else
        return new Group(NULL_GROUP_NAME, "");
    }

    protected String getName(Object view)
    {
      String[] props = propertyName.split("\\.");
      Object obj = view;
      for (int i = 0; i < props.length; i++)
      {
        if (obj == null)
          break;
        else
          obj = PojoUtils.getDeepStaticProperty(obj, props[i]);
      }
      if (obj != null)
      {
        if (obj instanceof List)
          return String.valueOf(((List)obj).get(0));
        else
          return String.valueOf(obj);
      }
      return null;
    }

    protected String getDescription(String keyName)
    {
      return keyName;
    }

    protected Object getListValue(Object obj, String propName)
    {
      String name = propName.substring(0, propName.indexOf("["));
      String index = propName.substring(propName.indexOf("[") + 1, propName.indexOf("]"));
      obj = PojoUtils.getStaticProperty(obj, name);
      if (obj != null && obj instanceof List)
      {
        try
        {
          int i = Integer.parseInt(index);
          return ((List)obj).get(i);
        }
        catch (NumberFormatException ex)
        {
          try
          {
            return PojoUtils.getDynamicProperty((List) obj, index);
          }
          catch (Exception ex1)
          {
            return null;
          }
        }

      }
      return null;
    }
  }   
  
  public class PropertyComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      int result = 0;

      for (String ob : orderBy)
      {
        boolean allDescendent = false;
        boolean nullDescendent = false;      
        if (ob.indexOf(":desc") > 0)
        {
          allDescendent = true;
          ob = ob.replaceAll(":desc", "");
        }
        if (ob.indexOf(":nulldesc") > 0)
        {
          nullDescendent = true;
          ob = ob.replaceAll(":nulldesc", "");
        }
        
        Object p1 = getPropertyValue(o1, ob);
        Object p2 = getPropertyValue(o2, ob);

        if (p1 != null && p1 instanceof List)
        {
          List lp1 = (List)p1;
          p1 = (lp1.isEmpty() ? null: lp1.get(0));
        }
        if (p2 != null && p2 instanceof List)
        {
          List lp2 = (List)p2;
          p2 = (lp2.isEmpty() ? null : lp2.get(0));
        }
        
        if (p1 == null && p2 != null)
          result = nullDescendent ? 1 : -1;
        else if (p1 == null && p2 == null)
          result = 0;
        else if (p1 != null && p2 == null)
          result = nullDescendent ? -1 : 1;
        else
          result = ((Comparable)p1).compareTo(p2);
        if (result != 0) 
          return allDescendent ? (result * -1) : result;          
      }
      return result;
    }
    
    protected Object getPropertyValue(Object obj, String propertyName)
    {
      return PojoUtils.getDeepStaticProperty(obj, propertyName);
    }
  }
  

}
