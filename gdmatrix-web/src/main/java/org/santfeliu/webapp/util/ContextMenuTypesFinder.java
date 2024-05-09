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
package org.santfeliu.webapp.util;

import javax.enterprise.inject.spi.CDI;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.TemplateBean;

/**
 *
 * @author realor
 */
public class ContextMenuTypesFinder extends GlobalMenuTypesFinder
{
  @Override
  public String findTopMid()
  {
    TemplateBean templateBean = CDI.current().select(TemplateBean.class).get();
    return templateBean.getContextMid();
  }

  @Override
  public String findTypeMid(String typeId)
  {
    TemplateBean templateBean = CDI.current().select(TemplateBean.class).get();
    String contextMid = templateBean.getContextMid();

    if (contextMid != null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor =
        userSessionBean.getMenuModel().getMenuItem(contextMid);
      MatchItem foundMenuItem = getMatchItem(cursor.getFirstChild(), typeId, null);

      if (foundMenuItem != null && !foundMenuItem.getCursor().isNull())
      {
        return foundMenuItem.getCursor().getMid();
      }
    }

    return super.findTypeMid(typeId);
  }
}
