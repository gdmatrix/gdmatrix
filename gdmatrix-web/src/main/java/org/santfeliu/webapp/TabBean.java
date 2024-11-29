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

import org.santfeliu.webapp.setup.EditTab;
import java.io.Serializable;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.setup.ActionObject;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
public abstract class TabBean extends BaseBean
{
  private String objectId = NavigatorBean.NEW_OBJECT_ID;

  public String getObjectId()
  {
    return objectId;
  }

  public void setObjectId(String objectId)
  {
    this.objectId = objectId;
  }

  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(objectId);
  }

  public boolean isModified()
  {
    return false;
  }

  public void load() throws Exception
  {
  }

  public void store() throws Exception
  {
  }

  public boolean isDialogVisible()
  {
    return false;
  }

  @Override
  public Serializable saveState()
  {
    return ""; // force NagigatorBean to call restoreBean
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  public String getAbsoluteId(String relativeId)
  {
    EditTab editTab = getObjectBean().getActiveEditTab();
    if (editTab == null) return "";

    String beanName = WebUtils.getBeanName(this);

    if (!beanName.equals(editTab.getBeanName())) return "";

    String id = ":mainform:search_tabs:tabs";
    if (editTab.getSubviewId() != null) id += ":" + editTab.getSubviewId();
    id += ":" + relativeId;

    return id;
  }

  @Override
  public void clear()
  {
    setObjectId(NEW_OBJECT_ID);
    try
    {
      load();
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  public String getTabBaseTypeId()
  {
    EditTab editTab = getObjectBean().getActiveEditTab();
    return (editTab != null ? editTab.getBaseTypeId() : null);
  }

  public boolean isLeafBaseType()
  {
    String baseTypeId = getTabBaseTypeId();
    if (baseTypeId == null) return false;
    TypeCache typeCache = TypeCache.getInstance();
    Type baseType = typeCache.getType(baseTypeId);
    return (baseType != null ? baseType.isLeaf() : false);
  }

  protected String getCreationTypeId()
  {
    String typeId = getTabBaseTypeId();
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null && type.isInstantiable())
      {
        return typeId;
      }
    }
    return null;
  }

  protected Object executeTabAction(String actionName, Object object)
  {
    ActionObject actionObject =
      getObjectBean().executeTabAction(actionName, object);
    return actionObject.getObject();
  }

}
