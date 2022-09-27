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
package org.santfeliu.cms.web;

import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.activation.DataHandler;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.cms.CMSConstants;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeChange;
import org.matrix.cms.NodeChangeType;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.cms.Workspace;
import org.matrix.cms.WorkspaceFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.pf.cms.CMSConfigHelper;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.cms.CNode;
import org.santfeliu.cms.CWorkspace;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBeanIntrospector;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author lopezrj-sf
 */
public class NodeEditBean extends FacesBean implements Serializable
{
  private static final String ACTION_PROPERTY = "action";
  private static final String BEAN_NAME_PROPERTY = "beanName";
  private static final String BEAN_ACTION_PROPERTY = "beanAction";
  private static final Set<String> COMMON_PROPERTY_NAMES =
    getCommonPropertyNames();
  private static final Integer MAX_SEARCH_ITEMS = 200;
  private static final String[] SPECIAL_PROPERTY_CHARS = 
    {".", "[", "]", "{", "}"};
  private static final Integer PROPERTIES_TAB_INDEX = 0;
  private static final Integer CSS_TAB_INDEX = 1;
  private static final Integer SYNC_TAB_INDEX = 2;
  private static final Integer SEARCH_TAB_INDEX = 3;  
  private static final Integer FORMS_TAB_INDEX = 4;   
  
  //Tree edit & selection
  private String currentWorkspaceId;
  private Set<String> expandedNodeIdSet;
  private String cutNodeId;
  private String copyNodeId;
  private boolean copySingleNode = false;
  private List<Node> rootNodeList;
  private String rootNodeId;

  //Property edit
  private List<Property> propertyList;
  private List<Property> userPropertyList;
  private Property beanNameProperty;
  private Property beanActionProperty;
  private SelectItem[] beanNames;
  private SelectItem[] beanActions;
  private Set<String> annotatedPropertyNameSet;
  private String nodeName;
  private boolean showPropertyHelp;
  private String nodeTipsBundlePath;
  private String mandatoryPropertyLabel;
  private String undefinedLabel;

  //Search
  private String inputSearch;
  private List<Property> searchPropertyList;
  private Map<String, NodeSearchItem> nodeSearchMap; //nodeId -> Properties
  private List<NodeSearchItem> nodeSearchItemList;
  private boolean searchDone = false;
  private String searchMode;
  private Integer searchFirstRowIndex;

  //Css
  private String cssText;

  //Sync
  private List<NodeChangeItem> fullNodeChangeItemList;
  private Map<String, NodeChange> nodeChangeMap;
  private List<NodeChangeItem> nodeChangeItemList;
  private List<NodeChangeItem> selectedNodeChangeItemList;
  private String toWorkspaceId;
  private SelectItem[] toWorkspaceItems;
  private Integer syncFirstRowIndex;  
  
  //Tree
  private TreeNode treeRoot;  
  
  //Misc
  private Integer activeTabIndex;
  private org.primefaces.model.menu.MenuModel nodePathModel;  
  private final CMSConfigHelper configHelper;
  
  public NodeEditBean()
  {
    configHelper = new CMSConfigHelper();
  }

  //********** setters/getters *********
  
  public List<NodeChangeItem> getFullNodeChangeItemList() 
  {
    analyzeWorkspaceChange();
    if (fullNodeChangeItemList == null)
    {
      loadFullNodeChangeItemList(currentWorkspaceId, getToWorkspaceId());
    }
    return fullNodeChangeItemList;
  }

  public void setFullNodeChangeItemList(List<NodeChangeItem> 
    fullNodeChangeItemList) 
  {
    this.fullNodeChangeItemList = fullNodeChangeItemList;
  }

  public Map<String, NodeChange> getNodeChangeMap()
  {
    analyzeWorkspaceChange();    
    if (nodeChangeMap == null)
    {
      loadFullNodeChangeItemList(currentWorkspaceId, getToWorkspaceId());  
    }
    return nodeChangeMap;
  }

  public void setNodeChangeMap(Map<String, NodeChange> nodeChangeMap)
  {
    this.nodeChangeMap = nodeChangeMap;
  }  
  
  public List<NodeChangeItem> getNodeChangeItemList() 
  {
    analyzeWorkspaceChange();    
    if (nodeChangeItemList == null)
    {
      loadNodeChangeItemList();      
    }
    return nodeChangeItemList;
  }

  public void setNodeChangeItemList(List<NodeChangeItem> nodeChangeItemList)
  {
    this.nodeChangeItemList = nodeChangeItemList;
  }

  public List<NodeChangeItem> getSelectedNodeChangeItemList() 
  {
    if (selectedNodeChangeItemList == null)
    {
      selectedNodeChangeItemList = new ArrayList();
    }
    return selectedNodeChangeItemList;
  }

  public void setSelectedNodeChangeItemList(List<NodeChangeItem> 
    selectedNodeChangeItemList) 
  {
    this.selectedNodeChangeItemList = selectedNodeChangeItemList;
  }

  public String getToWorkspaceId()
  {
    if (toWorkspaceId == null)
    {
      if (getToWorkspaceItems().length > 0)
      {
        toWorkspaceId = (String)getToWorkspaceItems()[0].getValue();
      }
    }
    return toWorkspaceId;
  }

  public void setToWorkspaceId(String toWorkspaceId)
  {
    this.toWorkspaceId = toWorkspaceId;
  }

  public Integer getSyncFirstRowIndex() 
  {
    if (syncFirstRowIndex == null)
    {
      syncFirstRowIndex = 0;
    }
    return syncFirstRowIndex;
  }

  public void setSyncFirstRowIndex(Integer syncFirstRowIndex) 
  {
    this.syncFirstRowIndex = syncFirstRowIndex;
  }

  public String getUndefinedLabel()
  {
    if (undefinedLabel == null)
    {
      String cmsBundlePath = "org.santfeliu.cms.web.resources.CMSBundle";
      ResourceBundle bundle = getBundle(cmsBundlePath);
      undefinedLabel = getBundleValue(bundle, "undefined");
    }
    return undefinedLabel;
  }

  public String getSearchInDescendantsLabel()
  {
    String cmsBundlePath = "org.santfeliu.cms.web.resources.CMSBundle";
    ResourceBundle bundle = getBundle(cmsBundlePath);
    return getBundleValue(bundle, "inDescendantsOfNode", getSelectedNodeId());
  }

  public String getMandatoryPropertyLabel()
  {
    if (mandatoryPropertyLabel == null)
    {
      String cmsBundlePath = "org.santfeliu.cms.web.resources.CMSBundle";
      ResourceBundle bundle = getBundle(cmsBundlePath);
      mandatoryPropertyLabel =
        "<" + getBundleValue(bundle, "putValueHere") + ">";
    }
    return mandatoryPropertyLabel;
  }

  public void switchToWorkspace()
  {
    resetSyncPanel();
  }

  public SelectItem[] getToWorkspaceItems()
  {
    analyzeWorkspaceChange();
    if (toWorkspaceItems == null)
    {
      loadToWorkspaceItems();
    }
    return toWorkspaceItems;
  }

  public void setToWorkspaceItems(SelectItem[] toWorkspaceItems)
  {
    this.toWorkspaceItems = toWorkspaceItems;
  }

  public SelectItem[] getBeanNames()
  {    
    if (beanNames == null)
    {
      loadBeanNames();
    }
    return beanNames;
  }

  public void setBeanNames(SelectItem[] beanNames)
  {
    this.beanNames = beanNames;
  }

  public SelectItem[] getBeanActions()
  {
    if (beanActions == null)
    {
      loadBeanActions();
    }
    return beanActions;
  }

  public void setBeanActions(SelectItem[] beanActions)
  {
    this.beanActions = beanActions;
  }

  public String getNodeTipsBundlePath()
  {
    analyzeWorkspaceChange();
    if (nodeTipsBundlePath == null)
    {
      loadNodeTipsBundlePath();
    }
    return nodeTipsBundlePath;
  }

  public void setNodeTipsBundlePath(String nodeTipsBundlePath)
  {
    this.nodeTipsBundlePath = nodeTipsBundlePath;
  }

  public Map<String, NodeSearchItem> getNodeSearchMap() 
  {
    if (nodeSearchMap == null)
    {
      nodeSearchMap = new HashMap();
    }
    return nodeSearchMap;
  }

  public void setNodeSearchMap(Map<String, NodeSearchItem> nodeSearchMap) 
  {
    this.nodeSearchMap = nodeSearchMap;
  }
  
  public List<NodeSearchItem> getNodeSearchItemList() 
  {
    if (nodeSearchItemList == null)
    {
      nodeSearchItemList = new ArrayList(getNodeSearchMap().values());
      Collections.sort(nodeSearchItemList, new Comparator<NodeSearchItem>() {
        @Override
        public int compare(NodeSearchItem n1, NodeSearchItem n2)
        {
          return n1.compareTo(n2);
        }
      });
    }
    return nodeSearchItemList;
  }

  public void setNodeSearchItemList(List<NodeSearchItem> nodeSearchItemList) 
  {
    this.nodeSearchItemList = nodeSearchItemList;
  }
  
  public boolean isShowPropertyHelp()
  {
    return showPropertyHelp;
  }

  public void setShowPropertyHelp(boolean showPropertyHelp)
  {
    this.showPropertyHelp = showPropertyHelp;
  }

