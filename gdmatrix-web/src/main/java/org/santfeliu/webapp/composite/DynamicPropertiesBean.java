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
package org.santfeliu.webapp.composite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.helpers.PropertyHelper;
import org.santfeliu.webapp.util.ComponentUtils;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DynamicPropertiesBean implements Serializable
{
  private final Map<String, List<SelectItem>> selectItemMap = new HashMap<>();
  private PropertyHelper propertyHelper;

  @PostConstruct
  public void init()
  {
    propertyHelper = new PropertyHelper()
    {
      @Override
      public List<Property> getProperties()
      {
        return WebUtils.getValue("#{cc.attrs.properties}");
      }
    };
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }

  public void setPropertyHelper(PropertyHelper propertyHelper)
  {
    this.propertyHelper = propertyHelper;
  }

  public List<SelectItem> getSelectItems()
  {
    String typeId = getTypeId();

    List<SelectItem> selectItems = selectItemMap.get(typeId);
    if (selectItems == null)
    {
      selectItems = new ArrayList<>();
      if (!StringUtils.isBlank(typeId))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

        String selectorBase =
          TypeFormBuilder.PREFIX + ":" + typeId +
          TypeFormBuilder.USERID + userSessionBean.getUserId() +
          TypeFormBuilder.PASSWORD + userSessionBean.getPassword();

        List<FormDescriptor> descriptors =
          FormFactory.getInstance().findForms(selectorBase);

        for (FormDescriptor descriptor : descriptors)
        {
          String selector = descriptor.getSelector();
          String label = descriptor.getTitle();
          selectItems.add(new SelectItem(selector, label));
        }
      }
      if (selectItems.isEmpty())
      {
        selectItems.add(new SelectItem("", "No form"));
      }
      selectItemMap.put(typeId, selectItems);
    }
    return selectItems;
  }

  public void onItemSelect(FacesEvent event)
  {
    UIComponent component = event.getComponent();
    UIComponent panel = component.findComponent("dyn_form");
    if (panel != null)
    {
      updateComponents(panel);
    }
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = event.getComponent();
    updateComponents(panel);
  }

  private void updateComponents(UIComponent panel)
  {
    try
    {
      List<SelectItem> selectItems = getSelectItems();
      String formSelector = getFormSelector();

      if (StringUtils.isBlank(formSelector) ||
          !isValidFormSelector(formSelector, selectItems))
      {
        // set first formSelector
        formSelector = (String)selectItems.get(0).getValue();
        setFormSelector(formSelector);
      }

      EditableValueHolder hidden =
        (EditableValueHolder)panel.findComponent("form_selector");
      String actualFormSelector = (String)hidden.getValue();

      if (!formSelector.equals(actualFormSelector))
      {
        ((EditableValueHolder)hidden).setValue(formSelector);

        panel.getChildren().clear();

        if (!StringUtils.isBlank(formSelector))
        {
          System.out.println(">>>> importing form: " + formSelector);

          ComponentUtils.includeFormComponents(panel, formSelector,
             "dynamicPropertiesBean.propertyHelper.value",
            Collections.emptyMap());
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public String getFormSelector()
  {
    return WebUtils.getValue("#{cc.attrs.formSelector}");
  }

  public void setFormSelector(String formSelector)
  {
    WebUtils.setValue("#{cc.attrs.formSelector}", String.class, formSelector);
  }

  public String getTypeId()
  {
    return WebUtils.getValue("#{cc.attrs.typeId}");
  }

  private boolean isValidFormSelector(String formSelector,
    List<SelectItem> selectItems)
  {
    for (SelectItem selectItem : selectItems)
    {
      if (formSelector.equals(selectItem.getValue())) return true;
    }
    return false;
  }
}
