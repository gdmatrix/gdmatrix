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

import org.apache.commons.lang.StringUtils;
import org.santfeliu.cases.web.CaseSearchBean;
import org.santfeliu.cases.web.InterventionSearchBean;
import org.santfeliu.doc.web.DocumentSearchBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ExternalEditable;
import org.santfeliu.web.obj.ObjectBean;
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
  public static final String JUMPCOMMAND_PARAMETER = "hiddenjumpcommand";
  public static final String DEFAULT_ID_PARAMETER = "joid";
  public static final String DEFAULT_ID_TAB_PARAMETER = "jtoid";
  public static final String JUMP_MID_PARAMETER = "jmid";

  private PageBean pageBean;
  private String idParameterName;
  private String idTabName;
  private String jumpMid;
  
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

  public String getJumpMid()
  {
    return jumpMid;
  }

  public void setJumpMid(String jumpMid)
  {
    this.jumpMid = jumpMid;
  }

  @Override
  public String execute(RequestParameters parameters)
  {  
    if (idParameterName == null)
      idParameterName = DEFAULT_ID_PARAMETER;
    
    JumpData jumpData = getJumpData(parameters); 
    
    if (jumpData.hasToJump())
    {
      try
      {
        if (jumpData.isObjectCreation())
          return pageBean.createObject();
        else if (jumpData.isJumpToTab()) 
          return jumpToTab(jumpData);
        else
          return jumpToMainObject(jumpData);
      }
      catch (Exception ex)
      {
        error(ex.getLocalizedMessage());
        return null;
      }            
    }
    
    return null;
  }
      
  private String jumpToMainObject(JumpData jumpData)
  {  
    if (checkJumpSuitability(jumpData))
    {
      if (jumpData.getJmid() != null)
      {
        String jMid = jumpData.getJmid();
        ControllerBean controllerBean = ControllerBean.getCurrentInstance();        
        return controllerBean.show(jMid, jumpData.getObjectId());
      }
      else
      {
        String typeId = jumpData.getTypeId();
        String objectId = jumpData.getObjectId();
        if (typeId != null && objectId != null)
          return pageBean.showObject(typeId, objectId);
      }
    }
    else
      notSuitable();

    return null;
  } 
  
  private String jumpToTab(JumpData jumpData)
  {
    //1. Jump to tab
    String tabmid = jumpData.getJmid();
    if (tabmid == null)
      tabmid = jumpData.getXmid();
    ControllerBean controllerBean = ControllerBean.getCurrentInstance();        
    String outcome = controllerBean.show(tabmid, jumpData.getObjectId());
    
    //2. Edit object
    PageBean tabPageBean = controllerBean.getPageBean();
    if (tabPageBean instanceof ExternalEditable)
    {
      String tabObjectId = jumpData.getTabObjectId();
      String editObjectId = 
        jumpData.isObjectTabCreation() ? null : tabObjectId;
      ((ExternalEditable)tabPageBean).editObject(editObjectId);            
    }

    return outcome;     
  }
  
  private boolean checkJumpSuitability(JumpData jumpData)
  {
    if (pageBean instanceof CheckJumpSuitability)
    {
      ((CheckJumpSuitability)pageBean).checkJumpSuitability(jumpData);
      return jumpData.isSuitable();
    }
    else
      return true;
  }
  
  private void notSuitable()
  {
    if (pageBean instanceof CheckJumpSuitability)
      error(((CheckJumpSuitability)pageBean).getNotSuitableMessage());
    else
      error("INVALID_OBJECT");
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
    if (!StringUtils.isBlank(jumpCommand)) //JS showObject
    {
      String[] parts = jumpCommand.split("\\|");
      if (parts[0].equals("type"))
      {
        data.setTypeId(parts[1]);
        data.setObjectId(parts[2]);
      }
      else if (parts[0].equals("tab"))
      {
        data.setJmid(parts[1]);
        data.setObjectId(parts[2]);
        data.setTabObjectId(parts[3]);
      }       
    }
    else //jump from URL
    {
      if (jumpMid == null) 
      {
        jumpMid = (String) parameters.getParameterValue(JUMP_MID_PARAMETER);
        if (jumpMid == null) //Jump by type (needs oc.xBean properties)
        { 
          ObjectBean objectBean = pageBean.getObjectBean();
          if (objectBean != null)
          {
            String objectTypeId = objectBean.getObjectTypeId(); 
            data.setTypeId(objectTypeId);
          }
          data.setXmid((String) parameters.getParameterValue("xmid"));
        }
        else 
          data.setJmid(jumpMid);
      }
      else
        data.setJmid(jumpMid); 
      
      data.setObjectId(getObjectId(parameters));
      data.setTabObjectId(getTabObjectId(parameters));       
    }
       
    return data;
  }  
  

  
}
