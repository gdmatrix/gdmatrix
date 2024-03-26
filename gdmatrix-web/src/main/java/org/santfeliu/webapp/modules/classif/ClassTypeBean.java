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
package org.santfeliu.webapp.modules.classif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.classif.ClassFilter;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.classif.ClassifModuleBean.getPort;
import org.matrix.classif.Class;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class ClassTypeBean extends TypeBean<Class, ClassFilter>
{
  private static final String BUNDLE_PREFIX = "$$classificationBundle.";
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CLASS_TYPE;
  }

  @Override
  public String getObjectId(Class classObject)
  {
    return classObject.getClassId();
  }

  @Override
  public String describe(Class classObject)
  {
    return classObject.getClassId() + ": " + classObject.getTitle();
  }

  @Override
  public Class loadObject(String objectId)
  {
    try
    {
      String dateTime = getDefaultDateTime();
      return getPort(true).loadClass(objectId, dateTime);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Class classObject)
  {
    return classObject.getClassTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/classif/class.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "pi pi-tag", "/pages/classif/class_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_policies", 
      "material-icons-outlined mi-policy text-lg", 
      "/pages/policy/class_policies.xhtml", "classPoliciesTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public ClassFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    ClassFilter filter = new ClassFilter();
    if (query.matches(".{0,4}[0-9]+"))
    {
      filter.setClassId(query);
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
  public String filterToQuery(ClassFilter filter)
  {
    if (!StringUtils.isBlank(filter.getClassId()))
    {
      return filter.getClassId();
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
  public List<Class> find(ClassFilter filter)
  {
    try
    {
      String dateTime = getDefaultDateTime();
      filter.setStartDateTime(dateTime);
      filter.setEndDateTime(dateTime);
      return getPort(true).findClasses(filter);
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

  private String getDefaultDateTime()
  {
    return TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
  }
}
