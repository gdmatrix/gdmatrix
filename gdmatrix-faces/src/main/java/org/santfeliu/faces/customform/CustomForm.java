package org.santfeliu.faces.customform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.matrix.sql.QueryParameter;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.matrix.sql.SQLManagerPort;
import org.matrix.sql.SQLManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.template.WebTemplate;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;


public class CustomForm extends UIComponentBase
{
  public static final String CHARSET = "utf-8";
  public static final String REQUIRED_VALUE = "dform.REQUIRED_VALUE";
  public static final String INVALID_VALUE = "dform.INVALID_VALUE";

  public static final String VAR_SEPARATOR = ".";
  public static final String FORMAT_SEPARATOR = ":";
  public static final String REQUIRED_TAG = "$";

  // format String: {boolean|text|number|date}[:<subformat>]
  public static final String BOOLEAN = "boolean";
  public static final String TEXT = "text";
  public static final String NUMBER = "number";
  public static final String DATE = "date";
  
  public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";

  private String _url;
  private Map _values;
  private Map _newValues;
  private boolean _valid; 
  private Map _submittedValues = new HashMap(); // contains submitted fields
  private Map _convertedValues = new HashMap(); // contains converted fields
  private Map _fieldFormats = new HashMap(); // contains all fields in form
  private HashSet _requiredFields = new HashSet();
  private HashSet _multivaluedFields = new HashSet();
  private Translator _translator;
  private String _translationGroup;

  public CustomForm()
  {
  }
  
  public String getFamily()
  {
    return "CustomForm";
  }
  
  public void setUrl(String url)
  {
    this._url = url;
  }

