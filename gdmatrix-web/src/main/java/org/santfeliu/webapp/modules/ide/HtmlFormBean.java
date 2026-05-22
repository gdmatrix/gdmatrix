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
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlParser;
import org.santfeliu.form.type.html.HtmlView;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.util.ComponentUtils;
import static org.santfeliu.webapp.util.FormImporter.ACTION_UPDATE_OPTION;
import static org.santfeliu.webapp.util.FormImporter.SUBMIT_BUTTON_OPTION;

/**
 *
 * @author realor
 */
@Named("htmlFormBean")
@RequestScoped
public class HtmlFormBean extends WebBean
{

  private static final String CONTAINER_ID = "panel";

  @Inject
  IdeBean ideBean;

  // Map<String, Object> data = new HashMap();
  Map<String, Object> dataUni = new HashMap();
  Map<String, Object> dataMulti = new MultiValueMap();

  boolean update = true;
  private boolean previewVisible = true;
  private boolean sourceModified;
  private String userPrompt;
  private boolean evaluateForm = false;
  private HtmlForm form = new HtmlForm();

  public IdeBean getIdeBean()
  {
    return ideBean;
  }

  public HtmlForm getForm()
  {
    return form;
  }

  public void setForm(HtmlForm form)
  {
    this.form = form;
  }

  public boolean isUpdate()
  {
    return update;
  }

  public void setUpdate(boolean update)
  {
    this.update = update;
  }

  public boolean isSourceModified()
  {
    return sourceModified;
  }

  public void setSourceModified(boolean sourceModified)
  {
    this.sourceModified = sourceModified;
  }

  public String getUserPrompt()
  {
    return userPrompt;
  }

  public void setUserPrompt(String userPrompt)
  {
    this.userPrompt = userPrompt;
  }

  public Map<String, Object> getData()
  {
    Map<String, Object> combined = new HashMap<>();
    combined.putAll(dataUni);
    combined.putAll(dataMulti);
    return combined;
  }

  public Map<String, Object> getDataUni()
  {
    return dataUni;
  }

  public void setDataUni(Map<String, Object> m)
  {
    this.dataUni = m;
  }

  public Map<String, Object> getDataMulti()
  {
    return dataMulti;
  }

  public void setDataMulti(Map<String, Object> m)
  {
    this.dataMulti = m;
  }

  public String getDataAsJSON()
  {
    Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .setPrettyPrinting().create();
    return gson.toJson(getData());
  }
  
  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = ComponentUtils.postAddToView(event);
    if (panel != null && update)
    {
      try
      {
        panel.getChildren().clear();
        if(previewVisible)
        {
          updateComponents(panel);
        }
        else
        {
          System.out.println("PREVIEW HIDDEN");
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        error("Error loading dynamic components: " + ex.getMessage());
      }
      finally
      {
        update = false;
      }
    }
  }
  
