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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.dic.Property;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */
public class DynamicFormsManager extends WebBean implements Serializable
{
  public static final String ALLOWED_FORM_SELECTORS = "formSelectors";

  private static final String OBJECT_ID_PROPERTY = "_objectId";

  protected String currentTypeId;
  protected Map data = new HashMap();
  protected String selector;

  private String typeFormBuilderPrefix;
  private List<String> allowedFormSelectors;

  public DynamicFormsManager(String typeFormBuilderPrefix)
  {
    this.typeFormBuilderPrefix = typeFormBuilderPrefix;
  }

  public DynamicFormsManager(String typeFormBuilderPrefix, List<String> allowedFormSelectors)
  {
    this.typeFormBuilderPrefix = typeFormBuilderPrefix;
    this.allowedFormSelectors = allowedFormSelectors;
  }

  public String getCurrentTypeId()
  {
    return currentTypeId;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    this.currentTypeId = currentTypeId;
    findForm();
  }

  public void refreshForms()
  {
    findForm();
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public String getSelector()
  {
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public List<String> getAllowedFormSelectors()
  {
    if (this.allowedFormSelectors != null)
      return allowedFormSelectors;
    else
      return getSelectedMenuItem()
        .getMultiValuedProperty(DynamicFormsManager.ALLOWED_FORM_SELECTORS);
  }

  public Form getForm() throws Exception
  {
    if (selector != null)
    {
      FormFactory formFactory = FormFactory.getInstance();
      return formFactory.getForm(selector, getData());
    }
    return null;
  }

  public List<Property> transformFormDataToProperties()
  {
    if (data != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        List<Property> propertyList = converter.toPropertyList(data);
        Property auxObjectIdProperty =
          DictionaryUtils.getPropertyByName(propertyList, OBJECT_ID_PROPERTY);
        if (auxObjectIdProperty != null)
        {
          propertyList.remove(auxObjectIdProperty);
        }
        return propertyList;
      }
    }
    return null;
  }

  public void setFormDataFromProperties(List<Property> properties,
    String objectId)
  {
    if (properties != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        data = converter.toPropertyMap(properties);
      }
      else data = new HashMap();
    }
    if (objectId != null)
      data.put(OBJECT_ID_PROPERTY, objectId);
  }

  public boolean isTypeUndefined()
  {
    return currentTypeId == null || currentTypeId.length() == 0;
  }

  public org.santfeliu.dic.Type getSelectedType()
  {
    TypeCache typeCache = TypeCache.getInstance();
    if (!isTypeUndefined())
      return typeCache.getType(currentTypeId);
    else return null;
  }

  private void findForm()
  {
    String newSelector = null;

    List<String> definedFormSelectors = getAllowedFormSelectors();
    if (definedFormSelectors != null && definedFormSelectors.size() == 1)
    {
      String definedSelector = definedFormSelectors.get(0);
      if (definedSelector.startsWith("doc:"))
        newSelector = definedSelector;
      this.selector = newSelector;
      updateForm(selector);
    }

    if (newSelector == null && !isTypeUndefined())
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectorBase =
        typeFormBuilderPrefix + ":" + currentTypeId +
        TypeFormBuilder.USERID + userSessionBean.getUserId() +
        TypeFormBuilder.PASSWORD + userSessionBean.getPassword();
      FormFactory formFactory = FormFactory.getInstance();
      List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);

      if (descriptors != null && !descriptors.isEmpty())
      {
        for (FormDescriptor descriptor : descriptors)
        {
          if (matches(definedFormSelectors, descriptor.getSelector()) &&
            newSelector == null)
            newSelector = descriptor.getSelector();
        }

        if (newSelector == null || !newSelector.equals(selector))
        {
          this.selector = newSelector;
          data.clear();
          updateForm(selector); //TODO: resituar
        }
      }
      else
      {
        data.clear();
        this.selector = null;
      }
    }
    else if (isTypeUndefined())
    {
      data.clear();
      this.selector = null;
    }
  }

  private void updateForm(String selector)
  {
    if (selector != null)
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForm(selector);
    }
  }

  private boolean matches(List<String> formSelectors, String selector)
  {
    if (selector != null && formSelectors != null && !formSelectors.isEmpty())
    {
      for (String formSelector : formSelectors)
      {
        if (selector.matches(formSelector))
          return true;
      }
    }
    return false;
  }
}
