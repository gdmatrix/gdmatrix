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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class DicModuleBean
{
  public static DictionaryManagerPort getPort(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(DictionaryManagerService.class);
    return endpoint.getPort(DictionaryManagerPort.class, userId, password);
  }

  public static DictionaryManagerPort getPort(boolean asAdmin) throws Exception
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getPort(userId, password);
  }

  public List<SelectItem> getActionSelectItems(String typeId)
  {
    ArrayList<SelectItem> items = new ArrayList();

    org.santfeliu.dic.TypeCache typeCache =
      org.santfeliu.dic.TypeCache.getInstance();

    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    String bundleName = context.getApplication().getMessageBundle();
    final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);

    Set<String> allActions = new HashSet<>();
    allActions.addAll(DictionaryConstants.standardActions);
    allActions.addAll(typeCache.getType(typeId).getActions());

    for (String action : allActions)
    {
      SelectItem item = new SelectItem();
      item.setValue(action);
      item.setLabel(getLocalizedAction(action, bundle));
      items.add(item);
    }

    Collections.sort(items,
      (a, b) -> ((SelectItem)a).getLabel().compareTo(((SelectItem)b).getLabel()));

    return items;
  }

  public String getLocalizedActions(List<String> actions)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    String bundleName = context.getApplication().getMessageBundle();
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);

    List<String> actionLabels = new ArrayList<>(actions.size());
    for (String action : actions)
    {
      actionLabels.add(getLocalizedAction(action, bundle));
    }
    Collections.sort(actionLabels);

    return String.join(", ", actionLabels);
  }

  private String getLocalizedAction(String action, ResourceBundle bundle)
  {
    String value;
    try
    {
      value = bundle.getString(action);
    }
    catch (MissingResourceException ex)
    {
      value = action;
    }
    return value;
  }

}
