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

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.cases.web.CaseSearchBean;
import org.santfeliu.cases.web.InterventionSearchBean;
import org.santfeliu.doc.web.DocumentSearchBean;
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
  public static final String DEFAULT_ID_PARAMETER = "joid";
  public static final String DEFAULT_ID_TAB_PARAMETER = "jtoid";
  public static final String JUMP_MID_PARAMETER = "xmid";

  private PageBean pageBean;
  private String idParameterName;
  private String idTabName;
  
  public JumpManager(PageBean pageBean)
  {
    this.pageBean = pageBean;
    
    //Old legacy ids.
    if (pageBean instanceof CaseSearchBean)
      this.idParameterName = "caseid";
    else if (pageBean instanceof DocumentSearchBean)
      this.idParameterName = "docid";
    else if (pageBean instanceof InterventionSearchBean)
    {
      this.idParameterName = "caseid";
      this.idTabName = "intid";
    }
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

  @Override
  public String execute(RequestParameters parameters)
  {  
    if (idParameterName == null)
      idParameterName = DEFAULT_ID_PARAMETER;
    
    JumpData jumpData = getJumpData(parameters);  
    
    String objectId = jumpData.getObjectId();
    if (objectId != null)
    {
      try
      {
        if (jumpData.isJumpToTab()) //Jump to tab object
        {
          //1. Jump to tab
          String mid = jumpData.getMid();
          ControllerBean controllerBean = ControllerBean.getCurrentInstance();        
          String outcome = controllerBean.show(mid, objectId);
          //2. Edit object
          PageBean tabPageBean = controllerBean.getPageBean();
          if (tabPageBean instanceof ExternalEditable)
          {
            String tabObjectId = jumpData.getTabObjectId();
            String editObjectId = 
              isObjectCreation(tabObjectId) ? null : tabObjectId;
            ((ExternalEditable)tabPageBean).editObject(editObjectId);            
          }
        
          return outcome;          
        }
        else if (isObjectCreation(objectId)) //Main object creation
          return pageBean.createObject();
        else 
        {
          String typeId = jumpData.getTypeId();
          if (typeId != null)
          {
            if (pageBean.checkJumpSuitability(objectId))            
              return pageBean.showObject(typeId, objectId);
            else
            {
              error(pageBean.getNotSuitableMessage());
              return null;
            }                
          }
        }
      }
      catch (Exception ex)
      {
        error(ex.getLocalizedMessage());
        return null;
      }            
    }          
    
    return null;
  }
  
  private boolean isObjectCreation(String objectId)
  {
    if (objectId != null)
      return objectId.equals(NEW_OBJECT_PARAMETER);
    else
      return false;    
  }
  
  private String getObjectId(RequestParameters parameters)
  {
    String value = null;

    //1. Try with idParameterName
    if (idParameterName != null)
      value = (String) parameters.getParameterValue(idParameterName);    
  
    //2. Try default idParameterName (joid)
    if (value == null)
      value = (String) parameters.getParameterValue(DEFAULT_ID_PARAMETER);
         
    return value;
  } 
  
  private String getTabObjectId(RequestParameters parameters)
  {
    String value = null;

    //1. Try with idTabName
    if (idTabName != null)
      value = (String) parameters.getParameterValue(idTabName);    
  
    //2. Try default idTabName (jtoid)
    if (value == null)
      value = (String) parameters.getParameterValue(DEFAULT_ID_TAB_PARAMETER);
         
    return value;
  }  
  
  private JumpData getJumpData(RequestParameters parameters)
  {
    JumpData data = new JumpData();
    
    String jumpCommand = 
      (String) parameters.getParameterValue(JUMPCOMMAND_PARAMETER);
    if (!StringUtils.isBlank(jumpCommand))
    {
      String[] parts = jumpCommand.split("\\|");
      if (parts[0].equals("type"))
      {
        data.setTypeId(parts[1]);
        data.setObjectId(parts[2]);
      }
      else if (parts[0].equals("tab"))
      {
        data.setMid(parts[1]);
        data.setObjectId(parts[2]);
        data.setTabObjectId(parts[3]);
      }       
    }
    else
    {
      String objectTypeId = pageBean.getObjectBean().getObjectTypeId(); 
      data.setTypeId(objectTypeId);
      data.setObjectId(getObjectId(parameters));
      data.setTabObjectId(getTabObjectId(parameters));
      String mid = 
        (String) parameters.getParameterValue(JUMP_MID_PARAMETER);
      data.setMid(mid);
    }
    
    return data;
  }  
  
  private class JumpData implements Serializable
  {
    private String objectId;
    private String tabObjectId;
    private String mid;
    private String typeId;
    private boolean objectCreation;

    public String getObjectId()
    {
      return objectId;
    }

    public void setObjectId(String objectId)
    {
      this.objectId = objectId;
      this.objectCreation = (NEW_OBJECT_PARAMETER.equals(objectId));
        
    }

    public String getTabObjectId()
    {
      return tabObjectId;
    }

    public void setTabObjectId(String tabObjectId)
    {
      this.tabObjectId = tabObjectId;
    }

    public String getMid()
    {
      return mid;
    }

    public void setMid(String mid)
    {
      this.mid = mid;
    }

    public boolean isObjectCreation()
    {
      return objectCreation;
    }

    public void setObjectCreation(boolean objectCreation)
    {
      this.objectCreation = objectCreation;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public void setTypeId(String typeId)
    {
      this.typeId = typeId;
    }
    
    public boolean isJumpToTab()
    {
      return this.tabObjectId != null;
    }    
  }  
  
}