  public List<Node> getRootNodeList()
  {
    analyzeWorkspaceChange();
    try
    {
      if (rootNodeList == null)
      {
        rootNodeList = new ArrayList();
        NodeFilter nodeFilter = new NodeFilter();
        nodeFilter.getWorkspaceId().add(
          UserSessionBean.getCurrentInstance().getWorkspaceId());
        nodeFilter.getParentNodeId().add(CMSConstants.NULL_PARENT_SYMBOL);
        rootNodeList =
          filterVisibleNodes(CMSConfigBean.getPort().findNodes(nodeFilter));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return rootNodeList;
  }

  public void setRootNodeList(List<Node> rootNodeList)
  {
    this.rootNodeList = rootNodeList;
  }
  
  public String getRootNodeId()
  {
    analyzeWorkspaceChange();
    if (rootNodeId == null)
    {
      MenuItemCursor root = getCursor().getRoot();
      rootNodeId = root.getMid();
    }
    return rootNodeId;
  }

  public void setRootNodeId(String rootNodeId)
  {
    this.rootNodeId = rootNodeId;
  }

  public String getRefWorkspaceId()
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    CWorkspace cWorkspace = cmsCache.getWorkspace(
      UserSessionBean.getCurrentInstance().getWorkspaceId());
    if (cWorkspace != null)
    {
      return cWorkspace.getWorkspace().getRefWorkspaceId();
    }
    return null;
  }

  public Set<String> getExpandedNodeIdSet() 
  {
    if (expandedNodeIdSet == null)
    {
      expandedNodeIdSet = new HashSet();      
    }
    return expandedNodeIdSet;
  }

  public void setExpandedNodeIdSet(Set<String> expandedNodeIdSet) 
  {
    this.expandedNodeIdSet = expandedNodeIdSet;
  }

  public boolean isSearchDone()
  {
    return searchDone;
  }

  public void setSearchDone(boolean searchDone)
  {
    this.searchDone = searchDone;
  }
  
  public String getSearchMode() 
  {
    if (searchMode == null)
    {
      searchMode = "workspace";
    }
    return searchMode;
  }

  public void setSearchMode(String searchMode) 
  {
    this.searchMode = searchMode;
  }

  public Integer getSearchFirstRowIndex() 
  {
    if (searchFirstRowIndex == null)
    {
      searchFirstRowIndex = 0;
    }
    return searchFirstRowIndex;
  }

  public void setSearchFirstRowIndex(Integer searchFirstRowIndex) 
  {
    this.searchFirstRowIndex = searchFirstRowIndex;
  }

  public String getCssText()
  {
    analyzeWorkspaceChange();
    if (cssText == null)
    {
      cssText = loadCssText();
    }
    return cssText;
  }

  public void setCssText(String cssText)
  {
    this.cssText = cssText;
  }

  public List<Property> getUserPropertyList()
  {
    if (userPropertyList == null)
    {
      List auxPropertyList = getPropertyList();
      userPropertyList = new ArrayList();
      userPropertyList.addAll(auxPropertyList);
      Property p = getPropertyInList(userPropertyList, BEAN_NAME_PROPERTY);
      if (p != null) userPropertyList.remove(p);
      p = getPropertyInList(userPropertyList, BEAN_ACTION_PROPERTY);
      if (p != null) userPropertyList.remove(p);
    }
    return userPropertyList;
  }

  public void setUserPropertyList(List<Property> userPropertyList)
  {
    this.userPropertyList = userPropertyList;
  }

  public Property getBeanActionProperty()
  {
    if (beanActionProperty == null)
    {
      Property auxBeanActionProperty = getPropertyInList(getPropertyList(),
        BEAN_ACTION_PROPERTY);
      if (auxBeanActionProperty != null)
      {
        beanActionProperty = auxBeanActionProperty;
      }
      else
      {
        Property p = new Property();
        p.setName(BEAN_ACTION_PROPERTY);
        p.getValue().add(getUndefinedLabel());
        beanActionProperty = p;
      }            
    }
    return beanActionProperty;
  }

  public void setBeanActionProperty(Property beanActionProperty)
  {
    this.beanActionProperty = beanActionProperty;
  }

  public Property getBeanNameProperty()
  {
    if (beanNameProperty == null)
    {
      Property auxBeanNameProperty = getPropertyInList(getPropertyList(),
        BEAN_NAME_PROPERTY);
      if (auxBeanNameProperty != null)
      {
        beanNameProperty = auxBeanNameProperty;
      }
      else
      {
        Property p = new Property();
        p.setName(BEAN_NAME_PROPERTY);
        p.getValue().add(getUndefinedLabel());
        beanNameProperty = p;
      }
    }
    return beanNameProperty;
  }

  public void setBeanNameProperty(Property beanNameProperty)
  {
    this.beanNameProperty = beanNameProperty;
  }

  public String getNodeName()
  {
    analyzeWorkspaceChange();
    if (nodeName == null)
    {
      CNode cNode = getSelectedCNode();
      if (cNode != null)
      {
        nodeName = cNode.getNode().getName();
      }
    }
    return nodeName;
  }

  public void setNodeName(String nodeName)
  {
    this.nodeName = nodeName;
  }

  public List<Property> getSearchPropertyList()
  {
    if (searchPropertyList == null)
    {
      searchPropertyList = new ArrayList();
    }
    return searchPropertyList;
  }

  public void setSearchPropertyList(List<Property> searchPropertyList)
  {
    this.searchPropertyList = searchPropertyList;
  }

  public String getInputSearch()
  {
    return inputSearch;
  }

  public void setInputSearch(String inputSearch)
  {
    this.inputSearch = inputSearch;
  }

  public TreeNode<NodeInfo> getTreeRoot() 
  {
    if (treeRoot == null)
    {
      treeRoot = new DefaultTreeNode("", null); //dummy      
      expandNode(getMainTreeNode());
      expandNodeId(UserSessionBean.getCurrentInstance().getSelectedMid(), 
        false);
      Set<String> auxExpandedNodeIdSet = new HashSet();
      auxExpandedNodeIdSet.addAll(getExpandedNodeIdSet());
      for (String nodeId : auxExpandedNodeIdSet)
      {
        expandNodeId(nodeId, true);
      }
    }
    return treeRoot;
  }

  public void setTreeRoot(TreeNode treeRoot) 
  {
    this.treeRoot = treeRoot;
  }

  public Integer getActiveTabIndex()
  {
    if (activeTabIndex == null)
    {
      activeTabIndex = 0;
    }
    return activeTabIndex;
  }

  public void setActiveTabIndex(Integer activeTabIndex)
  {
    this.activeTabIndex = activeTabIndex;
  }

  public org.primefaces.model.menu.MenuModel getNodePathModel() 
  {
    analyzeWorkspaceChange();    
    if (nodePathModel == null)
    {
      nodePathModel = new DefaultMenuModel();
      MenuItemCursor[] cursorPath = 
        getMenuModel().getSelectedMenuItem().getCursorPath();
      for (MenuItemCursor cursor : cursorPath)
      {      
        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(getMenuItemLabel(cursor, false));
        item.setIcon("ui-icon-triangle-1-e");        
        item.setCommand("#{nodeEditBean.selectMenuItem('" + cursor.getMid() + 
          "')}");
        item.setUpdate("@this @(.toolbarRenderingButtons) " + 
          ":mainform:topPanel :mainform:leftPanel :mainform:rightPanel");
        item.setAjax(true);
        item.setStyleClass("item");
        item.setOncomplete("treeScrollToSelected()");
        nodePathModel.getElements().add(item);
      }
    }
    return nodePathModel;
  }

  public void setNodePathModel(org.primefaces.model.menu.MenuModel 
    nodePathModel) 
  {
    this.nodePathModel = nodePathModel;
  }
  
  public void onNodeExpand(NodeExpandEvent event)
  {
    expandNode(event.getTreeNode());
  }

  public void onNodeCollapse(NodeCollapseEvent event)
  {
    collapseAllNodes(event.getTreeNode());
  }
  
  public void onNodeSelect(NodeSelectEvent event)
  {
    TreeNode<NodeInfo> treeNode = event.getTreeNode();
    String nodeId = treeNode.getData().getNodeId();
    selectMenuItem(nodeId);
  }

  public List getPropertyValues()
  {
    List<PropertyValueWrapper> values = new ArrayList();
    Property property = (Property)getValue("#{property}");
    for (int i = 0; i < property.getValue().size(); i++)
    {
      values.add(new PropertyValueWrapper(i));
    }
    return values;
  }

  public Set getEditRoles()
  {
    HashSet roles = new HashSet();
    MenuItemCursor mic = getCursor();
    try
    {
      roles.addAll(mic.getEditRoles());
    }
    catch (Exception ex)
    {
    }
    return roles;
  }

  public Set getParentEditRoles()
  {
    HashSet roles = new HashSet();
    MenuItemCursor mic = getCursor().getParent();
    try
    {
      roles.addAll(mic.getEditRoles());
    }
    catch (Exception ex)
    {
    }
    return roles;
  }

  public boolean isValidCSS()
  {
    try
    {
      String docId = 
        (String)getCursor().getDirectProperties().get(UserSessionBean.NODE_CSS);
      if (docId != null)
      {
        if (docId.contains("?"))
        {
          docId = docId.substring(0, docId.indexOf("?"));
        }
        Integer.parseInt(docId); //Check if node value is an integer
      }
    }
    catch (NumberFormatException ex)
    {
      return false;
    }
    return true;
  }
  
  public boolean isCustomCSS()
  {
    MenuItemCursor cursor = (MenuItemCursor)getValue("#{item}");
    if (cursor == null) cursor = getCursor();
    String docId = (String)cursor.getDirectProperties().
      get(UserSessionBean.NODE_CSS);
    return docId != null;
  }

  public boolean isSyncCssButtonRender()
  {
    try
    {
      String fromCSSDocId = 
        getNodeCSS(currentWorkspaceId, getSelectedNodeId());
      if (fromCSSDocId != null)
      {
        String refWorkspaceId = getRefWorkspaceId();
        if (refWorkspaceId != null)
        {
          String toCSSDocId = getNodeCSS(refWorkspaceId, getSelectedNodeId());
          return (!fromCSSDocId.equals(toCSSDocId));
        }
      }
    }
    catch (Exception ex)
    {      
    }
    return false;
  }

  public boolean isFoundNode(MenuItemCursor item)
  {
    return getNodeSearchMap().keySet().contains(item.getMid());
  }  

  public boolean isMoveNodeUpEnabled()
  {    
    return userCanDoOperation(getEditRoles()) &&
      getSelectedCNode().getPreviousSibling() != null;
  }

  public boolean isMoveNodeDownEnabled()
  {
    return userCanDoOperation(getEditRoles()) &&
      getSelectedCNode().getNextSibling() != null;
  }

  public boolean isAppendNodeEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isInsertBeforeNodeEnabled()
  {
    return userCanDoOperation(getParentEditRoles())
      && (getSelectedCNode().getParentNodeId() != null);
  }

  public boolean isInsertAfterNodeEnabled()
  {
    return userCanDoOperation(getParentEditRoles())
      && (getSelectedCNode().getParentNodeId() != null);
  }

  public boolean isRemoveNodeEnabled()
  {    
    return userCanDoOperation(getEditRoles());
  }

  public boolean isCutNodeEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isCopyNodeEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isPasteAsRootNodeEnabled()
  {
    analyzeWorkspaceChange();
    return (cutNodeId != null || copyNodeId != null);
  }

  public boolean isPasteInsideNodeEnabled()
  {
    analyzeWorkspaceChange();
    return ((cutNodeId != null || copyNodeId != null)
      && userCanDoOperation(getEditRoles()));
  }

  public boolean isPasteBeforeNodeEnabled()
  {
    analyzeWorkspaceChange();
    return ((cutNodeId != null || copyNodeId != null)
      && userCanDoOperation(getParentEditRoles())
      && (getSelectedCNode().getParentNodeId() != null));
  }

  public boolean isPasteAfterNodeEnabled()
  {
    analyzeWorkspaceChange();
    return ((cutNodeId != null || copyNodeId != null)
      && userCanDoOperation(getParentEditRoles())
      && (getSelectedCNode().getParentNodeId() != null));
  }

  public boolean isSyncNodeEnabled()
  {
    return getToWorkspaceItems().length > 0;
  }
  
  public boolean isNodeChangeItemListEmpty()
  {
    return (getNodeChangeItemList().isEmpty());
  }

  public boolean isUserPropertyListEmpty()
  {
    return (getUserPropertyList().isEmpty());
  }

  public boolean isBeanActionsRender()
  {
    return (getBeanActions().length > 0);
  }

  public boolean isSearchPropertyListEmpty()
  {
    return (getSearchPropertyList().isEmpty());
  }

  public boolean isAndLabelRender()
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("propertyRowIndex");
    int itemCount = getSearchPropertyList().size();
    return (itemCount > 1) && (index < itemCount - 1);
  }

