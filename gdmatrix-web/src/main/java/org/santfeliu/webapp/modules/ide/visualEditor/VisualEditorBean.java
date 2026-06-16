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
package org.santfeliu.webapp.modules.ide.visualEditor;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.output.WriterOutputStream;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;
import org.santfeliu.form.type.html.HtmlViewWrapper;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.ide.HtmlFormBean;

/**
 *
 * @author granadogj
 */
@Named("visualEditorBean")
@RequestScoped
public class VisualEditorBean extends WebBean
{

  @Inject
  private HtmlFormBean htmlFormBean;
  private String selectedViewId;
  private String editingId;
  private ElementDef selectedElementDef;
  private boolean showFormProperties = false;
  private String associatedLabelText = "";
  private String elementInnerText = "";
  private String screenType = "MONITOR"; // DEFAULT
  private static final String CONTAINER_ID = "panel";
  private static final String DEFAULT_MAP = "novetats_policia_60";
  private String serverScript = "";

  // Select vars
  private String selectedMode = ""; // DEFAULT
  private List<SelectOption> manualOptionsList = new ArrayList<>();

  // Spinners Width
  private Integer colDefault;
  private Integer colMd;
  private Integer colLg;
  private Integer colXl;

  /* -- CONFIG DICT FOR ALL ELEMENTS --*/
  public enum AddMode
  {
    BOTTOM, BEFORE, INSIDE
  };
  
  public HtmlView getSelectedView()
  {
    if (this.selectedViewId == null)
    {
      return null;
    }
    HtmlForm form = htmlFormBean.getForm();
    if (form != null && form.getRootView() != null)
    {
      HtmlView view = htmlFormBean.findViewByIdRecursively((HtmlView) form.getRootView(), this.selectedViewId);
//      // If view found, renovate element reference
//      if (view != null)
//      {
//        view.setReference(this.selectedViewId);
//      }
      return view;
    }

    return null;
  }

  public void setSelectedView(HtmlView view)
  {
    if (view != null)
    {
      this.selectedViewId = view.getId();
      view.setReference(this.selectedViewId);
    }
    else
    {
      this.selectedViewId = null;
    }
  }

  public String getEditingId()
  {
    return editingId;
  }

  public void setEditingId(String editingId)
  {
    this.editingId = editingId;
  }

  public ElementDef getSelectedElementDef()
  {
    return selectedElementDef;
  }

  public void setSelectedElementDef(ElementDef selectedElementDef)
  {
    this.selectedElementDef = selectedElementDef;
  }

  public boolean isShowFormProperties()
  {
    return showFormProperties;
  }

  public void setShowFormProperties(boolean showFormProperties)
  {
    this.showFormProperties = showFormProperties;
  }

  public ElementDef[] getElementDefs()
  {
    return ElementDef.values();
  }

  public ElementDef[] getFilteredElementDefs()
  {
    HtmlView currentView = getSelectedView();

    boolean isFieldset = currentView != null
      && "fieldset".equalsIgnoreCase(currentView.getNativeViewType());
    boolean isFieldsetChild = currentView != null && currentView.getParent() != null
      && "fieldset".equalsIgnoreCase(currentView.getParent().getNativeViewType());

    return Arrays.stream(ElementDef.values())
      .filter(def ->
      {
        if (isFieldset)
        {
          return true;
        }
        if (isFieldsetChild)
        {
          return def == ElementDef.RADIO || def == ElementDef.CHECKBOX;
        }

        return def != ElementDef.RADIO; // && def != ElementDef.CHECKBOX;
      })
      .toArray(ElementDef[]::new);
  }

