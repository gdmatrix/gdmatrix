package org.santfeliu.faces.render.myfaces.calendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendar;
import org.apache.myfaces.custom.calendar.DateBusinessConverter;
import org.apache.myfaces.custom.calendar.DefaultDateBusinessConverter;
import org.apache.myfaces.custom.calendar.FunctionCallProvider;
import org.apache.myfaces.custom.calendar.HtmlInputCalendar;
import org.apache.myfaces.custom.inputTextHelp.HtmlInputTextHelp;
import org.apache.myfaces.custom.prototype.PrototypeResourceLoader;
import org.apache.myfaces.dateformat.SimpleDateFormatter;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared_tomahawk.util.MessageUtils;
import org.apache.myfaces.tomahawk.util.Constants;
import org.santfeliu.faces.component.HtmlAriaCommandLink;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlCalendarRenderer extends org.apache.myfaces.custom.calendar.HtmlCalendarRenderer
{
    private final Log log = LogFactory.getLog(HtmlCalendarRenderer.class);

    private static final String JAVASCRIPT_ENCODED = "org.apache.myfaces.calendar.JAVASCRIPT_ENCODED";

    // TODO: move this to HtmlRendererUtils in shared
    private static final String RESOURCE_NONE = "none";

    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, HtmlInputCalendar.class);

        HtmlInputCalendar inputCalendar = (HtmlInputCalendar) component;

        Locale currentLocale = facesContext.getViewRoot().getLocale();
        log.debug("current locale:" + currentLocale.toString());

        String textValue;
        
        Converter converter = inputCalendar.getConverter();
        Object submittedValue = inputCalendar.getSubmittedValue();
        
        Date value;

        if (submittedValue != null)
        {
            //Don't need to convert anything, the textValue is the same as the submittedValue
            textValue = (String) submittedValue;
            
            if(textValue ==null || textValue.trim().length()==0 || textValue.equals(getHelperString(inputCalendar)))
            {
                value = null;
            }
            else
            {
                try
                {
                    String formatStr = CalendarDateTimeConverter.createJSPopupFormat(facesContext, inputCalendar.getPopupDateFormat());
                    Calendar timeKeeper = Calendar.getInstance(currentLocale);
                    int firstDayOfWeek = timeKeeper.getFirstDayOfWeek() - 1;
                    org.apache.myfaces.dateformat.DateFormatSymbols symbols = new org.apache.myfaces.dateformat.DateFormatSymbols(currentLocale);
    
                    SimpleDateFormatter dateFormat = new SimpleDateFormatter(formatStr, symbols, firstDayOfWeek);
                    value = dateFormat.parse(textValue);
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    value = null;
                }
            }
        }
        else
        {
            if (converter == null)
            {
                CalendarDateTimeConverter defaultConverter = new CalendarDateTimeConverter();
                
                value = (Date) getDateBusinessConverter(inputCalendar).getDateValue(facesContext, component, inputCalendar.getValue());

                textValue = defaultConverter.getAsString(facesContext, inputCalendar, value);
            }
            else
            {
                //Use converter to retrieve the value.
                value = (Date) inputCalendar.getValue();
                textValue = converter.getAsString(facesContext, inputCalendar, value);
            }
        }

        Calendar timeKeeper = Calendar.getInstance(currentLocale);
        timeKeeper.setTime(value!=null?value:new Date());

        DateFormatSymbols symbols = new DateFormatSymbols(currentLocale);

        if(inputCalendar.isRenderAsPopup())
        {
            renderPopup(facesContext, inputCalendar, textValue, timeKeeper, symbols);
        }
        else
        {
            renderInline(facesContext, inputCalendar, timeKeeper, symbols);
        }

        component.getChildren().removeAll(component.getChildren());
    }

    private void renderPopup(
            FacesContext facesContext, 
            HtmlInputCalendar inputCalendar,
            String value,
            Calendar timeKeeper,
            DateFormatSymbols symbols) throws IOException
    {
        if(inputCalendar.isAddResources())
            addScriptAndCSSResources(facesContext, inputCalendar);

         // Check for an enclosed converter:
         UIInput uiInput = (UIInput) inputCalendar;
         Converter converter = uiInput.getConverter();
         String dateFormat = null;
         if (converter != null && converter instanceof DateTimeConverter) {
             dateFormat = ((DateTimeConverter) converter).getPattern();
         }
         if (dateFormat == null) {
             dateFormat = CalendarDateTimeConverter.createJSPopupFormat(facesContext,
                                                                        inputCalendar.getPopupDateFormat());
         }

        Application application = facesContext.getApplication();

        HtmlInputTextHelp inputText = getOrCreateInputTextChild(inputCalendar, application);

        RendererUtils.copyHtmlInputTextAttributes(inputCalendar, inputText);

        inputText.setConverter(null); // value for this transient component will already be converted
        inputText.setTransient(true);
        inputText.setHelpText(inputCalendar.getHelpText());
        inputText.setSelectText(true);

        inputText.setValue(value);
        /*
        if (value == null && inputCalendar.getSubmittedValue() != null)
        {
            inputText.setValue(inputCalendar.getSubmittedValue());
        }
        else
        {
            inputText.setValue(getConverter(inputCalendar).getAsString(
                    facesContext,inputCalendar,value));
        }*/
        inputText.setDisabled(inputCalendar.isDisabled());
        inputText.setReadonly(inputCalendar.isReadonly());
        inputText.setEnabledOnUserRole(inputCalendar.getEnabledOnUserRole());
        inputText.setVisibleOnUserRole(inputCalendar.getVisibleOnUserRole());

        //This is where two components with the same id are in the tree,
        //so make sure that during the rendering the id is unique.

        inputCalendar.setId(inputCalendar.getId()+"tempId");

        inputCalendar.getChildren().add(inputText);

        RendererUtils.renderChild(facesContext, inputText);

        inputCalendar.getChildren().remove(inputText);

        //Set back the correct id to the input calendar
        inputCalendar.setId(inputText.getId());

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SPAN_ELEM,inputCalendar);
        writer.writeAttribute(HTML.ID_ATTR,inputCalendar.getClientId(facesContext)+"Span",
                              JSFAttr.ID_ATTR);
        writer.endElement(HTML.SPAN_ELEM);

        if (!isDisabled(facesContext, inputCalendar) && !inputCalendar.isReadonly())
        {
            writer.startElement(HTML.SCRIPT_ELEM, inputCalendar);
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR,HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);

            String calendarVar = JavascriptUtils.getValidJavascriptName(
                    inputCalendar.getClientId(facesContext)+"CalendarVar",false);

            writer.writeText(calendarVar+"=new org_apache_myfaces_PopupCalendar();\n",null);
            writer.writeText(getLocalizedLanguageScript(facesContext,symbols,
                                                        timeKeeper.getFirstDayOfWeek(),inputCalendar,calendarVar)+"\n",null);
            // pass the selectMode attribute
            StringBuffer script = new StringBuffer();
            setStringVariable(script, calendarVar +".initData.selectMode",inputCalendar.getPopupSelectMode());
            writer.writeText(script.toString(), null);

            writer.writeText(calendarVar+".init(document.getElementById('"+
                             inputCalendar.getClientId(facesContext)+"Span"+"'));\n",null);
            writer.endElement(HTML.SCRIPT_ELEM);
            if(!inputCalendar.isDisplayValueOnly())
            {
                getScriptBtn(writer, facesContext, inputCalendar,
                                              dateFormat,inputCalendar.getPopupButtonString(), new FunctionCallProvider(){
                    public String getFunctionCall(FacesContext facesContext, UIComponent uiComponent, String dateFormat)
                    {
                        String clientId = uiComponent.getClientId(facesContext);

                        String clientVar = JavascriptUtils.getValidJavascriptName(clientId+"CalendarVar",true);

                        return clientVar+"._popUpCalendar(this,document.getElementById('"+clientId+"'),'"+dateFormat+"')";
                    }
                });
            }
        }
    }

    private void renderInline(
            FacesContext facesContext, 
            HtmlInputCalendar inputCalendar,
            Calendar timeKeeper,
            DateFormatSymbols symbols) throws IOException
    {
      
        String[] weekdays = mapShortWeekdays(symbols);
        String[] months = mapMonths(symbols, facesContext.getViewRoot().getLocale());

        int lastDayInMonth = timeKeeper.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDay = timeKeeper.get(Calendar.DAY_OF_MONTH);

        if (currentDay > lastDayInMonth)
            currentDay = lastDayInMonth;

        timeKeeper.set(Calendar.DAY_OF_MONTH, 1);

        int weekDayOfFirstDayOfMonth = mapCalendarDayToCommonDay(timeKeeper.get(Calendar.DAY_OF_WEEK));

        int weekStartsAtDayIndex = mapCalendarDayToCommonDay(timeKeeper.getFirstDayOfWeek());

        ResponseWriter writer = facesContext.getResponseWriter();

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        writer.startElement(HTML.TABLE_ELEM, inputCalendar);
        HtmlRendererUtils.renderHTMLAttributes(writer, inputCalendar, HTML.UNIVERSAL_ATTRIBUTES);
        HtmlRendererUtils.renderHTMLAttributes(writer, inputCalendar, HTML.EVENT_HANDLER_ATTRIBUTES);
        HtmlRendererUtils.renderHTMLAttributes(writer, inputCalendar, HTML.COMMON_FIELD_EVENT_ATTRIBUTES_WITHOUT_ONSELECT_AND_ONCHANGE);
        writer.flush();

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        writer.startElement(HTML.TR_ELEM, inputCalendar);

        if(inputCalendar.getMonthYearRowClass() != null)
            writer.writeAttribute(HTML.CLASS_ATTR, inputCalendar.getMonthYearRowClass(), null);

        writeMonthYearHeader(facesContext, writer, inputCalendar, timeKeeper,
                             currentDay, weekdays, months);

        writer.endElement(HTML.TR_ELEM);

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        writer.startElement(HTML.TR_ELEM, inputCalendar);

        if(inputCalendar.getWeekRowClass() != null)
            writer.writeAttribute(HTML.CLASS_ATTR, inputCalendar.getWeekRowClass(), null);

        writeWeekDayNameHeader(weekStartsAtDayIndex, weekdays,
                               facesContext, writer, inputCalendar);

        writer.endElement(HTML.TR_ELEM);

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        writeDays(facesContext, writer, inputCalendar, timeKeeper,
                  currentDay, weekStartsAtDayIndex, weekDayOfFirstDayOfMonth,
                  lastDayInMonth, weekdays);

        writer.endElement(HTML.TABLE_ELEM);
    }

    private HtmlInputTextHelp getOrCreateInputTextChild(HtmlInputCalendar inputCalendar, Application application)
    {
        HtmlInputTextHelp inputText = null;

        List li = inputCalendar.getChildren();

        for (int i = 0; i < li.size(); i++)
        {
            UIComponent uiComponent = (UIComponent) li.get(i);

            if(uiComponent instanceof HtmlInputTextHelp)
            {
                inputText = (HtmlInputTextHelp) uiComponent;
                break;
            }
        }

        if(inputText == null)
        {
            inputText = (HtmlInputTextHelp) application.createComponent(HtmlInputTextHelp.COMPONENT_TYPE);
        }
        return inputText;
    }

    /**
     * Used by the x:inputDate renderer : HTMLDateRenderer
     */
    static public void addScriptAndCSSResources(FacesContext facesContext, UIComponent component){
        // Check to see if javascript has already been written (which could happen if more than one calendar
        // on the same page). Note that this means that if two calendar controls in the same page have
        // different styleLocation or scriptLocation settings then all but the first one get ignored.
        // Having different settings for calendars on the same page would be unusual, so ignore this
        // for now..
        if (facesContext.getExternalContext().getRequestMap().containsKey(JAVASCRIPT_ENCODED))
        {
            return;
        }

        AddResource addresource = AddResourceFactory.getInstance(facesContext);
        // Add the javascript and CSS pages

        String styleLocation = HtmlRendererUtils.getStyleLocation(component);

        if(styleLocation==null)
        {
            addresource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, HtmlCalendarRenderer.class, "WH/theme.css");
            addresource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, HtmlCalendarRenderer.class, "DB/theme.css");
        }
        else if (!RESOURCE_NONE.equals(styleLocation))
        {
            addresource.addStyleSheet(facesContext, AddResource.HEADER_BEGIN, styleLocation+"/theme.css");
        }
        else
        {
            // output nothing; presumably the page directly references the necessary stylesheet
        }

        String javascriptLocation = HtmlRendererUtils.getJavascriptLocation(component);

        if(javascriptLocation==null)
        {
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, PrototypeResourceLoader.class, "prototype.js");
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, HtmlCalendarRenderer.class, "date.js");
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, HtmlCalendarRenderer.class, "popcalendar.js");
        }
        else if (!RESOURCE_NONE.equals(javascriptLocation))
        {
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, javascriptLocation+ "/prototype.js");
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, javascriptLocation+ "/date.js");
            addresource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN, javascriptLocation+ "/popcalendar.js");
        }
        else
        {
            // output nothing; presumably the page directly references the necessary javascript
        }

        facesContext.getExternalContext().getRequestMap().put(JAVASCRIPT_ENCODED, Boolean.TRUE);
    }
    
    
    /**
     * Creates and returns a String which contains the initialisation data for
     * the popup calendar control as a sequence of javascript commands that
     * assign values to properties of a javascript object whose name is in
     * parameter popupCalendarVariable.
     * <p>
     * 
     * @param firstDayOfWeek
     *            is in java.util.Calendar form, ie Sun=1, Mon=2, Sat=7
     */
    public static String getLocalizedLanguageScript(FacesContext facesContext,
            DateFormatSymbols symbols, int firstDayOfWeek, UIComponent uiComponent,
            String popupCalendarVariable)
    {

        // Convert day value to java.util.Date convention (Sun=0, Mon=1, Sat=6).
        // This is the convention that javascript Date objects use.
        int realFirstDayOfWeek = firstDayOfWeek - 1;

        String[] weekDays;

        if (realFirstDayOfWeek == 0)
        {
            // Sunday
            weekDays = mapShortWeekdaysStartingWithSunday(symbols);
        }
        else if (realFirstDayOfWeek == 1)
        {
            // Monday
            weekDays = mapShortWeekdays(symbols);
        }
        else if (realFirstDayOfWeek == 6)
        {
            // Saturday. Often used in Arabic countries
            weekDays = mapShortWeekdaysStartingWithSaturday(symbols);
        }
        else
        {
            throw new IllegalStateException("Week may only start with saturday, sunday or monday.");
        }

        StringBuffer script = new StringBuffer();
        AddResource ar = AddResourceFactory.getInstance(facesContext);

        if (uiComponent instanceof HtmlInputCalendar)
        {
            HtmlInputCalendar calendar = (HtmlInputCalendar) uiComponent;
            // Set the themePrefix variable
            String popupTheme = calendar.getPopupTheme();
            if (popupTheme == null)
            {
                popupTheme = "DB";
            }
            setStringVariable(script, popupCalendarVariable + ".initData.themePrefix",
                    "jscalendar-" + popupTheme);

            // specify the URL for the directory in which all the .gif images
            // can be found
            String imageLocation = HtmlRendererUtils.getImageLocation(uiComponent);
            if (imageLocation == null)
            {
                String uri = ar.getResourceUri(facesContext, HtmlCalendarRenderer.class, popupTheme
                        + "/");
                setStringVariable(script, popupCalendarVariable + ".initData.imgDir",
                        JavascriptUtils.encodeString(uri));
            }
            else
            {
                setStringVariable(script, popupCalendarVariable + ".initData.imgDir",
                        (JavascriptUtils.encodeString(AddResourceFactory.getInstance(facesContext)
                                .getResourceUri(facesContext, imageLocation + "/"))));
            }
        }
        else
        {
            String imageLocation = HtmlRendererUtils.getImageLocation(uiComponent);
            if (imageLocation == null)
            {
                String uri = ar.getResourceUri(facesContext, HtmlCalendarRenderer.class, "images/");
                setStringVariable(script, popupCalendarVariable + ".initData.imgDir",
                        JavascriptUtils.encodeString(uri));
            }
            else
            {
                setStringVariable(script, popupCalendarVariable + ".initData.imgDir",
                        (JavascriptUtils.encodeString(AddResourceFactory.getInstance(facesContext)
                                .getResourceUri(facesContext, imageLocation + "/"))));
            }
        }
        defineStringArray(script, popupCalendarVariable + ".initData.monthName", mapMonths(symbols));
        defineStringArray(script, popupCalendarVariable + ".initData.dayName", weekDays);
        setIntegerVariable(script, popupCalendarVariable + ".initData.startAt", realFirstDayOfWeek);

        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.weekdays",
                mapWeekdaysStartingWithSunday(symbols));
        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.shortWeekdays",
                mapShortWeekdaysStartingWithSunday(symbols));
        
        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.shortMonths",
                mapShortMonths(symbols, facesContext.getViewRoot().getLocale()));
        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.months",
                mapMonths(symbols));
        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.eras", symbols
                .getEras());
        defineStringArray(script, popupCalendarVariable + ".dateFormatSymbols.ampms", symbols
                .getAmPmStrings());

        if (uiComponent instanceof HtmlInputCalendar)
        {

            HtmlInputCalendar inputCalendar = (HtmlInputCalendar) uiComponent;

            if (inputCalendar.getPopupGotoString() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.gotoString",
                        inputCalendar.getPopupGotoString());
            if (inputCalendar.getPopupTodayString() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.todayString",
                        inputCalendar.getPopupTodayString());
            if (inputCalendar.getPopupTodayDateFormat() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.todayDateFormat",
                        inputCalendar.getPopupTodayDateFormat());
            else if (inputCalendar.getPopupDateFormat() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.todayDateFormat",
                        inputCalendar.getPopupDateFormat());
            if (inputCalendar.getPopupWeekString() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.weekString",
                        inputCalendar.getPopupWeekString());
            if (inputCalendar.getPopupScrollLeftMessage() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.scrollLeftMessage",
                        inputCalendar.getPopupScrollLeftMessage());
            if (inputCalendar.getPopupScrollRightMessage() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.scrollRightMessage",
                        inputCalendar.getPopupScrollRightMessage());
            if (inputCalendar.getPopupSelectMonthMessage() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.selectMonthMessage",
                        inputCalendar.getPopupSelectMonthMessage());
            if (inputCalendar.getPopupSelectYearMessage() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.selectYearMessage",
                        inputCalendar.getPopupSelectYearMessage());
            if (inputCalendar.getPopupSelectDateMessage() != null)
                setStringVariable(script, popupCalendarVariable + ".initData.selectDateMessage",
                        inputCalendar.getPopupSelectDateMessage());
            setBooleanVariable(script, popupCalendarVariable + ".initData.popupLeft", inputCalendar
                    .isPopupLeft());

        }

        return script.toString();
    }

    private static void setBooleanVariable(StringBuffer script, String name, boolean value)
    {
        script.append(name);
        script.append(" = ");
        script.append(value);
        script.append(";\n");
    }

    private static void setIntegerVariable(StringBuffer script, String name, int value)
    {
        script.append(name);
        script.append(" = ");
        script.append(value);
        script.append(";\n");
    }

    private static void setStringVariable(StringBuffer script, String name, String value)
    {
        script.append(name);
        script.append(" = \"");
        script.append(StringEscapeUtils.escapeJavaScript(value));
        script.append("\";\n");
    }

    private static void defineStringArray(StringBuffer script, String arrayName, String[] array)
    {
        script.append(arrayName);
        script.append(" = new Array(");

        for(int i=0;i<array.length;i++)
        {
            if(i!=0)
                script.append(",");

            script.append("\"");
            script.append(StringEscapeUtils.escapeJavaScript(array[i]));
            script.append("\"");
        }

        script.append(");\n");
    }

    public static void getScriptBtn(ResponseWriter writer, FacesContext facesContext, UIComponent uiComponent,
                                      String dateFormat, String popupButtonString, FunctionCallProvider prov)
        throws IOException
    {
        boolean renderButtonAsImage = false;
        String popupButtonStyle = null;
        String popupButtonStyleClass = null;

        if(uiComponent instanceof HtmlInputCalendar)
        {
            HtmlInputCalendar calendar = (HtmlInputCalendar)uiComponent;
            renderButtonAsImage = calendar.isRenderPopupButtonAsImage();
            popupButtonStyle = calendar.getPopupButtonStyle();
            popupButtonStyleClass = calendar.getPopupButtonStyleClass();
        }

        if (!renderButtonAsImage) {
            // render the button
            writer.startElement(HTML.INPUT_ELEM, uiComponent);
            writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_BUTTON, null);

            writer.writeAttribute(HTML.ONCLICK_ATTR,
                                  prov.getFunctionCall(facesContext,uiComponent,dateFormat),
                                  null);

            if(popupButtonString==null)
                popupButtonString="...";
            writer.writeAttribute(HTML.VALUE_ATTR, StringEscapeUtils.escapeJavaScript(popupButtonString), null);

            if(popupButtonStyle != null)
            {
                writer.writeAttribute(HTML.STYLE_ATTR, popupButtonStyle, null);
            }

            if(popupButtonStyleClass != null)
            {
                writer.writeAttribute(HTML.CLASS_ATTR, popupButtonStyleClass, null);
            }
            
            writer.endElement(HTML.INPUT_ELEM);
        } else {
            // render the image
            writer.startElement(HTML.IMG_ELEM, uiComponent);
            AddResource addResource = AddResourceFactory.getInstance(facesContext);

            String imgUrl = (String) uiComponent.getAttributes().get("popupButtonImageUrl");

            if(imgUrl!=null)
            {
                writer.writeAttribute(HTML.SRC_ATTR, addResource.getResourceUri(facesContext, imgUrl), null);
            }
            else
            {
                writer.writeAttribute(HTML.SRC_ATTR, addResource.getResourceUri(facesContext, HtmlCalendarRenderer.class, "images/calendar.gif"), null);
            }

            if(popupButtonStyle != null)
            {
                writer.writeAttribute(HTML.STYLE_ATTR, popupButtonStyle, null);
            }
            else
            {
                writer.writeAttribute(HTML.STYLE_ATTR, "vertical-align:bottom;", null);
            }

            if(popupButtonStyleClass != null)
            {
                writer.writeAttribute(HTML.CLASS_ATTR, popupButtonStyleClass, null);
            }

            writer.writeAttribute(HTML.ONCLICK_ATTR, prov.getFunctionCall(facesContext, uiComponent, dateFormat),
                                  null);

            writer.endElement(HTML.IMG_ELEM);
        }
    }


    private void writeMonthYearHeader(FacesContext facesContext, ResponseWriter writer, UIInput inputComponent, Calendar timeKeeper,
                                      int currentDay, String[] weekdays,
                                      String[] months)
            throws IOException
    {
        Calendar cal = shiftMonth(facesContext, timeKeeper, currentDay, -1);

        writeCell(facesContext, writer, inputComponent, "<", "backwardMonth", cal.getTime(), null);

        writer.startElement(HTML.TD_ELEM, inputComponent);
        writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(weekdays.length - 2), null);
        writer.writeText(months[timeKeeper.get(Calendar.MONTH)] + " " + timeKeeper.get(Calendar.YEAR), null);
        writer.endElement(HTML.TD_ELEM);

        cal = shiftMonth(facesContext, timeKeeper, currentDay, 1);

        writeCell(facesContext, writer, inputComponent, ">", "forwardMonth", cal.getTime(), null);
    }

    private Calendar shiftMonth(FacesContext facesContext,
                                Calendar timeKeeper, int currentDay, int shift)
    {
        Calendar cal = copyCalendar(facesContext, timeKeeper);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + shift);

        if(currentDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            currentDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, currentDay);
        return cal;
    }

    private Calendar copyCalendar(FacesContext facesContext, Calendar timeKeeper)
    {
        Calendar cal = Calendar.getInstance(facesContext.getViewRoot().getLocale());
        cal.setTime(timeKeeper.getTime());
        return cal;
    }

    private void writeWeekDayNameHeader(int weekStartsAtDayIndex, String[] weekdays, FacesContext facesContext, ResponseWriter writer, UIInput inputComponent)
            throws IOException
    {
        for (int i = weekStartsAtDayIndex; i < weekdays.length; i++)
            writeTableHeader(facesContext,
                      writer, inputComponent, weekdays[i], null, null, null);
    }

    private void writeDays(FacesContext facesContext, ResponseWriter writer,
                           HtmlInputCalendar inputComponent, Calendar timeKeeper, int currentDay, int weekStartsAtDayIndex,
                           int weekDayOfFirstDayOfMonth, int lastDayInMonth, String[] weekdays)
            throws IOException
    {
        Calendar cal;

        int space = (weekStartsAtDayIndex < weekDayOfFirstDayOfMonth) ? (weekDayOfFirstDayOfMonth - weekStartsAtDayIndex)
                    : (weekdays.length - weekStartsAtDayIndex + weekDayOfFirstDayOfMonth);

        if (space == weekdays.length)
            space = 0;

        int columnIndexCounter = 0;

        for (int i = 0; i < space; i++)
        {
            if (columnIndexCounter == 0)
            {
                writer.startElement(HTML.TR_ELEM, inputComponent);
            }

            writeCell(facesContext, writer, inputComponent, "", null,
                      null, inputComponent.getDayCellClass());
            columnIndexCounter++;
        }

        for (int i = 0; i < lastDayInMonth; i++)
        {
            if (columnIndexCounter == 0)
            {
                writer.startElement(HTML.TR_ELEM, inputComponent);
            }

            cal = copyCalendar(facesContext, timeKeeper);
            cal.set(Calendar.DAY_OF_MONTH, i + 1);

            String cellStyle = inputComponent.getDayCellClass();
            String linkTitle = null;
            if((currentDay - 1) == i)
            {
                cellStyle = inputComponent.getCurrentDayCellClass();
                linkTitle = "selectedDay";
            }       
         

            writeCell(facesContext, writer,
                      inputComponent, String.valueOf(i + 1), linkTitle, 
                      cal.getTime(), cellStyle);

            columnIndexCounter++;

            if (columnIndexCounter == weekdays.length)
            {
                writer.endElement(HTML.TR_ELEM);
                HtmlRendererUtils.writePrettyLineSeparator(facesContext);
                columnIndexCounter = 0;
            }
        }

        if (columnIndexCounter != 0)
        {
            for (int i = columnIndexCounter; i < weekdays.length; i++)
            {
                writeCell(facesContext, writer,
                          inputComponent, "", null, null, inputComponent.getDayCellClass());
            }

            writer.endElement(HTML.TR_ELEM);
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        }
    }

    /**
     * Generate components and output for a single "day" cell within the calendar display.
     */
    private void writeCell(FacesContext facesContext,
                           ResponseWriter writer, UIInput component, String content, String linkTitle,
                           Date valueForLink, String styleClass)
            throws IOException
    {
        writer.startElement(HTML.TD_ELEM, component);

        if (styleClass != null)
            writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);

        if (valueForLink == null)
            writer.writeText(content, JSFAttr.VALUE_ATTR);
        else
        {
            writeLink(content, component, facesContext, valueForLink, linkTitle);
        }

        writer.endElement(HTML.TD_ELEM);
    }
    
    /**
     * Generate components and output for a single "day" cell within the calendar display.
     */
    private void writeTableHeader(FacesContext facesContext,
                           ResponseWriter writer, UIInput component, String content, String linkTitle,
                           Date valueForLink, String styleClass)
            throws IOException
    {
        writer.startElement(HTML.TH_ELEM, component);

        if (styleClass != null)
            writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);

        if (valueForLink == null)
            writer.writeText(content, JSFAttr.VALUE_ATTR);
        else
        {
            writeLink(content, component, facesContext, valueForLink, linkTitle);
        }

        writer.endElement(HTML.TH_ELEM);
    }    

    /**
     * Create child components to represent a link to a specific date value, and render them.
     * <p>
     * For a disabled calendar, this just creates a Text component, attaches it as a child
     * of the calendar and renders it. The value of the component is the string returned by
     * valueForLink.getTime().
     * <p>
     * For a non-disabled calendar, create an HtmlCommandLink child that wraps the text
     * returned by valueForLink.getTime(), and add it to the component.
     */
    private void writeLink(String content,
                           UIInput component,
                           FacesContext facesContext,
                           Date valueForLink,
                           String linkTitle)
            throws IOException
    {
        Converter converter = getConverter(component);
        Application application = facesContext.getApplication();

        HtmlOutputText text
                = (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        text.setValue(content);
        text.setId(component.getId() + "_" + valueForLink.getTime() + "_text");
        text.setTransient(true);

        HtmlInputCalendar calendar = (HtmlInputCalendar)component;
        if (isDisabled(facesContext, component) || calendar.isReadonly())
        {
            component.getChildren().add(text);

            RendererUtils.renderChild(facesContext, text);
            return;
        }

        HtmlAriaCommandLink link
                = (HtmlAriaCommandLink)application.createComponent("AriaCommandLink");
        link.setId(component.getId() + "_" + valueForLink.getTime() + "_link");
        link.setTransient(true);
        link.setImmediate(component.isImmediate());
        if (linkTitle != null)
        {
          Locale locale = facesContext.getViewRoot().getLocale();
          ResourceBundle bundle = ResourceBundle.getBundle(
            "org.santfeliu.faces.render.myfaces.calendar.resources.CalendarBundle", locale); 
          link.setTitle(bundle.getString(linkTitle));
          link.setAriaLabel(bundle.getString(linkTitle));
        }
          

        UIParameter parameter
                = (UIParameter)application.createComponent(UIParameter.COMPONENT_TYPE);
        parameter.setId(component.getId() + "_" + valueForLink.getTime() + "_param");
        parameter.setTransient(true);
        parameter.setName(component.getClientId(facesContext));
        parameter.setValue(converter.getAsString(facesContext, component, valueForLink));

        RendererUtils.addOrReplaceChild(component,link);
        link.getChildren().add(parameter);
        link.getChildren().add(text);

        RendererUtils.renderChild(facesContext, link);
    }

    private Converter getConverter(UIInput component)
    {
        Converter converter = component.getConverter();

        if (converter == null)
        {
            converter = new CalendarDateTimeConverter();
        }
        return converter;
    }
    
    private DateBusinessConverter getDateBusinessConverter(AbstractHtmlInputCalendar component)
    {
        DateBusinessConverter dateBusinessConverter = component.getDateBusinessConverter(); 
        if (dateBusinessConverter == null)
        {
            dateBusinessConverter = new DefaultDateBusinessConverter();
        }
        return dateBusinessConverter;
    }

    private int mapCalendarDayToCommonDay(int day)
    {
        switch (day)
        {
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return 0;
        }
    }

    private static String[] mapShortWeekdays(DateFormatSymbols symbols)
    {
        String[] weekdays = new String[7];

        String[] localeWeekdays = symbols.getShortWeekdays();

        weekdays[0] = localeWeekdays[Calendar.MONDAY];
        weekdays[1] = localeWeekdays[Calendar.TUESDAY];
        weekdays[2] = localeWeekdays[Calendar.WEDNESDAY];
        weekdays[3] = localeWeekdays[Calendar.THURSDAY];
        weekdays[4] = localeWeekdays[Calendar.FRIDAY];
        weekdays[5] = localeWeekdays[Calendar.SATURDAY];
        weekdays[6] = localeWeekdays[Calendar.SUNDAY];

        return weekdays;
    }

    private static String[] mapShortWeekdaysStartingWithSunday(DateFormatSymbols symbols)
    {
        String[] weekdays = new String[7];

        String[] localeWeekdays = symbols.getShortWeekdays();

        weekdays[0] = localeWeekdays[Calendar.SUNDAY];
        weekdays[1] = localeWeekdays[Calendar.MONDAY];
        weekdays[2] = localeWeekdays[Calendar.TUESDAY];
        weekdays[3] = localeWeekdays[Calendar.WEDNESDAY];
        weekdays[4] = localeWeekdays[Calendar.THURSDAY];
        weekdays[5] = localeWeekdays[Calendar.FRIDAY];
        weekdays[6] = localeWeekdays[Calendar.SATURDAY];

        return weekdays;
    }

    
    private static String[] mapShortWeekdaysStartingWithSaturday(DateFormatSymbols symbols) 
    {
        String[] weekdays = new String[7];

        String[] localeWeekdays = symbols.getShortWeekdays();

        weekdays[0] = localeWeekdays[Calendar.SATURDAY];
        weekdays[1] = localeWeekdays[Calendar.SUNDAY];
        weekdays[2] = localeWeekdays[Calendar.MONDAY];
        weekdays[3] = localeWeekdays[Calendar.TUESDAY];
        weekdays[4] = localeWeekdays[Calendar.WEDNESDAY];
        weekdays[5] = localeWeekdays[Calendar.THURSDAY];
        weekdays[6] = localeWeekdays[Calendar.FRIDAY];

        return weekdays;
    }    
    
    private static String[] mapWeekdaysStartingWithSunday(DateFormatSymbols symbols)
    {
        String[] weekdays = new String[7];

        String[] localeWeekdays = symbols.getWeekdays();

        weekdays[0] = localeWeekdays[Calendar.SUNDAY];
        weekdays[1] = localeWeekdays[Calendar.MONDAY];
        weekdays[2] = localeWeekdays[Calendar.TUESDAY];
        weekdays[3] = localeWeekdays[Calendar.WEDNESDAY];
        weekdays[4] = localeWeekdays[Calendar.THURSDAY];
        weekdays[5] = localeWeekdays[Calendar.FRIDAY];
        weekdays[6] = localeWeekdays[Calendar.SATURDAY];

        return weekdays;
    }

    public static String[] mapMonths(DateFormatSymbols symbols, Locale locale)
    {
        String[] months = new String[12];
 
/*      String[] localeMonths = symbols.getMonths();

        months[0] = localeMonths[Calendar.JANUARY];
        months[1] = localeMonths[Calendar.FEBRUARY];
        months[2] = localeMonths[Calendar.MARCH];
        months[3] = localeMonths[Calendar.APRIL];
        months[4] = localeMonths[Calendar.MAY];
        months[5] = localeMonths[Calendar.JUNE];
        months[6] = localeMonths[Calendar.JULY];
        months[7] = localeMonths[Calendar.AUGUST];
        months[8] = localeMonths[Calendar.SEPTEMBER];
        months[9] = localeMonths[Calendar.OCTOBER];
        months[10] = localeMonths[Calendar.NOVEMBER];
        months[11] = localeMonths[Calendar.DECEMBER]; */ 
       
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) 
        {
          cal.set(Calendar.MONTH, i);
          months[i] = TextUtils.formatDate(cal.getTime(), "LLLL", locale);
        }        

        return months;
    }

    public static String[] mapShortMonths(DateFormatSymbols symbols, Locale locale)
    {
        String[] months = new String[12];
/*
        String[] localeMonths = symbols.getShortMonths();

        months[0] = localeMonths[Calendar.JANUARY];
        months[1] = localeMonths[Calendar.FEBRUARY];
        months[2] = localeMonths[Calendar.MARCH];
        months[3] = localeMonths[Calendar.APRIL];
        months[4] = localeMonths[Calendar.MAY];
        months[5] = localeMonths[Calendar.JUNE];
        months[6] = localeMonths[Calendar.JULY];
        months[7] = localeMonths[Calendar.AUGUST];
        months[8] = localeMonths[Calendar.SEPTEMBER];
        months[9] = localeMonths[Calendar.OCTOBER];
        months[10] = localeMonths[Calendar.NOVEMBER];
        months[11] = localeMonths[Calendar.DECEMBER];
*/

        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) 
        {
          cal.set(Calendar.MONTH, i);
          months[i] = TextUtils.formatDate(cal.getTime(), "LLL", locale);
        }  
        
        return months;
    }
    
    public void decode(FacesContext facesContext, UIComponent component)
    {
        if(HtmlRendererUtils.isDisabledOrReadOnly(component))
        {
            // nothing to do here
            return;
        }

        RendererUtils.checkParamValidity(facesContext, component, HtmlInputCalendar.class);

        //String helperString = getHelperString(component);

        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                                               + component.getClientId(facesContext)
                                               + " is not an EditableValueHolder");
        }
        Map paramMap = facesContext.getExternalContext()
                .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);

        if(paramMap.containsKey(clientId))
        {
            String value = (String) paramMap.get(clientId);

            //if(!value.equalsIgnoreCase(helperString))
            //{
                ((EditableValueHolder) component).setSubmittedValue(value);
            //}
            //else
            //{
                // The field was initially filled with the "helper string", and has
                // not been altered by the user so treat this as if null had been
                // passed by the user.
                //
                // TODO: does this mean the target date is set to todays date?
                // And how does this affect the "required" property?
                //((EditableValueHolder) component).setSubmittedValue("");
            //}
        }
        else
        {
            log.warn(HtmlRendererUtils.NON_SUBMITTED_VALUE_WARNING +
                " Component : "+
                RendererUtils.getPathToComponent(component));
        }

    }
    
    protected static boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (!UserRoleUtils.isEnabledOnUserRole(uiComponent))
        {
            return true;
        }
        else
        {
            if (uiComponent instanceof HtmlInputCalendar)
            {
                return ((HtmlInputCalendar)uiComponent).isDisabled();
            }
            else
            {
                return org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils.getBooleanAttribute(uiComponent, HTML.DISABLED_ATTR, false);
            }
        }
    }

    public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent, Object submittedValue) throws ConverterException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlInputCalendar.class);

        AbstractHtmlInputCalendar uiInput = (AbstractHtmlInputCalendar) uiComponent;

        Converter converter = uiInput.getConverter();

        if (submittedValue != null && !(submittedValue instanceof String))
        {
            throw new IllegalArgumentException("Submitted value of type String expected");
        }
        
        //Do not convert if submittedValue is helper string  
        if(submittedValue != null && submittedValue.equals(getHelperString(uiComponent)))
            return null;
        
        if(converter==null)
        {
            converter = new CalendarDateTimeConverter();
            
            Date date = (Date) converter.getAsObject(facesContext, uiComponent, (String) submittedValue);
            
            return getDateBusinessConverter(uiInput).getBusinessValue(facesContext, uiComponent, date);
        }
        else
        {
            return converter.getAsObject(facesContext, uiComponent, (String) submittedValue);
        }
    }

    public interface DateConverter extends Converter
    {
        public Date getAsDate(FacesContext facesContext, UIComponent uiComponent);
    }

    private static String getHelperString(UIComponent uiComponent)
    {
        return uiComponent instanceof HtmlInputCalendar?((HtmlInputCalendar) uiComponent).getHelpText():null;
    }

    public static class CalendarDateTimeConverter implements DateConverter
    {
        private static final String CONVERSION_MESSAGE_ID = "org.apache.myfaces.calendar.CONVERSION";

        public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s)
        {
            if(s==null || s.trim().length()==0 || s.equals(getHelperString(uiComponent)))
                return null;

            if(uiComponent instanceof HtmlInputCalendar && ((HtmlInputCalendar) uiComponent).isRenderAsPopup())
            {
                HtmlInputCalendar calendar = (HtmlInputCalendar) uiComponent;
                String popupDateFormat = calendar.getPopupDateFormat();
                String formatStr = createJSPopupFormat(facesContext, popupDateFormat);
                Locale locale = facesContext.getViewRoot().getLocale();
                Calendar timeKeeper = Calendar.getInstance(locale);
                int firstDayOfWeek = timeKeeper.getFirstDayOfWeek() - 1;
                org.apache.myfaces.dateformat.DateFormatSymbols symbols = new org.apache.myfaces.dateformat.DateFormatSymbols(locale);
                SimpleDateFormatter dateFormat = new SimpleDateFormatter(formatStr, symbols, firstDayOfWeek);
                
                Date date = dateFormat.parse(s); 
                if (date != null) {
                    return date;
                }
                FacesMessage msg = MessageUtils.getMessage(Constants.TOMAHAWK_DEFAULT_BUNDLE,FacesMessage.SEVERITY_ERROR,CONVERSION_MESSAGE_ID,new Object[]{
                        uiComponent.getId(),s},facesContext);
                throw new ConverterException(msg);
            }
            else
            {
                DateFormat dateFormat = createStandardDateFormat(facesContext);
                dateFormat.setLenient(false);
                try
                {
                    Date date = dateFormat.parse(s); 
                    return date;
                }
                catch (ParseException e)
                {
                    FacesMessage msg = MessageUtils.getMessage(Constants.TOMAHAWK_DEFAULT_BUNDLE,FacesMessage.SEVERITY_ERROR,CONVERSION_MESSAGE_ID,new Object[]{
                            uiComponent.getId(),s},facesContext);
                    throw new ConverterException(msg,e);
                }
            }
        }

        public Date getAsDate(FacesContext facesContext, UIComponent uiComponent)
        {
            return RendererUtils.getDateValue(uiComponent);
        }

        public static String createJSPopupFormat(FacesContext facesContext, String popupDateFormat)
        {

            if(popupDateFormat == null)
            {
                SimpleDateFormat defaultDateFormat = createStandardDateFormat(facesContext);
                popupDateFormat = defaultDateFormat.toPattern();
            }

            return popupDateFormat;
        }

        public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o)
        {
            Date date = (Date) o;

            if(date==null)
                return getHelperString(uiComponent);

            if(uiComponent instanceof HtmlInputCalendar && ((HtmlInputCalendar) uiComponent).isRenderAsPopup())
            {
                HtmlInputCalendar calendar = (HtmlInputCalendar) uiComponent;
                String popupDateFormat = calendar.getPopupDateFormat();
                String formatStr = createJSPopupFormat(facesContext, popupDateFormat);
                Locale locale = facesContext.getViewRoot().getLocale();
                Calendar timeKeeper = Calendar.getInstance(locale);
                int firstDayOfWeek = timeKeeper.getFirstDayOfWeek() - 1;
                org.apache.myfaces.dateformat.DateFormatSymbols symbols = new org.apache.myfaces.dateformat.DateFormatSymbols(locale);

                SimpleDateFormatter dateFormat = new SimpleDateFormatter(formatStr, symbols, firstDayOfWeek);
                return dateFormat.format(date);
            }
            else
            {
                DateFormat dateFormat = createStandardDateFormat(facesContext);
                dateFormat.setLenient(false);
                return dateFormat.format(date);
            }
        }

        private static SimpleDateFormat createStandardDateFormat(FacesContext facesContext)
        {
            DateFormat dateFormat;
            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT,
                                                    facesContext.getViewRoot().getLocale());

            if(dateFormat instanceof SimpleDateFormat)
                return (SimpleDateFormat) dateFormat;
            else
                return new SimpleDateFormat("dd.MM.yyyy", facesContext.getViewRoot().getLocale());
        }
    }
}
