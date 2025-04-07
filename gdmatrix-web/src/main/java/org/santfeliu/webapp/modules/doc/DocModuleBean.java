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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.modules.dic.DicModuleBean;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class DocModuleBean
{
  public static DocumentManagerPort getPort(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(DocumentManagerService.class);

    return endpoint.getPort(DocumentManagerPort.class, userId, password);
  }

  public static DocumentManagerPort getPort(boolean asAdmin) throws Exception
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    } else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getPort(userId, password);
  }
  
  public static Map getUserDocTypes(String action)
  {
    Map typesMap = new HashMap();
    try
    {
      for (Type userDocType : getActionDocTypes(action))
      {
        String typeId = userDocType.getTypeId();
        org.santfeliu.dic.Type type =
          TypeCache.getInstance().getType(typeId);

        if (type.isInstantiable())
        {
          String typePath = null;
          if (DictionaryConstants.DOCUMENT_TYPE.equals(typeId))
            typePath = type.formatTypePath(false, true, true);
          else
            typePath = type.formatTypePath(false, true, false);

          typesMap.put(typeId, typePath);
        }
      }
    }
    catch(Exception ex)
    {
      //return empty map
    }    
    return typesMap;
  }

  private static List<Type> getActionDocTypes(String action)
    throws Exception
  {
    TypeFilter filter = new TypeFilter();
    filter.setAction(action);
    filter.setTypePath("/" + DictionaryConstants.DOCUMENT_TYPE + "/%");
    return DicModuleBean.getPort(false).findTypes(filter);
  }  
}
