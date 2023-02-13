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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import static org.matrix.dic.DictionaryConstants.TYPE_TYPE;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.util.WebUtils;

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
    System.out.println("TypeId: " + getTypeId());

    return typeTypeBean.getSelectItems(query, typeId, false, true);
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
    // TODO: preset typeId in typeFinderBean
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.find(TYPE_TYPE,
      getValueExpression().getExpressionString());
  }

}
