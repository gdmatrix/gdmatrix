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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class FavoriteObjectsBean
{
  private List<FavoriteObject> favoriteObjects;
  private int updateCount;

  @Inject
  NavigatorBean navigatorBean;

  public List<FavoriteObject> getFavoriteObjects()
  {
    if (navigatorBean.getUpdateCount() != updateCount)
    {
      favoriteObjects = null;
    }

    if (favoriteObjects == null)
    {
      System.out.println(">>> getFavoriteObjects");

      favoriteObjects = new ArrayList<>();

      NavigatorBean.BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
      if (baseTypeInfo == null) return favoriteObjects;

      String baseTypeId = baseTypeInfo.getBaseTypeId();
      TypeBean typeBean = TypeBean.getInstance(baseTypeId);

      List<String> objectIdList = baseTypeInfo.getFavoriteObjectIdList();

      for (String objectId : objectIdList)
      {
        FavoriteObject favoriteObject = new FavoriteObject();
        favoriteObject.objectId = objectId;
        if (typeBean != null)
        {
          favoriteObject.description = typeBean.getDescription(objectId);
        }
        else
        {
          favoriteObject.description = baseTypeInfo + " " + objectId;
        }
        favoriteObjects.add(favoriteObject);
      }
      Collections.sort(favoriteObjects);
      updateCount = navigatorBean.getUpdateCount();
    }
    return favoriteObjects;
  }

  public boolean isSelectedObject(FavoriteObject favoriteObject)
  {
    NavigatorBean.BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    if (baseTypeInfo == null) return false;
    return favoriteObject.getObjectId().equals(baseTypeInfo.getObjectId());
  }

  public String getIcon()
  {
    return navigatorBean.getBaseTypeInfo().getIcon();
  }

  public class FavoriteObject implements Comparable
  {
    String objectId;
    String description;

    public String getObjectId()
    {
      return objectId;
    }

    public String getDescription()
    {
      return description;
    }

    @Override
    public int compareTo(Object o)
    {
      if (o instanceof FavoriteObject)
      {
        FavoriteObject other = (FavoriteObject)o;
        return description.compareTo(other.description);
      }
      return -1;
    }
  }
}
