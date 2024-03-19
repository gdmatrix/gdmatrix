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
package org.santfeliu.webapp.modules.news;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.dic.DictionaryConstants;
import org.matrix.news.New;
import org.matrix.news.NewStoreOptions;
import org.matrix.news.Source;
import org.matrix.news.SourceFilter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.FinderBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.cms.CMSModuleBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class NewObjectBean extends ObjectBean
{
  public static final String NEWSECTION_PROPERTY = "newSection";
  private static final String LEGACY_SECTION_PROPERTY = "action";
  private static final String LEGACY_SECTION_VALUE = "%newSearchBySection%";  
  
  private New newObject = new New();
  private List<Source> sources;

  @Inject
  NewFinderBean newFinderBean;

  @Inject
  NewTypeBean newTypeBean;


  @PostConstruct
  public void init()
  {
    try
    {      
      sources = NewsModuleBean.getPort(true).findSources(new SourceFilter());
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public New getNewObject()
  {
    return newObject;
  }

  public void setNewObject(New newObject)
  {
    this.newObject = newObject;
  }

  public boolean isCustomUrlTargetBlank()
  {    
    return newObject.getCustomUrlTarget() != null 
      && newObject.getCustomUrlTarget().equals("_blank");
  }

  public void setCustomUrlTargetBlank(boolean customUrlTargetBlank)
  {
    if (customUrlTargetBlank)
      newObject.setCustomUrlTarget("_blank");
    else
      newObject.setCustomUrlTarget("");
  }

  public List<Source> getSources()
  {
    return sources;
  }

  public void setSources(List<Source> sources)
  {
    this.sources = sources;
  }
  
  public Date getStartDateTime()
  {
    if (newObject != null && newObject.getStartDate() != null)
    {
      return getDate(newObject.getStartDate(), newObject.getStartTime());
    }
    else
    {
      return null;
    }
  }

  public Date getEndDateTime()
  {
    if (newObject != null && newObject.getEndDate() != null)
    {
      return getDate(newObject.getEndDate(), newObject.getEndTime());
    }
    else
    {
      return null;
    }
  }

  public void setStartDateTime(Date date)
  {
    if (newObject != null)
    {
      if (date == null)
      {
        date = new Date();
      }
      newObject.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      newObject.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public void setEndDateTime(Date date)
  {
    if (date != null && newObject != null)
    {
      newObject.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      newObject.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
    else if (date == null && newObject != null)
    {
      newObject.setEndDate(null);
      newObject.setEndTime(null);
    }
      
  }
  
  public List<String> getKeywords()
  {
    String keywords = newObject.getKeywords();
    if (keywords != null)
      return Arrays.asList(keywords.split(" "));
    else
      return Collections.EMPTY_LIST;
  }
  
  public void setKeywords(List<String> keywords)
  {
    if (keywords != null && !keywords.isEmpty())
    {
      StringBuilder sb = new StringBuilder();
      keywords.stream().forEach(k -> sb.append(k).append(" "));
      newObject.setKeywords(sb.toString().trim());
    }
  }
  
  @Override
  public NewTypeBean getTypeBean()
  {
    return newTypeBean;
  }

  @Override
  public New getObject()
  {
    return isNew() ? null : newObject;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(newObject.getNewId());
  }

  public String getDescription(String newId)
  {
    return getTypeBean().getDescription(newId);
  }

  @Override
  public FinderBean getFinderBean()
  {
    return newFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.NEW_TYPE;
  }
  
  List<MenuItemCursor> getSectionNodes() throws Exception
  {
    List<String> nodeIds = new ArrayList<>();

    String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
    NodeFilter nodeFilter = new NodeFilter();

    nodeFilter.getWorkspaceId().add(workspaceId);
    
    //Search for legacy sections
    nodeFilter = new NodeFilter();
    nodeFilter.getWorkspaceId().add(workspaceId);

    Property property = new Property();
    property.setName(LEGACY_SECTION_PROPERTY);
    property.getValue().add(LEGACY_SECTION_VALUE);
    nodeFilter.getProperty().add(property);       
    List<Node> nodeList = CMSModuleBean.getPort(true).findNodes(nodeFilter);

    nodeFilter.getProperty().clear();
    property = new Property();
    property.setName(NEWSECTION_PROPERTY);
    property.getValue().add("%");
    nodeFilter.getProperty().add(property);       
    nodeList.addAll(CMSModuleBean.getPort(true).findNodes(nodeFilter));
    
    nodeList.stream().forEach(node -> nodeIds.add(node.getNodeId()));
    
    return UserSessionBean.getCurrentInstance()
      .getMenuModel().getMenuItemsByMid(nodeIds); 
  }
    
  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      newObject = NewsModuleBean.getPort(false).loadNew(objectId);
      decodeNewContent(newObject);
    }
    else
      newObject = new New();
  }

  @Override
  public void storeObject() throws Exception
  {
    NewStoreOptions newStoreOptions = new NewStoreOptions();
    newStoreOptions.setCleanSummary(true);
    newStoreOptions.setCleanText(true);  
    
    newObject.setUserId(UserSessionBean.getCurrentInstance().getUsername()); 
    newObject.setCustomUrl(newObject.getCustomUrl().trim());
    
    encodeNewContent(newObject);
    newObject = 
      NewsModuleBean.getPort(false).storeNew(newObject, newStoreOptions);
    setObjectId(newObject.getNewId());   
    decodeNewContent(newObject);
    
    newFinderBean.outdate();
  }
  
  @Override
  public void removeObject() throws Exception
  {
    if (!isNew())
      NewsModuleBean.getPort(false).removeNew(newObject.getNewId());
  }  

  @Override
  public Serializable saveState()
  {
    return newObject;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.newObject = (New)state;
  }
  
  private Date getDate(String date, String time)
  {
    String dateTime = TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);
  }
  
  private void encodeNewContent(New newObject)
  {
    String headline = Unicode.encode(newObject.getHeadline(), true);
    newObject.setHeadline(headline.length() > 1000 ?
      headline.substring(0, 1000) : headline);
    
    String summary = Unicode.encode(newObject.getSummary(), true);
    newObject.setSummary(summary.length() > 4000 ?
      summary.substring(0, 4000) : summary);  
    
    newObject.setText(Unicode.encode(newObject.getText(), true));
  }
  
  private void decodeNewContent(New newObject)
  {
    newObject.setHeadline(Unicode.decode(newObject.getHeadline()));      
    newObject.setSummary(Unicode.decode(newObject.getSummary()));
    newObject.setText(Unicode.decode(newObject.getText()));   
  }

}
