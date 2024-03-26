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
package org.santfeliu.webapp.modules.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.policy.PolicyModuleBean.getPort;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class PolicyTypeBean extends TypeBean<Policy, PolicyFilter>
{
  private static final String BUNDLE_PREFIX = "$$policyBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.POLICY_TYPE;
  }

  @Override
  public String getObjectId(Policy policy)
  {
    return policy.getPolicyId();
  }

  @Override
  public String describe(Policy policy)
  {
    return policy.getTitle() + " (" + policy.getPolicyId() + ")";
  }

  @Override
  public Policy loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadPolicy(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Policy policy)
  {
    return policy.getPolicyTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/policy/policy.xhtml");

    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab =
      new SearchTab("List", "/pages/policy/policy_list.xhtml");
//    searchTab.getColumns().add(new Column("policyId",
//      BUNDLE_PREFIX + "policySearch_policyId", "col-1"));
//    searchTab.getColumns().add(new Column("policyTypeId",
//      BUNDLE_PREFIX + "policySearch_policyTypeId", "col-3"));
//    Column titleColumn = new Column("title",
//      BUNDLE_PREFIX + "policySearch_title", "col-6");
//    titleColumn.setIcon("text-xl mr-2");
//    searchTab.getColumns().add(titleColumn);

    searchTabs.add(searchTab);

    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "material-icons-outlined mi-policy text-lg", "/pages/policy/policy_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_classes", "pi pi-tag", "/pages/policy/policy_classes.xhtml", "policyClassesTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public PolicyFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    PolicyFilter filter = new PolicyFilter();
    if (query.matches("\\d+"))
    {
      filter.setPolicyId(query);
    }
    else
    {
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";      
      filter.setTitle(query);
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(PolicyFilter filter)
  {
    if (filter.getPolicyId() != null)
    {
      return filter.getPolicyId();
    }
    else if (filter.getTitle() != null)
    {
      String query = filter.getTitle();
      if (query.startsWith("%")) query = query.substring(1);
      if (query.endsWith("%")) query = query.substring(0, query.length() - 1);
      return query;
    }
   return "";
  }

  @Override
  public List<Policy> find(PolicyFilter filter)
  {
    try
    {
      return getPort(true).findPolicies(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public String getValue(Object object)
  {
    return String.valueOf(object);
  }

}
