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
package org.santfeliu.webapp.modules.doc;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.Document;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.security.ACLTabBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class DocumentACLTabBean extends ACLTabBean
{
  @Inject
  DocumentObjectBean documentObjectBean;

  @Override
  public List<AccessControl> getAccessControlList()
  {
    return documentObjectBean.getDocument().getAccessControl();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public List<AccessControlEdit> getTypeRows()
  {
    List<AccessControlEdit> edits = new ArrayList<>();
    if (!documentObjectBean.isNew())
    {
      Document document = documentObjectBean.getDocument();
      String typeId = document.getDocTypeId();
      if (!StringUtils.isBlank(typeId))
      {
        TypeCache typeCache = TypeCache.getInstance();
        Type type = typeCache.getType(typeId);
        createAccessControlEdits(type.getAccessControl(), edits);
      }
    }
    return edits;
  }

}