  private void updateComponents(UIComponent panel)
  {
    try
    {
      System.out.println("UPDATE COMPONENTS");
      panel.getChildren().clear();

      String source = ideBean.getDocument().getSource();
      if (!StringUtils.isBlank(source))
      {
        //HtmlForm form = new HtmlForm();
        HtmlParser parser = new HtmlParser(form);
        parser.parse(new StringReader(source));

        Form finalForm = form;
        if (evaluateForm)
        {
          finalForm = form.evaluate(getData());
        }

        Map<String, Object> options = new HashMap<>();
        options.put(ACTION_UPDATE_OPTION, ":mainform:cnt");
        options.put(SUBMIT_BUTTON_OPTION, "mainform:editor:submit_form"); //Only required fields are validated if the submit button is used.

        ComponentUtils.includeFormComponents(panel, finalForm,
          "htmlFormBean.dataUni",
          "htmlFormBean.dataMulti", // multivalor
          options);
      }
      else
      {
        System.out.println("SOURCE IS BLANK");
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
    this.evaluateForm = false;
    this.previewVisible = true;
    this.update = true;
    //this.data.clear();
    this.dataUni.clear();
    this.dataMulti.clear();
  }

  public void evaluate()
  {
    this.evaluateForm = true;
    this.previewVisible = true;
    this.update = true;
    //this.data.clear();
    this.dataUni.clear();
    this.dataMulti.clear();
  }

  public void clear()
  {
    previewVisible = false;
    this.update = true;
    //this.data.clear();
    this.dataUni.clear();
    this.dataMulti.clear();
  }

  private HtmlForm loadForm() throws Exception
  {
    String source = ideBean.getDocument().getSource();
    if (source == null || source.trim().isEmpty())
    {
      return null;
    }
    //HtmlForm form = new HtmlForm();
    form = new HtmlForm();
    HtmlParser parser = new HtmlParser(form);
    parser.parse(new StringReader(source));
    return form;
  }

  private void showForm(HtmlForm form, List<HtmlView> views, Boolean assignOrder) throws Exception
  {
    if (views.isEmpty())
    {
      return;
    }
    RepairUtils repairUtils = new RepairUtils();
    // Rebuild container with the new view    
    repairUtils.rebuildHtml(form, CONTAINER_ID, views, assignOrder);
    // Transform to String
    StringWriter sw = new StringWriter();
    form.write(new WriterOutputStream(sw, StandardCharsets.UTF_8), null);
    ideBean.getDocument().setSource(sw.toString());
    this.update = true;
    // Mark the check
    sourceModified = true;
    ideBean.markChanged();
  }

  public void repairViewOrder()
  {
    try
    {
      HtmlForm form = loadForm();
      if (form == null)
      {
        return;
      }
      RepairUtils repairUtils = new RepairUtils();
      List<HtmlView> views = repairUtils.extractViews(form, CONTAINER_ID);
      // Sorting
      repairUtils.performSort(views);
      showForm(form, views, true);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
  }

  public void repairLabelsAndGroups()
  {
    try
    {
      HtmlForm form = loadForm();
      if (form == null)
      {
        return;
      }
      RepairUtils repairUtils = new RepairUtils();
      List<HtmlView> views = repairUtils.extractViews(form, CONTAINER_ID);
      repairUtils.performLabelsAndGroupsRepair(views);
      showForm(form, views, false);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
  }

  public void repairAll()
  {
    try
    {
      HtmlForm form = loadForm();
      if (form == null)
      {
        return;
      }
      RepairUtils repairUtils = new RepairUtils();
      List<HtmlView> views = repairUtils.extractViews(form, CONTAINER_ID);
      repairUtils.performSort(views);
      repairUtils.performLabelsAndGroupsRepair(views);
      showForm(form, views, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void repairAI()
  {
    try
    {
      String systemPrompt = this.getProperty("ia_prompt");
      String oldForm = ideBean.getDocument().getSource();
      String newForm = AiRepairUtils.processForm(oldForm, systemPrompt, userPrompt);
      ideBean.getDocument().setSource(newForm);

      this.update = true;
      // Mark the check in the front
      sourceModified = true;
      ideBean.markChanged();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void loadVisualEditor()
  {
    try
    {
      HtmlForm loadedForm = loadForm();
      this.form = (loadedForm != null) ? loadedForm : new HtmlForm();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  // Reproduce the core idea of PropertyHelper's PropertyMap in a simpler form
  public static class MultiValueMap extends AbstractMap<String, Object> 
    implements Serializable
  {
    private final Map<String, Object> inner = new HashMap<>();

    @Override
    public Object put(String key, Object value)
    {
      if (value instanceof String[])
      {
        String[] arr = (String[]) value;
        return inner.put(key, new ArrayList<>(java.util.Arrays.asList(arr)));
      }
      else if (value instanceof String)
      {
        List<String> list = new ArrayList<>();
        list.add((String) value);
        return inner.put(key, list);
      }
      return inner.put(key, value);
    }

    @Override
    public Object get(Object key)
    {
      return inner.get(key);
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
      return inner.entrySet();
    }
  }
}
