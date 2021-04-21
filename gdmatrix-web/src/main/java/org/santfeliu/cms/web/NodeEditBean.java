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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.activation.DataHandler;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.security.AccessControl;
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
import org.matrix.security.SecurityConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
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
 * @author unknown
 */
public class NodeEditBean extends FacesBean implements Serializable
{
  private static final String ACTION_PROPERTY = "action";
  private static final String BEAN_NAME_PROPERTY = "beanName";
  private static final String BEAN_ACTION_PROPERTY = "beanAction";
  private static final Set<String> COMMON_PROPERTY_NAMES =
    getCommonPropertyNames();
  private static final Integer MAX_SEARCH_ITEMS = 100;
  private static final String[] SPECIAL_PROPERTY_CHARS = 
    {".", "[", "]", "{", "}"};
  
  //Tree edit & selection
  private String currentWorkspaceId;
  private Set expandedMenuItems;
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
  private boolean searchDone = false;  

  //Css
  private String cssText;

  //Sync
  private Map<String, NodeChange> nodePaintMap;
  private List<NodeChangeRow> syncNodeChangeList;
  private String toWorkspaceId;
  private SelectItem[] toWorkspaceItems;
  private String toWorkspaceIdInput;

  public NodeEditBean()
  {
  }

  //********** setters/getters *********

  public List<NodeChangeRow> getSyncNodeChangeList()
  {
    analyzeWorkspaceChange();
    if (syncNodeChangeList == null)
    {
      loadSyncNodeChangeList();
    }
    return syncNodeChangeList;
  }

  public void setSyncNodeChangeList(List<NodeChangeRow> syncNodeChangeList)
  {
    this.syncNodeChangeList = syncNodeChangeList;
  }

  public String getToWorkspaceId()
  {
    return toWorkspaceId;
  }

  public void setToWorkspaceId(String toWorkspaceId)
  {
    this.toWorkspaceId = toWorkspaceId;
  }

  public String getToWorkspaceIdInput()
  {
    return toWorkspaceIdInput;
  }

  public void setToWorkspaceIdInput(String toWorkspaceIdInput)
  {
    this.toWorkspaceIdInput = toWorkspaceIdInput;
  }

