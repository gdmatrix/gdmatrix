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

import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.el.CompositeComponentExpressionHolder;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.NavigatorBean.Leap;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.dic.TypeFinderBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.util.WebUtils;
import static org.matrix.dic.DictionaryConstants.TYPE_TYPE;
import org.primefaces.event.SelectEvent;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class TypeReferenceBean extends ObjectReferenceBean
{
  @Inject
  TypeTypeBean typeTypeBean;

  @Override
  public List<SelectItem> complete(String query)
  {
    String typeId = getTypeId();
    boolean showNavigatorItems = isShowNavigatorItems();

    List<SelectItem> selectItems =
      typeTypeBean.getSelectItems(query, typeId, showNavigatorItems, true);

    if (StringUtils.isBlank(query) && showNavigatorItems)
    {
      String objectId = WebUtils.getValue("#{cc.attrs.value}");
      if (!StringUtils.isBlank(objectId))
      {
        boolean found = false;
        Iterator<SelectItem> iter = selectItems.iterator();
        while (iter.hasNext() && !found)
        {
          if (iter.next().getValue().equals(objectId))
          {
            found = true;
          }
        }
        if (!found)
        {
          selectItems.add(0,
            new SelectItem(objectId, typeTypeBean.getDescription(objectId)));
        }
      }
    }
    return selectItems;
  }

  @Override
  public SelectItem getSelectItem()
  {
    String objectId = WebUtils.getValue("#{cc.attrs.value}");
    if (StringUtils.isBlank(objectId))
    {
      return new SelectItem(NEW_OBJECT_ID, "");
    }
    else
    {
      return new SelectItem(objectId, typeTypeBean.getDescription(objectId));
    }
  }

  @Override
  public String show()
  {
    String typeId = WebUtils.getValue("#{cc.attrs.value}");
    if (typeId == null) typeId = NEW_OBJECT_ID;

    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.show(TYPE_TYPE, typeId);
  }

  @Override
  public String find()
  {
    resetFormSelector();
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.execute(new SelectTypeLeap(getTypeId()), true,
      getValueExpression().getExpressionString());
  }

  @Override
  public void onItemSelect(SelectEvent event)
  {
    super.onItemSelect(event);
    resetFormSelector();
  }

  @Override
  public void onClear()
  {
    WebUtils.setValue("#{cc.attrs.value}", String.class, null);
    resetFormSelector();
  }

  protected void resetFormSelector()
  {
    CompositeComponentExpressionHolder exprHolder =
      (CompositeComponentExpressionHolder)WebUtils.getValue("#{cc.attrs}");

    if (exprHolder.getExpression("formSelector") != null)
    {
      WebUtils.setValue("#{cc.attrs.formSelector}", String.class, null);
    }
  }

  public class SelectTypeLeap extends Leap
  {
    String typeId;

    public SelectTypeLeap(String typeId)
    {
      super(TYPE_TYPE);
      this.typeId = typeId;
    }

    @Override
    public void construct(ObjectBean objectBean)
    {
      TypeFinderBean typeFinderBean = (TypeFinderBean)objectBean.getFinderBean();
      typeFinderBean.getFilter().setSuperTypeId(typeId);
      typeFinderBean.setFilterTabSelector(1);

      objectBean.setObjectId(NEW_OBJECT_ID);
      objectBean.setSearchTabSelector(0);
      objectBean.setEditTabSelector(0);
      objectBean.load();
    }

  }
}
