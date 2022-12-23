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

import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
public abstract class FinderBean extends BaseBean
{
  private int tabIndex;
  private int objectPosition = -1;

  public int getTabIndex()
  {
    return tabIndex;
  }

  public void setTabIndex(int tabIndex)
  {
    this.tabIndex = tabIndex;
  }

  public abstract void find();

  public abstract void smartFind();

  public int getObjectCount()
  {
    return 0;
  }

  public String getObjectId(int position)
  {
    return NEW_OBJECT_ID;
  }

  public int getObjectPosition()
  {
    return objectPosition;
  }

  public void setObjectPosition(int objectPosition)
  {
    this.objectPosition = objectPosition;
  }

  public boolean isScrollEnabled()
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return false;

    ObjectBean objectBean = getObjectBean();

    return getObjectCount() > 1 && !objectBean.isNew() &&
      objectBean.getObjectId().equals(getObjectId(objectPosition));
  }

  public boolean hasNext()
  {
    return objectPosition >= 0 && objectPosition < getObjectCount() - 1;
  }

  public boolean hasPrevious()
  {
    return objectPosition >= 1;
  }

  public void view(int objectPosition)
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return;

    this.objectPosition = objectPosition;
    String objectId = getObjectId(objectPosition);
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      navigatorBean.view(objectId);
    }
  }

  public void viewNext()
  {
    if (hasNext())
    {
      view(objectPosition + 1);
    }
  }

  public void viewPrevious()
  {
    if (hasPrevious())
    {
      view(objectPosition - 1);
    }
  }
}
