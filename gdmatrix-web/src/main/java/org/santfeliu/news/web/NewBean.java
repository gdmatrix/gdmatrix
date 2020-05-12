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
package org.santfeliu.news.web;

import org.matrix.news.New;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author unknown
 */
public class NewBean extends ObjectBean
{
  private static final int HEADLINE_MAX_LENGTH = 40;

  public NewBean()
  {
  }

  public String getObjectTypeId()
  {
    return "New";
  }

  public String getDescription()
  {
    NewMainBean newMainBean = (NewMainBean)getBean("newMainBean");
    New newObject = newMainBean.getNewObject();
    return getDescription(newObject);
  }

  public String getDescription(String objectId)
  {
    try
    {
      New newObject = NewsConfigBean.getPort().loadNewFromCache(objectId);
      return getDescription(newObject);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  public String getDescription(New newObject)
  {
    StringBuffer buffer = new StringBuffer();
    if (newObject == null) return "";
    else
    {
      String headline = newObject.getHeadline();
      if (headline != null)
      {
        if (headline.length() > HEADLINE_MAX_LENGTH)
        {
          headline = headline.substring(0, HEADLINE_MAX_LENGTH) + "...";
        }
        buffer.append(headline + " ");
      }
      buffer.append("(");
      buffer.append(newObject.getNewId());
      buffer.append(")");
    }
    return buffer.toString();
  }

/*
  public String createRedirected()
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    return super.getControllerBean().create(mic.getProperty(PARAM_EDIT_NODE));
  }
*/
  
  @Override
  public String cancel()  
  {
    NewsManagerClient.getCache().clear();
    return super.cancel();
  }  
  
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        NewsConfigBean.getPort().removeNew(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
}
