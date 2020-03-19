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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;

import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.web.obj.util.JumpManager;
import org.santfeliu.web.obj.util.RequestParameters;
import org.santfeliu.web.obj.util.RowStyleClassGenerator;

public abstract class PageBean extends WebBean implements Savable
{
  public static final String ACTIONS_SCRIPT_NAME = 
    "_actionsScriptName";
  
  protected static final int PAGE_SIZE = 10;
  @CMSProperty
  public static final String PAGE_SIZE_PROPERTY = "pageSize";
  @CMSProperty
  public static final String PAGE_TITLE_PROPERTY = "oc.pageTitle";
  
  protected JumpManager jumpManager;
  
  public PageBean()
  {
    super();
    jumpManager = new JumpManager(this);
  }
   
  public String getTitle()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return getTitle(userSessionBean.getMenuModel().getSelectedMenuItem());
  }

  public String getTitle(MenuItemCursor cursor)
  {
    String title = cursor.getProperty(PAGE_TITLE_PROPERTY);
    if (title == null)
    {
      title = cursor.getLabel();
    }
    return title;
  }

  public abstract String show();
  
  public String jshow()
  {
    String outcome = jumpManager.execute(getRequestParameters());
    if (outcome != null)
      return outcome;
    else
      return show();
  } 
  
  public boolean checkJumpSuitability(String objectId)
  {
    return true;
  }
  
  public String getNotSuitableMessage()
  {
    return "INVALID_OBJECT";
  }    
  
  public String store()
  {
    //preStore();
    String outcome = show();
    //postStore();
    return outcome;
  }
  
//  public void preStore()
//  {
//    ObjectBean objectBean = getObjectBean();
//    if (objectBean != null)
//      objectBean.preStore();
//  };
//  
//  public void postStore()
//  {
//    ObjectBean objectBean = getObjectBean();
//    if (objectBean != null)
//      objectBean.postStore();
//  };
  
  protected void executeTypeAction(String actionName) throws Exception
  {
    executeTypeAction(actionName, getSelectedType());
  }
  
  protected void executeTypeAction(String actionName, Type selectedType)
    throws Exception
  {
    if (selectedType != null)
    {
      if (isActionsScriptEnabled(selectedType))
      {
        String action = UserSessionBean.ACTION_SCRIPT_PREFIX + ":" + 
          getActionsScriptName(selectedType) + "." + actionName;
        UserSessionBean.getCurrentInstance().executeScriptAction(action);
      }
    }
  }
  

  public boolean isModified()
  {
    return true;
  }

  public boolean isNew()
  {
    return ControllerBean.NEW_OBJECT_ID.equals(getObjectId());
  }

  public String getObjectId()
  {
    ObjectBean objectBean = getObjectBean();
    return objectBean == null ?
      ControllerBean.NEW_OBJECT_ID : objectBean.getObjectId();
  }
  
  public void setObjectId(String objectId)
  {
    getObjectBean().setObjectId(objectId);
  }
  
  public ObjectBean getObjectBean()
  {
    return ControllerBean.getCurrentInstance().getObjectBean();
  }
  
  public ControllerBean getControllerBean()
  {
    return ControllerBean.getCurrentInstance();
  }
  
  public String createObject()
  {
    return ControllerBean.getCurrentInstance().getObjectBean().create();
  }

  public String showObject(String typeId, String objectId)
  {
    return ControllerBean.getCurrentInstance().showObject(typeId, objectId);
  }  

  public int getPageSize()
  {
    String pageSize = getSelectedMenuItem().getProperty(PAGE_SIZE_PROPERTY);
    if (pageSize != null)
      return Integer.valueOf(pageSize).intValue();
    else
      return PAGE_SIZE;
  }

  /* Dictionary Property definition Maps */
  public Map getPropertySize()
  {
    return new PropertySizeMap(getSelectedType());
  }

  public Map getPropertyRequired()
  {
    return new PropertyRequiredMap(getSelectedType());
  }

  protected Type getSelectedType()
  {
    return null;
  }
  
  protected String getActionsScriptName(Type type)
  {
    String scriptName = null;
    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(ACTIONS_SCRIPT_NAME);
      if (pd != null && pd.getValue() != null)
      {
        String value = pd.getValue().get(0);
        if (value != null)
          scriptName = value;
      }
    }
    
    return scriptName;
  }
  
  protected boolean isActionsScriptEnabled(Type type)
  {
    return getActionsScriptName(type) != null;
  }
  
  public RowStyleClassGenerator getRowStyleClassGenerator()
  {
    return new DateTimeRowStyleClassGenerator(
      "startDate,startTime", "endDate,endTime", "before,after,between");
  }
  
  public String getRowStyleClass()
  {
    RowStyleClassGenerator styleClassGenerator = 
      getRowStyleClassGenerator();
    return styleClassGenerator.getStyleClass(getValue("#{row}"));    
  }
  
  protected RequestParameters getRequestParameters()
  {
    RequestParameters parameters = new RequestParameters();
    
    Map requestMap = getExternalContext().getRequestParameterMap();
    HttpServletRequest request = 
      (HttpServletRequest)getExternalContext().getRequest();
    String qs = request.getQueryString();

    for (Object key : requestMap.keySet())
    {
      String skey = String.valueOf(key);
      String svalue = String.valueOf(requestMap.get(key));
      parameters.add(skey, svalue, qs);
    }
  
    return parameters;    
  }
    
}