  public boolean isOrLabelRender()
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("valueRowIndex");
    int itemCount = getPropertyValues().size();
    return (itemCount > 1) && (index < itemCount - 1);
  }
  
  public boolean isAddPropertyEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isAddPropertyValueEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isRemovePropertyEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public boolean isSavePropertiesEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }
  
  public boolean isCompletePropertiesEnabled()
  {
    return userCanDoOperation(getEditRoles());
  }

  public String getPropertyNameStyleClass()
  {
    String styleClass = "propertyName";
    Property property = (Property)getFacesContext().getExternalContext().
      getRequestMap().get("property");
    String propertyName = property.getName();
    if (propertyName != null)
    {
      if (COMMON_PROPERTY_NAMES.contains(propertyName))
      {
        styleClass += " inheritedAnnotated";
      }
      else if (getAnnotatedPropertyNameSet().contains(propertyName))
      {
        styleClass += " directAnnotated";
      }        
      String currentNodeId = getSelectedNodeId();
      if (currentNodeId != null && 
        getNodeSearchMap().keySet().contains(currentNodeId) && 
        getNodeSearchMap().get(currentNodeId).isMarkPropertyName(propertyName))
      {
        styleClass += " found";
      }
    }
    return styleClass;
  }
  
  public String getPropertyValueStyleClass()
  {
    String styleClass = "propertyValue";
    Property property = (Property)getFacesContext().getExternalContext().
      getRequestMap().get("property"); 
    String propertyName = property.getName();
    if (propertyName != null)
    {
      String currentNodeId = getSelectedNodeId();
      if (currentNodeId != null && 
        getNodeSearchMap().keySet().contains(currentNodeId))
      {
        PropertyValueWrapper valueWrapper = (PropertyValueWrapper)
          getFacesContext().getExternalContext().getRequestMap().
            get("propertyValue");                
        if (getNodeSearchMap().get(currentNodeId).
          isMarkPropertyValue(propertyName, valueWrapper.index))
        {
          styleClass += " found";
        }
      }
    }
    return styleClass;
  }

  public String getSelectedNodeId()
  {
    return getCursor().getMid();
  }

  public String getMenuItemLabel()
  {
    MenuItemCursor item = (MenuItemCursor)getValue("#{item}");
    return getMenuItemLabel(item);
  }

  public String getNodeLabel()
  {
    Node node = (Node)getValue("#{node}");
    return getNodeLabel(node);
  }

  public String getRootLabel()
  {
    MenuItemCursor item = getCursor().getRoot();
    return getMenuItemLabel(item);
  }

  // ******** Action methods *********

  public void moveNodeUp()
  {
    try
    {
      swapNode(getSelectedCNode().getPreviousSibling());
      info("NODE_MOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void moveNodeDown()
  {
    try
    {
      swapNode(getSelectedCNode());
      info("NODE_MOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void appendNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String workspaceId = userSessionBean.getWorkspaceId();
      String parentNodeId = userSessionBean.getSelectedMid();
      CNode selectedCNode = getSelectedCNode();
      int index = 1;
      if (selectedCNode.getLastChild() != null)
      {
        index = selectedCNode.getLastChild().getNode().getIndex() + 1;
      }
      Node node = new Node();
      node.setWorkspaceId(workspaceId);
      node.setIndex(index);
      node.setParentNodeId(parentNodeId);
      node = CMSConfigBean.getPort().storeNode(node);
      String nodeId = node.getNodeId();
      updateCache();
      resetTree();
      goToNode(nodeId);
      getExpandedNodeIdSet().add(parentNodeId);
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void insertBeforeNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String workspaceId = userSessionBean.getWorkspaceId();
      CNode selectedCNode = getSelectedCNode();
      int index = selectedCNode.getNode().getIndex();
      String parentNodeId = selectedCNode.getNode().getParentNodeId();
      Node node = new Node();
      node.setWorkspaceId(workspaceId);
      node.setIndex(index);
      node.setParentNodeId(parentNodeId);
      node = CMSConfigBean.getPort().storeNode(node);
      String nodeId = node.getNodeId();
      updateCache();
      resetTree();      
      goToNode(nodeId);
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void insertAfterNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String workspaceId = userSessionBean.getWorkspaceId();
      CNode selectedCNode = getSelectedCNode();
      int index = selectedCNode.getNode().getIndex() + 1;
      String parentNodeId = selectedCNode.getNode().getParentNodeId();
      Node node = new Node();
      node.setWorkspaceId(workspaceId);
      node.setIndex(index);
      node.setParentNodeId(parentNodeId);
      node = CMSConfigBean.getPort().storeNode(node);
      String nodeId = node.getNodeId();
      updateCache();
      resetTree();      
      goToNode(nodeId);
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String workspaceId = userSessionBean.getWorkspaceId();
      String nodeId = userSessionBean.getSelectedMid();
      String newNodeId = null;
      CNode cNode = getSelectedCNode();
      if (!userSessionBean.isCmsAdministrator() &&
        nodeHasInvisibleDescendants(cNode))
      {
        throw new Exception("NODE_HAS_INVISIBLE_DESCENDANTS");
      }
      if (cNode.isRoot())
      {
        List<Node> rootList = getRootNodeList();
        List<String> rootNodeIdList = new ArrayList<String>();
        for (Node node : rootList) rootNodeIdList.add(node.getNodeId());
        rootNodeIdList.remove(cNode.getNodeId());
        List<MenuItemCursor> visibleRootMenuItemList =
          getMenuModel().getMenuItemsByMid(rootNodeIdList);
        if (visibleRootMenuItemList.isEmpty())
        {
          throw new Exception("ONLY_ONE_ROOT");
        }
        newNodeId = visibleRootMenuItemList.get(0).getMid();
      }      
      else
      {
        newNodeId = getNewVisibleNodeId();
      }
      CMSConfigBean.getPort().removeNode(workspaceId, nodeId);
      if (cNode.isRoot())
      {
        resetRootSelectionPanel(newNodeId);        
      }
      resetTree();      
      goToNode(newNodeId);
      updateCache();
      info("NODE_REMOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cutNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cutNodeId = userSessionBean.getSelectedMid();
      copyNodeId = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void copyNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cutNodeId = null;
      copyNodeId = userSessionBean.getSelectedMid();
      copySingleNode = false;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void copySingleNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cutNodeId = null;
      copyNodeId = userSessionBean.getSelectedMid();
      copySingleNode = true;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void pasteAsRootNode()
  {
    try
    {
      String message = "";
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      CWorkspace cWorkspace = userSessionBean.getMenuModel().getCWorkspace();
      Node node = null;
      if (cutNodeId != null)
      {
        CNode cutCNode = cWorkspace.getNode(cutNodeId);
        node = cloneCNode(cutCNode, true);
        cutNodeId = null;
        message = "NODE_MOVED";
      }
      else if (copyNodeId != null)
      {
        CNode copyCNode = cWorkspace.getNode(copyNodeId);
        if (copySingleNode)
        {
          node = cloneCNode(copyCNode, false);
        }
        else
        {
          node = cloneCNodeAndDescendants(copyCNode);
        }
        copyNodeId = null;
        message = "NODE_COPIED";
      }
      node.setParentNodeId(null);
      node.setIndex(1);
      node = CMSConfigBean.getPort().storeNode(node);
      updateCache();
      resetRootSelectionPanel(node.getNodeId());
      resetTree();      
      goToNode(node.getNodeId());      
      info(message);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void pasteInsideNode()
  {
    try
    {
      String message = "";
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectedNodeId = userSessionBean.getSelectedMid();
      if (selectedNodeId != null)
      {
        CWorkspace cWorkspace = userSessionBean.getMenuModel().getCWorkspace();
        CNode selectedCNode = cWorkspace.getNode(selectedNodeId);
        int index = 1;
        if (selectedCNode.getLastChild() != null)
        {
          index = selectedCNode.getLastChild().getNode().getIndex() + 1;
        }
        Node node = null;
        if (cutNodeId != null)
        {
          CNode cutCNode = cWorkspace.getNode(cutNodeId);
          node = cloneCNode(cutCNode, true);
          cutNodeId = null;
          message = "NODE_MOVED";
          if (cutCNode.isRoot()) resetRootSelectionPanel();
        }
        else if (copyNodeId != null)
        {
          CNode copyCNode = cWorkspace.getNode(copyNodeId);
          if (copySingleNode)
          {
            node = cloneCNode(copyCNode, false);
          }
          else
          {
            node = cloneCNodeAndDescendants(copyCNode);
          }
          copyNodeId = null;
          message = "NODE_COPIED";
        }
        node.setParentNodeId(selectedNodeId);
        node.setIndex(index);
        node = CMSConfigBean.getPort().storeNode(node);
        updateCache();
        resetTree();        
        goToNode(node.getNodeId());
        getExpandedNodeIdSet().add(selectedNodeId);
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void pasteBeforeNode()
  {    
    try
    {
      String message = "";
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectedNodeId = userSessionBean.getSelectedMid();
      if (selectedNodeId != null)
      {
        CWorkspace cWorkspace = userSessionBean.getMenuModel().getCWorkspace();
        CNode selectedCNode = cWorkspace.getNode(selectedNodeId);
        int index = selectedCNode.getNode().getIndex();
        Node node = null;
        if (cutNodeId != null)
        {
          CNode cutCNode = cWorkspace.getNode(cutNodeId);
          node = cloneCNode(cutCNode, true);
          cutNodeId = null;
          message = "NODE_MOVED";
          if (cutCNode.isRoot()) resetRootSelectionPanel();
        }
        else if (copyNodeId != null)
        {
          CNode copyCNode = cWorkspace.getNode(copyNodeId);
          if (copySingleNode)
          {
            node = cloneCNode(copyCNode, false);
          }
          else
          {
            node = cloneCNodeAndDescendants(copyCNode);
          }
          copyNodeId = null;
          message = "NODE_COPIED";
        }
        node.setParentNodeId(selectedCNode.getParentNodeId());
        node.setIndex(index);
        node = CMSConfigBean.getPort().storeNode(node);
        updateCache();
        resetTree();        
        goToNode(node.getNodeId());
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void pasteAfterNode()
  {    
    try
    {
      String message = "";
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectedNodeId = userSessionBean.getSelectedMid();
      if (selectedNodeId != null)
      {
        CWorkspace cWorkspace = userSessionBean.getMenuModel().getCWorkspace();
        CNode selectedCNode = cWorkspace.getNode(selectedNodeId);
        int index = selectedCNode.getNode().getIndex() + 1;
        Node node = null;
        if (cutNodeId != null)
        {
          CNode cutCNode = cWorkspace.getNode(cutNodeId);
          node = cloneCNode(cutCNode, true);
          cutNodeId = null;
          message = "NODE_MOVED";
          if (cutCNode.isRoot()) resetRootSelectionPanel();
        }
        else if (copyNodeId != null)
        {
          CNode copyCNode = cWorkspace.getNode(copyNodeId);
          if (copySingleNode)
          {
            node = cloneCNode(copyCNode, false);
          }
          else
          {
            node = cloneCNodeAndDescendants(copyCNode);
          }
          copyNodeId = null;
          message = "NODE_COPIED";
        }
        node.setParentNodeId(selectedCNode.getParentNodeId());
        node.setIndex(index);
        node = CMSConfigBean.getPort().storeNode(node);
        updateCache();
        resetTree();        
        goToNode(node.getNodeId());
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void syncNode()
  {
    try
    {
      if (getSelectedNodeChangeItemList().size() > 0)
      {        
        List<NodeChange> finalNodeChangeList = new ArrayList();
        for (NodeChangeItem nodeChangeItem : getSelectedNodeChangeItemList())
        {
          NodeChange nodeChange = nodeChangeItem.getNodeChange();
          if (nodeChange.getType().equals(NodeChangeType.MOVED) || 
            nodeChange.getType().equals(NodeChangeType.NAME_CHANGED) || 
            nodeChange.getType().equals(NodeChangeType.FALSE_UPDATE))
          {
            nodeChange.setType(NodeChangeType.UPDATED);
          }
          finalNodeChangeList.add(nodeChange);
        }
        CMSConfigBean.getPort().syncNodes(currentWorkspaceId,
          getToWorkspaceId(), finalNodeChangeList);
        updateCache(getToWorkspaceId());
        resetSyncPanel();
        info("NODE_SYNCHRONIZED");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
  }

  public void collapseAllNodes()
  {
    setFullTreeState(getTreeRoot(), false);
    expandedNodeIdSet = null;    
    expandNodeId(UserSessionBean.getCurrentInstance().getSelectedMid(), false);    
  }

  public void switchPropertyHelp()
  {
    showPropertyHelp = !showPropertyHelp;
  }

  public String getHelpButtonStyleClass()
  {
    return (showPropertyHelp ? "showHelpImageButton" : "imageButton");
  }

  public void saveProperties()
  {
    try
    {
      List<Property> auxPropertyList = new ArrayList();
      auxPropertyList.addAll(getUserPropertyList());
      Property auxBeanNameProperty = getBeanNameProperty();
      if (auxBeanNameProperty != null &&
        !auxBeanNameProperty.getValue().isEmpty() &&
        !auxBeanNameProperty.getValue().get(0).equals(getUndefinedLabel()))
      {
        auxPropertyList.add(auxBeanNameProperty);
        Property auxBeanActionProperty = getBeanActionProperty();
        if (auxBeanActionProperty != null &&
          !auxBeanActionProperty.getValue().isEmpty() &&
          !auxBeanActionProperty.getValue().get(0).equals(getUndefinedLabel()))
        {
          auxPropertyList.add(auxBeanActionProperty);
        }
      }
      auxPropertyList = clean(auxPropertyList, true, false);
      CNode selectedCNode = getSelectedCNode();
      Node selectedNode = selectedCNode.getNode();
      selectedNode.getProperty().clear();
      selectedNode.getProperty().addAll(auxPropertyList);
      selectedNode.setName(nodeName);      
      CMSConfigBean.getPort().storeNode(selectedNode);
      if (selectedCNode.isRoot())
      {
        resetRootSelectionPanel();
      }
      updateCache();
      resetTree();
      if (!nodeIsVisible(selectedNode.getNodeId()))
      {
        String newVisibleNodeId = getNewVisibleNodeId();
        goToNode(newVisibleNodeId);
      }
      else
      {
        resetTopPanel();       
        resetPropertiesPanel();
        resetCssPanel();        
        resetSyncPanel();        
      }
      info("NODE_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void completeProperties()
  {
    try
    {
      List<Property> auxPropertyList = new ArrayList();
      nodeTipsBundlePath = null;
      annotatedPropertyNameSet = null;
      userPropertyList = clean(getUserPropertyList(), true, false);
      auxPropertyList.addAll(getAnnotatedCommonProperties());
      String action = getAction();
      if (action != null)
      {        
        auxPropertyList.addAll(getAnnotatedBeanProperties(action));
        Property actionProperty = 
          getPropertyInList(getUserPropertyList(), ACTION_PROPERTY);
        if (actionProperty != null)
        {
          actionProperty.getValue().clear();
          actionProperty.getValue().add(action);
        }
        else
        {
          actionProperty = getPropertyInList(auxPropertyList, ACTION_PROPERTY);
          if (actionProperty != null)
          {
            actionProperty.getValue().clear();
            actionProperty.getValue().add(action);
          }
          else
          {
            actionProperty = new Property();
            actionProperty.setName(ACTION_PROPERTY);
            actionProperty.getValue().add(action);
            auxPropertyList.add(actionProperty);
          }
        }
      }
      if (!auxPropertyList.isEmpty())
      {
        Collections.sort(auxPropertyList, new Comparator()
          {
            public int compare(Object o1, Object o2)
            {
              Property p1 = (Property)o1;
              Property p2 = (Property)o2;
              return p1.getName().compareToIgnoreCase(p2.getName());
            }
          }
        );
      }      
      getUserPropertyList().addAll(auxPropertyList);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void revertProperties()
  {
    try
    {
      resetPropertiesPanel();
      info("NODE_REVERTED");      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void addProperty()
  {
    Property p = new Property();
    userPropertyList.add(p);
    p.getValue().add("");
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
  }

  public void addPropertyValue()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    p.getValue().add("");
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
  }

  public void removeProperty()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    userPropertyList.remove(p);
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
  }

  public void addSearchProperty()
  {
    Property p = new Property();
    searchPropertyList.add(p);
    p.getValue().add("");
  }

  public void removeSearchProperty()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    searchPropertyList.remove(p);
  }
  
  public void resetSearch()
  {
    resetSearchPanel();
    inputSearch = null;
  }  

  public void saveCSS()
  {
    try
    {
      Document cssDoc = saveCSS(currentWorkspaceId, getSelectedNodeId());
      String message = "Document " + 
        (cssDoc.getVersion() == 1 ? "creat" : "actualitzat");
      message = UserSessionBean.getCurrentInstance().translate(message, "cms") + 
        ": " + cssDoc.getDocId();     
      info("CSS_SAVED", new Object[]{message});
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void revertCSS()
  {
    cssText = null;
    info("CSS_REVERTED");    
  }
  
  public void syncCSS()
  {
    try
    {
      String refWorkspaceId = getRefWorkspaceId();
      if (refWorkspaceId != null)
      {
        Document cssDoc = saveCSS(refWorkspaceId, getSelectedNodeId());
        String message = "Document " + 
          (cssDoc.getVersion() == 1 ? "creat" : "actualitzat");
        message = UserSessionBean.getCurrentInstance().translate(message, 
          "cms") + ": " + cssDoc.getDocId();
        info("CSS_SYNCHRONIZED", new Object[]{message});      
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void selectMenuItem(String nodeId)
  {
    String newRootNodeId = getRootNodeId(nodeId);
    if (!rootNodeId.equals(newRootNodeId))
    {
      rootNodeId = newRootNodeId;      
      nodeChangeMap = null;
      fullNodeChangeItemList = null;
      resetTree();
    }
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(nodeId);
    resetTopPanel();
    resetPropertiesPanel();
    resetCssPanel();
    resetSyncPanel(false);
  }
  
  public void selectSearchMenuItem(String nodeId)
  {
    setActiveTabIndex(PROPERTIES_TAB_INDEX);
    selectMenuItem(nodeId); 
  }

  public void changeRootNode()
  {
    resetTree();
    goToNode(rootNodeId);
  }

  public void changeBeanName()
  {
    beanActions = null;
  }

  public void createRootNode()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String workspaceId = userSessionBean.getWorkspaceId();
      Node node = new Node();
      node.setWorkspaceId(workspaceId);
      node.setIndex(1);
      node.setParentNodeId(null);
      node = CMSConfigBean.getPort().storeNode(node);
      updateCache();
      resetRootSelectionPanel(node.getNodeId());      
      resetTree();      
      goToNode(node.getNodeId());
      info("NEW_ROOT_CREATED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void fastSearch()
  {
    try
    {
      resetSearchPanel();
      NodeFilter nodeFilter = new NodeFilter();
      nodeFilter.setMaxResults(MAX_SEARCH_ITEMS);
      nodeFilter.getWorkspaceId().add(
        UserSessionBean.getCurrentInstance().getWorkspaceId());
      if (inputSearch != null && inputSearch.trim().length() >= 3)
      {
        //Search by name
        nodeFilter.setName("%" + inputSearch.trim() + "%");
        List<Node> auxNodeList =  CMSConfigBean.getPort().findNodes(nodeFilter);
        for (Node node : auxNodeList)
        {
          getNodeSearchMap().put(node.getNodeId(), new NodeSearchItem(node));
        }

        //Search by "label" property
        nodeFilter.setName(null);
        Property property = new Property();
        property.setName("label");
        property.getValue().add("%" + inputSearch.trim() + "%");
        nodeFilter.getProperty().add(property);
        nodeFilter.setPropertyCaseSensitive(false);
        auxNodeList =  CMSConfigBean.getPort().findNodes(nodeFilter);
        for (Node node : auxNodeList)
        {
          getNodeSearchMap().put(node.getNodeId(), new NodeSearchItem(node));
        }

        //Search by "description" property
        nodeFilter.getProperty().clear();
        property = new Property();
        property.setName("description");
        property.getValue().add("%" + inputSearch.trim() + "%");
        nodeFilter.getProperty().add(property);
        auxNodeList =  CMSConfigBean.getPort().findNodes(nodeFilter);
        for (Node node : auxNodeList)
        {
          getNodeSearchMap().put(node.getNodeId(), new NodeSearchItem(node));
        }
      }

      //Search by nodeId
      try
      {
        Integer i = new Integer(inputSearch.trim());
        nodeFilter.getProperty().clear();
        nodeFilter.getNodeId().add(inputSearch.trim());
        List<Node> auxNodeList =  CMSConfigBean.getPort().findNodes(nodeFilter);
        for (Node node : auxNodeList)
        {
          getNodeSearchMap().put(node.getNodeId(), new NodeSearchItem(node));
        }
      }
      catch (NumberFormatException ex)
      {

      }

      collapseAllNodes();
      CWorkspace cWorkspace = 
        UserSessionBean.getCurrentInstance().getMenuModel().getCWorkspace();
      for (String nodeId : getNodeSearchMap().keySet())
      {
        CNode cNode = cWorkspace.getNode(nodeId);
        if (cNode != null && cNode.getParentNodeId() != null)
        {
          expandNodeId(cNode.getParentNodeId(), true);
        }
      }      
      searchDone = true;
      searchMode = null;
      setActiveTabIndex(SEARCH_TAB_INDEX);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void fullSearch()
  {
    try
    {
      resetSearchPanel(false);
      NodeFilter nodeFilter = new NodeFilter();
      nodeFilter.setMaxResults(MAX_SEARCH_ITEMS);
      nodeFilter.getWorkspaceId().add(
        UserSessionBean.getCurrentInstance().getWorkspaceId());
      nodeFilter.setPropertyCaseSensitive(false);
      if ("tree".equals(getSearchMode()))
      {
        nodeFilter.getPathNodeId().add(getSelectedNodeId());
      }
      List<Property> properties = clean(getSearchPropertyList(), false, true);
      if (!properties.isEmpty())
      {
        nodeFilter.getProperty().addAll(properties);
        List<Node> auxNodeList =
          CMSConfigBean.getPort().findNodes(nodeFilter);
        for (Node node : auxNodeList)
        {          
          NodeSearchItem nodeSearchItem = new NodeSearchItem(node);
          for (Property property : node.getProperty())
          {
            List<Integer> valueIndexList = matchProperty(property, 
              searchPropertyList);
            if (!valueIndexList.isEmpty())
            {
              nodeSearchItem.addProperty(property.getName(), valueIndexList);
            }
          }
          getNodeSearchMap().put(node.getNodeId(), nodeSearchItem);
        }
        
        collapseAllNodes();
        CWorkspace cWorkspace = 
          UserSessionBean.getCurrentInstance().getMenuModel().getCWorkspace();        
        for (String nodeId : getNodeSearchMap().keySet())
        {
          CNode cNode = cWorkspace.getNode(nodeId);
          if (cNode != null && cNode.getParentNodeId() != null)
          {
            expandNodeId(cNode.getParentNodeId(), true);
          }
        }
      }
      searchDone = true;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String getPropertyTip()
  {
    String tip = null;
    Property property = (Property)getValue("#{property}");
    if (property != null && property.getName() != null)
    {
      String propertyName = property.getName().trim();
      if (!propertyName.isEmpty())
      {
        tip = getBundleValue(getNodeTipsBundle(), propertyName);
        if (tip == null)
        {
          //common property
          tip = getBundleValue(getCommonTipsBundle(), propertyName);
        }
      }
    }
    return tip;
  }

  public boolean isPropertyTipRender()
  {
    return getPropertyTip() != null;
  }
 
  public void onTabChange(TabChangeEvent event) 
  {
    String tabId = event.getTab().getId();
    if (tabId.contains("propertiesTab"))
    {
      setActiveTabIndex(PROPERTIES_TAB_INDEX);      
    }
    else if (tabId.contains("cssTab"))
    {
      setActiveTabIndex(CSS_TAB_INDEX);      
    }
    else if (tabId.contains("syncTab"))
    {
      setActiveTabIndex(SYNC_TAB_INDEX);      
    }
    else if (tabId.contains("searchTab"))
    {
      setActiveTabIndex(SEARCH_TAB_INDEX);      
    }
  }

  public String getNodeLabel(Node node)
  {
    String label = node.getNodeId();
    for (Property property : node.getProperty())
    {
      if ("description".equals(property.getName()))
      {
        return property.getValue().get(0);
      }
      else if ("label".equals(property.getName()))
      {
        label = property.getValue().get(0);
      }
    }
    return label;
  }

  public String getSelectedNodeLabel()
  {
    return configHelper.getNodeLabel(UserSessionBean.getCurrentInstance().
      getSelectedMenuItem());
  }  
  
  public boolean isPropertiesTabSelected()
  {
    return PROPERTIES_TAB_INDEX.equals(getActiveTabIndex());
  }

  public boolean isCssTabSelected()
  {
    return CSS_TAB_INDEX.equals(getActiveTabIndex());
  }

  public boolean isSyncTabSelected()
  {
    return SYNC_TAB_INDEX.equals(getActiveTabIndex());
  }

  public boolean isSearchTabSelected()
  {
    return SEARCH_TAB_INDEX.equals(getActiveTabIndex());
  }  
  
  public boolean isFormsTabSelected()
  {
    return FORMS_TAB_INDEX.equals(getActiveTabIndex());
  }
  
  public void resetRootSelectionPanel()
  {
    resetRootSelectionPanel(null);
  }

  public void resetTree()
  {
    treeRoot = null;
  }
  
  public void resetTopPanel()
  {
    nodePathModel = null;
  }

  public void resetCssPanel()
  {
    cssText = null;    
  }
  
  public void resetSyncPanel()
  {
    resetSyncPanel(true);
  }
  
  public void resetPropertiesPanel()
  {
    propertyList = null;
    userPropertyList = null;
    beanNameProperty = null;
    beanActionProperty = null;
    beanActions = null;
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
    nodeName = null;    
  }  

  public void goToNode(String nodeId)
  {
    UserSessionBean.getCurrentInstance().setSelectedMid(nodeId);
    resetTopPanel();
    resetPropertiesPanel();
    resetCssPanel();
    resetSyncPanel();
  }

  public void goToParentNode(String propertyNames)
  {
    String nodeId = null;
    boolean found = false;
    List<String> propertyNameList = Arrays.asList(propertyNames.split(","));
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    while (cursor.moveParent() && !found)
    {
      for (String propertyName : propertyNameList)
      {
        if (cursor.getDirectProperty(propertyName) != null)
        {
          nodeId = cursor.getMid();
          found = true;
        }
      }
    }
    if (nodeId != null)
    {
      goToNode(nodeId);
    }
  }

  // ********* Private methods ********

  private void expandNode(TreeNode<NodeInfo> node)
  {
    node.setExpanded(true);
    getExpandedNodeIdSet().add(node.getData().getNodeId());
    for (TreeNode n : node.getChildren())
    {
      loadNode(n);      
    }
  }
  
  private void loadNode(TreeNode node)
  {
    if (node.getChildCount() == 0)
    {
      List<String> childrenNodeIds = getChildrenNodeIds(node);
      for (String childNodeId : childrenNodeIds)
      {
        TreeNode n = new DefaultTreeNode("Node", new NodeInfo(childNodeId), 
          node);
      }
    }    
  }
  
  private void expandNodeId(String nodeId, boolean expandLast)
  {
    try
    {
      String[] nodeIdPath = getMenuModel().getMenuItemByMid(nodeId).getPath();
      expandNodeIdPath(nodeIdPath, expandLast);
    }
    catch (Exception ex)
    {
    }    
  }
  
  private void expandNodeIdPath(String[] nodeIdPath, boolean expandLast)
  {
    TreeNode<NodeInfo> auxNode = getTreeRoot();
    for (String nodeId : nodeIdPath)
    {
      if (!expandLast && nodeId.equals(nodeIdPath[nodeIdPath.length - 1]))
      {
        return;
      }      
      if (auxNode == null) return; //ERROR
      else
      {      
        TreeNode nextNode = null;      
        for (TreeNode<NodeInfo> n : auxNode.getChildren())
        {
          if (n.getData().getNodeId().equals(nodeId))
          {
            expandNode(n);
            nextNode = n;
            break;
          }
        }
        auxNode = nextNode;
      }
    }  
  }

  private void collapseAllNodes(TreeNode<NodeInfo> node)
  {
    getExpandedNodeIdSet().remove(node.getData().getNodeId());
    for (TreeNode child : node.getChildren())
    {
      collapseAllNodes(child);
    }
  }

  private void setFullTreeState(TreeNode<NodeInfo> node, boolean expanded)
  {
    node.setExpanded(expanded);
    for (TreeNode child : node.getChildren())
    {
      setFullTreeState(child, expanded);
    }
  }
  
  private List<String> getChildrenNodeIds(TreeNode<NodeInfo> node)
  {
    List<String> result = new ArrayList();
    String nodeId = node.getData().getNodeId();
    MenuItemCursor menuItem = getMenuModel().getMenuItem(nodeId);
    if (menuItem.hasChildren())
    {
      MenuItemCursor child = menuItem.getFirstChild();
      while (!child.isNull())
      {
        result.add(child.getMid());
        child = child.getNext();        
      }
    }
    return result;
  }
  
  private TreeNode getMainTreeNode()
  {
    if (getTreeRoot().getChildCount() == 0)
    {
      NodeInfo nodeInfo = new NodeInfo(getRootNodeId());
      TreeNode mainNode = new DefaultTreeNode("Node", nodeInfo, getTreeRoot());
      loadNode(mainNode);
    }
    return getTreeRoot().getChildren().get(0);
  }  
  
  private Document saveCSS(String workspaceId, String nodeId) throws Exception
  {
    Document document = null;
    if (cssText != null)
    {
      CNode cNode = ApplicationBean.getCurrentInstance().
        getCmsCache().getWorkspace(workspaceId).getNode(nodeId);
      CachedDocumentManagerClient docClient = DocumentConfigBean.getClient();
      String docId = cNode.getSinglePropertyValue(UserSessionBean.NODE_CSS);
      if (docId != null) // existing CSS
      {
        //Update document
        if (docId.contains("?"))
        {
          docId = docId.substring(0, docId.indexOf("?"));
        }
        Integer.parseInt(docId); //Check if node value is an integer
        document = prepareCSSDocument();
        document.setDocId(docId);
        document.setIncremental(true);
        document.setVersion(DocumentConstants.NEW_VERSION);
        document = docClient.storeDocument(document);
      }
      else // new CSS
      {
        //Store new document
        document = prepareCSSDocument();        
        document.setDocTypeId("Web");
        document.setTitle("CSS node " + nodeId);
        document.setIncremental(false);
        AccessControl accessControl;
        accessControl = new AccessControl();
        accessControl.setAction(DictionaryConstants.READ_ACTION);
        accessControl.setRoleId(SecurityConstants.EVERYONE_ROLE);
        document.getAccessControl().add(accessControl);
        accessControl = new AccessControl();
        accessControl.setAction(DictionaryConstants.WRITE_ACTION);
        accessControl.setRoleId(CMSConstants.MENU_ADMIN_ROLE);
        document.getAccessControl().add(accessControl);
        document = docClient.storeDocument(document);        
        docId = document.getDocId();
        //Add nodeCSS property
        Node auxNode = cNode.getNode();
        Property nodeCssProperty = new Property();
        nodeCssProperty.setName(UserSessionBean.NODE_CSS);
        nodeCssProperty.getValue().add(docId);
        auxNode.getProperty().add(nodeCssProperty);
        getCMSManagerPort().storeNode(auxNode);
        //Update panels and workspace
        resetTree();
        resetSyncPanel();
        resetPropertiesPanel();
        updateCache(workspaceId);        
      }
    }
    return document;
  }
  
  private Document prepareCSSDocument()
  {
    Document document = new Document();
    MemoryDataSource ds = new MemoryDataSource(cssText.getBytes(),
      "data", "text/css");
    Content content = new Content();
    content.setContentType("text/css");
    content.setData(new DataHandler(ds));
    document.setContent(content);
    return document;
  }
  
  //Returns the property value indexes, if the property matches 
  //the search criteria
  private List<Integer> matchProperty(Property property, 
    List<Property> searchPropertyList)
  {
    Set<Integer> auxSet = new HashSet();
    for (Property searchProperty : searchPropertyList)
    {
      if (matchString(property.getName(), searchProperty.getName()))
      {
        for (String searchPropertyValue : searchProperty.getValue())
        {
          int idx = 0;
          for (String propertyValue : property.getValue())
          {
            if (matchString(propertyValue, searchPropertyValue)) 
              auxSet.add(idx);
            if (property.getValue().size() == auxSet.size())
            {
              return new ArrayList(auxSet);
            }
            idx++;
          }
        }
      }      
    }
    return new ArrayList(auxSet);
  }
  
  private boolean matchString(String text, String pattern)
  {
    for (String specialChar : SPECIAL_PROPERTY_CHARS)
    {
      pattern = pattern.replace(specialChar, "\\" + specialChar);
    }
    pattern = pattern.replace("%", "(.*)");
    return text.toLowerCase().matches(pattern.toLowerCase());
  }
    
  private void loadAnnotatedPropertyNameSet()
  {
    annotatedPropertyNameSet = new HashSet();
    String action = getAction();
    if (action != null)
    {
      Class c = getClassFromAction(action);
      if (c != null)
      {
        CMSManagedBeanIntrospector introspector =
          new CMSManagedBeanIntrospector();
        try
        {
          Map<String, CMSProperty> cmsPropertyMap =
            introspector.getProperties(c);
          for (String propertyName : cmsPropertyMap.keySet())
          {
            if (!COMMON_PROPERTY_NAMES.contains(propertyName))
            {
              annotatedPropertyNameSet.add(propertyName);
            }
          }
        }
        catch (Exception ex) { }
      }
    }
  }

  private List<NodeChange> getNodeChangeList(String fromWorkspaceId,
    String toWorkspaceId, String nodeId) throws Exception
  {
    List<NodeChange> nodeChangeList = CMSConfigBean.getPort().
      findNodeChanges(fromWorkspaceId, toWorkspaceId, nodeId);
    return nodeChangeList;
  }

  private String getMenuItemLabel(MenuItemCursor item)
  {
    return getMenuItemLabel(item, true);
  }
  
  private String getMenuItemLabel(MenuItemCursor item, 
    boolean includeChangeUserId)
  {
    String label = item.getMid();
    String value = (String)item.getDirectProperties().get("description");
    if (value != null)
    {
      label = value;
    }
    else
    {
      value = (String)item.getDirectProperties().get("label");
      if (value != null)
      {
        label = value;
      }
    }
    if (includeChangeUserId)
    {      
      if (isSyncTabSelected() && getToWorkspaceId() != null)
      {
        NodeChange nodeChange = getNodeChangeMap().get(item.getMid());
        if (nodeChange != null)
        {
          String changeUserId = nodeChange.getNode().getChangeUserId();
          if (changeUserId != null)
          {
            label += " (" + changeUserId + ")";
          }
        }
      }
    }
    return label;
  }

  private boolean isShowMenuItemIndex(MenuItemCursor item)
  {
    String value = (String)item.getProperty("showIndex");
    return (value == null ? false : value.equals("true"));
  }

  private int getMenuItemIndex(MenuItemCursor item)
  {
    String workspaceId = UserSessionBean.getCurrentInstance().
      getWorkspaceId();
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    CNode cNode = cmsCache.getWorkspace(workspaceId).getNode(item.getMid());
    return cNode.getNode().getIndex();
  }    
  
  private List<String> getMenuItemPath(String mid)
  {
    try
    {
      CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
      CNode cNode = cmsCache.getWorkspace(currentWorkspaceId).getNode(mid);
      return Arrays.asList(cNode.getNodeIdPath());
    }
    catch (Exception ex)
    {
      return new ArrayList();
    }
  }
  
  private int getMenuItemIdx(String mid)
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    CNode cNode = cmsCache.getWorkspace(currentWorkspaceId).getNode(mid);
    return cNode.getIndexOfChild();
  }
  
  private String getRootNodeId(String mid)
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    CNode cNode = cmsCache.getWorkspace(currentWorkspaceId).getNode(mid);
    return cNode.getRoot().getNodeId();
  }
  
  private String getNodeCSS(String workspaceId, String nodeId)
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();    
    CNode cNode = cmsCache.getWorkspace(workspaceId).getNode(nodeId);
    return cNode.getSinglePropertyValue(UserSessionBean.NODE_CSS);    
  }

  private MenuItemCursor getCursor()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
  }

  private void updateCache()
  {
    configHelper.updateCache();
  }
  
  private void updateCache(String workspaceId)
  {
    configHelper.updateCache(workspaceId);
  }

  private List<Property> clean(List<Property> properties,
    boolean excludeMandatoryValue, boolean allowRepetitions) throws Exception
  {
    Set<String> propertyNameSet = new HashSet();
    List<Property> finalPropertyList = new ArrayList();
    for (Property property : properties)
    {
      String propertyName = property.getName();
      if (propertyName != null && propertyName.trim().length() > 0)
      {
        Property newProperty = new Property();
        newProperty.setName(property.getName());
        for (String value : property.getValue())
        {
          if (value != null && value.trim().length() > 0)
          {
            if (excludeMandatoryValue)
            {
              if (!value.equals(getMandatoryPropertyLabel()))
              {
                newProperty.getValue().add(value);
              }
            }
            else
            {
              newProperty.getValue().add(value);
            }
          }
        }
        if (newProperty.getValue().size() > 0)
        {
          // add properties with one or more values only
          if (!allowRepetitions && 
            propertyNameSet.contains(newProperty.getName()))
          {
            throw new Exception("PROPERTIES_MUST_BE_UNIQUE");
          }
          else
          {
            propertyNameSet.add(newProperty.getName());
          }
          finalPropertyList.add(newProperty);
        }
      }
    }
    return finalPropertyList;
  }

  private CNode getSelectedCNode()
  {
    return configHelper.getSelectedCNode();
  }

  private String loadCssText()
  {
    try
    {
      MenuItemCursor cursor = getCursor();
      String docId = (String)cursor.getDirectProperties().
        get(UserSessionBean.NODE_CSS);
      if (docId != null) //css found
      {
        if (docId.indexOf("?") > 0)
          docId = docId.substring(0, docId.indexOf("?"));      
        CachedDocumentManagerClient docClient = DocumentConfigBean.getClient();
        Document document = docClient.loadDocument(docId, 0);
        DataHandler dh = DocumentUtils.getContentData(document);
        long size = document.getContent().getSize();
        int iSize = (int)size;
        InputStream is = dh.getInputStream();
        byte[] byteArray = new byte[iSize];
        is.read(byteArray);
        return new String(byteArray);
      }      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  private Node cloneCNode(CNode cNode, boolean preserveNodeId)
  {
    Node result = new Node();
    Node node = cNode.getNode();
    if (preserveNodeId)
    {
      result.setNodeId(node.getNodeId());
    }
    result.setName(node.getName());
    result.setParentNodeId(node.getParentNodeId());
    result.setIndex(node.getIndex());
    result.setWorkspaceId(node.getWorkspaceId());
    result.setChangeDateTime(node.getChangeDateTime());
    result.setChangeUserId(node.getChangeUserId());    
    result.getProperty().addAll(node.getProperty());
    return result;
  }

  private Node cloneCNodeAndDescendants(CNode cNode) throws Exception
  {
    return cloneCNodeAndDescendants(cNode, null);
  }

  private Node cloneCNodeAndDescendants(CNode cNode, String newParentNodeId)
    throws Exception
  {        
    Node newRootNode = cloneCNode(cNode, false);
    newRootNode.setParentNodeId(newParentNodeId);
    newRootNode = CMSConfigBean.getPort().storeNode(newRootNode);
    for (CNode childCNode : cNode.getChildren())
    {
      cloneCNodeAndDescendants(childCNode, newRootNode.getNodeId());
    }
    return newRootNode;
  }

  private boolean isCutNodeDescendant(MenuItemCursor item)
  {
    if (item == null || item.isNull()) return false;
    String nodeId = item.getMid();
    if (nodeId.equals(cutNodeId))
    {
      return true;
    }
    else
    {
      return isCutNodeDescendant(item.getParent());
    }
  }

  private boolean isCopyNodeDescendant(MenuItemCursor item)
  {
    if (item == null || item.isNull()) return false;
    String nodeId = item.getMid();
    if (nodeId.equals(copyNodeId))
    {
      return true;
    }
    else
    {
      if (copySingleNode)
      {
        return false;
      }
      else
      {
        return isCopyNodeDescendant(item.getParent());
      }
    }
  }

  private boolean userCanDoOperation(Set roleSet)
  {
    return (roleSet.isEmpty() ||
      UserSessionBean.getCurrentInstance().isCmsAdministrator() ||
      UserSessionBean.getCurrentInstance().isUserInRole(roleSet));
  }

  private void analyzeWorkspaceChange()
  {
    if (getFacesContext().getRenderResponse() && workspaceHasChanged())
    {
      rootNodeId = null;
      cutNodeId = null;
      copyNodeId = null;
      toWorkspaceId = null;
      toWorkspaceItems = null;
      resetRootSelectionPanel();
      resetTree();
      resetTopPanel();      
      resetPropertiesPanel();
      resetCssPanel();
      resetSyncPanel();
      resetSearchPanel();
      inputSearch = null;
      currentWorkspaceId = 
        UserSessionBean.getCurrentInstance().getWorkspaceId();
    }
  }

  private boolean workspaceHasChanged()
  {
    return (!UserSessionBean.getCurrentInstance().getWorkspaceId().
      equals(currentWorkspaceId));
  }
  
  private List<NodeChange> sortNodeChangeList(List<NodeChange> nodeChangeList)
  {
    if (nodeChangeList == null || nodeChangeList.size() <= 1)
      return nodeChangeList;
    List<NodeChange> result = new ArrayList();
    List<NodeChange> lessList = new ArrayList();
    List<NodeChange> greaterList = new ArrayList();
    NodeChange pivot = nodeChangeList.remove(0);
    for (NodeChange auxNodeChange : nodeChangeList)
    {
      Integer auxNodeId = new Integer(auxNodeChange.getNode().getNodeId());
      Integer pivotNodeId = new Integer(pivot.getNode().getNodeId());
      if (auxNodeId <= pivotNodeId)
      {
        lessList.add(auxNodeChange);
      }
      else
      {
        greaterList.add(auxNodeChange);
      }
    }
    result.addAll(sortNodeChangeList(lessList));
    result.add(pivot);
    result.addAll(sortNodeChangeList(greaterList));
    return result;
  }

  private void loadNodeChangeItemList()
  {
    try
    {
      nodeChangeItemList = new ArrayList();      
      String selectedNodeId =
        UserSessionBean.getCurrentInstance().getSelectedMid();
      List<NodeChangeItem> auxFullList = getFullNodeChangeItemList();      
      for (NodeChangeItem item : auxFullList)
      {
        if (item.getPath().contains("/" + selectedNodeId + "/"))
        {
          nodeChangeItemList.add(item);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void loadFullNodeChangeItemList(String fromWorkspaceId,
    String toWorkspaceId)
  {
    try
    {
      fullNodeChangeItemList = new ArrayList();
      nodeChangeMap = new HashMap();
      if (toWorkspaceId != null)
      {
        CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
        List<NodeChange> nodeChangeList = getNodeChangeList(fromWorkspaceId,
          toWorkspaceId, getRootNodeId());
        List<String> nodeIdList = new ArrayList();
        for (NodeChange nodeChange : nodeChangeList)
        {
          nodeIdList.add(nodeChange.getNode().getNodeId());
        }
        Map<String, CNode> fromCNodeMap =
          cmsCache.getWorkspace(fromWorkspaceId).getNodes(nodeIdList);
        Map<String, CNode> toCNodeMap =
          cmsCache.getWorkspace(toWorkspaceId).getNodes(nodeIdList);

        nodeChangeList = sortNodeChangeList(nodeChangeList);
        for (NodeChange nodeChange : nodeChangeList)
        {
          NodeChangeItem row = new NodeChangeItem(nodeChange);          
          String nodeId = nodeChange.getNode().getNodeId();
          String changedPropertiesText = null;
          if (nodeChange.getType().equals(NodeChangeType.UPDATED))
          {
            CNode fromCNode = fromCNodeMap.get(nodeId);
            CNode toCNode = toCNodeMap.get(nodeId);
            if (!fromCNode.hasTheSameProperties(toCNode))
            {
              List<String> properties = 
                fromCNode.getDifferentProperties(toCNode);
              if (properties != null && !properties.isEmpty())
              {
                changedPropertiesText = 
                  TextUtils.collectionToString(properties).replace(",", ", ");              
              }
              else
              {
                changedPropertiesText = "";
              }
              row.setChangedPropertiesText(changedPropertiesText);
              nodeChangeMap.put(nodeId, nodeChange);              
            }
            else
            {
              if (fromCNode.hasTheSameName(toCNode))
              {
                if (fromCNode.hasTheSameLocation(toCNode))
                {
                  nodeChange.setType(NodeChangeType.FALSE_UPDATE);
                }
                else
                {
                  nodeChange.setType(NodeChangeType.MOVED);
                }
              }
              else
              {
                nodeChange.setType(NodeChangeType.NAME_CHANGED);
                nodeChangeMap.put(nodeId, nodeChange);
              }
            }
            row.setPath(getNodeIdStringPath(fromCNode.getNodeIdPath()));
          }
          else if (nodeChange.getType().equals(NodeChangeType.CREATED))
          {
            nodeChangeMap.put(nodeId, nodeChange);
            CNode fromCNode = fromCNodeMap.get(nodeId);            
            row.setPath(getNodeIdStringPath(fromCNode.getNodeIdPath()));
          }
          else if (nodeChange.getType().equals(NodeChangeType.REMOVED))
          {
            CNode toCNode = toCNodeMap.get(nodeId);
            String parentNodeId = toCNode.getParentNodeId();
            if (parentNodeId != null && 
              !nodeChangeMap.containsKey(parentNodeId))
            {              
              nodeChangeMap.put(parentNodeId, null);
            }
            row.setPath(getNodeIdStringPath(toCNode.getNodeIdPath()));
          }           
          fullNodeChangeItemList.add(row);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  private void swapNode(CNode topCNode) throws Exception
  {
    if (topCNode != null)
    {
      CNode bottomCNode = topCNode.getNextSibling();
      if (bottomCNode != null)
      {
        int index1 = topCNode.getNode().getIndex();
        int index2 = bottomCNode.getNode().getIndex();
        if (index1 == index2)
        {
          topCNode.getNode().setIndex(index1 + 1);
          CMSConfigBean.getPort().storeNode(topCNode.getNode());
        }
        else
        {
          bottomCNode.getNode().setIndex(index1);
          CMSConfigBean.getPort().storeNode(bottomCNode.getNode());
        }
        updateCache();
        resetTree();        
        resetTopPanel();
        resetSyncPanel();
      }
    }
  }

  private List<Property> getPropertyList()
  {
    analyzeWorkspaceChange();
    try
    {
      if (propertyList == null)
      {
        loadPropertyList();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return propertyList;
  }

  private ResourceBundle getCommonTipsBundle()
  {
    String bundlePath = "org.santfeliu.web.resources.HelpBundle";
    return getBundle(bundlePath);
  }

  private ResourceBundle getNodeTipsBundle()
  {
    String bundlePath = getNodeTipsBundlePath();
    return getBundle(bundlePath);
  }

  private Set<String> getAnnotatedPropertyNameSet()
  {
    analyzeWorkspaceChange();
    if (annotatedPropertyNameSet == null)
    {
      loadAnnotatedPropertyNameSet();
    }
    return annotatedPropertyNameSet;
  }

  private void loadPropertyList()
  {
    String workspaceId = UserSessionBean.getCurrentInstance().
      getWorkspaceId();
    String nodeId = getCursor().getMid();
    if (nodeId != null)
    {
      CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
      CNode cNode = cmsCache.getWorkspace(workspaceId).getNode(nodeId);
      if (cNode != null)
      {
        propertyList = cNode.getProperties(true);
      }
    }
  }

  private void loadToWorkspaceItems()
  {
    try
    {
      CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
      CWorkspace cWorkspace = cmsCache.getWorkspace(currentWorkspaceId);
      String familyWorkspaceId = cWorkspace.getFamilyWorkspaceId();
      CMSManagerPort port = getCMSManagerPort();
      WorkspaceFilter filter = new WorkspaceFilter();
      List<Workspace> workspaceList = port.findWorkspaces(filter);
      List<Workspace> finalWorkspaceList = new ArrayList();
      for (Workspace w : workspaceList)
      {
        String auxFamilyWorkspaceId =
          cmsCache.getWorkspace(w.getWorkspaceId()).getFamilyWorkspaceId();
        if ((!w.getWorkspaceId().equals(currentWorkspaceId)) &&
          (familyWorkspaceId.equals(auxFamilyWorkspaceId)))
        {
          finalWorkspaceList.add(w);
        }
      }
      toWorkspaceItems = new SelectItem[finalWorkspaceList.size()];
      if (finalWorkspaceList.size() > 0)
      {
        int i = 0;
        for (Workspace w : finalWorkspaceList)
        {
          SelectItem item = new SelectItem();
          item.setLabel(w.getWorkspaceId() + " (" + w.getName() + ")");
          item.setValue(w.getWorkspaceId());
          toWorkspaceItems[i++] = item;
        }
      }
    }
    catch (Exception ex)
    {
      toWorkspaceItems = new SelectItem[0];
    }
  }

  private void loadBeanNames()
  {
    try
    {
      CMSManagedBeanIntrospector introspector =
        new CMSManagedBeanIntrospector();
      List<String> beanNameList = introspector.getBeanNames();
      Collections.sort(beanNameList);
      beanNames = new SelectItem[beanNameList.size() + 1];      
      SelectItem undefinedItem = new SelectItem();
      undefinedItem.setLabel(getUndefinedLabel());
      undefinedItem.setValue(getUndefinedLabel());
      beanNames[0] = undefinedItem;
      int i = 1;
      for (String beanName : beanNameList)
      {
        SelectItem item = new SelectItem();
        item.setLabel(beanName);
        item.setValue(beanName);
        beanNames[i++] = item;
      }
    }
    catch (Exception ex)
    {
      beanNames = new SelectItem[0];
    }
  }

  private void loadBeanActions()
  {
    try
    {
      List<String> beanActionList = new ArrayList();
      String selectedBeanName = getSelectedBeanName();
      if (selectedBeanName != null &&
        !selectedBeanName.equals(getUndefinedLabel()))
      {
        Class c = null;
        Object obj = getBean(selectedBeanName);
        if (obj != null) c = obj.getClass();
        CMSManagedBeanIntrospector introspector =
          new CMSManagedBeanIntrospector();
        if (c != null && introspector.getBeanClasses().contains(c))
        {
          Map<String, CMSAction> actionMap = introspector.getActions(c);
          beanActionList.addAll(actionMap.keySet());
          Collections.sort(beanActionList);
        }
      }
      beanActions = new SelectItem[beanActionList.size()];
      int i = 0;
      for (String beanAction : beanActionList)
      {
        SelectItem item = new SelectItem();
        item.setLabel(beanAction);
        item.setValue(beanAction);
        beanActions[i++] = item;
      }
    }
    catch (Exception ex)
    {
      beanActions = new SelectItem[0];
    }
  }

  private boolean nodeHasInvisibleDescendants(CNode cNode)
  {
    if (!nodeIsVisible(cNode.getNodeId())) return true;
    List<CNode> children = cNode.getChildren();
    for (CNode childCNode : children)
    {
      if (nodeHasInvisibleDescendants(childCNode)) return true;
    }
    return false;
  }

  private boolean nodeIsVisible(String nodeId)
  {
    return configHelper.nodeIsVisible(nodeId);
  }
  
  private List<Node> filterVisibleNodes(List<Node> nodeList)
  {
    List<Node> result = new ArrayList();
    Map<String, Node> nodeMap = new HashMap();
    List<String> nodeIdList = new ArrayList();
    for (Node node : nodeList)
    {
      nodeIdList.add(node.getNodeId());
      nodeMap.put(node.getNodeId(), node);
    }
    List<MenuItemCursor> visibleMenuItemList =
      getMenuModel().getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor cursor : visibleMenuItemList)
    {
      result.add(nodeMap.get(cursor.getMid()));
    }
    return result;
  }

  private String getNewVisibleNodeId()
  {
    return configHelper.getNewVisibleNodeId();
  }

  private void resetRootSelectionPanel(String newRootNodeId)
  {
    rootNodeList = null;
    if (newRootNodeId != null) rootNodeId = newRootNodeId;
  }
  
  private void resetSearchPanel()
  {
    resetSearchPanel(true);
  }
  
  private void resetSearchPanel(boolean removeProperties)
  {
    nodeSearchMap = null;
    nodeSearchItemList = null;
    searchDone = false;
    searchFirstRowIndex = null;
    if (removeProperties) searchPropertyList = null;
  }

  private void resetSyncPanel(boolean updateSyncCache)
  {
    nodeChangeItemList = null;
    selectedNodeChangeItemList = null;
    syncFirstRowIndex = null;
    if (updateSyncCache)
    {
      nodeChangeMap = null;
      fullNodeChangeItemList = null;
    }
  }   
  
  private MenuModel getMenuModel()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel();
  }

  private CMSManagerPort getCMSManagerPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CMSManagerService.class);
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return endpoint.getPort(CMSManagerPort.class,
      userSessionBean.getUsername(),
      userSessionBean.getPassword());
  }

  private List<Property> getAnnotatedCommonProperties()
  {
    List<Property> result = new ArrayList();
    for (String propertyName : COMMON_PROPERTY_NAMES)
    {
      if (!isPropertyInList(getUserPropertyList(), propertyName))
      {
        Property p = new Property();
        p.setName(propertyName);
        p.getValue().add("");
        result.add(p);
      }
    }
    return result;
  }

  private List<Property> getAnnotatedBeanProperties(String action)
    throws Exception
  {
    List<Property> result = new ArrayList();
    Class c = getClassFromAction(action);
    CMSManagedBeanIntrospector introspector =
      new CMSManagedBeanIntrospector();
    if (c != null && introspector.getBeanClasses().contains(c))
    {
      String[] split = action.split("\\.");
      String actionMethod = split[1];
      actionMethod = actionMethod.substring(0, actionMethod.length() - 1);
      Map<String, CMSAction> actionMap = introspector.getActions(c);
      if (actionMap.keySet().contains(actionMethod))
      {
        Map<String, CMSProperty> cmsPropertyMap = 
          introspector.getProperties(c);        
        for (String propertyName : cmsPropertyMap.keySet())
        {
          if (!COMMON_PROPERTY_NAMES.contains(propertyName))
          {
            if (!isPropertyInList(getUserPropertyList(), propertyName))
            {
              Property newProperty = new Property();
              newProperty.setName(propertyName);
              CMSProperty cmsProperty = cmsPropertyMap.get(propertyName);
              if (cmsProperty.mandatory())
              {
                newProperty.getValue().add(getMandatoryPropertyLabel());
              }
              else
              {
                newProperty.getValue().add("");
              }
              result.add(newProperty);
            }
          }
        }
      }
    }
    return result;
  }

  private boolean isPropertyInList(List<Property> propertyList,
    String propertyName)
  {
    return getPropertyInList(propertyList, propertyName) != null;
  }

  private Property getPropertyInList(List<Property> propertyList,
    String propertyName)
  {
    for (Property property : propertyList)
    {
      if (property.getName() != null &&
        property.getName().equals(propertyName)) return property;
    }
    return null;
  }

  private String getSelectedBeanName()
  {
    if (getBeanNameProperty() != null)
    {
      return getBeanNameProperty().getValue().get(0);
    }
    return null;
  }

  private String getSelectedBeanAction()
  {
    if (getBeanActionProperty() != null)
    {
      return getBeanActionProperty().getValue().get(0);
    }
    return null;
  }

  private void loadNodeTipsBundlePath()
  {
    String action = getAction();
    if (action != null)
    {
      Class c = getClassFromAction(action);
      if (c != null)
      {
        String className = c.getName();
        nodeTipsBundlePath =
          className.substring(0, className.lastIndexOf(".")) +
          ".resources.HelpBundle";        
      }
    }
  }

  private String getAction()
  {
    String action = null;
    String selectedBeanName = getSelectedBeanName();
    String selectedBeanAction = getSelectedBeanAction();
    if (selectedBeanName != null && !selectedBeanName.isEmpty() &&
      !selectedBeanName.equals(getUndefinedLabel()) &&
      selectedBeanAction != null && !selectedBeanAction.isEmpty() &&
      !selectedBeanAction.equals(getUndefinedLabel())) //selectors
    {
      return "#{" + selectedBeanName + "." + selectedBeanAction + "}";
    }
    else //action property
    {
      Property actionProperty =
        getPropertyInList(getUserPropertyList(), ACTION_PROPERTY);
      if (actionProperty != null)
      {
        action = actionProperty.getValue().get(0).trim();
        if (action != null && action.contains(".") &&
          action.startsWith("#{") && action.endsWith("}"))
        {
          return action;
        }
      }
    }
    return null;
  }

  private Class getClassFromAction(String action)
  {
    Class c = null;
    action = action.trim();
    String[] split = action.split("\\.");
    String simpleBeanName = split[0].substring(2);
    Object obj = getBean(simpleBeanName);
    if (obj != null) c = obj.getClass();
    return c;
  }

  private ResourceBundle getBundle(String bundlePath)
  {
    try
    {
      return ResourceBundle.getBundle(bundlePath, getLocale());
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  private String getBundleValue(ResourceBundle bundle, String key)
  {    
    try
    {      
      return bundle.getString(key);
    }
    catch (Exception ex) 
    {
    }
    return null;
  }
  
  private String getBundleValue(ResourceBundle bundle, String key, 
    String paramValue)
  {
    try
    {
      String pattern = getBundleValue(bundle, key);
      if (pattern != null)
      {
        return MessageFormat.format(pattern, paramValue);
      }
    }
    catch (Exception ex) 
    {
    }
    return null;
  }

  private static Set<String> getCommonPropertyNames()
  {
    Set<String> result = new HashSet();
    CMSManagedBeanIntrospector introspector =
      new CMSManagedBeanIntrospector();
    try
    {
      Class c = Class.forName("org.santfeliu.web.WebBean");
      Map<String, CMSProperty> cmsPropertyMap = introspector.getProperties(c);
      result.addAll(cmsPropertyMap.keySet());      
    }
    catch (Exception ex)
    {
    }
    return result;
  }

  private String getNodeIdStringPath(String[] nodeIdPath)
  {
    return ("/" + StringUtils.join(nodeIdPath, "/") + "/");
  }
  
  //INNER CLASSES
  
  //Represents a property value
  public class PropertyValueWrapper implements Serializable
  {
    private int index;

    public PropertyValueWrapper(int index)
    {
      this.index = index;
    }

    public String getValue()
    {
      Property property = (Property)NodeEditBean.this.getValue("#{property}");
      return property.getValue().get(index);
    }

    public void setValue(String value)
    {
      Property property = (Property)NodeEditBean.this.getValue("#{property}");
      property.getValue().set(index, value);
    }
  }  
  
  //Represents a sync table row
  public class NodeChangeItem implements Serializable
  {
    private String id;
    private NodeChange nodeChange;
    private String changedPropertiesText;
    private String label = null;
    private String path;    

    public NodeChangeItem(NodeChange nodeChange)
    {
      this.id = nodeChange.getNode().getNodeId() + ";" + 
        System.currentTimeMillis();
      this.nodeChange = nodeChange;
    }

    public String getId() 
    {
      return id;
    }

    public NodeChange getNodeChange()
    {
      return nodeChange;
    }

    public void setNodeChange(NodeChange nodeChange)
    {
      this.nodeChange = nodeChange;
    }

    public String getChangedPropertiesText()
    {
      return changedPropertiesText;
    }

    public void setChangedPropertiesText(String changedPropertiesText)
    {
      this.changedPropertiesText = changedPropertiesText;
    }

    public String getPath() 
    {
      return path;
    }

    public void setPath(String path) 
    {
      this.path = path;
    }

    public String getNodeId()
    {
      return nodeChange.getNode().getNodeId();
    }

    public String getType()
    {
      return nodeChange.getType().value();
    }

    public String getLabel()
    {
      if (label == null)
      {
        Node node = getNodeChange().getNode();
        label = getNodeLabel(node);
      }
      return label;
    }
  }  
  
  //Represents a found node
  public class NodeSearchItem implements Serializable
  {
    private final Node node;
    private String label = null;
    private List<String> nodeIdPath = null;    
    private Map<String, List<Integer>> propertyMap = new HashMap();
    
    public NodeSearchItem(Node node)
    {
      this.node = node;
    }
    
    public String getNodeId()
    {
      return node.getNodeId();
    }

    public String getLabel()
    {
      if (label == null)
      {
        label = getNodeLabel(node);
      }
      return label;
    }
    
    public List<String> getNodeIdPath()
    {
      if (nodeIdPath == null)
      {
        nodeIdPath = getMenuItemPath(getNodeId());
      }
      return nodeIdPath;
    }
    
    public String getNodeIdPathString()
    {
      return StringUtils.join(getNodeIdPath().toArray(), " / ");
    }
    
    public void addProperty(String propertyName, List<Integer> valueIndexList)
    {
      if (!propertyMap.containsKey(propertyName))
      {
        propertyMap.put(propertyName, new ArrayList());
      }
      propertyMap.get(propertyName).addAll(valueIndexList);
    }

    public boolean isMarkPropertyName(String propertyName)
    {
      return propertyMap.containsKey(propertyName);
    }
    
    public boolean isMarkPropertyValue(String propertyName, Integer valueIndex)
    {
      if (propertyMap.containsKey(propertyName))
      {
        return propertyMap.get(propertyName).contains(valueIndex);
      }
      return false;
    }
    
    public int compareTo(NodeSearchItem other)
    {
      int thisRootNodeId = Integer.parseInt(getRootNodeId(this.getNodeId()));
      int otherRootNodeId = Integer.parseInt(getRootNodeId(other.getNodeId()));
      if (thisRootNodeId != otherRootNodeId)
      {
        return thisRootNodeId - otherRootNodeId;
      }
      else
      {
        List<Integer> thisIdxList = this.getNodeIdxList();
        List<Integer> otherIdxList = other.getNodeIdxList();
        for (int i = 1; i < Math.max(thisIdxList.size(), otherIdxList.size()); 
          i++)
        {
          if (i >= thisIdxList.size()) return -1;
          else if (i >= otherIdxList.size()) return 1;
          else if (!thisIdxList.get(i).equals(otherIdxList.get(i)))
            return (thisIdxList.get(i) - otherIdxList.get(i));
        }
        return 0;
      }
    }
        
    private List<Integer> getNodeIdxList()
    {
      List<Integer> auxList = new ArrayList();
      for (String auxNodeId : getNodeIdPath())
      {
        auxList.add(getMenuItemIdx(auxNodeId));
      }
      return auxList;
    }
    
  }

  public class NodeInfo implements Serializable
  {
    private final String nodeId;

    public NodeInfo(String nodeId)
    {
      this.nodeId = nodeId;
    }

    public String getNodeId() 
    {
      return nodeId;
    }
    
    public String getBoxText()
    {
      MenuItemCursor cursor = getMenuItem();
      StringBuilder sb = new StringBuilder();
      sb.append(cursor.getMid());
      if (isShowMenuItemIndex(cursor))
      {
        sb.append(";");
        sb.append(getMenuItemIndex(cursor));
      }
      return sb.toString();
    }

    public String getBoxStyleClass()
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      if (isSyncTabSelected() && getToWorkspaceId() != null)
      {
        MenuItemCursor item = getMenuItem();
        if (getNodeChangeMap().containsKey(item.getMid()))
        {
          NodeChange nodeChange = getNodeChangeMap().get(item.getMid());
          if (nodeChange == null) //node with deleted children
          {
            return "midBox deleted";
          }
          else
          {
            if (nodeChange.getType() != NodeChangeType.MOVED)
            {
              String changeUserId = nodeChange.getNode().getChangeUserId();
              String currentUserId = userSessionBean.getUserId();
              if (currentUserId.equals(changeUserId))
              {
                return "midBox updatedByMe";
              }
              else
              {
                return "midBox updatedByOther";
              }
            }
          }
        }
      }
      return "midBox";
    }

    public String getLabel()
    {      
      MenuItemCursor item = getMenuItem();      
      return getMenuItemLabel(item);
    }

    public boolean isSelected()
    {
      return getNodeId().equals(
        UserSessionBean.getCurrentInstance().getSelectedMid());
    }
        
    public String getStyleClass()
    {
      StringBuilder sb = new StringBuilder();
      MenuItemCursor item = getMenuItem();
      if (isSelected())
      {
        sb.append("selected ");        
      }
      if (isFoundNode(item))
      {
        sb.append("found ");
      }
      if (isCutNodeDescendant(item))
      {
        sb.append("cut");
      }
      else if (isCopyNodeDescendant(item))
      {
        sb.append("copy");
      }
      return sb.toString().trim();
    }

    public boolean isCustomCSS()
    {
      MenuItemCursor cursor = getMenuItem();
      if (cursor == null) cursor = getCursor();
      String docId = (String)cursor.getDirectProperties().
        get(UserSessionBean.NODE_CSS);
      return docId != null;
    }
    
    private MenuItemCursor getMenuItem()
    {
      return getMenuModel().getMenuItem(nodeId);
    }    
  }  

}
