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
package org.santfeliu.webapp.modules.geo;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.security.AccessControl;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapRolesBean extends WebBean implements Serializable
{
  private String roleToAdd;

  @Inject
  GeoMapBean geoMapBean;

  public String getRoleToAdd()
  {
    return roleToAdd;
  }

  public void setRoleToAdd(String roleToAdd)
  {
    this.roleToAdd = roleToAdd;
  }

  public void updateAccessControl(AccessControl ac, String action)
  {
    ac.setAction(action);
  }

  public void removeAccessControl(AccessControl ac)
  {
    geoMapBean.getMapDocument().getAccessControl().remove(ac);
  }

  public void addAccessControl()
  {
    if (isBlank(roleToAdd)) return;

    MapDocument mapDocument = geoMapBean.getMapDocument();

    long count = mapDocument.getAccessControl().stream()
      .filter(ac -> ac.getRoleId().equals(roleToAdd)).count();

    if (count == 0)
    {
      AccessControl ac = new AccessControl();
      ac.setRoleId(roleToAdd);
      ac.setAction(READ_ACTION);
      mapDocument.getAccessControl().add(ac);
    }
    roleToAdd = null;
  }
}
