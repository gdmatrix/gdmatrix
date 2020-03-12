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
package org.santfeliu.web.obj.util;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.obj.BasicSearchBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ExternalEditable;
import org.santfeliu.web.obj.PageBean;


/**
 * This ParametersProcessor decides whether to execute the object creation 
 * action or to show the object based on the presenece of NEW_OBJECT_PARAMETER 
 * as objectId.
 * 
 * @author blanquepa
 */
public class JumpManager extends ParametersManager
{
  public static final String NEW_OBJECT_PARAMETER = "new"; 
  public static final String JUMPCOMMAND_PARAMETER = "hiddenjumpcommand";

  private PageBean searchBean;
  private String idParameterName;
  private String idTabName;
  private String objectTypeId;  
  
  public JumpManager(PageBean searchBean)
  {
    this(searchBean, null, null, null);
  }
  
  public JumpManager(PageBean searchBean, 
    String idParameterName, String objectTypeId)
  {
    this(searchBean, idParameterName, null, objectTypeId);
  }
  
  public JumpManager(PageBean searchBean, 
    String idParameterName, String idTabName, String objectTypeId)
  {
    this.searchBean = searchBean;
    this.idParameterName = idParameterName;
    this.idTabName = idTabName;
    this.objectTypeId = objectTypeId;
  }

  public String getIdParameterName()
  {
    return idParameterName;
  }

  public void setIdParameterName(String idParameterName)
  {
    this.idParameterName = idParameterName;
  }

  public String getIdTabName()
  {
    return idTabName;
  }

  public void setIdTabName(String idTabName)
  {
    this.idTabName = idTabName;
  }

  public String getObjectTypeId()
  {
    return objectTypeId;
  }

  public void setObjectTypeId(String objectTypeId)
  {
    this.objectTypeId = objectTypeId;
  }

  @Override
  public String execute(RequestParameters parameters)
  {
    //1. GET: jump to object specified in URL
    if (searchBean instanceof BasicSearchBean)
    {
      if (idParameterName == null)
        error("ID_PARAMETER_NAME_NOT_FOUND");
      else if(objectTypeId == null)
        error("OBJECT_TYPEID_NOT_FOUND");
      else
      {
        String objectId = parameters.getParameterValue(idParameterName);
        if (objectId != null)
        {
          try
          {
            if (objectId.equals(NEW_OBJECT_PARAMETER))
              return searchBean.createObject();
            else
              return searchBean.showObject(objectTypeId, objectId);
          }
          catch (Exception ex)
          {
            return null;
          }            
        }          
      }
    }
    
    //2. POST: jump to object specified in hiddenjumpcommand POST parameter.
    String jumpCommand = parameters.getParameterValue(JUMPCOMMAND_PARAMETER);
    if (!StringUtils.isBlank(jumpCommand))
    {
      String outcome = null;
      String[] parts = jumpCommand.split("\\|");
      if (parts[0].equals("type"))
      {
        String typeId = parts[1];
        String objectId = parts[2];
        outcome = ControllerBean.getCurrentInstance().showObject(typeId, objectId);
      }
      else if (parts[0].equals("tab"))
      {
        String mid = parts[1];
        String objectId = parts[2];
        String subObjectId = parts[3];
        outcome = ControllerBean.getCurrentInstance().show(mid, objectId);
        String beanName = getSelectedMenuItem().getProperty("oc.pageBean");
        ExternalEditable editableBean = (ExternalEditable)getBean(beanName);
        editableBean.editObject("new".equals(subObjectId) ? null : subObjectId);
      } 
      return outcome;
    }
    
    return null;
  }
  
  public boolean isObjectCreation()
  {
    Map requestMap = getExternalContext().getRequestParameterMap();
    String objectId = (String)requestMap.get(idParameterName);
    if (objectId != null)
      return objectId.equals(NEW_OBJECT_PARAMETER);
    else
      return false;
  }  
  
  public Object getParameter(String name)
  {
    return getExternalContext().getRequestParameterMap().get(name);
  }  
  
  public String getObjectId()
  {
    Object value = getParameter(getIdParameterName());
    return value != null ? String.valueOf(value) : null;
  }
  
  public String getTabObjectId()
  {
    Object value = getParameter(getIdTabName());
    return value != null ? String.valueOf(value) : null;
  }  
  
}
