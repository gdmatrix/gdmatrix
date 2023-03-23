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
package org.santfeliu.webapp.composite;

import java.util.Collections;
import java.util.List;
import javax.el.ValueExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.el.CompositeComponentExpressionHolder;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class ObjectReferenceBean
{
  public List<SelectItem> complete(String query)
  {
    String typeId = getTypeId();
    boolean showNavigatorItems = isShowNavigatorItems();

    TypeBean typeBean = TypeBean.getInstance(typeId);
    if (typeBean == null) return Collections.EMPTY_LIST;

    return typeBean.getSelectItems(query, typeId, showNavigatorItems, true);
  }

  public SelectItem getSelectItem()
  {
    String objectId = WebUtils.getValue("#{cc.attrs.value}");
    if (StringUtils.isBlank(objectId))
    {
      return new SelectItem(NEW_OBJECT_ID, "");
    }
    else
    {
      String typeId = getTypeId();
      TypeBean typeBean = TypeBean.getInstance(typeId);
      if (typeBean == null) return  new SelectItem(NEW_OBJECT_ID, "");

      return new SelectItem(objectId, typeBean.getDescription(objectId));
    }
  }

  public void setSelectItem(SelectItem selectItem)
  {
    if (selectItem != null)
    {
      String objectId = (String)selectItem.getValue();
      WebUtils.setValue("#{cc.attrs.value}", String.class, objectId);
    }
  }

  public void onItemSelect(SelectEvent event)
  {
    SelectItem selectItem = (SelectItem)event.getObject();
    String objectId = (String)selectItem.getValue();
    if (NEW_OBJECT_ID.equals(objectId)) objectId = null;
    WebUtils.setValue("#{cc.attrs.value}", String.class, objectId);
  }

  public void onClear()
  {
    WebUtils.setValue("#{cc.attrs.value}", String.class, null);
  }

  public String show()
  {
    String objectId = WebUtils.getValue("#{cc.attrs.value}");
    if (objectId == null) objectId = NEW_OBJECT_ID;

    String typeId = getTypeId();

    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.show(typeId, objectId);
  }

  public String find()
  {
    String typeId = getTypeId();
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.find(typeId,
      getValueExpression().getExpressionString());
  }

  public String getTypeId()
  {
    return WebUtils.getValue("#{cc.attrs.type}");
  }

  public boolean isShowNavigatorItems()
  {
    Object value = WebUtils.getValue("#{cc.attrs.showNavigatorItems}");
    if (value instanceof Boolean) return ((Boolean)value);
    return "true".equals(value);
  }

  public ValueExpression getValueExpression()
  {
    CompositeComponentExpressionHolder exprHolder =
      (CompositeComponentExpressionHolder)WebUtils.getValue("#{cc.attrs}");

    return exprHolder.getExpression("value");
  }

}
