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
package org.santfeliu.cases.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.CaseCaseCache;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.component.HtmlCalendar;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class CaseTreeBean extends PageBean
{
  //Node properties
  @CMSProperty(mandatory=true)
  public static String ROOT_CASE_ID_PROPERTY = "rootCaseId";
  @CMSProperty(mandatory=true)
  public static String RELATION_NAME_PROPERTY = "relationName";
  @CMSProperty(mandatory=true)
  public static String RELATION_DIRECTION_PROPERTY = "relationDirection";
  @CMSProperty
  public static String RELATION_ICON_PROPERTY = "relationIcon";
  @CMSProperty
  public static String RENDERED_PROPERTIES_CASETYPE_PROPERTY =
    "renderedProperties.caseType";
  @CMSProperty
  public static String RENDERED_PROPERTIES_LAYOUT_PROPERTY =
    "renderedProperties.layout";
  @CMSProperty
  public static String ROOT_ICON_PROPERTY = "rootIcon";
  @CMSProperty
  public static String SEARCH_HELP_LEAF_TYPE_PROPERTY = "searchHelp.leafType";
  @CMSProperty
  public static String SEARCH_HELP_CASE_TYPE_PROPERTY = "searchHelp.caseType";
  @CMSProperty
  public static String SEARCH_HELP_MAX_CASES = "searchHelp.maxCases";
  @CMSProperty
  public static String ORDER_BY_PROPERTY = "caseTree.orderBy";

  //Private constants  
  private static final String LIST_LAYOUT_TYPE = "list";
  private static final String POPUP_LAYOUT_TYPE = "popup";  

  //Type properties
  private static String DIRECT_SHORT_DESCRIPTION_PROPERTY =
    "_directShortDescription";
  private static String REVERSE_SHORT_DESCRIPTION_PROPERTY =
    "_reverseShortDescription";
  private static String OBJECT_LABEL_PROPERTY = "_objectLabel";

  private Case rootCase;
  private String rootIcon;
  private Map<String, String> relationDirectionMap;
  private Map<String, String> relationIconMap;
  private Map<String, CaseType> caseTypeMap;

  //Search helpers
  private List<String> treeLeafTypeList;
  private List<String> treeCaseTypeList;

  private transient TreeModelBase treeModel;
  private transient HtmlTree tree;
  private static Map<String, List<CaseProperty>> casePropertiesMap;
  
  private String inputSearchText;
  private String inputDate;

  private Set<String> loadedNodes;
  private Set<String> expandedNodes;
  private Set<String> selectedNodes;
  private Set<String> foundCases;
  private String lastMid = null;
  private Integer scroll;

  private transient HtmlCalendar htmlCalendar;
  private transient String treeDate;

  public CaseTreeBean()
  {    
  }

  public List<String> getTreeCaseTypeList()
  {
    if (treeCaseTypeList == null)
    {
      treeCaseTypeList = new ArrayList<String>();
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      treeCaseTypeList.addAll(mic.getMultiValuedProperty(
        SEARCH_HELP_CASE_TYPE_PROPERTY));
    }
    return treeCaseTypeList;
  }

  public void setTreeCaseTypeList(List<String> treeCaseTypeList)
  {
    this.treeCaseTypeList = treeCaseTypeList;
  }

  public List<String> getTreeLeafTypeList()
  {
    if (treeLeafTypeList == null)
    {
      treeLeafTypeList = new ArrayList<String>();
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      treeLeafTypeList.addAll(mic.getMultiValuedProperty(
        SEARCH_HELP_LEAF_TYPE_PROPERTY));
    }
    return treeLeafTypeList;
  }

  public void setTreeLeafTypeList(List<String> treeLeafTypeList)
  {
    this.treeLeafTypeList = treeLeafTypeList;
  }

  public TreeModelBase getTreeModel() throws Exception
  {
    if (treeModel == null)
    {
      loadedNodes = null; //children reload
      Case cas = getRootCase();
      CaseTreeNode rootNode = new CaseTreeNode();
      rootNode.setCaseId(cas.getCaseId());
      rootNode.setTitle(cas.getTitle());
      rootNode.setCaseTypeId(cas.getCaseTypeId());
      treeModel = new TreeModelBase(rootNode);
      for (String expandedNode : getExpandedNodes())
      {
        String[] sArray = new String[1];
        sArray[0] = expandedNode;          
        getTree().expandPath(sArray);
      }
    }
    return treeModel;
  }

  public void setTreeModel(TreeModelBase treeModel)
  {
    this.treeModel = treeModel;
  }

  public String getInputSearchText()
  {
    return inputSearchText;
  }

  public void setInputSearchText(String inputSearchText)
  {
    this.inputSearchText = inputSearchText;
  }

  public HtmlCalendar getHtmlCalendar()
  {
    if (htmlCalendar == null)
    {
      htmlCalendar = new HtmlCalendar();
    }
    return htmlCalendar;
  }

  public void setHtmlCalendar(HtmlCalendar htmlCalendar)
  {
    this.htmlCalendar = htmlCalendar;
  }

  public Integer getScroll()
  {
    return scroll;
  }

  public void setScroll(Integer scroll)
  {
    this.scroll = scroll;
  }

  public String getInputDate()
  {
    return inputDate;
  }

  public void setInputDate(String inputDate)
  {
    this.inputDate = inputDate;
  }

  private String getTreeDate() throws Exception
  {
    if (treeDate == null)
    {
      String submittedDate = (String)getHtmlCalendar().getSubmittedValue();
      if (submittedDate == null)
      {
        treeDate = inputDate;
      }
      else
      {
        if (!submittedDate.trim().isEmpty())
        {
          SimpleDateFormat humanFormat = new SimpleDateFormat("dd/MM/yyyy");
          SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMdd");
          treeDate = sysFormat.format(humanFormat.parse(submittedDate.trim()));          
        }
      }
      if (treeDate == null) treeDate = "";
    }
    return treeDate;
  }

  public HtmlTree getTree() throws Exception
  {
    if (tree == null)
    {
      tree = new HtmlTree();
      tree.setModel(getTreeModel());
    }
    return tree;
  }

  public void setTree(HtmlTree tree)
  {
    this.tree = tree;
  }

  public Set<String> getLoadedNodes()
  {
    if (loadedNodes == null)
    {
      loadedNodes = new HashSet<String>();
    }
    return loadedNodes;
  }

  public void setLoadedNodes(Set<String> loadedNodes)
  {
    this.loadedNodes = loadedNodes;
  }

  public Set<String> getExpandedNodes()
  {
    if (expandedNodes == null)
    {
      expandedNodes = new HashSet<String>();
    }
    return expandedNodes;
  }

  public void setExpandedNodes(Set<String> expandedNodes)
  {
    this.expandedNodes = expandedNodes;
  }

  public Set<String> getFoundCases()
  {
    if (foundCases == null)
    {
      foundCases = new HashSet<String>();
    }
    return foundCases;
  }

  public void setFoundCases(Set<String> foundCases)
  {
    this.foundCases = foundCases;
  }

  public Set<String> getSelectedNodes()
  {
    if (selectedNodes == null)
    {
      selectedNodes = new HashSet<String>();
    }
    return selectedNodes;
  }

  public void setSelectedNodes(Set<String> selectedNodes)
  {
    this.selectedNodes = selectedNodes;
  }

  public boolean isRenderUpdateButton()
  {
    return true;
  }

  @CMSAction
  public String show()
  {
    try
    {
      if (!(UserSessionBean.getCurrentInstance().getSelectedMid().
        equalsIgnoreCase(lastMid)))
      {
        inputSearchText = null;
        if (inputDate == null)
        {
          SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMdd");
          inputDate = sysFormat.format(new Date());
        }
        lastMid = UserSessionBean.getCurrentInstance().getSelectedMid();
        reset();
      }
      getSelectedNodes().clear();
      if (!isNew())
      {
        expandCase(getObjectId(), true);
      }      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "case_tree";
  }

  public String showCase()
  {
    return getControllerBean().showObject("Case",
      (String)getValue("#{node.caseId}"));
  }

  public String update()
  {
    try
    {
      CaseCaseCache.getInstance().clear();
      reset();            
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String search()
  {
    try
    {
      //Search for cases
      foundCases = null;
      if (inputSearchText != null && !inputSearchText.trim().isEmpty())
      {
        List<String> caseIdList = findCaseIdList(inputSearchText.trim());        
        getFoundCases().addAll(caseIdList);
        for (String caseId : caseIdList)
        {
          expandCase(caseId, false);
        }
      }

      //Open current case
      selectedNodes = null;
      if (!isNew())
      {
        expandCase(getObjectId(), true);
      }
      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public static Map<String, List<CaseProperty>> getCasePropertiesMap()
  {
    if (casePropertiesMap == null)
    {
      casePropertiesMap = new HashMap<String, List<CaseProperty>>();
    }
    return casePropertiesMap;
  }
  
  private void reset()
  {
    tree = null;
    treeModel = null;
    casePropertiesMap = null;
    rootCase = null;
    rootIcon = null;
    relationDirectionMap = null;
    relationIconMap = null;
    caseTypeMap = null;
    treeCaseTypeList = null;
    treeLeafTypeList = null;
    loadedNodes = null;
    expandedNodes = null;
    foundCases = null;
    selectedNodes = null;
  }

  private Case getRootCase() throws Exception
  {
    if (rootCase == null)
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      String rootCaseId = mic.getProperty(ROOT_CASE_ID_PROPERTY);
      rootCase = CaseConfigBean.getPort().loadCase(rootCaseId);
    }
    return rootCase;
  }

  private String getRootCaseId() throws Exception
  {
    return getRootCase().getCaseId();
  }

  private String getRootCaseTypeId() throws Exception
  {
    return getRootCase().getCaseTypeId();
  }

  private int getMaxCasesToSearch()
  {
    try
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      String maxCases = mic.getProperty(SEARCH_HELP_MAX_CASES);
      return Integer.valueOf(maxCases);
    }
    catch (Exception ex)
    {
      return 100;
    }
  }

  private List<String> getOrderByList()
  {
    try
    {      
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      return mic.getMultiValuedProperty(ORDER_BY_PROPERTY);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }
  
  private Map<String, String> getRelationDirectionMap() throws Exception
  {
    if (relationDirectionMap == null)
    {
      relationDirectionMap = new HashMap<String, String>();
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      List<String> relationNameList =
        mic.getMultiValuedProperty(RELATION_NAME_PROPERTY);
      List<String> relationDirectionList =
        mic.getMultiValuedProperty(RELATION_DIRECTION_PROPERTY);
      if (relationNameList.size() == relationDirectionList.size())
      {
        int i = 0;
        for (String relationName : relationNameList)
        {
          relationDirectionMap.put(relationName, relationDirectionList.get(i++));
        }
      }
      else
      {
        throw new Exception("INVALID_NODE_CONFIG");
      }
    }
    return relationDirectionMap;
  }

  private Map<String, String> getRelationIconMap() throws Exception
  {
    if (relationIconMap == null)
    {
      relationIconMap = new HashMap<String, String>();
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      List<String> relationIconList =
        mic.getMultiValuedProperty(RELATION_ICON_PROPERTY);
      if (!relationIconList.isEmpty())
      {
        List<String> relationNameList =
          mic.getMultiValuedProperty(RELATION_NAME_PROPERTY);
        if (relationNameList.size() == relationIconList.size())
        {
          int i = 0;
          for (String relationName : relationNameList)
          {
            relationIconMap.put(relationName, relationIconList.get(i++));
          }
        }
        else
        {
          throw new Exception("INVALID_NODE_CONFIG");
        }
      }
    }
    return relationIconMap;
  }

  private Map<String, CaseType> getCaseTypeMap() throws Exception
  {
    if (caseTypeMap == null)
    {
      caseTypeMap = new HashMap<String, CaseType>();
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getSelectedMenuItem();
      List<String> caseTypeIdList =
        mic.getMultiValuedProperty(RENDERED_PROPERTIES_CASETYPE_PROPERTY);
      if (!caseTypeIdList.isEmpty())
      {
        List<String> propertyLayoutList =
          mic.getMultiValuedProperty(RENDERED_PROPERTIES_LAYOUT_PROPERTY);
        if (caseTypeIdList.size() == propertyLayoutList.size())
        {
          int i = 0;
          for (String caseTypeId : caseTypeIdList)
          {
            String layoutType = LIST_LAYOUT_TYPE;
            String caseTypeLayout = propertyLayoutList.get(i);
            if (caseTypeLayout.contains(":"))
            {
              layoutType = caseTypeLayout.split(":")[0];
              caseTypeLayout = caseTypeLayout.split(":")[1];
            }
            CaseType caseType = new CaseType(caseTypeId, layoutType);            
            List<String> caseTypeLayoutPropertyList = Arrays.asList(caseTypeLayout.split(";"));
            for (String caseTypeLayoutProperty : caseTypeLayoutPropertyList)
            {
              String[] layoutPropertyArray = caseTypeLayoutProperty.split(",");
              if (layoutPropertyArray.length != 3)
              {
                throw new Exception("INVALID_NODE_CONFIG");                
              }
              else
              {
                String propertyFilter = layoutPropertyArray[2];
                if (!propertyFilter.contains("="))
                {
                  throw new Exception("INVALID_NODE_CONFIG");
                }
                else
                {
                  String format = layoutPropertyArray[0];
                  String decoration = layoutPropertyArray[1];                  
                  String filterName = propertyFilter.split("=")[0];
                  String filterValue = propertyFilter.split("=")[1];
                  CaseTypeProperty caseTypeProperty = new CaseTypeProperty(
                    caseTypeId, format, decoration, filterName, filterValue);
                  caseType.getCaseTypePropertyList().add(caseTypeProperty);                  
                }
              }
            }            
            caseTypeMap.put(caseTypeId, caseType);
            i++;            
          }
        }
        else
        {
          throw new Exception("INVALID_NODE_CONFIG");
        }
      }      
    }
    return caseTypeMap;
  }

  private void expandCase(String caseId, boolean markAsSelected)
    throws Exception
  {
    List<String> auxExpandedNodes = new ArrayList<String>();
    List<List<String>> caseIdPathList = getPathsToNode(caseId);
    for (List<String> caseIdPath : caseIdPathList)
    {
      List<String> nodeIdsToExpand = 
        getNodeIdsToExpand(caseIdPath, auxExpandedNodes);
      auxExpandedNodes.addAll(nodeIdsToExpand);
      if (markAsSelected)
      {
        getSelectedNodes().add(nodeIdsToExpand.get(nodeIdsToExpand.size() - 1));
      }
      String[] sArray = nodeIdsToExpand.toArray(new String[0]);
      getTree().expandPath(sArray);
    }
  }

  private List<String> findCaseIdList(String searchString) throws Exception
  {    
    List<String> result = new ArrayList<String>();
    Set<String> caseIdSet = new HashSet();
    CaseFilter filter = new CaseFilter();
    filter.setTitle(searchString);
    filter.setMaxResults(getMaxCasesToSearch());
    addWildcards(filter);
    if (getTreeCaseTypeList().isEmpty())
    {
      List<Case> caseList = CaseConfigBean.getPort().findCases(filter);
      for (Case cas : caseList)
      {
        caseIdSet.add(cas.getCaseId());
      }      
    }
    else
    {
      for (String caseTypeId : getTreeCaseTypeList())
      {
        filter.setCaseTypeId(caseTypeId);
        List<Case> caseList = CaseConfigBean.getPort().findCases(filter);
        for (Case cas : caseList)
        {
          caseIdSet.add(cas.getCaseId());
        }
      }
    }
    result.addAll(caseIdSet);
    return result;
  }  
  
  private void addWildcards(CaseFilter filter)
  {
    if (!StringUtils.isBlank(filter.getTitle()))
      filter.setTitle(FilterUtils.addWildcards(filter.getTitle()));
    else
      filter.setTitle(null);
  }

  private CaseTreeNode getRootNode() throws Exception
  {
    return (CaseTreeNode)getTreeModel().getNodeById("0");
  }

  private List<String> getNodeIdsToExpand(List<String> caseIdPath,
    List<String> expandedNodes) throws Exception
  {   
    List<String> result = new ArrayList<String>();
    List children = null;
    int i = 0;
    for (String caseId : caseIdPath)
    {
      if (i++ == 0) //root
      {
        CaseTreeNode rootNode = getRootNode();
        children = rootNode.getChildren();
        result.add(rootNode.getIdentifier());
      }
      else
      {
        boolean found = false;
        CaseTreeNode auxTreeNode = null;
        for (int iChild = 0; iChild < children.size() && !found; iChild++)
        {
          Object child = children.get(iChild);
          CaseTreeNode treeNode = (CaseTreeNode)child;
          if (caseId.equals(treeNode.getCaseId()))
          {
            auxTreeNode = treeNode;
            if (!expandedNodes.contains(treeNode.getIdentifier()))
            {
              found = true;
            }
          }
        }
        children = auxTreeNode.getChildren();
        result.add(auxTreeNode.getIdentifier());
      }
    }
    return result;
  }

  private List<List<String>> getPathsToNode(String caseId) throws Exception
  {
    List<String> path = new ArrayList<String>();
    path.add(caseId);
    return getPathsToPath(path);
  }

  private List<List<String>> getPathsToPath(List<String> path) throws Exception
  {
    List<List<String>> result = new ArrayList<List<String>>();

    String caseId = path.get(0);
    if (caseId.equals(getRootCaseId()))
    {
      result.add(path);
      return result;
    }

    //Direct relations
    List<CaseCaseCache.CacheItem> directCaseCases =
      CaseCaseCache.getInstance().getDirectCaseCases(caseId, getTreeDate());
    for (CaseCaseCache.CacheItem directCaseCase : directCaseCases)
    {
      try
      {
        String caseCaseTypeId = directCaseCase.getCaseCaseTypeId();
        if (getRelationDirectionMap().containsKey(caseCaseTypeId))
        {
          String direction = getRelationDirectionMap().get(caseCaseTypeId);
          if ("reverse".equals(direction))
          {
            List<String> pathAux = new ArrayList<String>();
            String itemCaseId = directCaseCase.getRelCaseId();
            if (path.contains(itemCaseId))
            {
              String s = itemCaseId + "," + TextUtils.collectionToString(path);
              throw new Exception(s);
            }
            pathAux.add(itemCaseId);
            pathAux.addAll(path);
            result.addAll(getPathsToPath(pathAux));
          }
        }
      }
      catch (Exception ex)
      {
        error("CASE_CYCLE_DETECTED", ex.getMessage());
      }
    }

    //Reverse relations
    List<CaseCaseCache.CacheItem> reverseCaseCases =
      CaseCaseCache.getInstance().getReverseCaseCases(caseId, getTreeDate());
    for (CaseCaseCache.CacheItem reverseCaseCase : reverseCaseCases)
    {
      try
      {
        String caseCaseTypeId = reverseCaseCase.getCaseCaseTypeId();
        if (getRelationDirectionMap().containsKey(caseCaseTypeId))
        {
          String direction = getRelationDirectionMap().get(caseCaseTypeId);
          if ("direct".equals(direction))
          {
            List<String> pathAux = new ArrayList<String>();
            String itemCaseId = reverseCaseCase.getMainCaseId();
            if (path.contains(itemCaseId))
            {
              String s = itemCaseId + "," + TextUtils.collectionToString(path);
              throw new Exception(s);              
            }
            pathAux.add(itemCaseId);
            pathAux.addAll(path);
            result.addAll(getPathsToPath(pathAux));
          }
        }
      }
      catch (Exception ex)
      {
        error("CASE_CYCLE_DETECTED", ex.getMessage());
      }
    }
    return result;
  }
  
  private List<CaseProperty> getCaseProperties(String caseId) throws Exception
  {
    if (!getCasePropertiesMap().containsKey(caseId))
    {      
      Map<String, CaseProperty> auxPropertyMap = new HashMap();
      Case cas = CaseConfigBean.getPort().loadCase(caseId);
      for (Property property : cas.getProperty())
      {
        CaseProperty caseProperty = new CaseProperty();
        caseProperty.setCaseId(caseId);
        caseProperty.setName(property.getName());
        caseProperty.setValue(property.getValue().get(0));
        caseProperty.setRendered(false);
        auxPropertyMap.put(property.getName(), caseProperty);
      }
      List<String> renderedProperties = new ArrayList();
      String caseTypeId = cas.getCaseTypeId();
      if (getCaseTypeMap().containsKey(caseTypeId))
      {
        for (int i = 0; i < getCaseTypeMap().get(caseTypeId).getCaseTypePropertyList().size(); i++)
        {
          CaseTypeProperty caseTypeProperty = 
            getCaseTypeMap().get(caseTypeId).getCaseTypePropertyList().get(i);
          renderedProperties.add(caseTypeProperty.getFilterName());
          Property property = DictionaryUtils.getPropertyByName(
            cas.getProperty(), caseTypeProperty.getFilterName());
          if (property != null && !property.getValue().isEmpty())
          {
            String searchValue = caseTypeProperty.getFilterValue();
            String value = property.getValue().get(0);              
            if (value.matches(searchValue))
            {
              CaseProperty caseProperty = auxPropertyMap.get(property.getName());
              if ("text".equals(caseTypeProperty.getFormat()))
              {
                caseProperty.setLabel(caseTypeProperty.getDecoration());
                if ("".equals(caseProperty.getLabel()))
                {
                  caseProperty.setLabel(null);
                }
              }
              else if ("icon".equals(caseTypeProperty.getFormat()))
              {
                caseProperty.setIcon(caseTypeProperty.getDecoration());
              }
              caseProperty.setRendered(true);                
            }
          }            
        }
      }
      List<CaseProperty> auxCasePropertyList = new ArrayList();
      for (String renderedProperty : renderedProperties)
      {
        CaseProperty cp = auxPropertyMap.remove(renderedProperty);
        if (cp != null) auxCasePropertyList.add(cp);
      }
      auxCasePropertyList.addAll(auxPropertyMap.values());
      getCasePropertiesMap().put(caseId, auxCasePropertyList);
    }
    return getCasePropertiesMap().get(caseId);
  }

  public class CaseTreeNode implements TreeNode, Serializable
  {
    private String caseId;
    private String caseTypeId;
    private String title;
    private boolean direct;
    private String caseCaseTypeId;
    private String caseCaseStartDate;
    private String caseCaseEndDate;

    private String identifier = "0";
    
    private transient List<CaseTreeNode> _children;
    private transient List<CaseCaseCache.CacheItem> _directCaseCases;
    private transient List<CaseCaseCache.CacheItem> _reverseCaseCases;

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId)
    {
      this.caseTypeId = caseTypeId;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public boolean isDirect()
    {
      return direct;
    }

    public void setDirect(boolean direct)
    {
      this.direct = direct;
    }

    public String getCaseCaseTypeId()
    {
      return caseCaseTypeId;
    }

    public void setCaseCaseTypeId(String caseCaseTypeId)
    {
      this.caseCaseTypeId = caseCaseTypeId;
    }

    public String getCaseCaseEndDate()
    {
      return caseCaseEndDate;
    }

    public void setCaseCaseEndDate(String caseCaseEndDate)
    {
      this.caseCaseEndDate = caseCaseEndDate;
    }

    public String getCaseCaseStartDate()
    {
      return caseCaseStartDate;
    }

    public void setCaseCaseStartDate(String caseCaseStartDate)
    {
      this.caseCaseStartDate = caseCaseStartDate;
    }

    public boolean isLeaf()
    {
      return getChildren().isEmpty();
    }

    public void setLeaf(boolean bln)
    {
      //nothing here
    }

    public List getChildren()
    {
      if (!getLoadedNodes().contains(getIdentifier()))
      {
        try
        {                      
          _children = new ArrayList<CaseTreeNode>();
          if (getTreeLeafTypeList().isEmpty() ||
            !getTreeLeafTypeList().contains(caseTypeId))
          {
            linkCaseCases();
            for (CaseCaseCache.CacheItem cacheItem : _directCaseCases)
            {
              CaseTreeNode node = new CaseTreeNode();
              node.setCaseId(cacheItem.getRelCaseId());
              node.setCaseTypeId(cacheItem.getRelCaseTypeId());
              node.setTitle(cacheItem.getRelCaseTitle());
              node.setDirect(true);
              node.setCaseCaseTypeId(cacheItem.getCaseCaseTypeId());
              node.setCaseCaseStartDate(cacheItem.getCaseCaseStartDate());
              node.setCaseCaseEndDate(cacheItem.getCaseCaseEndDate());
              _children.add(node);
            }
            for (CaseCaseCache.CacheItem cacheItem : _reverseCaseCases)
            {
              CaseTreeNode node = new CaseTreeNode();
              node.setCaseId(cacheItem.getMainCaseId());
              node.setCaseTypeId(cacheItem.getMainCaseTypeId());
              node.setTitle(cacheItem.getMainCaseTitle());
              node.setDirect(false);
              node.setCaseCaseTypeId(cacheItem.getCaseCaseTypeId());
              node.setCaseCaseStartDate(cacheItem.getCaseCaseStartDate());
              node.setCaseCaseEndDate(cacheItem.getCaseCaseEndDate());
              _children.add(node);
            }
            
            final List<String> orderByList = getOrderByList();
            if (!orderByList.isEmpty())
            {
              Collections.sort(_children, new Comparator() {
                @Override
                public int compare(Object o1, Object o2)
                {
                  int comparison = 0;
                  for (String orderBy : orderByList)
                  {
                    boolean reverse = false;
                    if (orderBy.endsWith(":desc"))
                    {
                      reverse = true;
                      orderBy = orderBy.substring(0, orderBy.length() - 5);
                    }
                    CaseTreeNode node1 = (reverse ? (CaseTreeNode)o2 : (CaseTreeNode)o1);
                    CaseTreeNode node2 = (reverse ? (CaseTreeNode)o1 : (CaseTreeNode)o2);
                    if ("caseCaseType".equals(orderBy))
                    {
                      comparison = node1.getCaseCaseTypeId().compareTo(node2.getCaseCaseTypeId());
                      if (comparison != 0) break;
                    }
                    else if ("caseType".equals(orderBy))
                    {
                      comparison = node1.getCaseTypeId().compareTo(node2.getCaseTypeId());
                      if (comparison != 0) break;
                    }
                    else if ("caseId".equals(orderBy))
                    {
                      Integer caseId1 = 
                        Integer.parseInt(node1.getCaseId().contains(":") ? 
                          node1.getCaseId().substring(node1.getCaseId().indexOf(":") + 1) : 
                          node1.getCaseId());
                      Integer caseId2 = 
                        Integer.parseInt(node2.getCaseId().contains(":") ? 
                          node2.getCaseId().substring(node2.getCaseId().indexOf(":") + 1) : 
                          node2.getCaseId());
                      comparison = caseId1 - caseId2;
                      if (comparison != 0) break;
                    }
                    else if (orderBy.startsWith("property[") && orderBy.endsWith("]"))
                    {
                      String propertyName = orderBy.substring(9, orderBy.length() - 1);
                      try
                      {
                        String propertyValue1 = node1.getProperty(propertyName);
                        if (propertyValue1 == null) propertyValue1 = "";
                        String propertyValue2 = node2.getProperty(propertyName);
                        if (propertyValue2 == null) propertyValue2 = "";
                        comparison = propertyValue1.compareTo(propertyValue2);
                      }                
                      catch (Exception ex) { }
                      if (comparison != 0) break;
                    }
                  }                
                  return comparison;
                }
              });            
            }
            
            int i = 0;
            for (CaseTreeNode node : _children)
            {              
              node.setIdentifier(this.getIdentifier() + ":" + i);
              i++;
            }
          }
          getLoadedNodes().add(this.getIdentifier());
          if (isExpandedInModel())
          {
            getExpandedNodes().add(this.getIdentifier());
          }
          else
          {
            getExpandedNodes().remove(this.getIdentifier());
          }
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      return _children;
    }

    public String getType()
    {
      return "case";
    }

    public void setType(String string)
    {
      //nothing here
    }

    public String getDescription()
    {
      try
      {
        return getRelationLabel() + ": " + getTitle();
      }
      catch (Exception ex)
      {
        return "";
      }
    }

    public void setDescription(String string)
    {
      //nothing here
    }

    public void setIdentifier(String string)
    {
      this.identifier = string;
    }

    public String getIdentifier()
    {
      return identifier;
    }

    public int getChildCount()
    {
      return getChildren().size();
    }

    public String getIcon() throws Exception
    {
      if (isRoot())
      {
        return getRootIcon();
      }
      else
      {
        return getRelationIconMap().get(caseCaseTypeId);
      }
    }

    public List<CaseProperty> getRenderedProperties() throws Exception
    {
      List<CaseProperty> result = new ArrayList();
      if (getCaseTypeMap().containsKey(caseTypeId))
      {
        for (CaseProperty caseProperty : getCaseProperties(caseId))
        {
          if (caseProperty.isRendered()) result.add(caseProperty);
        }
      }
      return result;
    }
    
    public String getProperty(String propertyName) throws Exception
    {
      List<CaseProperty> casePropertyList = getCaseProperties(caseId);
      for (CaseProperty caseProperty : casePropertyList)
      {
        if (caseProperty.getName().equals(propertyName))
        {
          return caseProperty.getValue();
        }
      }
      return null;      
    }    
    
    public boolean isRenderPropertiesAsList()
    {      
      try
      {
        CaseType caseType = getCaseTypeMap().get(caseTypeId);
        if (caseType != null) 
          return LIST_LAYOUT_TYPE.equals(caseType.getLayoutType());
      }
      catch (Exception ex)
      {        
      }
      return false;
    }

    public boolean isRenderPropertiesAsPopUp()
    {      
      try
      {
        CaseType caseType = getCaseTypeMap().get(caseTypeId);
        if (caseType != null) 
          return POPUP_LAYOUT_TYPE.equals(caseType.getLayoutType());
      }
      catch (Exception ex)
      {
      }
      return false;
    }
    
    public String getRelationLabel() throws Exception
    {
      if (isRoot())
      {
        return getRootLabel();
      }
      else
      {
        return getRelationLabel(caseCaseTypeId, direct);
      }      
    }

    public boolean isFound()
    {
      return getFoundCases().contains(getCaseId());
    }

    public boolean isSelected()
    {
      return getSelectedNodes().contains(getIdentifier());
    }

    public String getStyleClass()
    {
      try
      {
        String treeDate = getTreeDate();
        if (treeDate == null || treeDate.isEmpty()) //show full history
        {
          String startDate = "00000000";
          String endDate = "99999999";
          if (getCaseCaseStartDate() != null) startDate = getCaseCaseStartDate();
          if (getCaseCaseEndDate() != null) endDate = getCaseCaseEndDate();
          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
          String strNow = format.format(new Date());
          if (strNow.compareTo(startDate) < 0) //Future CaseCase
          {
            return "futureCaseCase";
          }
          else if (strNow.compareTo(endDate) > 0) //Old CaseCase
          {
            return "oldCaseCase";
          }
        }
      }
      catch (Exception ex)
      {
        //error(ex);
      }
      return "existingCaseCase"; //Current CaseCase
    }

    private boolean isExpandedInModel() throws Exception
    {
      return getTreeModel().getTreeState().isNodeExpanded(getIdentifier());
    }

    private void linkCaseCases() throws Exception
    {
      CaseCaseCache caseCaseCache = CaseCaseCache.getInstance();

      //Direct relations
      if (_directCaseCases == null)
      {
        _directCaseCases = new ArrayList<CaseCaseCache.CacheItem>();
        List<CaseCaseCache.CacheItem> directCaseCases =
          caseCaseCache.getDirectCaseCases(caseId, getTreeDate());
        for (CaseCaseCache.CacheItem directCaseCase : directCaseCases)
        {
          String auxCaseCaseTypeId = directCaseCase.getCaseCaseTypeId();
          if (getRelationDirectionMap().containsKey(auxCaseCaseTypeId))
          {
            String direction = getRelationDirectionMap().get(auxCaseCaseTypeId);
            if ("direct".equals(direction))
            {
              _directCaseCases.add(directCaseCase);
            }
          }
        }
      }

      //Reverse relations
      if (_reverseCaseCases == null)
      {
        _reverseCaseCases = new ArrayList<CaseCaseCache.CacheItem>();
        List<CaseCaseCache.CacheItem> reverseCaseCases =
          caseCaseCache.getReverseCaseCases(caseId, getTreeDate());
        for (CaseCaseCache.CacheItem reverseCaseCase : reverseCaseCases)
        {
          String auxCaseCaseTypeId = reverseCaseCase.getCaseCaseTypeId();
          if (getRelationDirectionMap().containsKey(auxCaseCaseTypeId))
          {
            String direction = getRelationDirectionMap().get(auxCaseCaseTypeId);
            if ("reverse".equals(direction))
            {
              _reverseCaseCases.add(reverseCaseCase);
            }
          }
        }
      }
    }

    private boolean isRoot()
    {
      return (caseCaseTypeId == null);
    }

    private String getRelationLabel(String typeId, boolean direct)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        String propertyName = null;
        if (direct)
          propertyName = DIRECT_SHORT_DESCRIPTION_PROPERTY;
        else
          propertyName = REVERSE_SHORT_DESCRIPTION_PROPERTY;
        PropertyDefinition pd = type.getPropertyDefinition(propertyName);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        {
          return pd.getValue().get(0);
        }
      }
      return "Case";
    }

    private String getRootLabel() throws Exception
    {
      Type type = TypeCache.getInstance().getType(getRootCaseTypeId());
      if (type != null)
      {
        PropertyDefinition pd = type.getPropertyDefinition(OBJECT_LABEL_PROPERTY);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        {
          return pd.getValue().get(0);
        }
      }
      return "Case";
    }

    private String getRootIcon()
    {
      if (rootIcon == null)
      {
        MenuItemCursor mic =
          UserSessionBean.getCurrentInstance().getSelectedMenuItem();
        rootIcon = mic.getProperty(ROOT_ICON_PROPERTY);
      }
      return rootIcon;
    }

  }

  public class CaseType implements Serializable
  {
    private final String caseTypeId;
    private final String layoutType;
    private List<CaseTypeProperty> caseTypePropertyList = new ArrayList();

    public CaseType(String caseTypeId, String layoutType)
    {
      this.caseTypeId = caseTypeId;
      this.layoutType = layoutType;
    }
    
    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public String getLayoutType()
    {
      return layoutType;
    }

    public List<CaseTypeProperty> getCaseTypePropertyList()
    {
      return caseTypePropertyList;
    }
  }
  
  public class CaseTypeProperty implements Serializable
  {
    private final String caseTypeId;
    private final String format; //'icon' or 'text'
    private final String decoration; //if 'icon' -> icon path; if 'text' -> label
    private final String filterName; //property name
    private final String filterValue; //property value (regular expression)
    
    public CaseTypeProperty(String caseTypeId, String format, String decoration, 
      String filterName, String filterValue)
    {
      this.caseTypeId = caseTypeId;
      this.format = format;
      this.decoration = decoration;
      this.filterName = filterName;
      this.filterValue = filterValue;
    }
    
    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public String getFormat()
    {
      return format;
    }

    public String getDecoration()
    {
      return decoration;
    }

    public String getFilterName()
    {
      return filterName;
    }

    public String getFilterValue()
    {
      return filterValue;
    }
  }

  public class CaseProperty implements Serializable
  {
    private String caseId;
    private String name;
    private String value;
    private String icon;
    private String label;
    private boolean rendered;

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public String getIcon()
    {
      return icon;
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public boolean isRendered()
    {
      return rendered;
    }

    public void setRendered(boolean rendered)
    {
      this.rendered = rendered;
    }
  }

}