  public String getUndefinedLabel()
  {
    if (undefinedLabel == null)
    {
      String cmsBundlePath = "org.santfeliu.cms.web.resources.CMSBundle";
      ResourceBundle bundle = getBundle(cmsBundlePath);
      undefinedLabel = "<" + getBundleValue(bundle, "undefined") + ">";
    }
    return undefinedLabel;
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

  public String switchToWorkspace()
  {
    toWorkspaceId = toWorkspaceIdInput;
    loadSyncNodeChangeList();
    return null;
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

  public Map<String, NodeChange> getNodePaintMap()
  {
    analyzeWorkspaceChange();
    if (nodePaintMap == null)
    {
      try
      {
        CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
        List<NodeChange> nodeChangeList = getNodeChangeList(currentWorkspaceId,
          getRefWorkspaceId(), getRootNodeId());
        List<String> nodeIdList = new ArrayList<String>();
        for (NodeChange nodeChange : nodeChangeList)
        {
          nodeIdList.add(nodeChange.getNode().getNodeId());
        }
        Map<String, CNode> currentCNodeMap =
          cmsCache.getWorkspace(currentWorkspaceId).getNodes(nodeIdList);
        Map<String, CNode> refCNodeMap =
          cmsCache.getWorkspace(getRefWorkspaceId()).getNodes(nodeIdList);
          
        nodeChangeList = sortNodeChangeList(nodeChangeList);
        nodePaintMap = new HashMap<String, NodeChange>();
        for (NodeChange nodeChange : nodeChangeList)
        {
          String nodeId = nodeChange.getNode().getNodeId();
          if (nodeChange.getType().equals(NodeChangeType.UPDATED))
          {
            CNode fromCNode = currentCNodeMap.get(nodeId);
            CNode toCNode = refCNodeMap.get(nodeId);
            if (!fromCNode.hasTheSameProperties(toCNode) || 
              !fromCNode.hasTheSameName(toCNode))
            {
              nodePaintMap.put(nodeId, nodeChange);
            }
          }
          else if (nodeChange.getType().equals(NodeChangeType.CREATED))
          {
            nodePaintMap.put(nodeId, nodeChange);
          }
          else if (nodeChange.getType().equals(NodeChangeType.REMOVED))
          {
            CNode toCNode = refCNodeMap.get(nodeId);
            String parentNodeId = toCNode.getParentNodeId();
            if (parentNodeId != null &&
              !nodePaintMap.containsKey(parentNodeId))
            {
              nodePaintMap.put(parentNodeId, null);
            }
          }          
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return nodePaintMap;
  }

  public void setNodePaintMap(Map<String, NodeChange> nodePaintMap)
  {
    this.nodePaintMap = nodePaintMap;
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
        rootNodeList = new ArrayList<Node>();
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
      getExpandedMenuItems().add(rootNodeId);
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
    return "";
  }

  public Set getExpandedMenuItems()
  {
    if (expandedMenuItems == null)
    {
      expandedMenuItems = createExpandedMenuItems();
    }
    return expandedMenuItems;
  }

  public void setExpandedMenuItems(Set expandedMenuItems)
  {
    this.expandedMenuItems = expandedMenuItems;
  }

  public boolean isSearchDone()
  {
    return searchDone;
  }

  public void setSearchDone(boolean searchDone)
  {
    this.searchDone = searchDone;
  }

  public String getCssText()
  {
    analyzeWorkspaceChange();
    try
    {
      if (cssText == null)
      {
        cssText = loadCSS();
      }
    }
    catch (Exception ex)
    {
      error(ex);
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
      userPropertyList = new ArrayList<Property>();
      userPropertyList.addAll(getPropertyList());
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
      searchPropertyList = new ArrayList<Property>();
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

  public boolean isCustomCSS()
  {
    MenuItemCursor cursor = (MenuItemCursor)getValue("#{item}");
    if (cursor == null) cursor = getCursor();
    String docId = (String)cursor.getDirectProperties().
      get(UserSessionBean.NODE_CSS);
    return docId != null;
  }

  public boolean isCustomRootCSS()
  {
    try
    {
      MenuItemCursor cursor = getCursor().getRoot();
      String docId = (String)cursor.getDirectProperties().
        get(UserSessionBean.NODE_CSS);
      return docId != null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }
  
  public boolean isSyncCssButtonRender()
  {
    try
    {
      String defaultWorkspaceId = 
        ApplicationBean.getCurrentInstance().getDefaultWorkspaceId();
      if (!defaultWorkspaceId.equals(currentWorkspaceId))
      {
        String fromCSSDocId = 
          getNodeCSS(currentWorkspaceId, getSelectedNodeId());
        if (fromCSSDocId != null)
        {
          String toCSSDocId = 
            getNodeCSS(defaultWorkspaceId, getSelectedNodeId());
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
  
  public boolean isSyncNodeChangeListEmpty()
  {
    return (getSyncNodeChangeList().isEmpty());
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

  public boolean isMenuItemSelected()
  {
    MenuItemCursor menuItemCursor = (MenuItemCursor)getValue("#{item}");
    if (menuItemCursor == null) return false;
    return menuItemCursor.getMid().equals(
      UserSessionBean.getCurrentInstance().getSelectedMid());
  }

  public boolean isRootSelected()
  {
    MenuItemCursor menuItemCursor = getCursor().getRoot();
    if (menuItemCursor == null) return false;
    return menuItemCursor.getMid().equals(
      UserSessionBean.getCurrentInstance().getSelectedMid());
  }

  public String getMenuItemStyleClass()
  {
    StringBuilder sb = new StringBuilder();
    MenuItemCursor item = (MenuItemCursor)getValue("#{item}");
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

  public String getMenuItemBoxStyleClass()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (getRefWorkspaceId() != null && userSessionBean.isSyncViewSelected())
    {
      MenuItemCursor item = (MenuItemCursor)getValue("#{item}");
      if (getNodePaintMap().containsKey(item.getMid()))
      {
        NodeChange nodeChange = getNodePaintMap().get(item.getMid());
        if (nodeChange == null) //node with deleted children
        {
          return "midBoxDeleted";
        }
        else
        {
          if (nodeChange.getType() != NodeChangeType.MOVED)
          {
            String changeUserId = nodeChange.getNode().getChangeUserId();
            String currentUserId = userSessionBean.getUserId();
            if (currentUserId.equals(changeUserId))
            {
              return "midBoxUpdatedByMe";
            }
            else
            {
              return "midBoxUpdatedByOther";
            }
          }
        }
      }
    }
    return "midBox";
  }

  public String getRootStyleClass()
  {
    StringBuilder sb = new StringBuilder();
    MenuItemCursor item = getCursor().getRoot();
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

  public String getRootBoxStyleClass()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (getRefWorkspaceId() != null && userSessionBean.isSyncViewSelected())
    {
      MenuItemCursor item = getCursor().getRoot();
      if (getNodePaintMap().containsKey(item.getMid()))
      {
        NodeChange nodeChange = getNodePaintMap().get(item.getMid());
        if (nodeChange == null) //node with deleted children
        {
          return "midBoxDeleted";
        }
        else
        {
          if (nodeChange.getType() != NodeChangeType.MOVED)
          {
            String changeUserId = nodeChange.getNode().getChangeUserId();
            String currentUserId = userSessionBean.getUserId();
            if (currentUserId.equals(changeUserId))
            {
              return "midBoxUpdatedByMe";
            }
            else
            {
              return "midBoxUpdatedByOther";
            }
          }
        }
      }
    }
    return "midBox";
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
        styleClass = "inheritedAnnotatedPropertyName";
      }
      else if (getAnnotatedPropertyNameSet().contains(propertyName))
      {
        styleClass = "directAnnotatedPropertyName";
      }        
      String currentNodeId = getSelectedNodeId();
      if (currentNodeId != null && 
        getNodeSearchMap().keySet().contains(currentNodeId) && 
        getNodeSearchMap().get(currentNodeId).isMarkPropertyName(propertyName))
      {
        styleClass += " foundProperty";
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
          styleClass += " foundProperty";
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

  public String getMenuItemBoxText()
  {
    MenuItemCursor cursor = (MenuItemCursor)getValue("#{item}");
    StringBuilder sb = new StringBuilder();
    sb.append(cursor.getMid());
    if (isShowMenuItemIndex(cursor))
    {
      sb.append(";");
      sb.append(getMenuItemIndex(cursor));
    }
    return sb.toString();
  }

  public String getNodeLabel()
  {
    Node node = (Node)getValue("#{node}");
    return getNodeLabel(node);
  }

  public String getNodeChangeRowLabel()
  {
    NodeChangeRow nodeChangeRow = (NodeChangeRow)getValue("#{nodeChangeRow}");
    Node node = nodeChangeRow.getNodeChange().getNode();
    StringBuilder sb = new StringBuilder();
    sb.append(getNodeLabel(node));
    if (nodeChangeRow.getChangedPropertiesText() != null)
    {
      sb.append(" [").append(nodeChangeRow.getChangedPropertiesText()).
        append("]");      
    }
    return sb.toString();
  }

  public String getRootLabel()
  {
    MenuItemCursor item = getCursor().getRoot();
    return getMenuItemLabel(item);
  }

  // ******** Action methods *********

  public String showRootSelection()
  {
    rootNodeList = null;
    return "root_select";
  }

  public String moveNodeUp()
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
    return null;
  }

  public String moveNodeDown()
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
    return null;
  }

  public String appendNode()
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
      UserSessionBean.getCurrentInstance().setSelectedMid(nodeId);
      getExpandedMenuItems().add(parentNodeId);
      resetProperties();
      nodeName = null;
      cssText = null;
      syncNodeChangeList = null;
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String insertBeforeNode()
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
      UserSessionBean.getCurrentInstance().setSelectedMid(nodeId);
      resetProperties();
      nodeName = null;
      cssText = null;
      syncNodeChangeList = null;
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String insertAfterNode()
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
      UserSessionBean.getCurrentInstance().setSelectedMid(nodeId);
      resetProperties();
      nodeName = null;
      cssText = null;
      syncNodeChangeList = null;
      info("NODE_ADDED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String removeNode()
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
      UserSessionBean.getCurrentInstance().setSelectedMid(newNodeId);
      if (cNode.isRoot())
      {
        setRootNode(newNodeId);
        rootNodeList = null;
      }
      resetProperties();
      nodeName = null;
      cssText = null;
      syncNodeChangeList = null;
      updateCache();
      info("NODE_REMOVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cutNode()
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
    return null;
  }

  public String copyNode()
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
    return null;
  }
  
  public String copySingleNode()
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
    return null;
  }

  public String pasteAsRootNode()
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
      goToRoot(node.getNodeId());
      info(message);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String pasteInsideNode()
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
        goToNode(node.getNodeId());
        getExpandedMenuItems().add(selectedNodeId);
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String pasteBeforeNode()
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
        goToNode(node.getNodeId());
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String pasteAfterNode()
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
        goToNode(node.getNodeId());
        info(message);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String syncNode()
  {
    try
    {
      List<NodeChange> finalNodeChangeList = new ArrayList<NodeChange>();
      for (NodeChangeRow row : syncNodeChangeList)
      {
        if (row.isChecked())
        {
          finalNodeChangeList.add(row.getNodeChange());
        }
      }
      if (finalNodeChangeList.size() > 0)
      {        
        for (NodeChange nodeChange : finalNodeChangeList)
        {
          if (nodeChange.getType().equals(NodeChangeType.MOVED) || 
            nodeChange.getType().equals(NodeChangeType.NAME_CHANGED) || 
            nodeChange.getType().equals(NodeChangeType.FALSE_UPDATE))
          {
            nodeChange.setType(NodeChangeType.UPDATED);
          }
        }
        CMSConfigBean.getPort().syncNodes(currentWorkspaceId,
          getToWorkspaceId(), finalNodeChangeList);
        nodePaintMap = null;
        syncNodeChangeList = null;
        info("NODE_SYNCHRONIZED");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "node_edit";
  }

  public String checkAllSyncNodes()
  {
    for (NodeChangeRow row : syncNodeChangeList)
    {
      row.setChecked(true);
    }
    return null;
  }

  public String collapseAllNodes()
  {
    getExpandedMenuItems().clear();
    getExpandedMenuItems().add(rootNodeId);
    return null;
  }

  public String resetSearch()
  {
    nodeSearchMap = null;
    searchDone = false;
    searchPropertyList = null;
    return null;
  }

  public String switchPropertyHelp()
  {
    showPropertyHelp = !showPropertyHelp;
    return null;
  }

  public String getHelpButtonStyleClass()
  {
    return (showPropertyHelp ? "showHelpImageButton" : "imageButton");
  }

  public String saveProperties()
  {
    try
    {
      List<Property> auxPropertyList = new ArrayList<Property>();
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
      resetProperties();
      nodeName = null;
      syncNodeChangeList = null;
      updateCache();
      if (!nodeIsVisible(selectedNode.getNodeId()))
      {
        String newVisibleNodeId = getNewVisibleNodeId();
        goToNode(newVisibleNodeId);
      }
      info("NODE_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String completeProperties()
  {
    try
    {
      List<Property> auxPropertyList = new ArrayList<Property>();
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
    return null;
  }

  public String revertProperties()
  {
    try
    {
      resetProperties();
      nodeName = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String addProperty()
  {
    Property p = new Property();
    userPropertyList.add(p);
    p.getValue().add("");
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
    return null;
  }

  public String addPropertyValue()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    p.getValue().add("");
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
    return null;
  }

  public String removeProperty()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    userPropertyList.remove(p);
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
    return null;
  }

  public String addSearchProperty()
  {
    Property p = new Property();
    searchPropertyList.add(p);
    p.getValue().add("");
    return null;
  }

  public String removeSearchProperty()
  {
    Property p = (Property)getFacesContext().
      getExternalContext().getRequestMap().get("property");
    searchPropertyList.remove(p);
    return null;
  }

  public String saveCSS()
  {
    try
    {
      Integer cssCount = saveCSS(currentWorkspaceId, getSelectedNodeId());
      String message = cssCount + 
        (cssCount == 1 ? " node actualitzat" : " nodes actualitzats");
      message = UserSessionBean.getCurrentInstance().translate(message, "cms");      
      info("CSS_SAVED", new Object[]{message});
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }

  public String revertCSS()
  {
    try
    {
      cssText = loadCSS();
      info("CSS_REVERTED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String syncCSS()
  {
    try
    {
      String defaultWorkspaceId = 
        ApplicationBean.getCurrentInstance().getDefaultWorkspaceId();
      Integer cssCount = saveCSS(defaultWorkspaceId, getSelectedNodeId());
      String message = cssCount + 
        (cssCount == 1 ? " node actualitzat" : " nodes actualitzats"); 
      message = UserSessionBean.getCurrentInstance().translate(message, "cms");
      info("CSS_SYNCHRONIZED", new Object[]{message});
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;      
  }
  
  public String selectMenuItem()
  {
    String nodeId;
    try
    {
      MenuItemCursor menuItemCursor = (MenuItemCursor)getFacesContext().
        getExternalContext().getRequestMap().get("item");
      nodeId = menuItemCursor.getMid();
    }
    catch (Exception ex)
    {
      NodeSearchItem nodeSearchItem = (NodeSearchItem)getFacesContext().
        getExternalContext().getRequestMap().get("nodeSearchItem");
      nodeId = nodeSearchItem.getNodeId();
    }
    String newRootNodeId = getRootNodeId(nodeId);
    if (!rootNodeId.equals(newRootNodeId))
    {
      setRootNode(newRootNodeId);
    }
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(nodeId);
    resetProperties();
    nodeName = null;
    cssText = null;
    syncNodeChangeList = null;
    if (userSessionBean.isSearchViewSelected())
    {
      userSessionBean.setViewMode(UserSessionBean.EDIT_VIEW);
    }
    return null;
  }

  public String selectRootMenuItem()
  {
    MenuItemCursor menuItemCursor = getCursor().getRoot();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(menuItemCursor.getMid());
    resetProperties();
    nodeName = null;
    cssText = null;
    syncNodeChangeList = null;
    if (userSessionBean.isSearchViewSelected())
    {
      userSessionBean.setViewMode(UserSessionBean.EDIT_VIEW);
    }
    return null;
  }

  public String changeRootNode()
  {
    Node root = (Node)getValue("#{node}");
    String newRootNodeId = root.getNodeId();
    if (!newRootNodeId.equals(rootNodeId))
    {
      return goToRoot(newRootNodeId);
    }
    else return "node_edit";
  }

  public String changeBeanName()
  {
    beanActions = null;
    return null;
  }

  public String createRootNode()
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
      rootNodeList = null;
      info("NEW_ROOT_CREATED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String fastSearch()
  {
    try
    {
      getNodeSearchMap().clear();
      searchDone = false;
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

      getExpandedMenuItems().addAll(getAscendants(getNodeSearchMap().keySet()));
      searchDone = true;      
      UserSessionBean.getCurrentInstance().setViewMode(
        UserSessionBean.SEARCH_VIEW);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String fullSearch()
  {
    try
    {
      getNodeSearchMap().clear();
      searchDone = false;
      NodeFilter nodeFilter = new NodeFilter();
      nodeFilter.setMaxResults(MAX_SEARCH_ITEMS);
      nodeFilter.getWorkspaceId().add(
        UserSessionBean.getCurrentInstance().getWorkspaceId());
      nodeFilter.setPropertyCaseSensitive(false);
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
        getExpandedMenuItems().addAll(
          getAscendants(getNodeSearchMap().keySet()));
      }
      searchDone = true;      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getSearchCount()
  {
    int searchSize = getNodeSearchMap().size();    
    return (searchSize == MAX_SEARCH_ITEMS ? ">= " : "") + 
      String.valueOf(searchSize);
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

  public List<NodeSearchItem> getNodeSearchItemList()
  {
    List<NodeSearchItem> result = new ArrayList(getNodeSearchMap().values());
    Collections.sort(result, new Comparator<NodeSearchItem>() {
      @Override
      public int compare(NodeSearchItem n1, NodeSearchItem n2)
      {
        return n1.compareTo(n2);
      }
    });
    return result;
  }
  
  // ********* Private methods ********

  private int saveCSS(String workspaceId, String nodeId) throws Exception
  {
    int cssCount = 0;
    if (cssText != null)
    {
      List<Node> nodeList = new ArrayList();
      CNode cNode = ApplicationBean.getCurrentInstance().
        getCmsCache().getWorkspace(workspaceId).getNode(nodeId);
      Document document;
      CachedDocumentManagerClient docClient = DocumentConfigBean.getClient();
      String docId = cNode.getSinglePropertyValue(UserSessionBean.NODE_CSS);
      if (docId != null) // existing CSS
      {
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
        NodeFilter filter = new NodeFilter();
        Property property = new Property();
        property.setName(UserSessionBean.NODE_CSS);
        property.getValue().add(docId + "%");
        filter.getProperty().add(property);
        List<Node> auxNodeList = getCMSManagerPort().findNodes(filter);
        for (Node auxNode : auxNodeList)
        {
          Property p = getNodeCSSProperty(auxNode);
          String nodeCSSValue = p.getValue().get(0);
          if (nodeCSSValue.equals(docId) || 
            nodeCSSValue.startsWith(docId + "?"))
          {
            removeNodeCSSProperty(auxNode);
            nodeList.add(auxNode);
          }
        }
      }
      else // new CSS
      {
        document = prepareCSSDocument();        
        document.setDocTypeId("Document");
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
        Node auxNode = cNode.getNode();
        nodeList.add(auxNode);
      }
      // update nodeCSS property
      for (Node node : nodeList)
      {
        Property nodeCssProperty = new Property();
        nodeCssProperty.setName(UserSessionBean.NODE_CSS);
        nodeCssProperty.getValue().add(docId + "?v=" + document.getVersion());
        node.getProperty().add(nodeCssProperty);
        getCMSManagerPort().storeNode(node);
        cssCount++;
      }
      resetProperties();
      updateCache();
    }
    return cssCount;
  }
  
  private Property getNodeCSSProperty(Node node)
  {
    Integer idx = getNodeCSSPropertyIndex(node);
    if (idx != null)
    {
      return node.getProperty().get(idx);
    }
    return null;
  }

  private boolean removeNodeCSSProperty(Node node)
  {
    Integer idx = getNodeCSSPropertyIndex(node);
    if (idx != null)
    {
      node.getProperty().remove(idx.intValue());
      return true;
    }
    return false;
  }

  private Integer getNodeCSSPropertyIndex(Node node)
  {
    for (int i = 0; i < node.getProperty().size(); i++)
    {
      Property p = node.getProperty().get(i);
      if (UserSessionBean.NODE_CSS.equals(p.getName()))
      {
        return i;        
      }
    }
    return null;
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
    annotatedPropertyNameSet = new HashSet<String>();
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
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (getRefWorkspaceId() != null && userSessionBean.isSyncViewSelected())
    {
      NodeChange nodeChange = getNodePaintMap().get(item.getMid());
      if (nodeChange != null)
      {
        String changeUserId = nodeChange.getNode().getChangeUserId();
        if (changeUserId != null)
        {
          label += " (" + changeUserId + ")";
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
  
  private String getNodeLabel(Node node)
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

  private MenuItemCursor getCursor()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
  }

  private void updateCache()
  {
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    String workspaceId =
      UserSessionBean.getCurrentInstance().getWorkspaceId();
    cmsCache.getWorkspace(workspaceId).purge();
    nodePaintMap = null;
  }

  private List<Property> clean(List<Property> properties,
    boolean excludeMandatoryValue, boolean allowRepetitions) throws Exception
  {
    Set<String> propertyNameSet = new HashSet<String>();
    List<Property> finalPropertyList = new ArrayList<Property>();
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
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String workspaceId = userSessionBean.getWorkspaceId();
    String nodeId = userSessionBean.getSelectedMid();
    CNode selectedCNode =
      cmsCache.getWorkspace(workspaceId).getNode(nodeId);
    return selectedCNode;
  }

  private String loadCSS() throws Exception
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
    else return null;
  }

  private Set createExpandedMenuItems()
  {
    HashSet set = new HashSet();
    MenuItemCursor selectedMenuItem = getCursor();
    MenuItemCursor cursor = selectedMenuItem.getClone();
    do 
    {
      set.add(cursor.getMid());
    } 
    while (cursor.moveParent());
    return set;
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

  private Set<String> getAscendants(String nodeId)
  {
    Set<String> nodeIdSet = new HashSet<String>();
    nodeIdSet.add(nodeId);
    return getAscendants(nodeIdSet);
  }

  private Set<String> getAscendants(Set<String> nodeIdSet)
  {
    Set<String> result = new HashSet<String>();
    Iterator<String> it = nodeIdSet.iterator();
    List<String> preList = new ArrayList<String>();
    CWorkspace cWorkspace = 
      ApplicationBean.getCurrentInstance().getCmsCache().
      getWorkspace(currentWorkspaceId);
    while (it.hasNext())
    {
      CNode cNodeAux = cWorkspace.getNode(it.next());
      if (cNodeAux != null)
      {
        while (cNodeAux.getParent() != null)
        {
          preList.add(cNodeAux.getParentNodeId());
          cNodeAux = cNodeAux.getParent();
        }
      }
    }
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    List<MenuItemCursor> micList = menuModel.getMenuItemsByMid(preList);
    for (MenuItemCursor mic : micList)
    {
      result.add(mic.getMid());
    }
    return result;
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
      cssText = null;
      propertyList = null;
      userPropertyList = null;
      beanNameProperty = null;
      beanActionProperty = null;
      beanActions = null;
      annotatedPropertyNameSet = null;
      nodeTipsBundlePath = null;
      nodeName = null;
      rootNodeList = null;
      rootNodeId = null;
      cutNodeId = null;
      copyNodeId = null;
      nodePaintMap = null;
      toWorkspaceId = null;
      toWorkspaceItems = null;
      syncNodeChangeList = null;
      searchPropertyList = null;
      nodeSearchMap = null;
      searchDone = false;
      currentWorkspaceId =
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      getExpandedMenuItems().addAll(getAscendants(getCursor().getMid()));
    }
  }

  private boolean workspaceHasChanged()
  {
    return (!UserSessionBean.getCurrentInstance().getWorkspaceId().
      equals(currentWorkspaceId));
  }

  private String goToRoot(String rootNodeId)
  {
    setRootNode(rootNodeId);
    return goToNode(rootNodeId);
  }

  private String goToNode(String nodeId)
  {
    UserSessionBean.getCurrentInstance().setSelectedMid(nodeId);
    resetProperties();
    nodeName = null;
    cssText = null;
    syncNodeChangeList = null;
    return "node_edit";
  }

  private void setRootNode(String nodeId)
  {
    nodePaintMap = null;
    rootNodeId = nodeId;
    getExpandedMenuItems().add(nodeId);
  }

  private List<NodeChange> sortNodeChangeList(List<NodeChange> nodeChangeList)
  {
    if (nodeChangeList == null || nodeChangeList.size() <= 1)
      return nodeChangeList;
    List<NodeChange> result = new ArrayList<NodeChange>();
    List<NodeChange> lessList = new ArrayList<NodeChange>();
    List<NodeChange> greaterList = new ArrayList<NodeChange>();
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

  private List<NodeChangeRow> getNodeChangeRowList(String fromWorkspaceId,
    String toWorkspaceId)
  {
    List<NodeChangeRow> result = new ArrayList<NodeChangeRow>();
    try
    {
      CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
      String selectedNodeId =
        UserSessionBean.getCurrentInstance().getSelectedMid();
      List<NodeChange> nodeChangeList = getNodeChangeList(fromWorkspaceId,
        toWorkspaceId, selectedNodeId);

      List<String> nodeIdList = new ArrayList<String>();
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
        String nodeId = nodeChange.getNode().getNodeId();
        String changedPropertiesText = null;
        if (nodeChange.getType().equals(NodeChangeType.UPDATED))
        {
          CNode fromCNode = fromCNodeMap.get(nodeId);
          CNode toCNode = toCNodeMap.get(nodeId);
          if (!fromCNode.hasTheSameProperties(toCNode))
          {
            List<String> properties = fromCNode.getDifferentProperties(toCNode);
            if (properties != null && !properties.isEmpty())
            {
              changedPropertiesText = 
                TextUtils.collectionToString(properties).replace(",", ", ");              
            }
            else
            {
              changedPropertiesText = "";
            }
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
            }
          }
        }
        NodeChangeRow row = 
          new NodeChangeRow(nodeChange, changedPropertiesText);
        result.add(row);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return result;
  }

  private void swapNode(CNode topCNode) throws Exception
  {
    if (topCNode != null)
    {
      CNode bottomCNode = null;
      bottomCNode = topCNode.getNextSibling();
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
        syncNodeChangeList = null;
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
      List<Workspace> finalWorkspaceList = new ArrayList<Workspace>();
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
      List<String> beanActionList = new ArrayList<String>();
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

  private void loadSyncNodeChangeList()
  {
    if (toWorkspaceId == null)
    {
      toWorkspaceId = (String)toWorkspaceItems[0].getValue();
      toWorkspaceIdInput = toWorkspaceId;
    }
    syncNodeChangeList =
      getNodeChangeRowList(currentWorkspaceId, toWorkspaceId);
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
    try
    {
      getMenuModel().getMenuItemByMid(nodeId);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  private List<Node> filterVisibleNodes(List<Node> nodeList)
  {
    List<Node> result = new ArrayList<Node>();
    Map<String, Node> nodeMap = new HashMap<String, Node>();
    List<String> nodeIdList = new ArrayList<String>();
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
    MenuItemCursor newCursor = getCursor().getPrevious();
    if (newCursor.getMid() == null) //no previous visible node
    {
      newCursor = getCursor().getNext();
      if (newCursor.getMid() == null) //no next visible node -> go to parent
      {
        newCursor = getCursor().getParent();
        if (newCursor.getMid() == null)
        {
          String mid = UserSessionBean.getCurrentInstance().getSelectedMid();          
          return getRootNodeId(mid);          
        }
      }
    }
    return newCursor.getMid();
  }

  private void resetProperties()
  {
    propertyList = null;
    userPropertyList = null;
    beanNameProperty = null;
    beanActionProperty = null;
    beanActions = null;
    nodeTipsBundlePath = null;
    annotatedPropertyNameSet = null;
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
    List<Property> result = new ArrayList<Property>();
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
    List<Property> result = new ArrayList<Property>();
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

  private static Set<String> getCommonPropertyNames()
  {
    Set<String> result = new HashSet<String>();
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
  public class NodeChangeRow implements Serializable
  {
    private boolean checked;
    private NodeChange nodeChange;
    private String changedPropertiesText;

    public NodeChangeRow(NodeChange nodeChange, String changedPropertiesText)
    {
      this.nodeChange = nodeChange;
      this.changedPropertiesText = changedPropertiesText;
    }

    public boolean isChecked()
    {
      return checked;
    }

    public void setChecked(boolean checked)
    {
      this.checked = checked;
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

    public String getNodeId()
    {
      return nodeChange.getNode().getNodeId();
    }

    public String getType()
    {
      return nodeChange.getType().value();
    }    
  }  
  
  //Represents a found node
  public class NodeSearchItem implements Serializable
  {
    private Node node;
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
        for (int i = 1; i < Math.max(thisIdxList.size(), otherIdxList.size()); i++)
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

}
