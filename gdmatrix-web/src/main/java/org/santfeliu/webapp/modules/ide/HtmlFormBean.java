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
package org.santfeliu.webapp.modules.ide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.form.Form;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlParser;
import org.santfeliu.webapp.util.ComponentUtils;
import static org.santfeliu.webapp.util.FormImporter.ACTION_METHOD_OPTION;
import static org.santfeliu.webapp.util.FormImporter.ACTION_UPDATE_OPTION;

/**
 *
 * @author realor
 */
@Named("htmlFormBean")
@RequestScoped
public class HtmlFormBean extends FacesBean
{
  @Inject IdeBean ideBean;
  Map<String, Object> data = new HashMap();
  boolean update = true;

  public Map<String, Object> getData()
  {
    return data;
  }

  public String getDataAsJSON()
  {
    Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .setPrettyPrinting().create();
    return gson.toJson(data);
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = ComponentUtils.postAddToView(event);
    if (panel != null && update)
    {
      updateComponents(panel);
      update = false;
    }
  }

  private void updateComponents(UIComponent panel)
  {
    try
    {
      panel.getChildren().clear();

      String source = ideBean.getDocument().getSource();
      if (!StringUtils.isBlank(source))
      {
        HtmlForm form = new HtmlForm();
        HtmlParser parser = new HtmlParser(form);
        parser.parse(new StringReader(source));

        Map<String, Object> options = new HashMap<>();
        options.put(ACTION_UPDATE_OPTION, ":mainform:cnt");

        ComponentUtils.includeFormComponents(panel, form,
           "htmlFormBean.data", "htmlFormBean.data", options);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
  }

  public void update()
  {
    this.update = true;
    this.data.clear();
  }
}
