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
package org.santfeliu.webapp.modules.classif;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.classif.ClassFilter;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.matrix.classif.Class;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.modules.classif.ClassifModuleBean.getPort;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class ClassFinderBean extends FinderBean
{
  private String smartFilter;
  private ClassFilter filter = new ClassFilter();
  private List<Class> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;
  private DefaultTreeNode rootsNode = new DefaultTreeNode("roots");

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  ClassTypeBean classTypeBean;

  @Inject
  ClassObjectBean classObjectBean;

  @Override
  public ClassObjectBean getObjectBean()
  {
    return classObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public ClassFilter getFilter()
  {
    return filter;
  }

  public void setFilter(ClassFilter filter)
  {
    this.filter = filter;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getClassId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  @Override
  public List<Class> getRows()
  {
    return rows;
  }

  public void setRows(List<Class> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = classTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
    rootsNode.getChildren().clear();
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = classTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
    rootsNode.getChildren().clear();
  }

  public void outdate()
  {
    outdated = true;
  }

  public void updateList()
  {
    if (outdated)
    {
      doFind(false);
      rootsNode.getChildren().clear();
    }
  }

  public void updateTree()
  {
    if (outdated)
    {
      doFind(false);
      rootsNode.getChildren().clear();
    }

    String classId = classObjectBean.getObjectId();
    if (!StringUtils.isBlank(classId))
    {
      expandNode(classId);
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    filter = new ClassFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
    formSelector = null;
    rootsNode.getChildren().clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(),
      filter, firstRow, getObjectPosition(), formSelector, getPageSize() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    setFinding((Boolean)stateArray[0]);
    setFilterTabSelector((Integer)stateArray[1]);
    filter = (ClassFilter)stateArray[2];
    smartFilter = classTypeBean.filterToQuery(filter);
    formSelector = (String)stateArray[5];
    setPageSize((Integer)stateArray[6]);

    doFind(false);

    firstRow = (Integer)stateArray[3];
    setObjectPosition((Integer)stateArray[4]);
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        String dateTime = getFilterDateTime();
        filter.setStartDateTime(dateTime);
        filter.setEndDateTime(dateTime);

        rows = new BigList(2 * getPageSize() + 1, getPageSize())
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return ClassifModuleBean.getPort(false).countClasses(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              return ClassifModuleBean.getPort(false).findClasses(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getClassId());
            classObjectBean.setSearchTabSelector(
              classObjectBean.getEditModeSelector());
          }
          else
          {
            classObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public TreeNode getRootsNode()
  {
    return rootsNode;
  }

  public void onNodeExpand(NodeExpandEvent event)
  {
    ClassTreeNode treeNode = (ClassTreeNode)event.getTreeNode();
    treeNode.load();
  }

  public void onNodeCollapse(NodeCollapseEvent event)
  {
//    ClassTreeNode treeNode = (ClassTreeNode)event.getTreeNode();
  }

  public void expandNode(String classId)
  {
    try
    {
      List<Class> classPath = getClassPath(classId);

      ClassTreeNode node = null;

      if (rootsNode.getChildCount() > 0)
      {
        node = (ClassTreeNode)rootsNode.getChildren().get(0);
        node.setExpanded(true);
      }

      if (node == null ||
          !classPath.get(0).getClassId().equals(node.getData().getClassId()))
      {
        rootsNode.getChildren().clear();
        node = new ClassTreeNode(new ClassData(classPath.get(0)));
        node.load();
        node.setExpanded(true);
        rootsNode.getChildren().add(node);
      }

      for (int level = 1; level < classPath.size() - 1 && node != null; level++)
      {
        node = node.expandChild(classPath.get(level).getClassId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void viewNodeInTree(String classId)
  {
    navigatorBean.view(classId);
    expandNode(classId);
  }

  public String getFilterDateTime()
  {
    if (StringUtils.isBlank(filter.getStartDateTime()))
    {
      return TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    }
    return filter.getStartDateTime();
  }

  private List<Class> getClassPath(String classId) throws Exception
  {
    List<Class> classPath = new ArrayList<>();
    String dateTime = getFilterDateTime();
    while (!StringUtils.isBlank(classId))
    {
      Class classObject = getPort(true).loadClass(classId, dateTime);
      classPath.add(classObject);
      classId = classObject.getSuperClassId();
    }
    Collections.reverse(classPath);
    return classPath;
  }

  public static class ClassTreeNode extends DefaultTreeNode<ClassData>
  {
    protected boolean loaded;

    public ClassTreeNode(ClassData classData)
    {
      super(classData);
    }

    @Override
    public boolean isLeaf()
    {
      load();

      return super.isLeaf();
    }

    public ClassTreeNode expandChild(String classId)
    {
      load();
      setExpanded(true);
      Iterator<TreeNode<ClassData>> iter = getChildren().iterator();
      while (iter.hasNext())
      {
        TreeNode<ClassData> next = iter.next();
        if (next.getData().getClassId().equals(classId))
        {
          next.setExpanded(true);
          return (ClassTreeNode)next;
        }
      }
      return null;
    }

    private void load()
    {
      if (!loaded)
      {
        try
        {
          loaded = true;

          ClassData data = getData();
          String classId = data.getClassId();

          String dateTime =
            WebUtils.getValue("#{classFinderBean.filterDateTime}");

          ClassFilter filter = new ClassFilter();
          filter.setSuperClassId(classId);
          filter.setStartDateTime(dateTime);
          filter.setEndDateTime(dateTime);
          List<Class> classList = getPort(true).findClasses(filter);
          for (Class cls : classList)
          {
            getChildren().add(new ClassTreeNode(new ClassData(cls)));
          }
        }
        catch (Exception ex)
        {
          FacesUtils.addMessage(ex);
        }
      }
    }
  }

  public static class ClassData implements Serializable
  {
    private String classId;
    private String title;

    public ClassData(String classId, String title)
    {
      this.classId = classId;
      this.title = title;
    }

    public ClassData(Class cls)
    {
      this.classId = cls.getClassId();
      this.title = cls.getTitle();
    }

    public String getClassId()
    {
      return classId;
    }

    public void setClassId(String classId)
    {
      this.classId = classId;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }
  }

}