  public String getUrl()
  {
    if (_url != null) return _url;
    ValueBinding vb = getValueBinding("url");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setValues(Map values)
  {
    this._values = values;
  }

  public Map getValues()
  {
    if (_values != null) return _values;
    ValueBinding vb = getValueBinding("values");
    return vb != null ? (Map)vb.getValue(getFacesContext()) : null;
  }

  public void setNewValues(Map newValues)
  {
    this._newValues = newValues;
  }

  public Map getNewValues()
  {
    if (_newValues != null) return _newValues;
    ValueBinding vb = getValueBinding("newValues");
    return vb != null ? (Map)vb.getValue(getFacesContext()) : null;
  }

  public void setTranslator(Translator translator)
  {
    this._translator = translator;
  }
  
  public Translator getTranslator()
  {
    if (_translator != null) return _translator;
    ValueBinding vb = getValueBinding("translator");
    return vb != null ? (Translator)vb.getValue(getFacesContext()) : null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }
  
  public String getTranslationGroup()
  {
    if (_translationGroup != null) return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public boolean isValid()
  {
    return _valid;
  }
  
  @Override
  public void decode(FacesContext context)
  {
    _valid = true;
    _submittedValues.clear();
    _convertedValues.clear();
    
    String clientId = getClientId(context);
    String prefix = clientId + VAR_SEPARATOR;
    Map parameterMap = context.getExternalContext().getRequestParameterMap();
    Iterator iter = _fieldFormats.keySet().iterator();
    while (iter.hasNext())
    {
      String fieldName = (String)iter.next();
      String parameterName = prefix + fieldName;
      if (parameterMap.containsKey(parameterName))
      {
        String value = (String)parameterMap.get(parameterName);
        _submittedValues.put(fieldName, value); // store value
      }
      else if (_fieldFormats.get(fieldName).equals(BOOLEAN))
      {
        _submittedValues.put(fieldName, "false"); // for checkboxes
      }
    }
  }

  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
    try
    {
      validate(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
    if (!isValid())
    {
      context.renderResponse();
    }
  }
  
  public void validate(FacesContext context)
  {
    Iterator iter = _submittedValues.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String name = (String)entry.getKey();
      String value = (String)entry.getValue();      
      validateField(name, value);
    }
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    Map newValues = getNewValues();
    if (newValues != null)
    {
      newValues.putAll(_convertedValues);
      System.out.println("NEWVALUES:" + newValues);
      System.out.println();
    }
    _submittedValues.clear();
    _convertedValues.clear();
  }

  // encoding methods
  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      _fieldFormats.clear();
      _requiredFields.clear();
      _multivaluedFields.clear();

      ResponseWriter writer = context.getResponseWriter();
      HttpClient httpClient = new HttpClient();
      httpClient.setURL(getUrl());
      httpClient.setForceHttp(true);
      httpClient.setDownloadContentType("text/");
      httpClient.setMaxContentLength(524288);
      httpClient.setRequestProperty("User-Agent", HttpClient.USER_AGENT_IE6);
      httpClient.setRequestProperty("Accept-Charset", "utf-8");
      httpClient.setRequestProperty("Accept-Language", getLanguage());
      httpClient.connect();
      
      String content = httpClient.getContentAsString();
      if (content == null) return;

      // copy variables to new Map to avoid lateral effects
      Map values = new HashMap();
      values.putAll(getValues());
      content = WebTemplate.create(content).merge(values);

      // translate document when necessary
      InputStream input = null;
      String contentLanguage = httpClient.getContentLanguage();
      Translator translator = (contentLanguage == null) ? getTranslator(): null;
      if (translator != null)
      {
        Reader in = new StringReader(content);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(bos, CHARSET);
        translator.translate(in, out, "text/html",
          getLanguage(), getTranslationGroup());
        input = new ByteArrayInputStream(bos.toByteArray());
      }
      else input = new ByteArrayInputStream(content.getBytes(CHARSET));

      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setShowWarnings(false);
      tidy.setInputEncoding(CHARSET);

      // parse document with Tidy
      Document document = tidy.parseDOM(input, null);
      // write to output
      encodeDocument(document, writer, values);
      System.out.println("----CustomForm------------------------------------");
      System.out.println("fieldFormats:" + _fieldFormats);
      System.out.println();
      System.out.println("requiredFields:" + _requiredFields);
      System.out.println();
      System.out.println("multivaluedFields:" + _multivaluedFields);
      System.out.println();
      System.out.println("VALUES:" + values);
      System.out.println();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
  }
  
  private void encodeDocument(Document document, ResponseWriter writer, 
    Map values) throws IOException
  {
    // look for html element
    Node node = document.getFirstChild();
    node = findNode(node, "html");
    if (node == null) throw new IOException("Invalid html document.");
    
    System.out.println("HTML element found");

    // look for body element
    node = node.getFirstChild();
    node = findNode(node, "body");
    if (node == null) throw new IOException("Invalid html document.");

    System.out.println("BODY element found");
   
    // write body contents
    writeNode(document, node, writer, values);
  }

  private Node findNode(Node node, String nodeName)
  {
    Node foundNode = null;
    Node currentNode = node;
    while (foundNode == null && currentNode != null)
    {
      String name = currentNode.getNodeName();
      if (nodeName.equalsIgnoreCase(name))
      {
        foundNode = currentNode;
      }
      else currentNode = currentNode.getNextSibling();
    }
    return foundNode;
  }

  private void writeNode(Document document, 
    Node node, ResponseWriter writer, Map values)
    throws IOException
  {
    if (node instanceof Text)
    {
      String text = node.getNodeValue();
      writer.writeText(text, null);
    }
    else if (node instanceof Comment)
    {
      // ommit comments in rendered page
      return;
      //writer.writeComment(node.getNodeValue());
    }
    else if (node instanceof Element)// normal tag
    {
      Element element = (Element)node;
      String tag = element.getNodeName().toLowerCase();
      if (tag.equals("body"))
      {
        // encode child elements, but remove body tag
        writeChildren(document, node, writer, values);
        return;
      }
      else if (tag.equals("form")) 
      {
        // encode child elements, but remove form tag
        writeChildren(document, node, writer, values);
        return;
      }
      else if (tag.equals("input")) // it's a input
      {
        String type = element.getAttribute("type").toLowerCase();
        if (type.equals("submit"))
        {
          // not supported
          return;
        }
        else if (type.equals("button"))
        {
          // not supported
          return;
        }
        else if (type.equals("hidden"))
        {
          // not supported
          return;
        }
        else if (type.equals("text") || type.equals("password"))
        {
          String name = transformElement(element, values);
          setInputTextValue(element, getParameterValue(name));
          String maxLength = element.getAttribute("maxlength");
          if ("0".equals(maxLength))
          {
            element.removeAttribute("maxlength");
          }
        }
        else if (type.equals("radio"))
        {
          String name = transformElement(element, values);
          setSelectedRadio(element, getParameterValue(name));
        }
        else if (type.equals("checkbox"))
        {
          String name = element.getAttribute("name");
          element.setAttribute("value", "true");
          element.setAttribute("name",
            getClientId(getFacesContext()) + VAR_SEPARATOR + name);
          _fieldFormats.put(name, BOOLEAN);
          setSelectedCheckBox(element, getParameterValue(name));
        }
      }
      else if (tag.equals("select"))
      {
        String name = transformElement(element, values);
        fillSelectOptions(document, element);
        setSelectedOption(element, getParameterValue(name));
        element.removeAttribute("offset");
      }
      else if (tag.equals("textarea"))
      {
        String name = transformElement(element, values);
        setTextAreaValue(element, getParameterValue(name));
        String maxLength = element.getAttribute("maxlength");
        if (maxLength != null && maxLength.trim().length() > 0 &&
           !"0".equals(maxLength))
        {
          element.setAttribute("onkeypress", 
            "checkMaxLength(this, " + maxLength + ");");
        }
        element.removeAttribute("maxlength");
      }
      writer.startElement(tag, this);
      writeAttributes(element, writer);
      writeChildren(document, node, writer, values);
      writer.endElement(tag);
    }
  }

  private void writeChildren(Document document, 
    Node node, ResponseWriter writer, Map values)
    throws IOException
  {
    // render children
    Node child = node.getFirstChild();
    while (child != null)
    {
      writeNode(document, child, writer, values);
      child = child.getNextSibling();
    }
  }
  
  private void writeAttributes(Element element, ResponseWriter writer)
    throws IOException
  {
    NamedNodeMap map = element.getAttributes();
    int count = map.getLength();
    for (int i = 0; i < count; i++)
    {
      Node attribute = map.item(i);
      String name = attribute.getNodeName();
      String value = attribute.getNodeValue();
      if (value != null)
      {
        writer.writeAttribute(name, value, null);
      }
    }
  }

  // detects format, required and multivalued attributes for element
  private String transformElement(Element element, Map values)
  {
    // read fieldName attribute
    String name = element.getAttribute("name");

    // read initial value
    Object value = values.get(name);
    if (value instanceof List)
    {
      // add to multivalued fields
      _multivaluedFields.add(name);
      List list = (List)value;
      if (list.size() > 0) value = list.get(0);
    }

    // read multivalued attribute
    String multivalued = element.getAttribute("multivalued");
    if (multivalued != null)
    {
      if ("true".equalsIgnoreCase(multivalued))
      {
        _multivaluedFields.add(name);
      }
      element.removeAttribute("multivalued");
    }

    // read format attribute
    String format = element.getAttribute("format");
    if (format == null) // format not specified, autodetect from value
    {
      if (value instanceof Number) format = NUMBER;
      else if (value instanceof Boolean) format = BOOLEAN;
      else format = TEXT;
    }
    else // format specified
    {
      element.removeAttribute("format");
      if (!format.startsWith(TEXT) && 
          !format.startsWith(NUMBER) &&
          !format.startsWith(BOOLEAN) &&
          !format.startsWith(DATE))
      {
        format = TEXT;
      }
    }
    System.out.println("set format for " + name + " = " + format);
    _fieldFormats.put(name, format);

    // read required attribute
    String required = element.getAttribute("required");
    if (required != null)
    {
      if ("true".equalsIgnoreCase(required))
      {
        _requiredFields.add(name);
      }
      element.removeAttribute("required");
    }

    // read disabled attribute
    String disabled = element.getAttribute("disabled");
    if (disabled != null && disabled.trim().length() > 0)
    {
      if ("false".equalsIgnoreCase(disabled.trim()))
      {
        element.removeAttribute("disabled");
      }
      else // disabled = true
      {
        _requiredFields.remove(name);
      }
    }

    // change fieldName attribute (add clientId prefix)
    element.setAttribute("name",
      getClientId(getFacesContext()) + VAR_SEPARATOR + name);

    return name;
  }

  private void setInputTextValue(Element element, String value)
  {
    if (value != null)
    {
      element.setAttribute("value", value);
    }
  }

  private void setSelectedRadio(Element element, String value)
  {
    if (value != null)
    {
      String radioValue = element.getAttribute("value");
      if (value.equals(radioValue))
      {
        element.setAttribute("checked", "true");
      }
      else
      {
        element.removeAttribute("checked");
      }
    }
  }

  private void setSelectedCheckBox(Element element, String value)
  {
    if (value != null)
    {
      if ("true".equals(value))
      {
        element.setAttribute("checked", "true");
      }
      else
      {
        element.removeAttribute("checked");
      }
    }
  }

  private void setSelectedOption(Element element, String value)
  {
    if (value != null)
    {
      Element option = (Element)element.getFirstChild();
      while (option != null)
      {
        String optionValue = option.getAttribute("value");
        if (value.equals(optionValue))
        {
          option.setAttribute("selected", "selected");
        }
        else
        {
          option.removeAttribute("selected");
        }
        option = (Element)option.getNextSibling();
      }
    }
  }

  private void setTextAreaValue(Element element, String textValue)
  {
    if (textValue != null)
    {
      Text text;
      Node node = element.getFirstChild();
      if (node == null)
      {
        text = element.getOwnerDocument().createTextNode(textValue);
        element.appendChild(text);
      }
      else if (node instanceof Text)
      {
        text = (Text)node;
        text.setNodeValue(textValue);
      }
    }
  }

  private void fillSelectOptions(Document document, Element element)
  {
    try
    {
      String sql = element.getAttribute("sql");
      String connection = element.getAttribute("connection");
      String username = element.getAttribute("username");
      String password = element.getAttribute("password");

      if (sql != null && sql.trim().length() > 0 &&
         connection != null && connection.trim().length() > 0)
      {
        element.removeAttribute("sql");
        element.removeAttribute("connection");
        element.removeAttribute("username");
        element.removeAttribute("password");
        Map values = getValues();
        QueryParameters queryParameters = new QueryParameters();
        Set<Map.Entry> entries = values.entrySet();
        for (Map.Entry entry : entries)
        {
          String name = String.valueOf(entry.getKey());
          Object value = entry.getValue();
          if (value instanceof List)
          {
            List list = (List)value;
            value = (list.size() > 0) ? list.get(0) : null;
          }
          QueryParameter param = new QueryParameter();
          param.setName(name);
          param.setValue(value);
          queryParameters.getParameters().add(param);
        }
        // get port to SQL service
        WSDirectory dir = WSDirectory.getInstance();
        WSEndpoint endpoint = dir.getEndpoint(SQLManagerService.class);
        SQLManagerPort port = endpoint.getPort(SQLManagerPort.class);

        sql = sql.replaceAll("\\\\n", "\n");
        QueryTable table = port.executeAliasQuery(sql, queryParameters,
          connection, username, password);

        for (QueryRow row : table.getQueryRow())
        {
          Element option = document.createElement("option");
          option.setAttribute("value",
            String.valueOf(row.getValues().get(0)));
          Text text = document.createTextNode(
            String.valueOf(row.getValues().get(1)));
          option.appendChild(text);
          element.appendChild(option);
        }
      }
    }
    catch (Exception ex)
    {
      System.out.println("Executing SQL: " + ex.toString());
    }
  }

  private void validateField(String fieldName, String value)
  {
    // get field properties
    String format = (String)_fieldFormats.get(fieldName);
    boolean required = _requiredFields.contains(fieldName);
    boolean multivalued = _multivaluedFields.contains(fieldName);

    if (value == null) value = "";
    else value = value.trim();
    if (value.length() == 0 && required) // check when required
    {
      _valid = false;
      FacesUtils.addMessage(REQUIRED_VALUE, new Object[]{fieldName},
        FacesMessage.SEVERITY_ERROR);
    }
    else // convert
    {
      Object convertedValue = value;
      try
      {
        convertedValue = convertValueFromString(value, format, multivalued);
        _convertedValues.put(fieldName, convertedValue);
      }
      catch (Exception ex)
      {
        _valid = false;
        FacesUtils.addMessage(INVALID_VALUE, new Object[]{fieldName},
          FacesMessage.SEVERITY_ERROR);
      }
    }
  }

  private String getParameterValue(String fieldName)
  {
    String format = (String)_fieldFormats.get(fieldName);
    boolean multivalued = _multivaluedFields.contains(fieldName);

    // look first in submittedValues
    String strValue = (String)_submittedValues.get(fieldName);
    if (strValue == null)
    {
      // if not found, look in initial values
      Map values = getValues();
      if (values != null)
      {
        Object value = values.get(fieldName);
        if (value != null)
        {
          try
          {
            System.out.println(">> convert field: " + fieldName);
            System.out.println(" value: " + value);
            System.out.println(" format: " + format);
            System.out.println(" multivalued: " + multivalued);
            strValue = convertValueToString(value, format, multivalued);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
            // bad initial value, ignore
          }
        }
      }
    }
    return strValue;
  }

  // converts value from Object to String
  private String convertValueToString(Object value, String format, 
    boolean multivalued) throws Exception
  {
    String type;
    String parameters;
    int index = format.indexOf(FORMAT_SEPARATOR);
    if (index != -1)
    {
      type = format.substring(0, index);
      parameters = format.substring(index + 1);
    }
    else
    {
      type = format;
      parameters = null;
    }

    if (value instanceof List)
    {
      // get first element only
      List list = (List)value;
      if (list.size() > 0)
      {
        value = list.get(0);
      }
      else value = null;
    }
    // parse value according to format
    if (value == null)
    {
    }
    else if (TEXT.equals(type))
    {
    }
    else if (DATE.equals(type))
    {
      String dateFormat = DEFAULT_DATE_FORMAT;
      if (parameters != null)
      {
        dateFormat = parameters;
      }
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      df.setLenient(false);
      Date date = df.parse(value.toString());
      df = new SimpleDateFormat(dateFormat);
      value = df.format(date);
    }
    else if (NUMBER.equals(type))
    {
      Double number = new Double(value.toString());
      if (number.intValue() == number.doubleValue())
      {
        // remove decimal point
        value = String.valueOf(number.intValue());
      }
    }
    else if (BOOLEAN.equals(type))
    {
      value = new Boolean(value.toString());
    }
    return value == null ? null : value.toString();
  }

  // converts a value from String to Object
  private Object convertValueFromString(String value, String format,
    boolean multivalued) throws Exception
  {
    // value is never null here (converted to "")
    String type;
    String parameters;
    int index = format.indexOf(FORMAT_SEPARATOR);
    if (index != -1)
    {
      type = format.substring(0, index);
      parameters = format.substring(index + 1);
    }
    else
    {
      type = format;
      parameters = null;
    }
    Object convertedValue = null;
    if (TEXT.equals(type)) // TEXT format
    {
      if (parameters != null)
      {
        if (!value.matches(parameters))
          throw new Exception("Invalid value");
      }
      if (value.length() == 0)
      {
        convertedValue = null;
      }
      else
      {
        convertedValue = value;
      }
    }
    else if (NUMBER.equals(type)) // NUMBER format
    {
      if (value.length() == 0)
      {
        convertedValue = null;
      }
      else
      {
        double number = Double.parseDouble(value);
        if (parameters != null)
        {
          String tokens[] = parameters.split(",");
          double min = Double.parseDouble(tokens[0].trim());
          double max = Double.parseDouble(tokens[1].trim());
          if (number < min || number > max)
            throw new Exception("Value out of range");
        }
        convertedValue = new Double(number);
      }
    }
    else if (BOOLEAN.equals(type)) // BOOLEAN format
    {
      convertedValue = new Boolean(value);
    }
    else if (DATE.equals(type)) // DATE format
    {
      if (value.length() == 0)
      {
        convertedValue = null;
      }
      else
      {
        String dateFormat = DEFAULT_DATE_FORMAT;
        if (parameters != null)
        {
          dateFormat = parameters;
        }
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        df.setLenient(false);
        Date date = df.parse(value);
        df = new SimpleDateFormat("yyyyMMdd"); // Internal date format
        convertedValue = df.format(date);
      }
    }
    if (multivalued) // return a list
    {
      ArrayList list = new ArrayList();
      list.add(convertedValue);
      convertedValue = list;
    }
    return convertedValue;
  }

  private String getLanguage()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    return context.getViewRoot().getLocale().getLanguage();
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _url;
    values[2] = _values;
    values[3] = _newValues;    
    values[4] = _submittedValues;
    values[5] = _fieldFormats;
    values[6] = _requiredFields;
    values[7] = _multivaluedFields;
    values[8] = _translationGroup;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _url = (String)values[1];
    _values = (Map)values[2];
    _newValues = (Map)values[3];
    _submittedValues = (Map)values[4];
    _fieldFormats = (Map)values[5];
    _requiredFields = (HashSet)values[6];
    _multivaluedFields = (HashSet)values[7];
    _translationGroup = (String)values[8];
  }
}
