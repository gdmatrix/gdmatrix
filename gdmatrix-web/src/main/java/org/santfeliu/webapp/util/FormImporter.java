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
package org.santfeliu.webapp.util;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.enterprise.inject.spi.CDI;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.component.chips.Chips;
import org.primefaces.component.datepicker.DatePicker;
import org.primefaces.component.inputnumber.InputNumber;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.outputpanel.OutputPanel;
import org.primefaces.component.password.Password;
import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectoneradio.SelectOneRadio;
import org.primefaces.component.toggleswitch.ToggleSwitch;
import org.santfeliu.faces.codemirror.CodeMirror;
import org.santfeliu.faces.maplibre.MapLibre;
import org.santfeliu.faces.quill.Quill;
import org.santfeliu.faces.tinymce.TinyMCE;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlRadioView;
import org.santfeliu.form.type.html.HtmlSelectView;
import org.santfeliu.form.type.html.HtmlView;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.converters.SpecialCharsConverter;
import org.santfeliu.webapp.modules.geo.io.MapDocument;
import org.santfeliu.webapp.modules.geo.io.MapStore;


/**
 *
 * @author realor
 */

public class FormImporter
{
  public static final String STACKED_OPTION = "stacked";
  public static final String INSPECT_OPTION = "inspect";
  public static final String SUBMIT_BUTTON_OPTION = "checkExpression";
  public static final String ACTION_METHOD_OPTION = "actionMethod";
  public static final String ACTION_UPDATE_OPTION = "actionUpdate";
  public static final String SEARCH_FORM_OPTION = "searchForm";

  protected Map<String, Object> options = new HashMap<>();
  protected Form form;
  protected UIComponent formRoot;
  protected String propertyPathUni;
  protected String propertyPathMulti;
  protected HashSet<String> importedReferences = new HashSet<>();

  public Map<String, Object> getOptions()
  {
    return options;
  }

  public void importForm(Form form, UIComponent formRoot,
    String propertyPathUni, String propertyPathMulti)
  {
    this.form = form;
    this.formRoot = formRoot;
    this.propertyPathUni = propertyPathUni;
    this.propertyPathMulti = propertyPathMulti;

    if (form instanceof HtmlForm)
    {
      HtmlForm htmlForm = (HtmlForm)form;
      HtmlView rootView = (HtmlView)htmlForm.getRootView();
      importHtmlView(rootView, formRoot);
    }
    else
    {
      importFields();
    }
  }

  protected void importHtmlView(HtmlView view, UIComponent parent)
  {
    String tag = view.getNativeViewType();

    if (tag == null) tag = "span";

    if (tag.equals("label"))
    {
      // discard, paint labels with fields
    }
    else if (tag.equals("input"))
    {
      String type = view.getProperty("type");
      if ("submit".equals(type))
      {
        importSubmit(view, parent);
      }
      else
      {
        String reference = view.getReference();
        Field field = form.getField(reference);
        importField(field, parent);
      }
    }
    else if (tag.equals("textarea") ||
             tag.equals("select") ||
             tag.equals("checkbox"))
    {
      String reference = view.getReference();
      Field field = form.getField(reference);
      importField(field, parent);
    }
    else if (tag.equals("a"))
    {
      importLink(view, parent);
    }
    else if (tag.equals("script"))
    {
      importScript(view, parent);
    }
    else if (tag.equals("img"))
    {
      importImage(view, parent);
    }
    else if (View.TEXT.equals(view.getViewType()))
    {
      importText(view, parent);
    }
    else if (tag.equals("div") && !
      "body".equals(view.getParent().getNativeViewType()))
    {
      String renderer = view.getProperty("renderer");
      if ("maplibre".equalsIgnoreCase(renderer))
      {
        importMapLibre(view, parent);
      }
      else
      {
        importOutputText(view, parent);
      }
    }
    else
    {
      importChildren(view, parent);
    }
  }

