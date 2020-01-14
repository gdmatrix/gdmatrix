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
package org.santfeliu.faces.render.myfaces.menu;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable;
import org.apache.myfaces.shared_tomahawk.component.EscapeCapable;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.HTMLEncoder;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;

import javax.faces.FacesException;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ExternalContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

/**
 * @author Manfred Geiler (latest modification by $Author: skitching $)
 * @version $Revision: 613572 $ $Date: 2008-01-20 10:07:27 -0500 (Sun, 20 Jan 2008) $
 */
public final class HtmlRendererUtils {
    private static final Log log = LogFactory.getLog(HtmlRendererUtils.class);

    //private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String LINE_SEPARATOR = System.getProperty(
        "line.separator", "\r\n");
    private static final char TABULATOR = '\t';

    public static final String HIDDEN_COMMANDLINK_FIELD_NAME = "_idcl";
    public static final String HIDDEN_COMMANDLINK_FIELD_NAME_MYFACES_OLD = "_link_hidden_";
    public static final String HIDDEN_COMMANDLINK_FIELD_NAME_TRINIDAD = "source";

    public static final String CLEAR_HIDDEN_FIELD_FN_NAME =
        "clearFormHiddenParams";
    public static final String SUBMIT_FORM_FN_NAME = "oamSubmitForm";
    public static final String ALLOW_CDATA_SECTION_ON = "org.apache.myfaces.ResponseWriter.CdataSectionOn";

    private static final String SET_HIDDEN_INPUT_FN_NAME = "oamSetHiddenInput";
    private static final String CLEAR_HIDDEN_INPUT_FN_NAME = "oamClearHiddenInput";

    private static final String AUTO_SCROLL_PARAM = "autoScroll";
    private static final String AUTO_SCROLL_FUNCTION = "getScrolling";

    private static final String FIRST_SUBMIT_SCRIPT_ON_PAGE = "org.apache.MyFaces.FIRST_SUBMIT_SCRIPT_ON_PAGE";

    public static final String NON_SUBMITTED_VALUE_WARNING
    = "There should always be a submitted value for an input if it is rendered,"
    + " its form is submitted, and it was not originally rendered disabled or read-only."
    + "  You cannot submit a form after disabling an input element via javascript."
    + "  Consider setting read-only to true instead"
    + " or resetting the disabled value back to false prior to form submission.";

    private HtmlRendererUtils() {
        // utility class, do not instantiate
    }

    /**
     * Utility to set the submitted value of the provided component from the
     * data in the current request object.
     * <p/>
     * Param component is required to be an EditableValueHolder. On return
     * from this method, the component's submittedValue property will be
     * set if the submitted form contained that component.
     */
    public static void decodeUIInput(FacesContext facesContext,
                                     UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                + component.getClientId(facesContext)
                + " is not an EditableValueHolder");
        }
        Map paramMap = facesContext.getExternalContext()
            .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);

        if (isDisabledOrReadOnly(component))
            return;

        if (paramMap.containsKey(clientId)) {
            ((EditableValueHolder) component).setSubmittedValue(paramMap
                .get(clientId));
        }
        else {
            log.warn(NON_SUBMITTED_VALUE_WARNING +
                " Component : " +
                    RendererUtils.getPathToComponent(component));
        }
    }

    /**
     * X-CHECKED: tlddoc h:selectBooleanCheckbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectBoolean(FacesContext facesContext,
                                             UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                + component.getClientId(facesContext)
                + " is not an EditableValueHolder");
        }

        if (isDisabledOrReadOnly(component))
            return;

        Map paramMap = facesContext.getExternalContext()
            .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);
        if (paramMap.containsKey(clientId)) {
            String reqValue = (String) paramMap.get(clientId);
            if ((reqValue.equalsIgnoreCase("on")
                || reqValue.equalsIgnoreCase("yes") || reqValue
                .equalsIgnoreCase("true"))) {
                ((EditableValueHolder) component)
                    .setSubmittedValue(Boolean.TRUE);
            }
            else {
                ((EditableValueHolder) component)
                    .setSubmittedValue(Boolean.FALSE);
            }
        }
        else {
            ((EditableValueHolder) component)
                .setSubmittedValue(Boolean.FALSE);
        }
    }

    public static boolean isDisabledOrReadOnly(UIComponent component) {
        return isDisplayValueOnly(component) ||
            isTrue(component.getAttributes().get("disabled")) ||
            isTrue(component.getAttributes().get("readonly"));
    }

    private static boolean isTrue(Object obj) {
        if (!(obj instanceof Boolean))
            return false;

        return ((Boolean) obj).booleanValue();
    }

    /**
     * X-CHECKED: tlddoc h:selectManyListbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectMany(FacesContext facesContext,
                                          UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                + component.getClientId(facesContext)
                + " is not an EditableValueHolder");
        }
        Map paramValuesMap = facesContext.getExternalContext()
            .getRequestParameterValuesMap();
        String clientId = component.getClientId(facesContext);

        if (isDisabledOrReadOnly(component))
            return;

        if (paramValuesMap.containsKey(clientId)) {
            String[] reqValues = (String[]) paramValuesMap.get(clientId);
            ((EditableValueHolder) component).setSubmittedValue(reqValues);
        }
        else {
            /* request parameter not found, nothing to decode - set submitted value to an empty array
               as we should get here only if the component is on a submitted form, is rendered
               and if the component is not readonly or has not been disabled.

               So in fact, there must be component value at this location, but for listboxes, comboboxes etc.
               the submitted value is not posted if no item is selected. */
            ((EditableValueHolder) component).setSubmittedValue(new String[]{});
        }
    }

    /**
     * X-CHECKED: tlddoc h:selectManyListbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectOne(FacesContext facesContext,
                                         UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                + component.getClientId(facesContext)
                + " is not an EditableValueHolder");
        }

        if (isDisabledOrReadOnly(component))
            return;

        Map paramMap = facesContext.getExternalContext()
            .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);
        if (paramMap.containsKey(clientId)) {
            //request parameter found, set submitted value
            ((EditableValueHolder) component).setSubmittedValue(paramMap
                .get(clientId));
        }
        else {
            //see reason for this action at decodeUISelectMany
            ((EditableValueHolder) component).setSubmittedValue(RendererUtils.NOTHING);
        }
    }

    /*
     * public static void renderCheckbox(FacesContext facesContext, UIComponent
     * uiComponent, String value, String label, boolean checked) throws
     * IOException { String clientId = uiComponent.getClientId(facesContext);
     *
     * ResponseWriter writer = facesContext.getResponseWriter();
     *
     * writer.startElement(HTML.INPUT_ELEM, uiComponent);
     * writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_CHECKBOX, null);
     * writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
     * writer.writeAttribute(HTML.ID_ATTR, clientId, null);
     *
     * if (checked) { writer.writeAttribute(HTML.CHECKED_ATTR,
     * HTML.CHECKED_ATTR, null); }
     *
     * if ((value != null) && (value.length() > 0)) {
     * writer.writeAttribute(HTML.VALUE_ATTR, value, null); }
     *
     * renderHTMLAttributes(writer, uiComponent,
     * HTML.INPUT_PASSTHROUGH_ATTRIBUTES); renderDisabledOnUserRole(writer,
     * uiComponent, facesContext);
     *
     * if ((label != null) && (label.length() > 0)) {
     * writer.write(HTML.NBSP_ENTITY); writer.writeText(label, null); }
     *
     * writer.endElement(HTML.INPUT_ELEM); }
     */

    public static void renderListbox(FacesContext facesContext,
                                     UISelectOne selectOne, boolean disabled, int size)
        throws IOException {
        internalRenderSelect(facesContext, selectOne, disabled, size, false);
    }

    public static void renderListbox(FacesContext facesContext,
                                     UISelectMany selectMany, boolean disabled, int size)
        throws IOException {
        internalRenderSelect(facesContext, selectMany, disabled, size, true);
    }

    public static void renderMenu(FacesContext facesContext,
                                  UISelectOne selectOne, boolean disabled) throws IOException {
        internalRenderSelect(facesContext, selectOne, disabled, 1, false);
    }

    public static void renderMenu(FacesContext facesContext,
                                  UISelectMany selectMany, boolean disabled) throws IOException {
        internalRenderSelect(facesContext, selectMany, disabled, 1, true);
    }

    private static void internalRenderSelect(FacesContext facesContext,
                                             UIComponent uiComponent, boolean disabled, int size,
                                             boolean selectMany) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SELECT_ELEM, uiComponent);
        HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        writer.writeAttribute(HTML.NAME_ATTR, uiComponent
            .getClientId(facesContext), null);

        List selectItemList;
        Converter converter;
        if (selectMany) {
            writer.writeAttribute(HTML.MULTIPLE_ATTR, HTML.MULTIPLE_ATTR, null);
            selectItemList = org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils
                .getSelectItemList((UISelectMany) uiComponent);
            converter = findUISelectManyConverterFailsafe(facesContext, uiComponent);
        }
        else {
            selectItemList = RendererUtils
                .getSelectItemList((UISelectOne) uiComponent);
            converter = findUIOutputConverterFailSafe(facesContext, uiComponent);
        }

        if (size == 0) {
            //No size given (Listbox) --> size is number of select items
            writer.writeAttribute(HTML.SIZE_ATTR, Integer
                .toString(selectItemList.size()), null);
        }
        else {
            writer.writeAttribute(HTML.SIZE_ATTR, Integer.toString(size), null);
        }
        renderHTMLAttributes(writer, uiComponent,
                             HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        if (disabled) {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        Set lookupSet = getSubmittedOrSelectedValuesAsSet(selectMany, uiComponent, facesContext, converter);

        renderSelectOptions(facesContext, uiComponent, converter, lookupSet,
                            selectItemList);
        // bug #970747: force separate end tag
        writer.writeText("", null);
        writer.endElement(HTML.SELECT_ELEM);
    }

    public static Set getSubmittedOrSelectedValuesAsSet(boolean selectMany, UIComponent uiComponent, FacesContext facesContext, Converter converter) {
        Set lookupSet;

        if (selectMany) {
            UISelectMany uiSelectMany = (UISelectMany) uiComponent;
            lookupSet = RendererUtils.getSubmittedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
            if (lookupSet == null) {
                lookupSet = RendererUtils.getSelectedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
            }
        }
        else {
            UISelectOne uiSelectOne = (UISelectOne) uiComponent;
            Object lookup = uiSelectOne.getSubmittedValue();
            if (lookup == null) {
                lookup = uiSelectOne.getValue();
                String lookupString = RendererUtils.getConvertedStringValue(facesContext, uiComponent, converter, lookup);
                lookupSet = Collections.singleton(lookupString);
            }
            else if (org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils.NOTHING.equals(lookup)) {
                lookupSet = Collections.EMPTY_SET;
            }
            else {
                lookupSet = Collections.singleton(lookup);
            }
        }
        return lookupSet;
    }

    public static Converter findUISelectManyConverterFailsafe(FacesContext facesContext, UIComponent uiComponent) {
        Converter converter;
        try {
            converter = RendererUtils.findUISelectManyConverter(
                facesContext, (UISelectMany) uiComponent);
        }
        catch (FacesException e) {
            log.error("Error finding Converter for component with id "
                + uiComponent.getClientId(facesContext), e);
            converter = null;
        }
        return converter;
    }

    public static Converter findUIOutputConverterFailSafe(FacesContext facesContext, UIComponent uiComponent) {
        Converter converter;
        try {
            converter = RendererUtils.findUIOutputConverter(facesContext,
                                                            (UIOutput) uiComponent);
        }
        catch (FacesException e) {
            log.error("Error finding Converter for component with id "
                + uiComponent.getClientId(facesContext), e);
            converter = null;
        }
        return converter;
    }

    /**
     * Renders the select options for a <code>UIComponent</code> that is
     * rendered as an HTML select element.
     *
     * @param context        the current <code>FacesContext</code>.
     * @param component      the <code>UIComponent</code> whose options need to be
     *                       rendered.
     * @param converter      <code>component</code>'s converter
     * @param lookupSet      the <code>Set</code> to use to look up selected options
     * @param selectItemList the <code>List</code> of <code>SelectItem</code> s to be
     *                       rendered as HTML option elements.
     * @throws IOException
     */
    public static void renderSelectOptions(FacesContext context,
                                           UIComponent component, Converter converter, Set lookupSet,
                                           List selectItemList) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (Iterator it = selectItemList.iterator(); it.hasNext();) {
            SelectItem selectItem = (SelectItem) it.next();

            if (selectItem instanceof SelectItemGroup) {
                writer.startElement(HTML.OPTGROUP_ELEM, component);
                writer.writeAttribute(HTML.LABEL_ATTR, selectItem.getLabel(),
                                      null);
                SelectItem[] selectItems = ((SelectItemGroup) selectItem)
                    .getSelectItems();
                renderSelectOptions(context, component, converter, lookupSet,
                                    Arrays.asList(selectItems));
                writer.endElement(HTML.OPTGROUP_ELEM);
            }
            else {
                String itemStrValue = org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils.getConvertedStringValue(context, component,
                                                                                                                converter, selectItem);

                writer.write(TABULATOR);
                writer.startElement(HTML.OPTION_ELEM, component);
                if (itemStrValue != null) {
                    writer.writeAttribute(HTML.VALUE_ATTR, itemStrValue, null);
                }

                if (lookupSet.contains(itemStrValue))
                {  //TODO/FIX: we always compare the String vales, better fill lookupSet with Strings only when useSubmittedValue==true, else use the real item value Objects
                    writer.writeAttribute(HTML.SELECTED_ATTR,
                                          HTML.SELECTED_ATTR, null);
                }

                boolean disabled = selectItem.isDisabled();
                if (disabled) {
                    writer.writeAttribute(HTML.DISABLED_ATTR,
                                          HTML.DISABLED_ATTR, null);
                }

                String labelClass;
                boolean componentDisabled = isTrue(component.getAttributes().get("disabled"));

                if (componentDisabled || disabled) {
                    labelClass = (String) component.getAttributes().get(JSFAttr.DISABLED_CLASS_ATTR);
                }
                else {
                    labelClass = (String) component.getAttributes().get(JSFAttr.ENABLED_CLASS_ATTR);
                }
                if (labelClass != null) {
                    writer.writeAttribute("class", labelClass, "labelClass");
                }

                boolean escape;
                if (component instanceof EscapeCapable) {
                    escape = ((EscapeCapable) component).isEscape();
                }
                else {
                    escape = RendererUtils.getBooleanAttribute(component, JSFAttr.ESCAPE_ATTR,
                                                               true); //default is to escape
                }

                if (StringUtils.isBlank(selectItem.getLabel()))
                  writer.writeAttribute("label", " ", null);  
                
                if (escape)                 
                  writer.writeText(selectItem.getLabel(), null); 
                else 
                  writer.write(selectItem.getLabel());                 


                writer.endElement(HTML.OPTION_ELEM);
            }
        }
    }

    /*
     * public static void renderRadio(FacesContext facesContext, UIInput
     * uiComponent, String value, String label, boolean checked) throws
     * IOException { String clientId = uiComponent.getClientId(facesContext);
     *
     * ResponseWriter writer = facesContext.getResponseWriter();
     *
     * writer.startElement(HTML.INPUT_ELEM, uiComponent);
     * writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_RADIO, null);
     * writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
     * writer.writeAttribute(HTML.ID_ATTR, clientId, null);
     *
     * if (checked) { writer.writeAttribute(HTML.CHECKED_ATTR,
     * HTML.CHECKED_ATTR, null); }
     *
     * if ((value != null) && (value.length() > 0)) {
     * writer.writeAttribute(HTML.VALUE_ATTR, value, null); }
     *
     * renderHTMLAttributes(writer, uiComponent,
     * HTML.INPUT_PASSTHROUGH_ATTRIBUTES); renderDisabledOnUserRole(writer,
     * uiComponent, facesContext);
     *
     * if ((label != null) && (label.length() > 0)) {
     * writer.write(HTML.NBSP_ENTITY); writer.writeText(label, null); }
     *
     * writer.endElement(HTML.INPUT_ELEM); }
     */

    public static void writePrettyLineSeparator(FacesContext facesContext)
        throws IOException {
        if (MyfacesConfig.getCurrentInstance(facesContext.getExternalContext())
            .isPrettyHtml()) {
            facesContext.getResponseWriter().write(LINE_SEPARATOR);
        }
    }

    public static void writePrettyIndent(FacesContext facesContext)
        throws IOException {
        if (MyfacesConfig.getCurrentInstance(facesContext.getExternalContext())
            .isPrettyHtml()) {
            facesContext.getResponseWriter().write(TABULATOR);
        }
    }

    /**
     * @return true, if the attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttribute(ResponseWriter writer,
                                              String componentProperty, String attrName, Object value)
        throws IOException {
        if (!RendererUtils.isDefaultAttributeValue(value)) {
            // render JSF "styleClass" and "itemStyleClass" attributes as "class"
            String htmlAttrName =
                attrName.equals(HTML.STYLE_CLASS_ATTR) ?
                    HTML.CLASS_ATTR : attrName;
            writer.writeAttribute(htmlAttrName, value, componentProperty);
            return true;
        }

        return false;
    }

    /**
     * @return true, if the attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttribute(ResponseWriter writer,
                                              UIComponent component, String componentProperty, String htmlAttrName)
        throws IOException {
        Object value = component.getAttributes().get(componentProperty);
        return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                                   value);
    }

    /**
     * @return true, if an attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttributes(ResponseWriter writer,
                                               UIComponent component, String[] attributes) throws IOException {
        boolean somethingDone = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            if (renderHTMLAttribute(writer, component, attrName, attrName)) {
                somethingDone = true;
            }
        }
        return somethingDone;
    }

    public static boolean renderHTMLAttributeWithOptionalStartElement(
        ResponseWriter writer, UIComponent component, String elementName,
        String attrName, Object value, boolean startElementWritten)
        throws IOException {
        if (!org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils.isDefaultAttributeValue(value)) {
            if (!startElementWritten) {
                writer.startElement(elementName, component);
                startElementWritten = true;
            }
            renderHTMLAttribute(writer, attrName, attrName, value);
        }
        return startElementWritten;
    }

    public static boolean renderHTMLAttributesWithOptionalStartElement(
        ResponseWriter writer, UIComponent component, String elementName,
        String[] attributes) throws IOException {
        boolean startElementWritten = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            Object value = component.getAttributes().get(attrName);
            if (!RendererUtils.isDefaultAttributeValue(value)) {
                if (!startElementWritten) {
                    writer.startElement(elementName, component);
                    startElementWritten = true;
                }
                renderHTMLAttribute(writer, attrName, attrName, value);
            }
        }
        return startElementWritten;
    }

    public static boolean renderOptionalEndElement(ResponseWriter writer,
                                                   UIComponent component, String elementName, String[] attributes)
        throws IOException {
        boolean endElementNeeded = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            Object value = component.getAttributes().get(attrName);
            if (!RendererUtils.isDefaultAttributeValue(value)) {
                endElementNeeded = true;
                break;
            }
        }
        if (endElementNeeded) {
            writer.endElement(elementName);
            return true;
        }

        return false;
    }

    public static void writeIdIfNecessary(ResponseWriter writer, UIComponent component,
                                          FacesContext facesContext)
        throws IOException {
        if (component.getId() != null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext), null);
        }
    }

    public static void renderDisplayValueOnlyForSelects(FacesContext facesContext, UIComponent
        uiComponent)
        throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        List selectItemList = null;
        Converter converter = null;
        boolean isSelectOne = false;

        if (uiComponent instanceof UISelectBoolean) {
            converter = findUIOutputConverterFailSafe(facesContext, uiComponent);

            writer.startElement(HTML.SPAN_ELEM, uiComponent);
            writeIdIfNecessary(writer, uiComponent, facesContext);
            renderDisplayValueOnlyAttributes(uiComponent, writer);
            writer.writeText(RendererUtils.getConvertedStringValue(facesContext, uiComponent,
                                                                   converter, ((UISelectBoolean) uiComponent).getValue()), JSFAttr.VALUE_ATTR);
            writer.endElement(HTML.SPAN_ELEM);

        }
        else {
            if (uiComponent instanceof UISelectMany) {
                isSelectOne = false;
                selectItemList = RendererUtils
                    .getSelectItemList((UISelectMany) uiComponent);
                converter = findUISelectManyConverterFailsafe(facesContext, uiComponent);
            }
            else if (uiComponent instanceof UISelectOne) {
                isSelectOne = true;
                selectItemList = RendererUtils
                    .getSelectItemList((UISelectOne) uiComponent);
                converter = findUIOutputConverterFailSafe(facesContext, uiComponent);
            }

            writer.startElement(isSelectOne ? HTML.SPAN_ELEM : HTML.UL_ELEM, uiComponent);
            writeIdIfNecessary(writer, uiComponent, facesContext);

            renderDisplayValueOnlyAttributes(uiComponent, writer);

            Set lookupSet = getSubmittedOrSelectedValuesAsSet(
                uiComponent instanceof UISelectMany,
                uiComponent, facesContext, converter);

            renderSelectOptionsAsText(facesContext, uiComponent, converter, lookupSet,
                                      selectItemList, isSelectOne);

            // bug #970747: force separate end tag
            writer.writeText("", null);
            writer.endElement(isSelectOne ? HTML.SPAN_ELEM : HTML.UL_ELEM);
        }

    }

    public static void renderDisplayValueOnlyAttributes(UIComponent uiComponent, ResponseWriter writer) throws IOException {
        if (!(uiComponent instanceof org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable)) {
            log.error("Wrong type of uiComponent. needs DisplayValueOnlyCapable.");
            renderHTMLAttributes(writer, uiComponent,
                                 HTML.COMMON_PASSTROUGH_ATTRIBUTES);

            return;
        }

        if (getDisplayValueOnlyStyle(uiComponent) != null || getDisplayValueOnlyStyleClass(uiComponent) != null) {
            if (getDisplayValueOnlyStyle(uiComponent) != null) {
                writer.writeAttribute(HTML.STYLE_ATTR, getDisplayValueOnlyStyle(uiComponent), null);
            }
            else if (uiComponent.getAttributes().get("style") != null) {
                writer.writeAttribute(HTML.STYLE_ATTR, uiComponent.getAttributes().get("style"), null);
            }

            if (getDisplayValueOnlyStyleClass(uiComponent) != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, getDisplayValueOnlyStyleClass(uiComponent), null);
            }
            else if (uiComponent.getAttributes().get("styleClass") != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, uiComponent.getAttributes().get("styleClass"), null);
            }

            renderHTMLAttributes(writer, uiComponent,
                                 HTML.COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE);
        }
        else {
            renderHTMLAttributes(writer, uiComponent,
                                 HTML.COMMON_PASSTROUGH_ATTRIBUTES);
        }
    }

    private static void renderSelectOptionsAsText(FacesContext context,
                                                  UIComponent component, Converter converter, Set lookupSet,
                                                  List selectItemList, boolean isSelectOne) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (Iterator it = selectItemList.iterator(); it.hasNext();) {
            SelectItem selectItem = (SelectItem) it.next();

            if (selectItem instanceof SelectItemGroup) {
                SelectItem[] selectItems = ((SelectItemGroup) selectItem)
                    .getSelectItems();
                renderSelectOptionsAsText(context, component, converter, lookupSet,
                                          Arrays.asList(selectItems), isSelectOne);
            }
            else {
                String itemStrValue = RendererUtils.getConvertedStringValue(context, component,
                                                                            converter, selectItem);

                if (lookupSet.contains(itemStrValue))
                {  //TODO/FIX: we always compare the String vales, better fill lookupSet with Strings only when useSubmittedValue==true, else use the real item value Objects

                    if (! isSelectOne)
                        writer.startElement(HTML.LI_ELEM, component);
                    writer.writeText(selectItem.getLabel(), null);
                    if (! isSelectOne)
                        writer.endElement(HTML.LI_ELEM);

                    if (isSelectOne) {
                        //take care of several choices with the same value; use only the first one
                        return;
                    }
                }
            }
        }
    }

    public static String getDisplayValueOnlyStyleClass(UIComponent component) {

        if (component instanceof org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) {
            if (((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyleClass() != null)
                return ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyleClass();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable &&
                    ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyleClass() != null)
                {
                    return ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyleClass();
                }
            }
        }

        return null;
    }

    public static String getDisplayValueOnlyStyle(UIComponent component) {

        if (component instanceof DisplayValueOnlyCapable) {
            if (((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyle() != null)
                return ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyle();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable &&
                    ((DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyle() != null) {
                    return ((DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyle();
                }
            }
        }

        return null;
    }

    public static boolean isDisplayValueOnly(UIComponent component) {

        if (component instanceof DisplayValueOnlyCapable) {
            if (((DisplayValueOnlyCapable) component).isSetDisplayValueOnly())
                return ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) component).isDisplayValueOnly();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof DisplayValueOnlyCapable &&
                    ((DisplayValueOnlyCapable) parent).isSetDisplayValueOnly()) {
                    return ((org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable) parent).isDisplayValueOnly();
                }
            }
        }

        return false;
    }

    public static void renderDisplayValueOnly(FacesContext facesContext, UIComponent input) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.SPAN_ELEM, input);

        writeIdIfNecessary(writer, input, facesContext);

        renderDisplayValueOnlyAttributes(input, writer);

        String strValue = RendererUtils.getStringValue(facesContext, input);
        writer.write(HTMLEncoder.encode(strValue, true, true));

        writer.endElement(HTML.SPAN_ELEM);
    }

    public static void appendClearHiddenCommandFormParamsFunctionCall(StringBuffer buf, String formName) {
        appendClearHiddenCommandFormParamsFunctionCall(new ScriptContext(buf,false), formName);
    }

    private static void appendClearHiddenCommandFormParamsFunctionCall(ScriptContext context, String formName) {

        String functionName = HtmlRendererUtils.getClearHiddenCommandFormParamsFunctionName(formName);

        if(formName == null)
        {
            context.prettyLine();
            context.append("var clearFn = ");
            context.append(functionName);
            context.append(";");
            context.prettyLine();
            context.append("if(typeof eval('window.'+clearFn)!='undefined')");
            context.append("{");
            context.append("eval('window.'+clearFn+'(formName)');");
            context.append("}");
        }
        else
        {
            context.prettyLine();
            context.append("if(typeof window.");
            context.append(functionName);
            context.append("!='undefined')");
            context.append("{");
            context.append(functionName).append("('").append(formName).append("');");
            context.append("}");
        }
    }


    public static void renderFormSubmitScript(FacesContext facesContext)
            throws IOException
    {

        Map map = facesContext.getExternalContext().getRequestMap();
        Boolean firstScript = (Boolean) map.get(FIRST_SUBMIT_SCRIPT_ON_PAGE);

        if (firstScript == null || firstScript.equals(Boolean.TRUE)) {
            map.put(FIRST_SUBMIT_SCRIPT_ON_PAGE, Boolean.FALSE);
            HtmlRendererUtils.renderFormSubmitScriptIfNecessary(facesContext);

        }
    }

    private static void renderFormSubmitScriptIfNecessary(FacesContext facesContext) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);

        final ExternalContext externalContext = facesContext.getExternalContext();
        final MyfacesConfig currentInstance = MyfacesConfig.getCurrentInstance(externalContext);
        boolean autoScroll = currentInstance.isAutoScroll();

        ScriptContext context = new ScriptContext(currentInstance
            .isPrettyHtml());
        context.prettyLine();
        context.increaseIndent();

        prepareScript(context, autoScroll);

        writer.writeText(context.toString(),null);

        writer.endElement(HTML.SCRIPT_ELEM);
    }

    private static void prepareScript(ScriptContext context, boolean autoScroll)
    {

        context.prettyLine();

        //render a function to create a hidden input, if it doesn't exist
        context.append("function ");
        context.append(SET_HIDDEN_INPUT_FN_NAME).append("(formname, name, value)");
        context.append("{");
        context.append("var form = document.forms[formname];");
        context.prettyLine();
        context.append("if(typeof form.elements[name]=='undefined')");
        context.append("{");
        context.append("var newInput = document.createElement('input');");
        context.prettyLine();
        context.append("newInput.setAttribute('type','hidden');");
        context.prettyLine();
        context.append("newInput.setAttribute('id',name);"); //  // IE hack; See MYFACES-1805
        context.prettyLine();
        context.append("newInput.setAttribute('name',name);");
        context.prettyLine();
        context.append("newInput.setAttribute('value',value);");
        context.prettyLine();
        context.append("form.appendChild(newInput);");
        context.append("}");
        context.append("else");
        context.append("{");
        context.append("form.elements[name].value=value;");
        context.append("}");

        context.append("}");

        context.prettyLine();

        context.prettyLine();

        //render a function to clear a hidden input, if it exists
        context.append("function ");
        context.append(CLEAR_HIDDEN_INPUT_FN_NAME).append("(formname, name, value)");
        context.append("{");
        context.append("var form = document.forms[formname];");
        context.prettyLine();
        context.append("if(typeof form.elements[name]!='undefined')");
        context.append("{");
        context.append("form.elements[name].value=null;");
        context.append("}");

        context.append("}");

        context.prettyLine();

        context.append("function ");
        context.append(SUBMIT_FORM_FN_NAME).append("(formName, linkId, target, params)");
        context.append("{");

        //call the script to clear the form (clearFormHiddenParams_<formName>) method - optionally, only necessary for IE5.5.
        //todo: if IE5.5. is ever desupported, we can get rid of this and instead rely on the last part of this script to
        //clear the parameters
        HtmlRendererUtils.appendClearHiddenCommandFormParamsFunctionCall(context, null);

        if (autoScroll)
        {
            appendAutoScrollAssignment(context, null);
        }

        context.prettyLine();

        //set the target (and save it)
        context.append("var oldTarget = '';");
        context.prettyLine();
        context.append("if((typeof target!='undefined') && target != null)");
        context.append("{");
        context.append("oldTarget=document.forms[formName].target;");
        context.prettyLine();
        context.append("document.forms[formName].target=target;");
        context.append("}");

        //set the submit parameters

        context.append("if((typeof params!='undefined') && params != null)");
        context.append("{");
        context.append("for(var i=0; i<params.length; i++)");
        context.append("{");
        context.append(SET_HIDDEN_INPUT_FN_NAME).append("(formName,params[i][0], params[i][1]);");
        context.append("}");
        context.append("}");

        context.prettyLine();

        context.append(SET_HIDDEN_INPUT_FN_NAME);
        context.append("(formName,formName +'"+NamingContainer.SEPARATOR_CHAR+
                "'+'"+HtmlRendererUtils.HIDDEN_COMMANDLINK_FIELD_NAME+"',linkId);");

        context.prettyLine();
        context.prettyLine();

        //do the actual submit calls

        context.append("if(document.forms[formName].onsubmit)");
        context.append("{");
        context.append("var result=document.forms[formName].onsubmit();");
        context.prettyLine();
        context.append("if((typeof result=='undefined')||result)");
        context.append("{");
        context.append("document.forms[formName].submit();");
        context.append("}");
        context.append("}");
        context.append("else ");
        context.append("{");
        context.append("document.forms[formName].submit();");
        context.append("}");

        //reset the target
        context.append("if(oldTarget==null) oldTarget='';");
        context.prettyLine();
        context.append("document.forms[formName].target=oldTarget;");
        context.prettyLine();

        //clear the individual parameters - to make sure that even if the clear-function isn't called,
        // the back button/resubmit functionality will still work in all browsers except IE 5.5.

        context.append("if((typeof params!='undefined') && params != null)");
        context.append("{");
        context.append("for(var i=0; i<params.length; i++)");
        context.append("{");
        context.append(CLEAR_HIDDEN_INPUT_FN_NAME).append("(formName,params[i][0], params[i][1]);");
        context.append("}");
        context.append("}");

        context.prettyLine();

        context.append(CLEAR_HIDDEN_INPUT_FN_NAME);
        context.append("(formName,formName +'"+NamingContainer.SEPARATOR_CHAR+
                "'+'"+HtmlRendererUtils.HIDDEN_COMMANDLINK_FIELD_NAME+"',linkId);");


        //return false, so that browser does not handle the click
        context.append("return false;");
        context.append("}");

        context.prettyLineDecreaseIndent();
    }

    /**
     * Adds the hidden form input value assignment that is necessary for the autoscroll
     * feature to an html link or button onclick attribute.
     */
    public static void appendAutoScrollAssignment(StringBuffer onClickValue, String formName)
    {
        appendAutoScrollAssignment(new ScriptContext(onClickValue,false),formName);
    }

    private static void appendAutoScrollAssignment(ScriptContext scriptContext, String formName)
    {
        String formNameStr = formName == null? "formName" : (new StringBuffer("'").append(formName).append("'").toString());
        String paramName = new StringBuffer().append("'").
                append(AUTO_SCROLL_PARAM).append("'").toString();
        String value = new StringBuffer().append(AUTO_SCROLL_FUNCTION).append("()").toString();

        scriptContext.prettyLine();
        scriptContext.append("if(typeof window."+AUTO_SCROLL_FUNCTION+"!='undefined')");
        scriptContext.append("{");
        scriptContext.append(SET_HIDDEN_INPUT_FN_NAME);
        scriptContext.append("(").append(formNameStr).append(",").append(paramName).append(",").append(value).append(");");
        scriptContext.append("}");

    }

    /**
     * Renders the hidden form input that is necessary for the autoscroll feature.
     */
    public static void renderAutoScrollHiddenInput(FacesContext facesContext, ResponseWriter writer) throws IOException
    {
        writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.INPUT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, AUTO_SCROLL_PARAM, null);
        writer.endElement(HTML.INPUT_ELEM);
        writePrettyLineSeparator(facesContext);
    }

    /**
     * Renders the autoscroll javascript function.
     */
    public static void renderAutoScrollFunction(FacesContext facesContext,
                                                ResponseWriter writer) throws IOException
    {
        writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.SCRIPT_ELEM,null);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);

        ScriptContext script = new ScriptContext(
                MyfacesConfig.getCurrentInstance(facesContext.getExternalContext()).isPrettyHtml());

        script.prettyLineIncreaseIndent();

        script.append("function ");
        script.append(AUTO_SCROLL_FUNCTION);
        script.append("()");
        script.append("{");
        script.append("var x = 0; var y = 0;");
        script.append( "if (self.pageXOffset || self.pageYOffset)");
        script.append("{");
        script.append("x = self.pageXOffset;");
        script.prettyLine();
        script.append("y = self.pageYOffset;");
        script.append("}");
        script.append(" else if ((document.documentElement && document.documentElement.scrollLeft)||(document.documentElement && document.documentElement.scrollTop))");
        script.append("{");
        script.append("x = document.documentElement.scrollLeft;");
        script.prettyLine();
        script.append("y = document.documentElement.scrollTop;");
        script.append("}");
        script.append(" else if (document.body) ");
        script.append("{");
        script.append("x = document.body.scrollLeft;");
        script.prettyLine();
        script.append("y = document.body.scrollTop;");
        script.append("}");
        script.append("return x + \",\" + y;");
        script.append("}");

        ExternalContext externalContext = facesContext.getExternalContext();
        String oldViewId = JavascriptUtils.getOldViewId(externalContext);
        if (oldViewId != null && oldViewId.equals(facesContext.getViewRoot().getViewId()))
        {
            //ok, we stayed on the same page, so let's scroll it to the former place
            String scrolling = (String)externalContext.getRequestParameterMap().get(AUTO_SCROLL_PARAM);
            if (scrolling != null && scrolling.length() > 0)
            {
                int x = 0;
                int y = 0;
                int comma = scrolling.indexOf(',');
                if (comma == -1)
                {
                    log.warn("Illegal autoscroll request parameter: " + scrolling);
                }
                else
                {
                    try {
                        //we convert to int against XSS vulnerability
                        x = Integer.parseInt(scrolling.substring(0, comma));
                    } catch (NumberFormatException e) {
                        log.warn("Error getting x offset for autoscroll feature. Bad param value: " + scrolling);
                        x = 0; //ignore false numbers
                    }

                    try {
                        //we convert to int against XSS vulnerability
                        y = Integer.parseInt(scrolling.substring(comma + 1));
                    } catch (NumberFormatException e) {
                        log.warn("Error getting y offset for autoscroll feature. Bad param value: " + scrolling);
                        y = 0; //ignore false numbers
                    }
                }
                script.append("window.scrollTo(").append(x).append(",").append(y).append(");\n");
            }
        }

        writer.writeText(script.toString(),null);

        writer.endElement(HTML.SCRIPT_ELEM);
        writePrettyLineSeparator(facesContext);
    }

    public static boolean isAllowedCdataSection(FacesContext fc) {
        Boolean value = null;

        if (fc != null) {
            value = (Boolean) fc.getExternalContext().getRequestMap().get(ALLOW_CDATA_SECTION_ON);
        }

        return value != null && ((Boolean) value).booleanValue();
    }

    public static void allowCdataSection(FacesContext fc, boolean cdataSectionAllowed)
    {
          fc.getExternalContext().getRequestMap().put(ALLOW_CDATA_SECTION_ON,Boolean.valueOf(cdataSectionAllowed));
    }

    public static class LinkParameter {
        private String _name;

        private Object _value;

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public Object getValue() {
            return _value;
        }

        public void setValue(Object value) {
            _value = value;
        }

    }

    public static void renderHiddenCommandFormParams(ResponseWriter writer,
                                                     Set dummyFormParams) throws IOException {
        for (Iterator it = dummyFormParams.iterator(); it.hasNext();) {
            Object name = it.next();
            renderHiddenInputField(writer, name, null);
        }
    }

    public static void renderHiddenInputField(ResponseWriter writer, Object name, Object value)
        throws IOException {
        writer.startElement(HTML.INPUT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, JSFAttr.TYPE_ATTR);
        writer.writeAttribute(HTML.NAME_ATTR, name, HTML.NAME_ATTR);
        if (value != null) {
            writer.writeAttribute(HTML.VALUE_ATTR, value, JSFAttr.VALUE_ATTR);
        }
        writer.endElement(HTML.INPUT_ELEM);
    }

    /**
     * Render the javascript function that is called on a click on a commandLink
     * to clear the hidden inputs. This is necessary because on a browser back,
     * each hidden input still has it's old value (browser cache!) and therefore
     * a new submit would fire the according action once more!
     *
     * @param writer
     * @param formName
     * @param dummyFormParams
     * @param formTarget
     * @throws IOException
     */
    public static void renderClearHiddenCommandFormParamsFunction(
        ResponseWriter writer, String formName, Set dummyFormParams,
        String formTarget) throws IOException {
        //render the clear hidden inputs javascript function

        String functionName = getClearHiddenCommandFormParamsFunctionName(formName);
        String myfacesFunctionName = getClearHiddenCommandFormParamsFunctionNameMyfacesLegacy(formName);

        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);

        ScriptContext script = new ScriptContext(MyfacesConfig.getCurrentInstance(
                FacesContext.getCurrentInstance().getExternalContext()).isPrettyHtml());

        script.prettyLineIncreaseIndent();

        script.append("function ").append(myfacesFunctionName).append("()");
        script.append("{");
        script.append(functionName).append("('").append(formName).append("');");
        script.append("}");
        script.prettyLine();
        //the parameter in the following parameter list is added for compatibility to the RI
        //of course, it isn't necessary - the current form is defined
        //already by the formName passed as a parameter to this function, and included in the name of the function
        script.append("function ").append(functionName).append("(currFormName)");
        script.append("{");
        if (dummyFormParams != null) {
            script.append("var f = document.forms[");
            script.append("'").append(formName).append("'");
            script.append("];");
            for (Iterator it = dummyFormParams.iterator(); it.hasNext();) {
                script.prettyLine();
                script.append("f.elements['");
                script.append((String) it.next());
                script.append("'].value='';");
            }
            script.prettyLine();
        }
        // clear form target
        script.append("f.target=");
        if (formTarget == null || formTarget.length() == 0) {
            //Normally one would think that setting target to null has the
            //desired effect, but once again IE is different...
            //Setting target to null causes IE to open a new window!
            script.append("'';");
        }
        else {
            script.append("'");
            script.append(formTarget);
            script.append("';");
        }
        script.append("}");

        //To be sure, we call this clear method on each load.
        //If we don't do this and someone submits a form by pressing Enter
        //within a text input, the hidden inputs won't be cleared!
        script.prettyLine();
        script.append(functionName);
        script.append("();");

        writer.writeText(script.toString(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }

    /**
     * Prefixes the given String with CLEAR_HIDDEN_FIELD_FN_NAME and removes special characters
     *
     * @param formName
     * @return String
     */
    public static String getClearHiddenCommandFormParamsFunctionName(
        String formName) {
        if(formName == null)
        {
            return "'" +CLEAR_HIDDEN_FIELD_FN_NAME
                + "_'+formName.replace(/-/g, '\\$"+NamingContainer.SEPARATOR_CHAR+"').replace(/"+NamingContainer.SEPARATOR_CHAR+"/g,'_')";
        }

        return JavascriptUtils.getValidJavascriptNameAsInRI(CLEAR_HIDDEN_FIELD_FN_NAME
                + "_"
                + formName
                .replace(NamingContainer.SEPARATOR_CHAR, '_'));
    }

    public static String getClearHiddenCommandFormParamsFunctionNameMyfacesLegacy(
            String formName) {
        return "clear_"
               + JavascriptUtils.getValidJavascriptName(formName, false);
    }

    /**
     * Get the name of the request parameter that holds the id of the
     * link-type component that caused the form to be submitted.
     * <p/>
     * Within each page there may be multiple "link" type components that
     * cause page submission. On the server it is necessary to know which
     * of these actually caused the submit, in order to invoke the correct
     * listeners. Such components therefore store their id into the
     * "hidden command link field" in their associated form before
     * submitting it.
     * <p/>
     * The field is always a direct child of each form, and has the same
     * <i>name</i> in each form. The id of the form component is therefore
     * both necessary and sufficient to determine the full name of the
     * field.
     */
    public static String getHiddenCommandLinkFieldName(FormInfo formInfo) {
        if (RendererUtils.isAdfOrTrinidadForm(formInfo.getForm())) {
            return HIDDEN_COMMANDLINK_FIELD_NAME_TRINIDAD;
        }
        return formInfo.getFormName() + NamingContainer.SEPARATOR_CHAR
            + HIDDEN_COMMANDLINK_FIELD_NAME;
    }

    public static String getHiddenCommandLinkFieldNameMyfacesOld(FormInfo formInfo) {
        return formInfo.getFormName() + NamingContainer.SEPARATOR_CHAR
            + HIDDEN_COMMANDLINK_FIELD_NAME_MYFACES_OLD;
    }


    private static String HTML_CONTENT_TYPE = "text/html";
    private static String TEXT_ANY_CONTENT_TYPE = "text/*";
    private static String ANY_CONTENT_TYPE = "*/*";

    public static String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
    private static String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    private static String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static String TEXT_XML_CONTENT_TYPE = "text/xml";


    public static String selectContentType(String contentTypeListString) {
        if (contentTypeListString == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                contentTypeListString = (String)
                    context.getExternalContext().getRequestHeaderMap().get("Accept");
            }

            if (contentTypeListString == null) {
                if (log.isDebugEnabled())
                    log.debug("No content type list given, creating HtmlResponseWriterImpl with default content type.");

                contentTypeListString = HTML_CONTENT_TYPE;
            }
        }

        List contentTypeList = splitContentTypeListString(contentTypeListString);
        String[] supportedContentTypeArray = getSupportedContentTypes();

        String selectedContentType = null;

        for (int i = 0; i < supportedContentTypeArray.length; i++) {
            String supportedContentType = supportedContentTypeArray[i].trim();

            for (int j = 0; j < contentTypeList.size(); j++) {
                String contentType = (String) contentTypeList.get(j);

                if (contentType.indexOf(supportedContentType) != -1) {
                    if (isHTMLContentType(contentType)) {
                        selectedContentType = HTML_CONTENT_TYPE;
                    }

                    else if (isXHTMLContentType(contentType)) {
                        selectedContentType = XHTML_CONTENT_TYPE;
                    }
                    break;
                }
            }
            if (selectedContentType != null) {
                break;
            }
        }

        if (selectedContentType == null) {
            throw new IllegalArgumentException("ContentTypeList does not contain a supported content type: " +
                contentTypeListString);
        }
        return selectedContentType;
    }

    public static String[] getSupportedContentTypes() {
        //noinspection UnnecessaryLocalVariable
        String[] supportedContentTypeArray = new String[]{HTML_CONTENT_TYPE, TEXT_ANY_CONTENT_TYPE, ANY_CONTENT_TYPE,
                                                          XHTML_CONTENT_TYPE, APPLICATION_XML_CONTENT_TYPE, TEXT_XML_CONTENT_TYPE};
        return supportedContentTypeArray;
    }

    private static boolean isHTMLContentType(String contentType) {
        return contentType.indexOf(HTML_CONTENT_TYPE) != -1 ||
            contentType.indexOf(ANY_CONTENT_TYPE) != -1 ||
            contentType.indexOf(TEXT_ANY_CONTENT_TYPE) != -1 ;
    }

    public static boolean isXHTMLContentType(String contentType) {
        return contentType.indexOf(XHTML_CONTENT_TYPE) != -1 ||
            contentType.indexOf(APPLICATION_XML_CONTENT_TYPE) != -1 ||
            contentType.indexOf(TEXT_XML_CONTENT_TYPE) != -1;
    }

    private static List splitContentTypeListString(String contentTypeListString) {
        List contentTypeList = new ArrayList();

        StringTokenizer st = new StringTokenizer(contentTypeListString, ",");
        while (st.hasMoreTokens()) {
            String contentType = st.nextToken().trim();

            int semicolonIndex = contentType.indexOf(";");

            if (semicolonIndex != -1) {
                contentType = contentType.substring(0, semicolonIndex);
            }

            contentTypeList.add(contentType);
        }

        return contentTypeList;
    }

    public static String getJavascriptLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.JAVASCRIPT_LOCATION);
    }

    public static String getImageLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.IMAGE_LOCATION);
    }

    public static String getStyleLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.STYLE_LOCATION);
    }

    /**
     * The ScriptContext offers methods and fields
     * to help with rendering out a script and keeping a
     * proper formatting.
     */
    public static class ScriptContext
    {
        private long currentIndentationLevel;
        private StringBuffer buffer = new StringBuffer();
        private boolean prettyPrint = false;
        /**
         *  automatic formatting will render
         *  new-lines and indents if blocks are opened
         *  and closed - attention: you need to append
         *  opening and closing brackets of blocks separately in this case!
         */
        private boolean automaticFormatting = true;

        public ScriptContext()
        {

        }

        public ScriptContext(boolean prettyPrint)
        {
            this.prettyPrint = prettyPrint;
        }

        public ScriptContext(StringBuffer buf, boolean prettyPrint)
        {
            this.prettyPrint = prettyPrint;
            this.buffer = buf;
        }

        public void increaseIndent()
        {
            currentIndentationLevel++;
        }

        public void decreaseIndent()
        {
            currentIndentationLevel--;

            if(currentIndentationLevel<0)
                currentIndentationLevel=0;
        }

        public void prettyLine()
        {
            if(prettyPrint)
            {
                append(LINE_SEPARATOR);

                for(int i=0; i<getCurrentIndentationLevel(); i++)
                    append(TABULATOR);
            }
        }

        public void prettyLineIncreaseIndent()
        {
            increaseIndent();
            prettyLine();
        }

        public void prettyLineDecreaseIndent()
        {
            decreaseIndent();
            prettyLine();
        }

        public long getCurrentIndentationLevel()
        {
            return currentIndentationLevel;
        }

        public void setCurrentIndentationLevel(long currentIndentationLevel)
        {
            this.currentIndentationLevel = currentIndentationLevel;
        }

        public ScriptContext append(String str)
        {

            if(automaticFormatting && str.length()==1)
            {
                boolean openBlock = str.equals("{");
                boolean closeBlock = str.equals("}");

                if(openBlock)
                {
                    prettyLine();
                }
                else if(closeBlock)
                {
                    prettyLineDecreaseIndent();
                }

                buffer.append(str);

                if(openBlock)
                {
                    prettyLineIncreaseIndent();
                }
                else if(closeBlock)
                {
                    prettyLine();
                }
            }
            else
            {
                buffer.append(str);
            }
            return this;
        }

        public ScriptContext append(char c)
        {
            buffer.append(c);
            return this;
        }

        public ScriptContext append(int i) {
            buffer.append(i);
            return this;
        }

        public String toString()
        {
            return buffer.toString();
        }
    }
}
