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

import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  private ElementDef selectedElementDef;
  private boolean showFormProperties = false;
  private String associatedLabelText = "";
  private String elementInnerText = "";
  private String screenType = "MONITOR"; // DEFAULT
  private static final String CONTAINER_ID = "panel";
  private static final String DEFAULT_MAP = "novetats_policia_60";

  // Select vars
  private String selectedMode = ""; // DEFAULT
  private List<SelectOption> manualOptionsList = new ArrayList<>();

  // Spinners Width
  private Integer colDefault;
  private Integer colMd;
  private Integer colLg;
  private Integer colXl;

  /* -- CONFIG DICT FOR ALL ELEMENTS --*/
  public enum ElementDef
  {
    INPUT("Input Text", "input", true, true, "col-12 md:col-6", "text", "inputText_template.xhtml", "inputText_props.xhtml"),
    RADIO("Input Radio", "input", true, false, "", "radio", "inputRadio_template.xhtml", "inputRadio_props.xhtml"),
    CHECKBOX("Input CheckBox", "input", true, false, "", "checkbox", "inputCheckbox_template.xhtml", "inputCheckbox_props.xhtml"),
    TEXTAREA("Textarea", "textarea", true, true, "col-12 md:col-12", null, "textArea_template.xhtml", "textArea_props.xhtml"),
    SELECT("Select", "select", true, true, "col-12 md:col-6", null, "select_template.xhtml", "select_props.xhtml"),
    BUTTON("Button", "button", false, true, "col-12 md:col-6", "button", "button_template.xhtml", "button_props.xhtml"),
    IMAGE("Image", "img", false, true, "col-12 md:col-6", null, "image_template.xhtml", "image_props.xhtml"),
    FIELDSET("Fieldset", "fieldset", false, true, "col-12 md:col-6", null, "fieldset_template.xhtml", "fieldset_props.xhtml"),
    MAPLIBRE("MapLibre", "div", false, false, "col-12 md:col-12", "mapLibre", "divMaplibre_template.xhtml", "divMaplibre_props.xhtml"),
    DIV("RawHtml", "div", false, true, "col-12", null, "div_template.xhtml", "div_props.xhtml");

    private final String label;
    private final String tag;
    private final boolean hasLabel;
    private final boolean resizable;
    private final String defaultWidth;
    private final String defaultType;
    private final String templateFile;
    private final String propFile;

    ElementDef(String label, String tag, boolean hasLabel, boolean resizable, String defaultWidth, String defaultType, String templateFile, String propFile)
    {
      this.label = label;
      this.tag = tag;
      this.hasLabel = hasLabel;
      this.resizable = resizable;
      this.defaultWidth = defaultWidth;
      this.defaultType = defaultType;
      this.templateFile = templateFile;
      this.propFile = propFile;
    }

    public static ElementDef fromTagAndType(String tag, String type)
    {
      if (tag == null)
      {
        return null;
      }

      if ("input".equals(tag) && (type == null || type.isEmpty()))
      {
        type = "text";
      }
      if ("button".equals(tag) && (type == null || type.isEmpty()))
      {
        type = "button";
      }

      for (ElementDef def : values())
      {
        if (def.getTag().equals(tag))
        {
          if (def.getDefaultType() == null)
          {
            return def;
          }
          if (def.getDefaultType().equals(type))
          {
            return def;
          }
        }
      }
      return null;
    }

    public static ElementDef fromView(HtmlViewWrapper wrapper)
    {
      if (wrapper == null || wrapper.getNativeViewType() == null)
      {
        return null;
      }

      String tag = wrapper.getNativeViewType();
      String type = wrapper.getInputType();

      if ("div".equals(tag) && "mapLibre".equals(wrapper.getRenderer()))
      {
        type = "mapLibre";
      }

      return fromTagAndType(tag, type);
    }

    public String getLabel()
    {
      return label;
    }

    public String getTag()
    {
      return tag;
    }

    public boolean isHasLabel()
    {
      return hasLabel;
    }

    public boolean isResizable()
    {
      return resizable;
    }

    public String getDefaultWidth()
    {
      return defaultWidth;
    }

    public String getDefaultType()
    {
      return defaultType;
    }

    public String getTemplateFile()
    {
      return templateFile;
    }

    public String getPropFile()
    {
      return propFile;
    }
  }

  public enum AddMode
  {
    BOTTOM, BEFORE, INSIDE
  };

  /* -- DTO FOR VISUAL GROUPING -- */
  public static class VisualCanvasBlock
  {

    private HtmlViewWrapper label;
    private HtmlViewWrapper element;
    private boolean labelFirst = true;
    private List<VisualCanvasBlock> childrenBlocks = new ArrayList();

    public VisualCanvasBlock(HtmlViewWrapper label, HtmlViewWrapper element)
    {
      this.label = label;
      this.element = element;
    }

    public HtmlViewWrapper getLabel()
    {
      return this.label;
    }

    public HtmlViewWrapper getElement()
    {
      return this.element;
    }

    public List<VisualCanvasBlock> getChildrenBlocks()
    {
      return childrenBlocks;
    }

    public void setChildrenBlocks(List<VisualCanvasBlock> childrenBlocks)
    {
      this.childrenBlocks = childrenBlocks;
    }

    public boolean isLabelFirst()
    {
      return labelFirst;
    }

    public void setLabelFirst(boolean labelFirst)
    {
      this.labelFirst = labelFirst;
    }
  }

  /* -- DTO FOR SELECT OPTIONS -- */
  public static class SelectOption implements Serializable
  {

    private static final long serialVersionUID = 1L;
    private String value;
    private String text;

    public SelectOption()
    {
    }

    public SelectOption(String value, String text)
    {
      this.value = value;
      this.text = text;
    }

    public static long getSerialVersionUID()
    {
      return serialVersionUID;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public String getText()
    {
      return text;
    }

    public void setText(String text)
    {
      this.text = text;
    }
  }

  public HtmlView getSelectedView()
  {
    if (this.selectedViewId == null)
    {
      return null;
    }
    HtmlForm form = htmlFormBean.getForm();
    if (form != null && form.getRootView() != null)
    {
      HtmlView view = findViewByIdRecursively((HtmlView) form.getRootView(), this.selectedViewId);

      // If view found, renovate element reference
      if (view != null)
      {
        view.setReference(this.selectedViewId);
      }

      return view;
    }
    return null;
  }

  public void setSelectedView(HtmlView view)
  {
    if (view != null)
    {
      this.selectedViewId = view.getId();
    }
    else
    {
      this.selectedViewId = null;
    }
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

        return def != ElementDef.RADIO && def != ElementDef.CHECKBOX;
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

  // Por si volvemos a querer el UI:REPEAT
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
    List<HtmlView> processedElements = new ArrayList<>();

    for (int i = 0; i < children.size(); i++)
    {
      HtmlView currentView = (HtmlView) children.get(i);
      if (processedElements.contains(currentView))
      {
        continue;
      }

      String type = currentView.getNativeViewType();
      if ("#text".equals(type) || "legend".equals(type))
      {
        continue;
      }

      HtmlViewWrapper labelWrapper = null;
      HtmlViewWrapper elementWrapper = null;
      boolean isLabelFirst = true;

      if ("label".equals(type))
      {
        labelWrapper = new HtmlViewWrapper(currentView);
        processedElements.add(currentView);
        String forAttr = labelWrapper.getProperty("for");
        if (forAttr != null && !forAttr.trim().isEmpty())
        {
          for (int j = 0; j < children.size(); j++)
          {
            HtmlView candidate = (HtmlView) children.get(j);
            if (processedElements.contains(candidate))
            {
              continue;
            }
            String candidateId = candidate.getId();
            if (candidateId == null)
            {
              candidateId = (String) candidate.getProperty("id");
            }
            if (forAttr.equals(candidateId))
            {
              elementWrapper = new HtmlViewWrapper(candidate);
              processedElements.add(candidate);
              isLabelFirst = (i < j);
              break;
            }
          }
        }
      }
      else
      {
        elementWrapper = new HtmlViewWrapper(currentView);
        processedElements.add(currentView);
        String myId = elementWrapper.getId();
        if (myId != null && !myId.trim().isEmpty())
        {
          for (int j = 0; j < children.size(); j++)
          {
            HtmlView candidate = (HtmlView) children.get(j);
            if (processedElements.contains(candidate))
            {
              continue;
            }
            if ("label".equals(candidate.getNativeViewType()))
            {
              String forAttr = (String) candidate.getProperty("for");
              if (myId.equals(forAttr))
              {
                labelWrapper = new HtmlViewWrapper(candidate);
                processedElements.add(candidate);
                isLabelFirst = (j < i);
                break;
              }
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
    return findViewByIdRecursively((HtmlView) form.getRootView(), CONTAINER_ID);
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

  private HtmlView findViewByIdRecursively(HtmlView view, String id)
  {
    if (view == null || id == null)
    {
      return null;
    }

    String viewId = view.getId();
    if (viewId == null || viewId.trim().isEmpty())
    {
      viewId = (String) view.getProperty("id");
    }

    if (id.equals(viewId) || id.equals(view.getReference()))
    {
      return view;
    }

    for (View child : view.getChildren())
    {
      HtmlView found = findViewByIdRecursively((HtmlView) child, id);
      if (found != null)
      {
        return found;
      }
    }
    return null;
  }

  public void saveVisualEditorToText()
  {
    try
    {
      HtmlForm form = htmlFormBean.getForm();
      if (form != null)
      {
        if (form.getRootView() != null)
        {
          cleanWhitespaceNodes((HtmlView) form.getRootView());
        }

        StringWriter sw = new StringWriter();
        form.write(new WriterOutputStream(sw, StandardCharsets.UTF_8), null);

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
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
    if (selectedElementDef != null)
    {
      HtmlView currentView = getSelectedView();
      if (currentView != null && "fieldset".equals(currentView.getNativeViewType()))
      {
        if (selectedElementDef != ElementDef.RADIO && selectedElementDef != ElementDef.CHECKBOX)
        {
          return;
        }
      }
      addElement(selectedElementDef.getTag(), selectedElementDef.getDefaultType(), AddMode.INSIDE);
    }
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
    String id = "i" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
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
    String baseWidth = config != null ? config.getDefaultWidth() : "col-12";
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
    boolean isLabelAfter = config != null && "radio".equals(config.getDefaultType());
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
      // Insertion into the final
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

    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
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
            else if (!this.manualOptionsList.isEmpty())
            {
              this.selectedMode = "MANUAL";
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
      error("No element selected. Please click on an elemenet in the canvas first.");
      return;
    }

    List<View> container = getParentContainer(getPanelChildren(), currentView);
    HtmlView labelView = (container != null) ? getAssociatedLabel(container, currentView) : null;

    HtmlViewWrapper wrapper = new HtmlViewWrapper(currentView);
    String newId = wrapper.getId();

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
      String dataRef = wrapper.getDataRef();
      String sql = wrapper.getSql();

      boolean hasExternalSource = (dataRef != null && !dataRef.trim().isEmpty())
        || (sql != null && !sql.trim().isEmpty());
//      boolean hasManualOptions = this.manualOptionsList != null
//        && !this.manualOptionsList.isEmpty();

      if (hasExternalSource)
      {
        wrapper.getChildren().clear(); //Clear manualOptions
      }
      else
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
    }
    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();

  }

  public void saveFormProperties()
  {
    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
  }

  public void cancelElement()
  {
    this.selectedViewId = null;
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

    String searchId = target.getReference();
    if (searchId == null)
    {
      searchId = target.getId();
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
    for (int i = blockStart - 1; i >= 0; i--)
    {
      HtmlView v = (HtmlView) container.get(i);
      if ("#text".equals(v.getNativeViewType()))
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
          if ("#text".equals(v2.getNativeViewType()))
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
          if ("#text".equals(v2.getNativeViewType()))
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

    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
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
      if ("#text".equals(v.getNativeViewType()))
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
          if ("#text".equals(v2.getNativeViewType()))
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
          if ("#text".equals(v2.getNativeViewType()))
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

    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
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

    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
  }

  private void cleanWhitespaceNodes(HtmlView view)
  {
    cleanWhitespaceNodesRecursive(view, false);
  }

  private void cleanWhitespaceNodesRecursive(HtmlView view, boolean preserveWhitespace)
  {
    if (view == null || view.getChildren() == null)
    {
      return;
    }

    boolean isPre = preserveWhitespace || "pre".equalsIgnoreCase(view.getNativeViewType()) || "code".equalsIgnoreCase(view.getNativeViewType());

    for (int i = view.getChildren().size() - 1; i >= 0; i--)
    {
      View child = view.getChildren().get(i);
      if (child instanceof HtmlView)
      {
        HtmlView htmlChild = (HtmlView) child;
        if ("#text".equals(htmlChild.getNativeViewType()))
        {
          String text = htmlChild.getProperty("text");

          // If it has only blank spaces or is empty, erase it
          if (text == null || text.replace("&#160;", "").replace("&nbsp;", "").trim().isEmpty())
          {
            if (!isPre)
            {
              view.getChildren().remove(i);
            }
          }
          else if (!isPre)
          {
            // Convert to simple withespaces
            String cleaned = text.replaceAll("[\\r\\n\\t]+", " ");

            cleaned = cleaned.replaceAll("(?:&#160;|&nbsp;| )+", " ");

            htmlChild.setProperty("text", cleaned);
          }
        }
        else
        {
          cleanWhitespaceNodesRecursive(htmlChild, isPre);
        }
      }
    }
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

    htmlFormBean.setUpdate(true);
    saveVisualEditorToText();
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

      // Syn the spinners
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

      htmlFormBean.setUpdate(true);
      saveVisualEditorToText();
    }
  }

  public String getSimulatedGridClass(HtmlViewWrapper wrapper)
  {
    if (wrapper == null)
    {
      return "col-12 md:col-6";
    }

    String realGridClass = wrapper.getGridClass();

    if ("MOBILE".equals(screenType))
    {
      return realGridClass.replaceAll("(?:sm:|md:|lg:|xl:)col-\\d+", "").replaceAll("\\s+", " ").trim();
    }
    else if ("TABLET".equals(screenType))
    {
      return realGridClass.replaceAll("(?:lg:|xl:)col-\\d+", "").replaceAll("\\s+", " ").trim();
    }
    else if ("LAPTOP".equals(screenType))
    {
      return realGridClass.replaceAll("(?:xl:)col-\\d+", "").replaceAll("\\s+", " ").trim();
    }

    //If it's Monitor (xl) do not remove anything
    return realGridClass;
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

    // First-load case: The div is parsed in the tree. Extract the literal contenet from the original source. (Preserves identation).
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
    // Remove only leading/trailing line breaks, but keep internal user indentation
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

  public boolean isLegacyFormDetected()
  {
    String source = htmlFormBean.getIdeBean().getDocument().getSource();
    if (source == null)
    {
      return false;
    }

    return source.contains("position:absolute")
      || source.contains("data-outputorder")
      || source.contains("FORM");
  }
}
