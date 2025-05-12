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
package org.santfeliu.webapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.NavigatorBean.DirectLeap;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class HistoryBean implements Serializable
{
  @Inject
  NavigatorBean navigatorBean;
  List<DirectLeap> entries;
  private int updateCount = -1;

  public List<DirectLeap> getEntries()
  {
    if (WebUtils.isRenderResponsePhase() &&
        navigatorBean.getUpdateCount() != updateCount)
    {
      entries = null;
    }

    if (entries == null)
    {
      entries = new ArrayList<>();
      entries.addAll(navigatorBean.getHistory().getEntries());
      updateCount = navigatorBean.getUpdateCount();
      removeDuplicatedEntries();
    }
    return entries;
  }

  public String getDescription(DirectLeap leap)
  {
    String baseTypeId = leap.getBaseTypeId();
    if (StringUtils.isBlank(leap.getObjectId()))
    {
      NavigatorBean.BaseTypeInfo baseTypeInfo =
        navigatorBean.getBaseTypeInfo(baseTypeId);
      return baseTypeInfo == null ? baseTypeId : baseTypeInfo.getLabel();
    }
    else
    {
      TypeBean typeBean = TypeBean.getInstance(baseTypeId);
      if (typeBean == null)
        return baseTypeId + " " + leap.getObjectId();
      else
        return typeBean.getDescription(leap.getObjectId());
    }
  }

  public boolean isCurrentBaseType(DirectLeap leap)
  {
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    return leap.getBaseTypeId().equals(baseTypeId);
  }

  public void view(DirectLeap leap)
  {
    navigatorBean.view(leap.getObjectId(), 0, true);
  }

  public String show(DirectLeap leap)
  {
    return navigatorBean.show(leap.getBaseTypeId(),
      leap.getObjectId(), leap.getEditTabSelector());
  }

  public String getIcon(DirectLeap leap)
  {
    String baseTypeId = leap.getBaseTypeId();
    NavigatorBean.BaseTypeInfo baseTypeInfo =
      navigatorBean.getBaseTypeInfo(baseTypeId);
    return baseTypeInfo == null ? null : baseTypeInfo.getIcon();
  }
  
  private void removeDuplicatedEntries() 
  {
    List<DirectLeap> copyEntries = new ArrayList(entries);
    for (DirectLeap entry : copyEntries)
    {
      if (!NavigatorBean.NEW_OBJECT_ID.equals(entry.getObjectId()))
      {      
        if (entry.getObjectId().equals(navigatorBean.getObjectId()))
        {
          entries.remove(entry);        
        }
        else
        {
          for (DirectLeap otherEntry : copyEntries)
          {
            if (otherEntry != entry && 
              otherEntry.getObjectId().equals(entry.getObjectId()))
            {
              if (isTypeDescendantOf(otherEntry, entry))
              {
                entries.remove(entry);
                break;
              }
            }
          }     
        }
      }
    }
  }  
  
  private boolean isTypeDescendantOf(DirectLeap entry1, DirectLeap entry2)
  {
    Type type = TypeCache.getInstance().getType(entry1.getBaseTypeId());
    if (type != null)
    {
      return type.isDerivedFrom(entry2.getBaseTypeId());          
    }
    return false;
  }  
}
