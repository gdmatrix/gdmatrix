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
package org.santfeliu.webapp.modules.dic;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
//import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.TreeNodeChildren;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class TypeFinderBean extends FinderBean
{
  private TypeFilter filter = new TypeFilter();
  private List<org.matrix.dic.Type> rows;

  private String smartFilter;
  private int firstRow;
  private boolean outdated;

  private String rootTypeId;
  private boolean searchByTypePath;
  private final TypeTreeNode rootNode = new TypeTreeNode() ;
  private String treeTypeId;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @Inject
  TypeObjectBean typeObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return typeObjectBean;
  }

  public String getRootTypeId()
  {
    return rootTypeId;
  }

  public void setRootTypeId(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }

  public TypeTreeNode getRootNode()
  {
    return rootNode;
  }

  public TypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(TypeFilter filter)
  {
    this.filter = filter;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public List<org.matrix.dic.Type> getRows()
  {
    return rows;
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
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getTypeId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public void onRootTypeChange(SelectEvent event)
  {
    String typeId = (String) event.getObject();
    if (!StringUtils.isBlank(typeId))
    {
      String typePath = DictionaryConstants.TYPE_PATH_SEPARATOR + typeId +
          DictionaryConstants.TYPE_PATH_SEPARATOR + "%";
      filter.setTypePath(typePath);
    }
    else
      filter.setTypePath(null);
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);

    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    if (typeTypeBean.getRootTypeId().equals(baseTypeId))
      baseTypeId = null;
    filter = typeTypeBean.queryToFilter(smartFilter, baseTypeId);
    rootTypeId = null;

    doFind(true);
    firstRow = 0;
    rootNode.getChildren().clear();
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);

    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    if (typeTypeBean.getRootTypeId().equals(baseTypeId))
      baseTypeId = rootTypeId;
    searchByTypePath = !StringUtils.isBlank(filter.getTypePath());
    String typePath = searchByTypePath ? filter.getTypePath() : baseTypeId;

    if (!StringUtils.isBlank(typePath) &&
      !typeTypeBean.getRootTypeId().equals(typePath))
    {
      if (!typePath.contains("%"))
      {
        boolean isRoot = DictionaryConstants.rootTypeIds.contains(typePath);
        typePath = (!isRoot ? "%" : "") +
          DictionaryConstants.TYPE_PATH_SEPARATOR + typePath +
          DictionaryConstants.TYPE_PATH_SEPARATOR + "%";
      }
      filter.setTypePath(typePath);
    }

    smartFilter = typeTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
    if (baseTypeId != null && typeObjectBean.getSearchTabSelector() == 1)
      changeTreeRootType(baseTypeId);
    else if (baseTypeId != null && typeObjectBean.getSearchTabSelector() != 1)
      treeTypeId = baseTypeId;
    else if (baseTypeId == null)
      rootNode.getChildren().clear();
  }

  public void outdate()
  {
    this.outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  public void updateTree()
  {
    if (outdated)
      doFind(false);

    if (!StringUtils.isBlank(treeTypeId))
      expandNode(treeTypeId);
    else
    {
      String typeId = typeObjectBean.getObjectId();
      if (!StringUtils.isBlank(typeId))
      {
        expandNode(typeId);
      }
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    filter = new TypeFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
    rootTypeId = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      getObjectPosition(), rootTypeId };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (TypeFilter)stateArray[2];
      smartFilter = typeTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
      rootTypeId = ((String)stateArray[5]);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.emptyList();
      }
      else
      {
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return DicModuleBean.getPort(false).countTypes(filter);
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
              return DicModuleBean.getPort(false).findTypes(filter);
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
            navigatorBean.view(rows.get(0).getTypeId());
            typeObjectBean.setSearchTabSelector(
              typeObjectBean.getEditModeSelector());
          }
          else if (typeObjectBean.getSearchTabSelector() ==
              typeObjectBean.getEditModeSelector())
          {
            typeObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void viewNodeInTree(String typeId)
  {
    navigatorBean.view(typeId);
    expandNode(typeId);
  }

  private void expandNode(String typeId)
  {
    treeTypeId = null;
    Type type = TypeCache.getInstance().getType(typeId);
    TypeTreeNode node = new TypeTreeNode(type.getRootType());
    changeTreeRootType(node);

    List<String> typePathList = type.getTypePathList();
    for (int level = 1; level < typePathList.size(); level++)
    {
      String childTypeId = typePathList.get(level);
      Iterator<TreeNode<Type>> it = node.getChildren().iterator();
      boolean found = false;
      while (it.hasNext() && !found)
      {
        TreeNode<Type> childNode = it.next();
        if (childTypeId.equals(childNode.getData().getTypeId()))
        {
          found = true;
          node = (TypeTreeNode) childNode;
          node.expand();
        }
      }
    }
  }

  private void changeTreeRootType(String typeId)
  {
    Type type = TypeCache.getInstance().getType(typeId);
    TypeTreeNode node = new TypeTreeNode(type.getRootType());
    changeTreeRootType(node);
  }

  private void changeTreeRootType(TypeTreeNode node)
  {
    rootNode.getChildren().clear();
    rootNode.getChildren().add(node);
    node.expand();
  }

  public static class TypeTreeNode extends DefaultTreeNode<Type>
  {
    private boolean lazyLoaded;

    /**
     * It's a root node without type.
     */
    public TypeTreeNode()
    {
      super();
      lazyLoaded = true;
      setExpanded(true);
    }

    public TypeTreeNode(Type data)
    {
      super(data);
    }

    public void expand()
    {
      this.lazyLoad();
      this.setExpanded(true);
    }

    @Override
    public List<TreeNode<Type>> getChildren()
    {
      if (isLeaf())
        return Collections.emptyList();

      lazyLoad();

      return super.getChildren();
    }

    @Override
    public int getChildCount()
    {
      if (lazyLoaded)
        return getChildren().size();
      else
        return getData().getDerivedTypeCount();
    }

    @Override
    public boolean isLeaf()
    {
      if (getData() == null) //Is foo root node
        return false;

      return getData().isLeaf();
    }

    private void lazyLoad()
    {
      if (!lazyLoaded)
      {
        lazyLoaded = true;

        List<TypeTreeNode> childNodes =
          getData().getDerivedTypes().stream()
            .map(f -> new TypeTreeNode(f))
            .collect(Collectors.toList());

        super.getChildren().addAll(childNodes);
      }
    }

    @Override
    protected List<TreeNode<Type>> initChildren() {
        return new TypeTreeNodeChildren(this);
    }

    public static class TypeTreeNodeChildren extends TreeNodeChildren<Type>
    {

      public TypeTreeNodeChildren(TypeTreeNode parent)
      {
        super(parent);
      }

      @Override
      protected void updateRowKeys(TreeNode<?> node)
      {
        if (((TypeTreeNode) node).lazyLoaded)
        {
          super.updateRowKeys(node);
        }
      }

      @Override
      protected void updateRowKeys(int index, TreeNode<?> node)
      {
        if (((TypeTreeNode) node).lazyLoaded)
        {
          super.updateRowKeys(index, node);
        }
      }

      @Override
      protected void updateRowKeys(TreeNode<?> node, TreeNode<?> childNode, int i)
      {
        if (((TypeTreeNode) node).lazyLoaded)
        {
          super.updateRowKeys(node, childNode, i);
        }
      }

      @Override
      public boolean add(TreeNode<Type> node)
      {
        return super.add(node);
      }

    }

  }



}