  public boolean isGridSizeEditable()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null)
    {
      return false;
    }

    ElementDef config = ElementDef.fromView(new HtmlViewWrapper(currentView));
    return config != null ? config.isResizable() : true; // Default True
  }

  public String getAssociatedLabelText()
  {
    return associatedLabelText;
  }

  public void setAssociatedLabelText(String associatedLabelText)
  {
    this.associatedLabelText = associatedLabelText;
  }

  public String getSelectedMode()
  {
    return selectedMode;
  }

  public void setSelectedMode(String selectedMode)
  {
    this.selectedMode = selectedMode;
  }

  public List<SelectOption> getManualOptionsList()
  {
    return manualOptionsList;
  }

  // In case we want back the UI:REPEAT
  public void setManualOptionsList(List<SelectOption> manualOptionsList)
  {
    this.manualOptionsList = manualOptionsList;
  }

  public String getElementInnerText()
  {
    return elementInnerText;
  }

  public void setElementInnerText(String elementInnerText)
  {
    this.elementInnerText = elementInnerText;
  }

  public String getScreenType()
  {
    return screenType;
  }

  public void setScreenType(String screenType)
  {
    this.screenType = screenType;
  }
  
  public String getServerScript()
  {
    return serverScript;
  }
  
  public void setServerScript(String serverScript)
  {
    this.serverScript = serverScript;
  }

  public Integer getColDefault()
  {
    return colDefault;
  }

  public void setColDefault(Integer colDefault)
  {
    this.colDefault = colDefault;
  }

  public Integer getColMd()
  {
    return colMd;
  }

  public void setColMd(Integer colMd)
  {
    this.colMd = colMd;
  }

  public Integer getColLg()
  {
    return colLg;
  }

  public void setColLg(Integer colLg)
  {
    this.colLg = colLg;
  }

  public Integer getColXl()
  {
    return colXl;
  }

  public void setColXl(Integer colXl)
  {
    this.colXl = colXl;
  }

  public List<VisualCanvasBlock> getCanvasBlocks()
  {
    return buildBlocksFromList(getPanelChildren());
  }

  private List<VisualCanvasBlock> buildBlocksFromList(List<View> children)
  {
    List<VisualCanvasBlock> blocks = new ArrayList<>();

    Set<HtmlView> processed = Collections.newSetFromMap(new IdentityHashMap<>());

    // Pre-index id → position and for → position.
    Map<String, Integer> indexById = new HashMap<>();
    Map<String, Integer> indexByLabelFor = new HashMap<>();
    for (int i = 0; i < children.size(); i++)
    {
      HtmlView v = (HtmlView) children.get(i);
      String id = v.getId();
      if (id == null)
      {
        id = (String) v.getProperty("id");
      }
      if (id != null && !id.isEmpty())
      {
        indexById.putIfAbsent(id, i);
      }
      if ("label".equals(v.getNativeViewType()))
      {
        String forAttr = (String) v.getProperty("for");
        if (forAttr != null && !forAttr.isEmpty())
        {
          indexByLabelFor.putIfAbsent(forAttr, i);
        }
      }
    }

    for (int i = 0; i < children.size(); i++)
    {
      HtmlView currentView = (HtmlView) children.get(i);
      if (processed.contains(currentView))
      {
        continue;
      }

      String type = currentView.getNativeViewType();
      if ("#comment".equals(type) || "#text".equals(type) || "legend".equals(type))
      {
        continue;
      }

      HtmlViewWrapper labelWrapper = null;
      HtmlViewWrapper elementWrapper = null;
      boolean isLabelFirst = true;

      if ("label".equals(type))
      {
        labelWrapper = new HtmlViewWrapper(currentView);
        processed.add(currentView);

        String forAttr = labelWrapper.getProperty("for");
        if (forAttr != null && !forAttr.isEmpty())
        {
          Integer pairIdx = indexById.get(forAttr);
          if (pairIdx != null)
          {
            HtmlView pair = (HtmlView) children.get(pairIdx);
            if (!processed.contains(pair))
            {
              elementWrapper = new HtmlViewWrapper(pair);
              processed.add(pair);
              isLabelFirst = (i < pairIdx);
            }
          }
        }
      }
      else
      {
        elementWrapper = new HtmlViewWrapper(currentView);
        processed.add(currentView);

        String myId = elementWrapper.getId();
        if (myId != null && !myId.isEmpty())
        {
          Integer pairIdx = indexByLabelFor.get(myId);
          if (pairIdx != null)
          {
            HtmlView pair = (HtmlView) children.get(pairIdx);
            if (!processed.contains(pair))
            {
              labelWrapper = new HtmlViewWrapper(pair);
              processed.add(pair);
              isLabelFirst = (pairIdx < i);
            }
          }
        }
      }

      VisualCanvasBlock block = new VisualCanvasBlock(labelWrapper, elementWrapper);
      block.setLabelFirst(isLabelFirst);

      if ("fieldset".equals(type))
      {
        block.setChildrenBlocks(buildBlocksFromList(currentView.getChildren()));
      }
      blocks.add(block);
    }
    return blocks;
  }

  public String getDynamicPropFile()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null || currentView.getNativeViewType() == null)
    {
      return "empty_props.xhtml";
    }
    ElementDef config = ElementDef.fromView(new HtmlViewWrapper(currentView));
    if (config != null && config.getPropFile() != null)
    {
      return config.getPropFile();
    }
    return "default_props.xhtml";
  }

  public String getDynamicTemplateFileFor(HtmlViewWrapper wrapper)
  {
    if (wrapper == null || wrapper.getNativeViewType() == null)
    {
      return "empty_template.xhtml";
    }
    ElementDef config = ElementDef.fromView(wrapper);
    if (config != null && config.getTemplateFile() != null)
    {
      return config.getTemplateFile();
    }
    return "default_template.xhtml";
  }

  public HtmlViewWrapper getElementProps()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null)
    {
      return null;
    }
    return new HtmlViewWrapper(currentView);
  }

  private HtmlView getPanel()
  {
    HtmlForm form = htmlFormBean.getForm();
    if (form == null || form.getRootView() == null)
    {
      return null;
    }
    return htmlFormBean.findViewByIdRecursively((HtmlView) form.getRootView(), CONTAINER_ID);
  }

  public List<View> getPanelChildren()
  {
    HtmlView panel = getPanel();
    if (panel != null)
    {
      return panel.getChildren();
    }

    HtmlForm form = htmlFormBean.getForm();
    if (form != null && form.getRootView() != null)
    {
      return form.getRootView().getChildren();
    }

    return Collections.emptyList();
  }

  public List<HtmlViewWrapper> getWrappedPanelChildren()
  {
    List<HtmlViewWrapper> wrappers = new ArrayList<>();
    for (View v : getPanelChildren())
    {
      wrappers.add(new HtmlViewWrapper((HtmlView) v));
    }
    return wrappers;
  }

  public void saveVisualEditorToText()
  {
    try
    {
      HtmlForm form = htmlFormBean.getForm();
      if (form == null)
      {
        return;
      }

      StringWriter sw = new StringWriter();
      try (WriterOutputStream wos = new WriterOutputStream(sw, StandardCharsets.UTF_8))
      {
        form.write(wos, null);
        wos.flush();
      }

      htmlFormBean.getIdeBean().getDocument().setSource(sw.toString());

      // Recreate the tree from the plain text
      htmlFormBean.loadVisualEditor();

      // After recosntructing the tree, re-select the current element by id
      HtmlView currentView = getSelectedView();
      if (currentView != null)
      {
        currentView.setReference(this.selectedViewId);
      }

      htmlFormBean.setSourceModified(true);
      htmlFormBean.getIdeBean().markChanged();
      htmlFormBean.setUpdate(true);
    }
    catch (Exception ex)
    {
      error("Error in saveVisualEditorToText :" + ex.getMessage());
    }
  }

  private void commit()
  {
    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
  }

  public void addBottomSelectedElement()
  {
    if (selectedElementDef != null)
    {
      addElement(selectedElementDef.getTag(), selectedElementDef.getDefaultType(), AddMode.BOTTOM);
    }
  }

  public void addBeforeSelectedElement()
  {
    if (selectedElementDef != null)
    {
      addElement(selectedElementDef.getTag(), selectedElementDef.getDefaultType(), AddMode.BEFORE);
    }
  }

  public void addInsideSelectedElement()
  {
    if (selectedElementDef == null)
    {
      error("Please select an elemen type first.");
      return;
    }

    HtmlView currentView = getSelectedView();
    if (currentView == null || !"fieldset".equals(currentView.getNativeViewType()))
    {
      error("Inside insertion is only allowed inside a fieldset");
      return;
    }
    if (selectedElementDef != ElementDef.RADIO && selectedElementDef != ElementDef.CHECKBOX)
    {
      error("Only radio/checkbox elements can be added inside a fieldset");
      return;
    }
    addElement(selectedElementDef.getTag(), selectedElementDef.getDefaultType(), AddMode.INSIDE);
  }

  public void addElement(String tag)
  {
    addElement(tag, null, AddMode.BOTTOM);
  }

  public void addElement(String tag, String type, AddMode mode)
  {
    this.showFormProperties = false;
    HtmlForm form = htmlFormBean.getForm();
    if (form.getRootView() == null)
    {
      htmlFormBean.loadVisualEditor();
    }

    ElementDef config = ElementDef.fromTagAndType(tag, type);
    if (config == null)
    {
      error("Unrecognized element type: tag='" + tag + "', type='" + type + "'");
      return;
    }

    boolean needsLabel = config.isHasLabel();
    String id = htmlFormBean.generateUniqueId();
    HtmlViewWrapper wrapper = new HtmlViewWrapper(new HtmlView(config.getTag()));

    if ("input".equals(config.getTag()) && config.getDefaultType() != null)
    {
      wrapper.setViewType(config.getDefaultType());
      wrapper.setInputType(config.getDefaultType());
    }

    wrapper.setId(id);
    wrapper.setReference(id);
    this.associatedLabelText = "New " + tag;

    // Assign defaultWith depending on the element
    String baseWidth = config.getDefaultWidth();
    wrapper.setStyleClass(baseWidth);

    if ("fieldset".equals(config.getTag()))
    {
      HtmlViewWrapper legendWrapper = new HtmlViewWrapper(new HtmlView("legend"));
      legendWrapper.setInnerText(this.associatedLabelText);
      wrapper.getChildren().add(legendWrapper.getUnderlyingView());
    }

    if ("div".equals(config.getTag()) && "mapLibre".equals(config.getDefaultType()))
    {
      wrapper.setRenderer("mapLibre");
      String mapName = (this.elementInnerText == null || this.elementInnerText.trim().isEmpty()) ? DEFAULT_MAP : this.elementInnerText;
      wrapper.setInnerText(mapName);
    }

    List<View> targetContainer = getPanelChildren();
    HtmlView currentSelected = getSelectedView();
    int insertIndex = -1; // -1 = add to the final

    // --- POSITIONING LOGIC ---
    if (currentSelected != null)
    {
      if (mode == AddMode.INSIDE && "fieldset".equals(currentSelected.getNativeViewType()))
      {
        // Insert inside the selected Fieldset
        targetContainer = currentSelected.getChildren();
        if ("radio".equals(config.getDefaultType()))
        {
          wrapper.setName(currentSelected.getId());
        }
      }
      else
      {
        // For AddMode.BEFORE and AddMode.BOTTOM, the container must be the "box" of the actual item
        List<View> parentContainer = getParentContainer(getPanelChildren(), currentSelected);
        if (parentContainer != null)
        {
          targetContainer = parentContainer;

          /* If we are inserting next to an element that lives within a fieldset,
          we look for who this fieldset father is and automatically link the 'name' to him. */
          HtmlView parentFieldset = getParentFieldset(getPanelChildren(), currentSelected);
          if (parentFieldset != null && "radio".equals(config.getDefaultType()))
          {
            wrapper.setName(parentFieldset.getId());
          }
        }

        // If it’s BEFORE, calculate the index into which we put the element
        if (mode == AddMode.BEFORE)
        {
          HtmlView labelView = getAssociatedLabel(targetContainer, currentSelected);
          int elemIndex = targetContainer.indexOf(currentSelected);
          int labelIndex = (labelView != null) ? targetContainer.indexOf(labelView) : -1;

          if (elemIndex != -1)
          {
            insertIndex = elemIndex;
            if (labelIndex != -1 && labelIndex < elemIndex)
            {
              insertIndex = labelIndex;
            }
          }
        }
      }
    }

    // --- CONSTRUCTION OF THE BLOCK (Label + Element) ---
    boolean isLabelAfter = "radio".equals(config.getDefaultType());
    HtmlViewWrapper labelWrapper = null;

    if (needsLabel)
    {
      labelWrapper = new HtmlViewWrapper(new HtmlView("label"));
      labelWrapper.setFor(id);
      labelWrapper.setInnerText(this.associatedLabelText);
    }

    // Insert into the container
    if (insertIndex != -1)
    {
      // Insertion in a specific position (BEFORE)
      if (needsLabel && !isLabelAfter && labelWrapper != null)
      {
        targetContainer.add(insertIndex++, labelWrapper.getUnderlyingView());
      }
      targetContainer.add(insertIndex++, wrapper.getUnderlyingView());
      if (needsLabel && isLabelAfter && labelWrapper != null)
      {
        targetContainer.add(insertIndex++, labelWrapper.getUnderlyingView());
      }
    }
    else
    {
      // Insertion into the end
      if (needsLabel && !isLabelAfter && labelWrapper != null)
      {
        targetContainer.add(labelWrapper.getUnderlyingView());
      }
      targetContainer.add(wrapper.getUnderlyingView());
      if (needsLabel && isLabelAfter && labelWrapper != null)
      {
        targetContainer.add(labelWrapper.getUnderlyingView());
      }
    }

    this.selectedViewId = id;
    this.editingId = id;
    // Extract the exact columns to synchronize the spinners
    this.colDefault = extractColSize(baseWidth, "col-");
    this.colMd = extractColSize(baseWidth, "md:col-");
    this.colLg = extractColSize(baseWidth, "lg:col-");
    this.colXl = extractColSize(baseWidth, "xl:col-");

    if (this.colDefault == null)
    {
      this.colDefault = 12;
    }

    this.manualOptionsList.clear();
    this.elementInnerText = "";

    commit();
  }

  public void selectElementFromCanvas()
  {
    this.showFormProperties = false;
    HtmlForm form = htmlFormBean.getForm();
    Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String id = params.get("elementId");

    if (id != null && form != null)
    {

      this.selectedViewId = id;
      HtmlView currentView = getSelectedView();

      if (currentView != null)
      {
        this.editingId = currentView.getId();

        // Even if JSF changes the ID, this way Java doesn’t lose it
        currentView.setReference(this.selectedViewId);

        HtmlViewWrapper wrapper = new HtmlViewWrapper(currentView);
        String tag = wrapper.getNativeViewType();
        String css = wrapper.getStyleClass();

        this.colDefault = extractColSize(css, "col-");
        this.colMd = extractColSize(css, "md:col-");
        this.colLg = extractColSize(css, "lg:col-");
        this.colXl = extractColSize(css, "xl:col-");

        this.associatedLabelText = "";
        this.elementInnerText = "";

        if ("fieldset".equals(tag))
        {
          this.associatedLabelText = wrapper.getLegendText();
        }
        else
        {
          if ("select".equals(tag))
          {
            this.manualOptionsList.clear();
            for (View child : wrapper.getChildren())
            {
              HtmlViewWrapper childWrapper = new HtmlViewWrapper((HtmlView) child);
              if ("option".equals(childWrapper.getNativeViewType()))
              {
                String val = childWrapper.getValue();
                String text = childWrapper.getInnerText();
                this.manualOptionsList.add(new SelectOption(
                  val != null ? val : "",
                  text != null ? text : ""
                ));
              }
            }
            // Detect Select Mode
            String dataRef = wrapper.getDataRef();
            String sql = wrapper.getSql();
            if (dataRef != null && !dataRef.trim().isEmpty())
            {
              this.selectedMode = "ENUM";
            }
            else if (sql != null && !sql.trim().isEmpty())
            {
              this.selectedMode = "SQL";
            }
            else
            {
              this.selectedMode = "";
            }
          }
          else if ("div".equals(tag) && "mapLibre".equals(wrapper.getRenderer()))
          {
            this.elementInnerText = wrapper.getInnerText();
          }
          else if ("div".equals(tag))
          {
            this.elementInnerText = getInnerHtml(wrapper);
          }

          List<View> container = getParentContainer(getPanelChildren(), currentView);
          HtmlView label = (container != null) ? getAssociatedLabel(container, currentView) : null;
          if (label != null)
          {
            this.associatedLabelText = new HtmlViewWrapper(label).getInnerText();
          }
        }
      }
    }
  }

  public void saveElement()
  {
    HtmlView currentView = getSelectedView();

    if (currentView == null)
    {
      error("No element selected. Please click on an element in the canvas first.");
      return;
    }

    List<View> container = getParentContainer(getPanelChildren(), currentView);
    HtmlView labelView = (container != null) ? getAssociatedLabel(container, currentView) : null;

    HtmlViewWrapper wrapper = new HtmlViewWrapper(currentView);
    // String newId = wrapper.getId();
    String newId = (this.editingId != null && !this.editingId.trim().isEmpty())
      ? this.editingId.trim() : wrapper.getId();

    if (isIdInUse(newId, currentView))
    {
      error("The id '" + newId + "' is already in use. Please choose a different one.");
      return;
    }

    wrapper.setReference(newId);
    wrapper.setId(newId);
    currentView.setProperty("id", newId);

    // Update master ID if it has been changed by the user
    this.selectedViewId = newId;

    ElementDef config = ElementDef.fromView(wrapper);
    boolean hasLabel = (config != null && config.isHasLabel());

    if (config != null && ("input".equals(config.getTag()) || "select".equals(config.getTag())
      || "textarea".equals(config.getTag()) || "button".equals(config.getTag())))
    {
      boolean isGroupedInput = "radio".equals(config.getDefaultType());
      boolean isButton = "button".equals(config.getDefaultType()) || "button".equals(config.getTag());
      if (!isGroupedInput && !isButton)
      {
        wrapper.setName(newId);
      }
    }

    if (hasLabel && labelView != null)
    {
      HtmlViewWrapper labelWrapper = new HtmlViewWrapper(labelView);
      labelWrapper.setFor(newId);
      labelWrapper.setInnerText(this.associatedLabelText);
    }

    if ("div".equals(wrapper.getNativeViewType()))
    {
      if ("mapLibre".equals(wrapper.getRenderer()))
      {
        String mapName = (this.elementInnerText == null || this.elementInnerText.trim().isEmpty()) ? DEFAULT_MAP : this.elementInnerText;
        wrapper.setInnerText(mapName);
        this.elementInnerText = mapName;
      }
      else
      {
        wrapper.setInnerText(this.elementInnerText);
      }
    }

    if ("fieldset".equals(wrapper.getNativeViewType()))
    {
      for (View child : wrapper.getChildren())
      {
        HtmlViewWrapper childWrapper = new HtmlViewWrapper((HtmlView) child);
        if ("legend".equals(childWrapper.getNativeViewType()))
        {
          childWrapper.setInnerText(this.associatedLabelText);
        }
        else if ("input".equals(childWrapper.getNativeViewType()) && "radio".equals(childWrapper.getInputType()))
        {
          childWrapper.setName(newId);
        }
      }
    }
    if ("select".equals(wrapper.getNativeViewType()))
    {
      // Override source options
      wrapper.getChildren().clear();
      for (SelectOption opt : this.manualOptionsList)
      {
        if ((opt.getValue() == null || opt.getValue().trim().isEmpty())
          && (opt.getText() == null || opt.getText().trim().isEmpty()))
        {
          continue;
        }

        String val = opt.getValue() != null ? opt.getValue().trim() : "";
        String text = opt.getText() != null && !opt.getText().trim().isEmpty()
          ? opt.getText().trim() : val;

        HtmlViewWrapper optWrapper = new HtmlViewWrapper(new HtmlView("option"));
        if (!val.isEmpty())
        {
          optWrapper.setValue(val);
        }
        optWrapper.setInnerText(text);
        wrapper.getChildren().add(optWrapper.getUnderlyingView());
      }
    }
    commit();
  }

  public void cancelElement()
  {
    this.selectedViewId = null;
    this.editingId = null;
    this.associatedLabelText = "";
    this.elementInnerText = "";
    this.manualOptionsList.clear();
    this.colDefault = null;
    this.colMd = null;
    this.colLg = null;
    this.colXl = null;
    this.showFormProperties = false;
    this.selectedMode = "";
  }

  private HtmlView getAssociatedLabel(List<View> container, HtmlView element)
  {
    if (element == null || container == null)
    {
      return null;
    }

    String oldId = (String) element.getProperty("id");
    String currentId = element.getId();
    if (oldId == null && currentId == null)
    {
      return null;
    }

    for (View v : container)
    {
      if (v instanceof HtmlView)
      {
        HtmlView view = (HtmlView) v;
        if ("label".equals(view.getNativeViewType()))
        {
          String forAttr = (String) view.getProperty("for");
          if ((oldId != null && oldId.equals(forAttr)) || (currentId != null && currentId.equals(forAttr)))
          {
            return view;
          }
        }
      }
    }
    return null;
  }

  private List<View> getParentContainer(List<View> root, HtmlView target)
  {
    if (target == null)
    {
      return null;
    }

    String searchId = target.getId();
    if (searchId == null)
    {
      searchId = target.getReference();
    }

    for (View v : root)
    {
      if (v instanceof HtmlView)
      {
        HtmlView hv = (HtmlView) v;
        if ((searchId != null && searchId.equals(hv.getId())) || (searchId != null && searchId.equals(hv.getReference())))
        {
          return root;
        }
      }
    }

    for (View v : root)
    {
      if (v instanceof HtmlView && "fieldset".equals(((HtmlView) v).getNativeViewType()))
      {
        List<View> found = getParentContainer(v.getChildren(), target);
        if (found != null)
        {
          return found;
        }
      }
    }
    return null;
  }

  private boolean isIdInUse(String candidateId, HtmlView currentView)
  {
    if (candidateId == null || candidateId.trim().isEmpty())
    {
      return false;
    }
    HtmlForm form = htmlFormBean.getForm();
    if (form == null || form.getRootView() == null)
    {
      return false;
    }
    return existsIdExcluding((HtmlView) form.getRootView(), candidateId.trim(), currentView);
  }

  private boolean existsIdExcluding(HtmlView view, String candidateId, HtmlView exclude)
  {
    if (view == null)
    {
      return false;
    }
    if (view != exclude)
    {
      String viewId = view.getId();
      if (viewId == null || viewId.trim().isEmpty())
      {
        viewId = (String) view.getProperty("id");
      }
      if (candidateId.equals(viewId))
      {
        return true;
      }
    }
    for (View child : view.getChildren())
    {
      if (existsIdExcluding((HtmlView) child, candidateId, exclude))
      {
        return true;
      }
    }
    return false;
  }

  private HtmlView getParentFieldset(List<View> root, HtmlView target)
  {
    if (target == null)
    {
      return null;
    }

    for (View v : root)
    {
      if (v instanceof HtmlView && "fieldset".equals(((HtmlView) v).getNativeViewType()))
      {
        HtmlView fieldset = (HtmlView) v;

        List<View> found = getParentContainer(fieldset.getChildren(), target);
        if (found != null)
        {
          return fieldset;
        }
      }
    }
    return null;
  }

  public void moveUpSelected()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null || currentView.getId() == null)
    {
      return;
    }

    List<View> container = getParentContainer(getPanelChildren(), currentView);
    if (container == null)
    {
      return;
    }

    int myIndex = container.indexOf(currentView);
    if (myIndex < 0)
    {
      return;
    }

    HtmlView myLabel = getAssociatedLabel(container, currentView);
    int myLabelIndex = (myLabel != null) ? container.indexOf(myLabel) : -1;

    int blockStart = myIndex;
    boolean labelIsFirst = false;

    if (myLabel != null && myLabelIndex != -1)
    {
      blockStart = Math.min(myIndex, myLabelIndex);
      labelIsFirst = myLabelIndex < myIndex; // Label first?
    }

    if (blockStart <= 0)
    {
      return;
    }

    // Find where the previous block starts to skip it
    int targetInsertIdx = -1;
    int minIndex = getFirstVisualElementIndex(container); // Calculate safe zone
    for(int i = blockStart -1; i >= minIndex; i--)
    {
      HtmlView v = (HtmlView) container.get(i);
      if (isInsignificantNode(v))
      {
        continue;
      }
      if ("legend".equals(v.getNativeViewType()))
      {
        break;
      }

      targetInsertIdx = i;

      if ("label".equals(v.getNativeViewType()))
      {
        String forAttr = (String) v.getProperty("for");
        for (int j = i - 1; j >= 0; j--)
        {
          HtmlView v2 = (HtmlView) container.get(j);
          if (isInsignificantNode(v2))
          {
            continue;
          }
          if (forAttr != null && forAttr.equals(v2.getId()))
          {
            targetInsertIdx = j;
          }
          break;
        }
      }
      else
      {
        String elemId = v.getId();
        for (int j = i - 1; j >= 0; j--)
        {
          HtmlView v2 = (HtmlView) container.get(j);
          if (isInsignificantNode(v2))
          {
            continue;
          }
          if ("label".equals(v2.getNativeViewType()))
          {
            String forAttr = (String) v2.getProperty("for");
            if (elemId != null && elemId.equals(forAttr))
            {
              targetInsertIdx = j;
            }
          }
          break;
        }
      }
      break;
    }

    if (targetInsertIdx == -1)
    {
      return;
    }

    container.remove(currentView);
    if (myLabel != null)
    {
      container.remove(myLabel);
    }

    // Reinsert element following the original order (Checkbox vs Text)
    if (myLabel != null)
    {
      if (labelIsFirst)
      {
        container.add(targetInsertIdx, myLabel);
        container.add(targetInsertIdx + 1, currentView);
      }
      else
      {
        container.add(targetInsertIdx, currentView);
        container.add(targetInsertIdx + 1, myLabel);
      }
    }
    else
    {
      container.add(targetInsertIdx, currentView);
    }

    commit();
  }

  public void moveDownSelected()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null || currentView.getId() == null)
    {
      return;
    }

    List<View> container = getParentContainer(getPanelChildren(), currentView);
    if (container == null)
    {
      return;
    }

    int myIndex = container.indexOf(currentView);
    if (myIndex < 0)
    {
      return;
    }

    HtmlView myLabel = getAssociatedLabel(container, currentView);
    int myLabelIndex = (myLabel != null) ? container.indexOf(myLabel) : -1;

    // Save block limit and original order
    int blockEnd = myIndex;
    boolean labelIsFirst = false;

    if (myLabel != null && myLabelIndex != -1)
    {
      blockEnd = Math.max(myIndex, myLabelIndex);
      labelIsFirst = myLabelIndex < myIndex; // Label First
    }

    if (blockEnd >= container.size() - 1)
    {
      return;
    }

    // Find where the previous block starts to skip it
    int targetInsertIdx = -1;
    for (int i = blockEnd + 1; i < container.size(); i++)
    {
      HtmlView v = (HtmlView) container.get(i);
      if (isInsignificantNode(v))
      {
        continue;
      }

      targetInsertIdx = i;

      if ("label".equals(v.getNativeViewType()))
      {
        String forAttr = (String) v.getProperty("for");
        for (int j = i + 1; j < container.size(); j++)
        {
          HtmlView v2 = (HtmlView) container.get(j);
          if (isInsignificantNode(v2))
          {
            continue;
          }
          if (forAttr != null && forAttr.equals(v2.getId()))
          {
            targetInsertIdx = j;
          }
          break;
        }
      }
      else
      {
        String elemId = v.getId();
        for (int j = i + 1; j < container.size(); j++)
        {
          HtmlView v2 = (HtmlView) container.get(j);
          if (isInsignificantNode(v2))
          {
            continue;
          }
          if ("label".equals(v2.getNativeViewType()))
          {
            String forAttr = (String) v2.getProperty("for");
            if (elemId != null && elemId.equals(forAttr))
            {
              targetInsertIdx = j;
            }
          }
          break;
        }
      }
      break;
    }

    if (targetInsertIdx == -1)
    {
      return;
    }

    container.remove(currentView);
    if (myLabel != null)
    {
      container.remove(myLabel);
    }

    // Adjust the target index after deleting our own elements
    int shift = 1 + (myLabel != null ? 1 : 0);
    targetInsertIdx -= shift;
    targetInsertIdx++; // Add 1 to get in right AFTER the skipped block

    if (targetInsertIdx > container.size())
    {
      targetInsertIdx = container.size();
    }
    if (targetInsertIdx < 0)
    {
      targetInsertIdx = 0;
    }

    // Reinsert element following the original order (Checkbox vs Text)
    if (myLabel != null)
    {
      if (labelIsFirst)
      {
        container.add(targetInsertIdx, myLabel);
        container.add(targetInsertIdx + 1, currentView);
      }
      else
      {
        container.add(targetInsertIdx, currentView);
        container.add(targetInsertIdx + 1, myLabel);
      }
    }
    else
    {
      container.add(targetInsertIdx, currentView);
    }

    commit();
  }

  // FUNCIÓN PARA EL DRAG AND DROP
  /**
   * Moves a dragged element (and its associated label) to a new position,
   * relative to a target element. Reused by the drag-and-drop feature.
   *
   * Request params: draggedId - id of the element being dragged targetId - id
   * of the element it was dropped onto position - "before" or "after" (relative
   * to the target)
   */
  public void moveElementToPosition()
  {
    Map<String, String> params = FacesContext.getCurrentInstance()
      .getExternalContext().getRequestParameterMap();
    String draggedId = params.get("draggedId");
    String targetId = params.get("targetId");
    String containerId = params.get("containerId");
    String position = params.get("position");

    if (draggedId == null)
    {
      return;
    }

    HtmlForm form = htmlFormBean.getForm();
    if (form == null || form.getRootView() == null)
    {
      return;
    }

    HtmlView draggedView = htmlFormBean.findViewByIdRecursively(
      (HtmlView) form.getRootView(), draggedId);
    if (draggedView == null)
    {
      return;
    }

    // Source: container + label of the dragged element
    List<View> sourceContainer = getParentContainer(getPanelChildren(), draggedView);
    if (sourceContainer == null)
    {
      return;
    }
    HtmlView draggedLabel = getAssociatedLabel(sourceContainer, draggedView);
    HtmlView sourceFieldset = getParentFieldset(getPanelChildren(), draggedView);

    // --- Resolve destination container and insertion index ---
    List<View> destContainer;
    int insertIndex;
    HtmlView destFieldset;

    if ("end".equals(position) && containerId != null)
    {
      // Drop into empty area → end of the named container
      if ("panel".equals(containerId))
      {
        destContainer = getPanelChildren();
        destFieldset = null;
      }
      else
      {
        HtmlView fieldset = htmlFormBean.findViewByIdRecursively(
          (HtmlView) form.getRootView(), containerId);
        if (fieldset == null)
        {
          return;
        }
        destContainer = fieldset.getChildren();
        destFieldset = fieldset;
      }

      if (!isMoveAllowed(draggedView, destFieldset != null))
      {
        return;
      }

      // Remove dragged (and label) from source first
      sourceContainer.remove(draggedView);
      if (draggedLabel != null)
      {
        sourceContainer.remove(draggedLabel);
      }

      insertIndex = destContainer.size(); // end
    }
    else
    {
      // Drop relative to a target element (existing before/after logic)
      if (targetId == null || draggedId.equals(targetId))
      {
        return;
      }
      HtmlView targetView = htmlFormBean.findViewByIdRecursively(
        (HtmlView) form.getRootView(), targetId);
      if (targetView == null)
      {
        return;
      }

      destContainer = getParentContainer(getPanelChildren(), targetView);
      if (destContainer == null)
      {
        return;
      }
      destFieldset = getParentFieldset(getPanelChildren(), targetView);

      HtmlViewWrapper targetWrapper = new HtmlViewWrapper(targetView);
      if ("fieldset".equals(targetWrapper.getNativeViewType()) && destFieldset == targetView)
      {
        destFieldset = null;
      }

      if (!isMoveAllowed(draggedView, destFieldset != null))
      {
        return;
      }

      sourceContainer.remove(draggedView);
      if (draggedLabel != null)
      {
        sourceContainer.remove(draggedLabel);
      }

      int targetIndex = destContainer.indexOf(targetView);
      if (targetIndex == -1)
      {
        return;
      }

      HtmlView targetLabel = getAssociatedLabel(destContainer, targetView);
      int targetLabelIndex = (targetLabel != null) ? destContainer.indexOf(targetLabel) : -1;

      if ("before".equals(position))
      {
        insertIndex = targetIndex;
        if (targetLabelIndex != -1 && targetLabelIndex < targetIndex)
        {
          insertIndex = targetLabelIndex;
        }
      }
      else
      {
        insertIndex = targetIndex + 1;
        if (targetLabelIndex != -1 && targetLabelIndex > targetIndex)
        {
          insertIndex = targetLabelIndex + 1;
        }
      }
    }
    
    int minIndex = getFirstVisualElementIndex(destContainer);
    if (insertIndex < minIndex)
    {
      insertIndex = minIndex; 
    }
    if (insertIndex > destContainer.size())
    {
      insertIndex = destContainer.size();
    }

    // --- Insert preserving label↔element order ---
    HtmlViewWrapper draggedWrapper = new HtmlViewWrapper(draggedView);
    boolean isLabelAfter = "radio".equals(draggedWrapper.getInputType());

    if (draggedLabel != null)
    {
      if (isLabelAfter)
      {
        destContainer.add(insertIndex, draggedView);
        destContainer.add(insertIndex + 1, draggedLabel);
      }
      else
      {
        destContainer.add(insertIndex, draggedLabel);
        destContainer.add(insertIndex + 1, draggedView);
      }
    }
    else
    {
      destContainer.add(insertIndex, draggedView);
    }

    // Reassign radio group name if moved into a different fieldset
    if (destFieldset != null && destFieldset != sourceFieldset
      && "radio".equals(draggedWrapper.getInputType()))
    {
      draggedWrapper.setName(destFieldset.getId());
    }
    
    this.selectedViewId = draggedId;
    this.editingId = draggedId;
    commit();
  }

  public void removeSelected()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null || currentView.getId() == null)
    {
      return;
    }

    List<View> container = getParentContainer(getPanelChildren(), currentView);
    if (container == null)
    {
      return;
    }

    HtmlView label = getAssociatedLabel(container, currentView);

    container.remove(currentView);
    if (label != null)
    {
      container.remove(label);
    }

    cancelElement();

    commit();
  }

  private Integer extractColSize(String css, String prefix)
  {
    if (css == null || css.trim().isEmpty())
    {
      return null;
    }

    Matcher m = Pattern.compile("(?:\\s|^)" + prefix + "(\\d+)(?=\\s|$)").matcher(css);
    if (m.find())
    {
      return Integer.parseInt(m.group(1));
    }
    return null;
  }

  public void updateGridClasses()
  {
    HtmlView currentView = getSelectedView();
    if (currentView == null)
    {
      return;
    }

    HtmlViewWrapper wrapper = new HtmlViewWrapper(currentView);
    String css = wrapper.getStyleClass();
    if (css == null)
    {
      css = "";
    }

    css = css.replaceAll("(?:\\s|^)col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)md:col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)lg:col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)xl:col-\\d+(?=\\s|$)", "").trim();

    StringBuilder newCss = new StringBuilder(css);

    // Mobile always has to have minimum 1 column
    if (this.colDefault != null && this.colDefault > 0)
    {
      newCss.append(" col-").append(this.colDefault);
    }
    else
    {
      newCss.append(" col-12");
      this.colDefault = 12; // Sync the bean so that it does not stay at 0
    }

    if (this.colMd != null && this.colMd > 0)
    {
      newCss.append(" md:col-").append(this.colMd);
    }
    if (this.colLg != null && this.colLg > 0)
    {
      newCss.append(" lg:col-").append(this.colLg);
    }
    if (this.colXl != null && this.colXl > 0)
    {
      newCss.append(" xl:col-").append(this.colXl);
    }

    String finalCss = newCss.toString().replaceAll("\\s+", " ").trim();
    wrapper.setStyleClass(finalCss);

    currentView.setProperty("class", finalCss);

    commit();
  }

  public void resizeElement()
  {
    Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String columnsStr = params.get("columns");

    HtmlView currentView = getSelectedView();

    if (currentView != null && columnsStr != null)
    {
      HtmlViewWrapper wrapper = new HtmlViewWrapper(currentView);

      // parse the new colSize sent from the JS
      int newColSize = 12;
      try
      {
        newColSize = Integer.parseInt(columnsStr);
      }
      catch (NumberFormatException e)
      {
        newColSize = 12;
      }

      String css = wrapper.getGridClass();

      Integer currentCol = extractColSize(css, "col-");
      Integer currentMd = extractColSize(css, "md:col-");
      Integer currentLg = extractColSize(css, "lg:col-");
      Integer currentXl = extractColSize(css, "xl:col-");

      // col-12 default base
      if (currentCol == null)
      {
        currentCol = 12;
      }

      // Modify only the gridClass of the selected screenType
      if ("MOBILE".equals(this.screenType))
      {
        currentCol = newColSize;
      }
      else if ("TABLET".equals(this.screenType))
      {
        currentMd = newColSize;
      }
      else if ("LAPTOP".equals(this.screenType))
      {
        currentLg = newColSize;
      }
      else if ("MONITOR".equals(this.screenType))
      {
        currentXl = newColSize;
      }

      // Sync the spinners
      this.colDefault = currentCol;
      this.colMd = currentMd;
      this.colLg = currentLg;
      this.colXl = currentXl;

      StringBuilder newGridClass = new StringBuilder("col-").append(currentCol);
      if (currentMd != null)
      {
        newGridClass.append(" md:col-").append(currentMd);
      }
      if (currentLg != null)
      {
        newGridClass.append(" lg:col-").append(currentLg);
      }
      if (currentXl != null)
      {
        newGridClass.append(" xl:col-").append(currentXl);
      }

      String baseCss = wrapper.getBaseStyleClass();
      String finalCss = (baseCss + " " + newGridClass.toString()).replaceAll("\\s+", " ").trim();

      wrapper.setStyleClass(finalCss);

      commit();
    }
  }

  public String getSimulatedGridClass(HtmlViewWrapper wrapper)
  {
    if (wrapper == null)
    {
      return "col-12";
    }

    String css = wrapper.getGridClass();

    Integer base = extractColSize(css, "col-");
    Integer md = extractColSize(css, "md:col-");
    Integer lg = extractColSize(css, "lg:col-");
    Integer xl = extractColSize(css, "xl:col-");

    // PF is mobile-first. The base is 12
    if (base == null)
    {
      base = 12;
    }

    int effective;
    switch (screenType)
    {
      case "MOBILE":
        effective = base;
        break;
      case "TABLET":
        effective = (md != null) ? md : base;
        break;
      case "LAPTOP":
        effective = (lg != null) ? lg : (md != null ? md : base);
        break;
      case "MONITOR":
      default:
        effective = (xl != null) ? xl : (lg != null ? lg : (md != null ? md : base));
        break;
    }

    return "col-" + effective;
  }

  public void addManualOption()
  {
    this.manualOptionsList.add(new SelectOption("", ""));
  }

  public void removeManualOption(int index)
  {
    if (index >= 0 && index < manualOptionsList.size())
    {
      manualOptionsList.remove(index);
    }
  }

  public String getManualOptionsText()
  {
    if (manualOptionsList == null || manualOptionsList.isEmpty())
    {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (SelectOption opt : manualOptionsList)
    {
      if (sb.length() > 0)
      {
        sb.append("\n");
      }
      String val = opt.getValue() != null ? opt.getValue() : "";
      String text = opt.getText() != null ? opt.getText() : "";
      sb.append(val);
      if (!text.isEmpty() && !text.equals(val))
      {
        sb.append("|").append(text);
      }
    }
    return sb.toString();
  }

  public void setManualOptionsText(String text)
  {
    this.manualOptionsList.clear();
    if (text == null || text.trim().isEmpty())
    {
      return;
    }
    String[] lines = text.split("\\r?\\n");
    for (String line : lines)
    {
      String trimmed = line.trim();
      if (trimmed.isEmpty())
      {
        continue;
      }

      int sepIdx = trimmed.indexOf('|');
      String val;
      String txt;
      if (sepIdx == -1)
      {
        val = trimmed;
        txt = trimmed;
      }
      else
      {
        val = trimmed.substring(0, sepIdx).trim();
        txt = trimmed.substring(sepIdx + 1).trim();
        if (txt.isEmpty())
        {
          txt = val;
        }
      }
      this.manualOptionsList.add(new SelectOption(val, txt));
    }
  }

  public String getInnerHtml(HtmlViewWrapper wrapper)
  {
    HtmlView view = wrapper.getUnderlyingView();

    // RAW already exists. The div has a #text child that was previosly saved as raw. Return the text as-is.
    List<View> children = view.getChildren();
    if (children.size() == 1 && children.get(0) instanceof HtmlView)
    {
      HtmlView only = (HtmlView) children.get(0);
      if (View.TEXT.equals(only.getViewType()))
      {
        String text = (String) only.getProperty("text");
        return text == null ? "" : text;
      }
    }

    // First-load case: The div is parsed in the tree. Extract the literal contenet from the original source. (Preserves indentation).
    String divId = wrapper.getId();
    if (divId == null || divId.isEmpty())
    {
      return "";
    }

    return extractInnerHtmlFromSource(divId);
  }

  private String extractInnerHtmlFromSource(String divId)
  {
    String source = htmlFormBean.getIdeBean().getDocument().getSource();
    if (source == null)
    {
      return "";
    }

    // Find the opening div with the id we want
    Pattern openPattern = Pattern.compile(
      "<div\\b[^>]*\\bid\\s*=\\s*[\"']" + Pattern.quote(divId) + "[\"'][^>]*>",
      Pattern.CASE_INSENSITIVE);
    Matcher m = openPattern.matcher(source);
    if (!m.find())
    {
      return "";
    }

    int contentStart = m.end();

    // Fint the corresponding <div> while respecting nested inner divs
    int depth = 1;
    int pos = contentStart;
    Pattern divPattern = Pattern.compile("<(/?)div\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    Matcher dm = divPattern.matcher(source);

    while (dm.find(pos))
    {
      if (dm.group(1).isEmpty())
      {
        depth++;
      }
      else
      {
        depth--;
        if (depth == 0)
        {
          String inner = source.substring(contentStart, dm.start());
          return trimSurroundingNewLines(inner);
        }
      }
      pos = dm.end();
    }
    return "";
  }

  private String trimSurroundingNewLines(String str)
  {
    // Trim leading line breaks and trailing whitespaces whithout affecting internal user indentation
    int start = 0;
    int end = str.length();
    while (start < end && (str.charAt(start) == '\n' || str.charAt(start) == '\r'))
    {
      start++;
    }
    while (end > start && (str.charAt(end - 1) == '\n' || str.charAt(end - 1) == '\r'
      || str.charAt(end - 1) == ' ' || str.charAt(end - 1) == '\t'))
    {
      end--;
    }
    return str.substring(start, end);
  }

  private boolean isMoveAllowed(HtmlView draggedView, boolean destIsFieldset)
  {
    HtmlViewWrapper w = new HtmlViewWrapper(draggedView);
    String tag = w.getNativeViewType();
    String inputType = w.getInputType();

    boolean isRadio = "radio".equals(inputType);
    boolean isCheckbox = "checkbox".equals(inputType);
    boolean isFieldset = "fieldset".equals(tag);

    if (isRadio)
    {
      return destIsFieldset;
    }
    if (isCheckbox)
    {
      return true;
    }
    if (isFieldset)
    {
      return !destIsFieldset;
    }
    return !destIsFieldset;
  }
  
  public void loadServerScript()
  {
    this.serverScript = "";
    HtmlView panel = getPanel();
    if (panel == null) return;
    
    for (View v : panel.getChildren())
    {
      HtmlView hv = (HtmlView) v;
      if (View.COMMENT.equals(hv.getViewType()))
      {
        String text = (String) hv.getProperty("text");
        if (text != null)
        {
          this.serverScript = unwrapScript(text);
        }
        return; // only the first comment
      }
      if (!"#text".equals(hv.getNativeViewType())) return;
    }
  }
  
  private String unwrapScript(String text)
  {
    String t = text.trim();
    if (t.startsWith("${") && t.endsWith("}"))
    {
      return t.substring(2, t.length() - 1);
    }
    return text;
  }
  
  public void saveServerScript()
  {
    HtmlView panel = getPanel();
    if (panel == null) return;
    
    // Reconstruct the comment content using the ${} wrapper
    String body = (serverScript != null) ? serverScript.trim() : "";
    String commentText = "${" + body + "}";
    
    HtmlView existing = null;
    for (View v : panel.getChildren())
    {
      HtmlView hv = (HtmlView) v;
      if ("#comment".equals(hv.getNativeViewType()))
      {
        existing = hv;
        break;
      }
      if (!"#text".equals(hv.getNativeViewType())) break; // First node != comment
    }
    
    if (body.isEmpty())
    {
      // If empty script, remove the #comment node
      if (existing != null) panel.getChildren().remove(existing);
    }
    else if (existing != null)
    {
      existing.setProperty("text", commentText);
    }
    else
    {
      HtmlView comment = new HtmlView();
      comment.setViewType(View.COMMENT);
      comment.setNativeViewType("#comment");
      comment.setProperty("text", commentText);
      panel.getChildren().add(0, comment);
    }
    
    commit();
  }
  
  private int getFirstVisualElementIndex(List<View> container)
  {
    if (container == null) return 0;
    for (int i = 0; i < container.size(); i++)
    {
      HtmlView hv = (HtmlView) container.get(i);
      String type = hv.getNativeViewType();
      // Ignore comments (Server Script) and text nodes (code line hops)
      if (!"#text".equals(type) && !"#comment".equals(type))
      {
        return i; // First element index
      }
    }
    return container.size();
  }
  
  private static boolean isInsignificantNode(View v)
  {
    if (v == null) return true;
    if (!(v instanceof HtmlView)) return true;
    HtmlView hv = (HtmlView) v;
    return "#text".equals(hv.getNativeViewType())
      || View.COMMENT.equals(hv.getViewType());
  }
}
