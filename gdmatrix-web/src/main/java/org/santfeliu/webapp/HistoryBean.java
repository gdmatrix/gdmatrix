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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.webapp.NavigatorBean.ReturnInfo;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class HistoryBean
{
  @Inject
  NavigatorBean navigatorBean;

  public String getDescription(ReturnInfo returnInfo)
  {
    String baseTypeId = returnInfo.getBaseTypeId();
    TypeBean typeBean = TypeBean.getInstance(baseTypeId);
    if (typeBean == null) return baseTypeId + " " + returnInfo.getObjectId();
    return typeBean.getDescription(returnInfo.getObjectId());
  }

  public boolean isCurrentBaseType(ReturnInfo returnInfo)
  {
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    return returnInfo.getBaseTypeId().equals(baseTypeId);
  }

  public void view(ReturnInfo returnInfo)
  {
    navigatorBean.view(returnInfo.getObjectId());
  }

  public String show(ReturnInfo returnInfo)
  {
    return navigatorBean.show(returnInfo.getBaseTypeId(),
      returnInfo.getObjectId(), returnInfo.getTabIndex());
  }

  public String getIcon(ReturnInfo returnInfo)
  {
    String baseTypeId = returnInfo.getBaseTypeId();
    NavigatorBean.BaseTypeInfo baseTypeInfo =
      navigatorBean.getBaseTypeInfo(baseTypeId);
    return baseTypeInfo.getIcon();
  }
}
