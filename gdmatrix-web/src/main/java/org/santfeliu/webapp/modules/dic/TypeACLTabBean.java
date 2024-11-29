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
package org.santfeliu.webapp.modules.dic;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.AccessControl;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.security.ACLTabBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class TypeACLTabBean extends ACLTabBean
{
  @Inject
  TypeObjectBean typeObjectBean;

  @Override
  public List<AccessControl> getAccessControlList()
  {
    return typeObjectBean.getType().getAccessControl();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return typeObjectBean;
  }

  @Override
  public List<SelectItem> getActionSelectItems()
  {
    List<SelectItem> items = super.getActionSelectItems();
    items.add(new SelectItem(
      DictionaryConstants.DERIVE_DEFINITION_ACTION,
      dicModuleBean.getLocalizedAction(
        DictionaryConstants.DERIVE_DEFINITION_ACTION)));

    items.add(new SelectItem(
      DictionaryConstants.MODIFY_DEFINITION_ACTION,
      dicModuleBean.getLocalizedAction(
        DictionaryConstants.MODIFY_DEFINITION_ACTION)));

    return items;
  }

}