  protected void importChildren(HtmlView view, UIComponent parent)
  {
    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      importHtmlView((HtmlView)childView, parent);
    }
  }

  protected void importText(HtmlView view, UIComponent parent)
  {
    String text = view.getProperty("text");
    if (StringUtils.isBlank(text)) return;
    if (text.endsWith(":")) return;
    if (text.toLowerCase().contains("dd/mm/")) return;

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    HtmlOutputText outputText =
      (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);

    outputText.setValue(text);
    outputText.setStyleClass("field col-12 md:col-6");
    parent.getChildren().add(outputText);
  }

  protected void importSubmit(HtmlView view, UIComponent parent)
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    HtmlPanelGroup group =
      (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
    group.setLayout("block");

    Object styleClassValue = view.getProperty("class");
    String styleClass = styleClassValue instanceof String ?
      (String)styleClassValue : "";

    if (!styleClass.contains("field"))
    {
      styleClass += " field";
    }
    if (!styleClass.contains("col-"))
    {
      styleClass += " col-12";
    }
    group.setStyleClass(styleClass.trim());

    CommandButton commandButton =
      (CommandButton)application.createComponent(CommandButton.COMPONENT_TYPE);

    String name = view.getProperty("name");
    String value = view.getProperty("value");

    commandButton.setValue(value);
    group.getChildren().add(commandButton);

    String actionMethod = (String)options.get(ACTION_METHOD_OPTION);
    if (actionMethod != null && name != null && value != null)
    {
      ExpressionFactory expressionFactory = application.getExpressionFactory();
      ELContext elContext = facesContext.getELContext();

      String expression =
        "#{" + actionMethod + "('" + name + "', '" + value + "')}";

      MethodExpression expr = expressionFactory.createMethodExpression(
        elContext, expression, Void.class,
        new Class[]{ String.class, String.class });
      commandButton.setActionExpression(expr);
      String actionUpdate = (String)options.get(ACTION_UPDATE_OPTION);
      if (actionUpdate != null)
      {
        commandButton.setUpdate(actionUpdate);
      }
    }
    parent.getChildren().add(group);
  }

  protected void importLink(HtmlView view, UIComponent parent)
  {
    String url = view.getProperty("href");
    String target = view.getProperty("target");

    List<View> children = view.getChildren();
    if (children.size() == 1 && !StringUtils.isBlank(url))
    {
      HtmlView childView = (HtmlView)children.get(0);
      if (View.TEXT.equals(childView.getViewType()))
      {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        HtmlOutputLink outputLink =
          (HtmlOutputLink)application.createComponent(HtmlOutputLink.COMPONENT_TYPE);
        outputLink.setValue(url);
        outputLink.setTarget(target);

        HtmlOutputText outputText =
          (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        outputText.setValue(childView.getProperty("text"));
        outputLink.getChildren().add(outputText);

        parent.getChildren().add(outputLink);
      }
    }
  }

  protected void importScript(HtmlView view, UIComponent parent)
  {
    List<View> children = view.getChildren();
    if (!children.isEmpty())
    {
      HtmlView childView = (HtmlView) children.get(0);
      if (View.TEXT.equals(childView.getViewType()))
      {
        StringBuilder sb = new StringBuilder();
        sb.append("<")
          .append(view.getNativeViewType())
          .append(" >")
          .append(childView.getProperty("text"))
          .append("</")
          .append(view.getNativeViewType())
          .append(">");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        HtmlOutputText outputText =
          (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        outputText.setEscape(false);
        outputText.setValue(sb.toString());
        parent.getChildren().add(outputText);
      }
    }
  }

  protected void importImage(HtmlView view, UIComponent parent)
  {
    String url = view.getProperty("src");
    String alt = view.getProperty("alt");
    String styleClass = view.getProperty("class");
    String style = view.getProperty("style");

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    HtmlGraphicImage graphicImage =
      (HtmlGraphicImage) application.createComponent(HtmlGraphicImage.COMPONENT_TYPE);
    graphicImage.setUrl(url);
    graphicImage.setAlt(alt);
    graphicImage.setStyleClass(styleClass);
    graphicImage.setStyle(style);

    parent.getChildren().add(graphicImage);
  }

  protected void importFields()
  {
    Collection<Field> fields = form.getFields();
    for (Field field : fields)
    {
      importField(field, formRoot);
    }
  }

  protected void importField(Field field, UIComponent parent)
  {
    boolean isStacked = "true".equals(options.get(STACKED_OPTION));

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    String labelText = field.getLabel();
    String reference = field.getReference();
    String fieldType = field.getType();
    View view = form.getView(reference);

    UIComponent component = null;
    boolean isMultiple = false;

    if (field.isReadOnly() && view == null)
    {
      // ignore
    }
    else if (Field.DATE.equals(fieldType) || Field.DATETIME.equals(fieldType))
    {
      DatePicker datePicker =
        (DatePicker)application.createComponent(DatePicker.COMPONENT_TYPE);
      datePicker.setLocale(facesContext.getViewRoot().getLocale());
      datePicker.setConverter(
        application.createConverter("datePickerConverter"));
      String pattern = Field.DATE.equals(fieldType)
        ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm";
      datePicker.setPattern(pattern);
      datePicker.setPlaceholder(pattern);
      datePicker.setShowIcon(true);
      datePicker.setShowOnFocus(false);
      datePicker.setYearNavigator(true);
      datePicker.setMonthNavigator(true);
      SimpleDateFormat df = new SimpleDateFormat("yyyy");
      long nowMillis = System.currentTimeMillis();
      long millisPerYear = 365 * 24 * 60 * 60 * 1000L;
      Date minDate = new Date(nowMillis - 100 * millisPerYear);
      Date maxDate = new Date(nowMillis + 20 * millisPerYear);
      String minYear = df.format(minDate);
      String maxYear = df.format(maxDate);
      datePicker.setYearRange(minYear + ":" + maxYear);
      datePicker.setReadonly(field.isReadOnly());
      datePicker.setDisabled(field.isReadOnly());
      if (field.getMinOccurs() > 0) setRequired(datePicker);
      component = datePicker;
    }
    else if (Field.NUMBER.equals(fieldType))
    {
      InputNumber inputNumber =
        (InputNumber)application.createComponent(InputNumber.COMPONENT_TYPE);
      inputNumber.setReadonly(field.isReadOnly());
      inputNumber.setPadControl(false);

      //Convert value to number.
      NumberConverter converter = new NumberConverter();
      converter.setGroupingUsed(false);
      converter.setLocale(Locale.US);
      inputNumber.setConverter(converter);

      //Set separators format
      DecimalFormatSymbols dfs =
        DecimalFormatSymbols.getInstance(facesContext.getViewRoot().getLocale());
      inputNumber.setDecimalSeparator(String.valueOf(dfs.getDecimalSeparator()));
      inputNumber.setThousandSeparator(String.valueOf(dfs.getGroupingSeparator()));
      inputNumber.setDecimalPlaces("18");

      if (field.getMinOccurs() > 0) setRequired(inputNumber);
      component = inputNumber;
    }
    else if (Field.BOOLEAN.equals(fieldType))
    {
      if (isStacked || field.getMinOccurs() == 0)
      {
        SelectOneMenu selectOneMenu =
          (SelectOneMenu)application.createComponent(SelectOneMenu.COMPONENT_TYPE);
        selectOneMenu.setTouchable(true);
        selectOneMenu.setAutoWidth("false");
        selectOneMenu.setReadonly(field.isReadOnly());

        UISelectItem selectItem = new UISelectItem();
        selectItem.setItemValue(null);
        selectItem.setItemLabel("");
        selectOneMenu.getChildren().add(selectItem);

        selectItem = new UISelectItem();
        selectItem.setItemValue(false);
        selectItem.setItemLabel("FALSE");
        selectOneMenu.getChildren().add(selectItem);

        selectItem = new UISelectItem();
        selectItem.setItemValue(true);
        selectItem.setItemLabel("TRUE");
        selectOneMenu.getChildren().add(selectItem);
        component = selectOneMenu;
      }
      else
      {
        ToggleSwitch toogleSwitch =
          (ToggleSwitch)application.createComponent(ToggleSwitch.COMPONENT_TYPE);
        toogleSwitch.setStyleClass("block");
        toogleSwitch.setDisabled(field.isReadOnly());
        component = toogleSwitch;
      }
    }
    else
    {
      if (view instanceof HtmlSelectView)
      {        
        String multipleValue = String.valueOf(view.getProperty("multiple"));
        isMultiple = "true".equals(multipleValue);

        if (isMultiple)
        {
          SelectCheckboxMenu select =
            (SelectCheckboxMenu)application.createComponent(SelectCheckboxMenu.COMPONENT_TYPE);
          select.setMultiple(true);
          select.setReadonly(field.isReadOnly());
          component = select;
        }
        else
        {
          SelectOneMenu select =
            (SelectOneMenu)application.createComponent(SelectOneMenu.COMPONENT_TYPE);
          select.setReadonly(field.isReadOnly());
          select.setDisabled(field.isReadOnly());
          select.setAutoWidth("false");
          select.setTouchable(true);
          String renderer = (String)view.getProperty("renderer");                  
          if ("autocomplete".equals(renderer))
          {
            select.setFilter(true);
            select.setFilterMatchMode("contains");
            select.setFilterNormalize(true);
          }
          String onChange = (String)view.getProperty("onchange");
          if (onChange != null)
          {
            onChange = onChange.trim();
            if ("submit()".equals(onChange) || "submit();".equals(onChange))
            {
              AjaxBehavior ajax = new AjaxBehavior();
              ajax.setProcess("@this");
              ajax.setUpdate("dyn_form");
              select.addClientBehavior("itemSelect", ajax);
            }
          }
          component = select;          
        }

        String searchForm = (String) options.get(SEARCH_FORM_OPTION);
        boolean addEmptyValue = searchForm != null && searchForm.equals("true");

        List<View> children = view.getChildren();

        try
        {
          if (addEmptyValue && !isMultiple)
          {
            component.getChildren().add(new UISelectItem());
          }

          for (View child : children)
          {
            if (View.ITEM.equals(child.getViewType()))
            {
              String itemValue = (String)child.getProperty("value");
              String itemLabel = child.getChildren().isEmpty() ? "" :
                (String)child.getChildren().get(0).getProperty("text");

              if (!isBlank(itemLabel) || !addEmptyValue)
              {
                UISelectItem selectItem = new UISelectItem();
                selectItem.setItemValue(itemValue);
                selectItem.setItemLabel(itemLabel);
                component.getChildren().add(selectItem);
              }
            }
          }
        }
        catch (Exception exx)
        { exx.printStackTrace(); };
      }
      else if (view instanceof HtmlRadioView)
      {
        if (!importedReferences.contains(reference))
        {
          List<View> radioViews = new ArrayList<>();
          findViews(form.getRootView(), reference, radioViews);
          SelectOneRadio selectOneRadio =
            (SelectOneRadio)application.createComponent(SelectOneRadio.COMPONENT_TYPE);          
          
          for (View radioView : radioViews)
          {
            String radioId = radioView.getId();
            String itemValue = String.valueOf(radioView.getProperty("value"));
            String itemLabel = itemValue;
            if (!StringUtils.isBlank(radioId))
            {
              View labelView =
                findLabelViewForId(form.getRootView(), radioId);
              if (labelView != null && !labelView.getChildren().isEmpty())
              {
                View textView = labelView.getChildren().get(0);
                itemLabel = String.valueOf(textView.getProperty("text"));
              }
            }
            UISelectItem selectItem = new UISelectItem();
            selectItem.setItemValue(itemValue);
            selectItem.setItemLabel(itemLabel);
            selectOneRadio.getChildren().add(selectItem);
            if (selectOneRadio.getLayout() == null)
            {
              String layout = (String)radioView.getProperty("renderer");
              if (layout != null) selectOneRadio.setLayout(layout);
            }
            if (!selectOneRadio.getAttributes().containsKey("viewStyleClass"))
            {
              String styleClass = (String)radioView.getProperty("class");
              if (styleClass != null)
              {
                selectOneRadio.getAttributes().put("viewStyleClass", 
                  styleClass);
              }
            }
          }
          importedReferences.add(reference);
          labelText = null;
          component = selectOneRadio;
        }
      }
      else
      {
        if (view != null && "textarea".equalsIgnoreCase(view.getNativeViewType()))
        {
          String renderer = (String)view.getProperty("renderer");
          if (renderer == null) renderer = "textarea";

          switch (renderer)
          {
            case "htmlEditor": // compatibility
            case "quill":
            {
              Quill quill =
                (Quill)application.createComponent(Quill.COMPONENT_TYPE);
              quill.setReadonly(field.isReadOnly());
              Object maxLength = view.getProperty("maxlength");
              if (maxLength == null)
                quill.setMaxLength(0);
              else
                quill.setMaxLength(Integer.valueOf((String) maxLength));
              if (field.getMinOccurs() > 0) setRequired(quill);
              quill.setConverter(new SpecialCharsConverter());
              component = quill;
            }
            break;

            case "tinymce":
            {
              TinyMCE tinymce =
                (TinyMCE)application.createComponent(TinyMCE.COMPONENT_TYPE);
              tinymce.setReadonly(field.isReadOnly());
              if (field.getMinOccurs() > 0) setRequired(tinymce);
              tinymce.setConverter(new SpecialCharsConverter());
              component = tinymce;
            }
            break;

            case "codemirror":
            case "codemirror:javascript":
            case "codemirror:sql":
            case "codemirror:html":
            case "codemirror:json":
            {
              CodeMirror codemirror =
                (CodeMirror)application.createComponent(CodeMirror.COMPONENT_TYPE);
              codemirror.setReadonly(field.isReadOnly());
              int index = renderer.lastIndexOf(":");
              String language = index == -1 ?
                "javascript" : renderer.substring(index + 1);
              codemirror.setLanguage(language);
              component = codemirror;
            }
            break;

            default:
            {
              InputTextarea inputTextarea =
                (InputTextarea)application.createComponent(InputTextarea.COMPONENT_TYPE);
              inputTextarea.setReadonly(field.isReadOnly());
              if (field.getMinOccurs() > 0) setRequired(inputTextarea);
              inputTextarea.setConverter(new SpecialCharsConverter());
              component = inputTextarea;
            }
          }
        }
        else
        {
          Object inputType = view == null ? null : view.getProperty("type");

          if (inputType == null || "text".equals(inputType))
          {
            String renderer =
              view != null ? (String)view.getProperty("renderer") : "text";
            if (renderer == null)
              renderer = "text";
            isMultiple = field.getMaxOccurs() != 1;

            switch (renderer)
            {
              case "chips":
              case "multiple":
              case "multivalued": isMultiple = true;
              break;
            }

            if (!isMultiple)
            {
              InputText inputText =
               (InputText)application.createComponent(InputText.COMPONENT_TYPE);
              inputText.setReadonly(field.isReadOnly());
              if (field.getMinOccurs() > 0) setRequired(inputText);
              component = inputText;
            }
            else
            {
              Chips chips =
                (Chips)application.createComponent(Chips.COMPONENT_TYPE);
              if (field.getMaxOccurs() > 1)
              {
                chips.setMax(field.getMaxOccurs());
              }
              component = chips;
            }
          }
          else if ("password".equals(inputType))
          {
            Password password =
             (Password)application.createComponent(Password.COMPONENT_TYPE);
            password.setReadonly(field.isReadOnly());
            component = password;
          }
        }
      }
    }

    // add component to panel
    if (component instanceof ValueHolder)
    {
      String styleClass = null;
      if (view != null)
      {
        Object styleClassValue = view.getProperty("class");
        styleClass = styleClassValue instanceof String ?
          (String)styleClassValue : null;
      }

      HtmlPanelGroup group =
        (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
      if (isStacked)
      {
        styleClass = "col-12";
      }
      else if (component instanceof InputTextarea ||
        component instanceof Quill ||
        component instanceof TinyMCE ||
        component instanceof CodeMirror)
      {
        if (styleClass == null) styleClass = "col-12";        
        else if (!styleClass.startsWith("col-") && 
          !styleClass.contains(" col-"))
        {
          styleClass += " col-12";
        }
      }
      else if (component instanceof SelectOneRadio)
      {
        String viewStyleClass = 
          (String)component.getAttributes().get("viewStyleClass");
        styleClass = (viewStyleClass != null ? viewStyleClass : "col-12");
      }
      else
      {
        if (styleClass == null || !styleClass.contains("col-"))
        {
          styleClass = "col-12 md:col-6";
        }
      }
      group.setStyleClass("field " + styleClass);
      group.setLayout("block");
      parent.getChildren().add(group);

      OutputLabel label = null;

      if (!StringUtils.isBlank(labelText))
      {
        label =
          (OutputLabel)application.createComponent(OutputLabel.COMPONENT_TYPE);

        label.setValue(labelText);
        label.setEscape(false);
        label.setFor("@next");
        if (field.getMinOccurs() > 1) label.setIndicateRequired("true");

        group.getChildren().add(label);
      }

      String propertyPath = isMultiple ? propertyPathMulti : propertyPathUni;

      String expression = "#{" + propertyPath + "[\"" + reference + "\"]}";

      Class type = isMultiple ? Collection.class : Object.class;
      ValueExpression valueExpression =
        WebUtils.createValueExpression(expression, type);

      component.setValueExpression("value", valueExpression);

      //Inputgroup
      HtmlPanelGroup inputgroup = null;
      if (view != null)
      {
        Object infoIcon = view.getProperty("infoicon");
        Object infoText = view.getProperty("infotext");

        if (infoIcon != null || infoText != null)
        {
          inputgroup =
            (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
          inputgroup.setStyleClass("ui-inputgroup");

          boolean leftIcon = false;
          boolean leftText = false;
          OutputPanel iconAddon = null;
          OutputPanel textAddon = null;

          if (infoIcon != null)
          {
            String info = String.valueOf(infoIcon);
            if (info.startsWith("left:"))
            {
              leftIcon = true;
              info = info.substring(5, info.length());
            }
            iconAddon =
              (OutputPanel)application.createComponent(OutputPanel.COMPONENT_TYPE);
            iconAddon.setStyleClass("ui-inputgroup-addon");

            HtmlPanelGroup outputPanel =
              (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);

            outputPanel.setStyleClass(info);

            iconAddon.getChildren().add(outputPanel);

            if (leftIcon)
              inputgroup.getChildren().add(iconAddon);
          }

          if (infoText != null)
          {
            String info = String.valueOf(infoText);
            if (info.startsWith("left:"))
            {
              leftText = true;
              info = info.substring(5, info.length());
            }

            textAddon =
              (OutputPanel)application.createComponent(OutputPanel.COMPONENT_TYPE);
            textAddon.setStyleClass("ui-inputgroup-addon");

            HtmlOutputText outputText =
              (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            outputText.setValue(info);
            outputText.setEscape(false);

            textAddon.getChildren().add(outputText);

            if (leftText)
              inputgroup.getChildren().add(textAddon);
          }

          inputgroup.getChildren().add(component);

          if (infoIcon != null && !leftIcon)
              inputgroup.getChildren().add(iconAddon);
          if (infoText != null && !leftText)
              inputgroup.getChildren().add(textAddon);

          if (label != null)
          {
            int idx = inputgroup.getChildren().indexOf(component);
            label.setFor("@next:@child(" + idx + ")");
          }

          group.getChildren().add(inputgroup);
        }
      }

      //Component without no inputgrup
      if (inputgroup == null)
        group.getChildren().add(component);

      //Help text
      if (view != null)
      {
        Object helpText = view.getProperty("helptext");
        if (helpText != null)
        {
          HtmlOutputText helpOutputText =
            (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
          helpOutputText.setStyleClass("text-xs");
          helpOutputText.setValue(String.valueOf(helpText));
          helpOutputText.setEscape(false);
          group.getChildren().add(helpOutputText);
        }
      }

      if (isInspectMode() && view.getProperty("name") != null)
      {
        HtmlOutputText varNameOutputText = (HtmlOutputText)application.
          createComponent(HtmlOutputText.COMPONENT_TYPE);
        varNameOutputText.setStyleClass("text-xs text-red-700 block");
        varNameOutputText.setStyle("word-break: break-all;" +
          varNameOutputText.getStyle());
        varNameOutputText.setValue(view.getProperty("name"));
        group.getChildren().add(varNameOutputText);
      }
    }
  }

  protected void importMapLibre(HtmlView view, UIComponent parent)
  {
    try
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Application application = facesContext.getApplication();

      String mapName = encodeView(view, new StringBuilder()).toString();
      if (mapName == null) return;

      String id = view.getId();
      if (id != null)
      {
        InputText hiddenInput =
          (InputText)application.createComponent(InputText.COMPONENT_TYPE);
        hiddenInput.setId(id);
        hiddenInput.setStyleClass("hidden");
        hiddenInput.setWidgetVar(id);

        String expression = "#{" + propertyPathUni + "[\"" + id + "\"]}";
        ValueExpression valueExpression =
          WebUtils.createValueExpression(expression, String.class);

        hiddenInput.setValueExpression("value", valueExpression);
        parent.getChildren().add(hiddenInput);
      }

      MapLibre mapLibre = (MapLibre)application.
        createComponent(MapLibre.COMPONENT_TYPE);
      mapLibre.setStyleClass("m-2 surface-border border-solid border-1");

      String height = "300px";
      String style = view.getProperty("style");
      if (style != null)
      {
        int index = style.indexOf("height:");
        if (index != -1)
        {
          style = style.substring(index + 7);
          index = style.indexOf(";");
          if (index != -1) style = style.substring(0, index);
          height = style;
        }
      }

      mapLibre.setStyle("width:100%;height:" + height);

      MapStore mapStore = CDI.current().select(MapStore.class).get();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String userId = userSessionBean.getUserId();
      String password = userSessionBean.getPassword();
      mapStore.setCredentials(userId, password);
      MapDocument mapDocument = mapStore.loadMap(mapName);

      mapLibre.setValue(mapDocument.getStyle());

      parent.getChildren().add(mapLibre);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  protected void importOutputText(HtmlView view, UIComponent parent)
  {
    String style = view.getProperty("style");
    boolean isTextArea = style != null && !style.contains("line-height");
    if (isTextArea)
    {
      List<View> children = view.getChildren();
      if (!children.isEmpty())
      {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        HtmlOutputText outputText =
          (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        outputText.setEscape(false);
        String styleClass = view.getProperty("class");
        if (styleClass == null) styleClass = "col-12";
        if (!styleClass.contains("py-"))
        {
           styleClass += " py-2";
        }
        outputText.setStyleClass(styleClass);
        outputText.setValue(encodeView(view, new StringBuilder()).toString());
        parent.getChildren().add(outputText);
      }
    }
  }

  protected StringBuilder encodeView(HtmlView view, StringBuilder sb)
  {
    for (int i = 0; i < view.getChildren().size(); i++)
    {
      HtmlView childView = (HtmlView) view.getChildren().get(i);
      if (View.TEXT.equals(childView.getViewType()))
      {
        sb.append(childView.getProperty("text"));
      }
      else
      {
        sb.append("<").append(childView.getNativeViewType());
        for (String name : childView.getPropertyNames())
        {
          sb.append(" ").append(name).append("=\"")
            .append(childView.getProperty(name))
            .append("\"");
        }
        sb.append(" >");
        sb = encodeView(childView, sb);
        sb.append("</").append(childView.getNativeViewType()).append(">");
      }
    }
    return sb;
  }

  protected void findViews(View base, String reference, List<View> views)
  {
    List<View> children = base.getChildren();
    for (View child : children)
    {
      if (reference.equals(child.getReference()))
      {
        views.add(child);
      }
      else
      {
        findViews(child, reference, views);
      }
    }
  }

  protected View findLabelViewForId(View base, String id)
  {
    Object forId = base.getProperty("for");
    if (id.equals(forId)) return base;

    List<View> children = base.getChildren();
    for (View childView : children)
    {
      View view = findLabelViewForId(childView, id);
      if (view != null) return view;
    }
    return null;
  }

  protected void setRequired(UIInput input)
  {
    String submitButton = (String)options.get(SUBMIT_BUTTON_OPTION);
    if (submitButton == null)
    {
      input.setRequired(true);
    }
    else
    {
      input.setValueExpression("required", WebUtils.createValueExpression(
        "#{not empty param['" + submitButton + "']}", Boolean.class));
    }
  }

  private boolean isInspectMode()
  {
    Map<String, Object> panelAttributes = formRoot.getPassThroughAttributes();
    Boolean inspectMode = (Boolean)panelAttributes.get(INSPECT_OPTION);
    return (inspectMode == null ? false : inspectMode);
  }

}
