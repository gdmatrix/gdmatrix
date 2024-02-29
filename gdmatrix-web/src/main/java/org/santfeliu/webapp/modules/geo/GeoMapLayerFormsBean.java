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
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.santfeliu.form.FormFactory;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.metadata.LayerForm;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapLayerFormsBean extends WebBean implements Serializable
{
  private LayerForm editingLayerForm;

  @Inject
  GeoMapBean geoMapBean;

  public List<LayerForm> getLayerForms()
  {
    StyleMetadata styleMetadata = new StyleMetadata(geoMapBean.getStyle());
    return styleMetadata.getLayerForms(true);
  }

  public LayerForm getEditingLayerForm()
  {
    return editingLayerForm;
  }

  public void addLayerForm()
  {
    editingLayerForm = new LayerForm();
    geoMapBean.setDialogVisible(true);
  }

  public void editLayerForm(LayerForm form)
  {
    editingLayerForm = form;
    geoMapBean.setDialogVisible(true);
  }

  public void removeLayerForm(LayerForm form)
  {
    getLayerForms().remove(form);
  }

  public void refreshLayerForms()
  {
    FormFactory formFactory = FormFactory.getInstance();

    List<LayerForm> layerForms = getLayerForms();
    if (layerForms == null) return;

    for (LayerForm layerForm : layerForms)
    {
      String formSelector = layerForm.getFormSelector();
      if (formSelector == null)
      {
        String layer = layerForm.getLayer();
        int index = layer.indexOf(":");
        if (index != -1) layer = layer.substring(index + 1);
        formSelector = "form:" + layer;
      }
      else
      {
        int index = formSelector.indexOf(":");
        if (index == -1) formSelector = "form:" + formSelector;
      }
      formFactory.clearForm(formSelector);
    }
    growl("FORMS_UPDATED");
  }

  public void acceptLayerForm()
  {
    if (!getLayerForms().contains(editingLayerForm))
    {
      getLayerForms().add(editingLayerForm);
    }
    editingLayerForm = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelLayerForm()
  {
    editingLayerForm = null;
    geoMapBean.setDialogVisible(false);
  }
}
