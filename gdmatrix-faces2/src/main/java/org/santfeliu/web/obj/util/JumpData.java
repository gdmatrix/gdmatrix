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


/**
 *
 * @author blanquepa
 */
public class JumpData implements Serializable
{
  public static final String NEW_OBJECT_PARAMETER = "new";
  public static final String DETAIL_PARAMETER = "detail";
  
  private String objectId;
  private String tabObjectId;
  private String jmid;
  private String xmid;
  private String typeId;
  private RequestType requestType;
  private enum RequestType {URL, JS}  
  private boolean suitable = true;
  
  public String getObjectId()
  {
    return objectId;
  }

  public void setObjectId(String objectId)
  {
    this.objectId = objectId;
  }

  public String getTabObjectId()
  {
    return tabObjectId;
  }

  public void setTabObjectId(String tabObjectId)
  {
    this.tabObjectId = tabObjectId;
  }

  public String getJmid()
  {
    return jmid;
  }

  public void setJmid(String mid)
  {
    this.jmid = mid;
  }

  public String getXmid()
  {
    return xmid;
  }

  public void setXmid(String xmid)
  {
    this.xmid = xmid;
  }

  public boolean isObjectCreation()
  {
    if (objectId != null)
      return objectId.equals(NEW_OBJECT_PARAMETER);
    else
      return false;    
  }   
  
  public boolean isObjectTabCreation()
  {
    if (tabObjectId != null)
      return tabObjectId.equals(NEW_OBJECT_PARAMETER);
    else
      return false;    
  }  
  
  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public boolean isSuitable()
  {
    return suitable;
  }

  public void setSuitable(boolean suitable)
  {
    this.suitable = suitable;
  }
  
  public boolean isJumpToTab()
  {
    return this.tabObjectId != null;
  }
  
  public boolean hasToJump()
  {
    return (this.objectId != null && (this.jmid != null || this.typeId != null));
  }

  public void setJSRequestType()
  {
    this.requestType = RequestType.JS;
  }
  
  public boolean isJSRequestType()
  {
    return this.requestType == RequestType.JS;
  }
  
  public void setURLRequestType()
  {
    this.requestType = RequestType.URL;
  }
  
  public boolean isURLRequestType()
  {
    return this.requestType == RequestType.URL;
  }
 
}
