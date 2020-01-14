package org.santfeliu.dic.util;


import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.doc.Document;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.WebTemplate;

/**
 * This class helps to dump an instance of an object using its Type
 * definition from dictionary.
 * The dump method returns a list with the description and instance value of each
 * property of the object instance.
 * A list of filtering properties that describe the object could be set by
 * parameter in construction.
 *
 * @author blanquepa
 */
public class ObjectDumper implements Serializable
{  
  private List<String> filteredProperties;
  private DictionaryManagerPort port;
  
  //docId -> js code
  private static Map<String, String> jsCodeMap = new HashMap<String, String>();
  private static long lastJsCodeRefreshMillis = System.currentTimeMillis();
  private static final long JS_CODE_REFRESH = 5 * 60 * 1000; //5 minutes

  private static Map<String, Object[]> enumTypeMap = 
    Collections.synchronizedMap(new HashMap<String, Object[]>());  
  private static final long ENUM_TYPE_REFRESH = 60 * 1000; //60 seconds
  
  public ObjectDumper()
  {
    filteredProperties = null;
  }
  
  public ObjectDumper(DictionaryManagerPort port)
  {
    this.port = port;
  }

  public ObjectDumper(String filterPropertiesString)
  {
    if (filterPropertiesString != null)
    {
      filteredProperties = new ArrayList<String>();
      
      List<String> propNames = captureProperties(filterPropertiesString);
      for (String propName : propNames)
      {
        filteredProperties.add(propName);
      }
    }
  }

  public ObjectDumper(List<String> filterProperties)
  {
    this.filteredProperties = filterProperties;
  }

  public List<Property> dump(Object object, Type type)
  {
    List<Property> result = new ArrayList();

    if (filteredProperties == null)
      filteredProperties = getObjectPropertyNames(type);

    for (String propName : filteredProperties)
    {
      String assignedLabel = null;      
      String[] splitByLabel = propName.split("::");
      propName = splitByLabel[0].trim();
      if (splitByLabel.length > 1)
      {
        assignedLabel = splitByLabel[1].trim();
      }
      if (propName.startsWith("js(") && propName.endsWith(")"))
      {
        try
        {
          String jsCode = propName.substring(3, propName.length() - 1);
          try
          {
            String docId = String.valueOf(Integer.parseInt(jsCode));
            if (System.currentTimeMillis() > lastJsCodeRefreshMillis + JS_CODE_REFRESH)
            {
              lastJsCodeRefreshMillis = System.currentTimeMillis();
              jsCodeMap.clear();
            }
            if (!jsCodeMap.containsKey(docId))
            {
              Document doc = DocumentConfigBean.getClient().loadDocument(docId);
              DataHandler dh = DocumentUtils.getContentData(doc);
              InputStream is = dh.getInputStream();
              int size = doc.getContent().getSize().intValue();
              byte[] byteArray = new byte[size];            
              is.read(byteArray);
              jsCodeMap.put(docId, new String(byteArray, "UTF-8"));              
            }
            jsCode = jsCodeMap.get(docId);
          }
          catch (NumberFormatException ex) //not docId -> js code
          {

          }          
          Object value = getJSPropertyValue(jsCode, object, type);
          if (value instanceof NativeArray)
          {
            Scriptable arr = (Scriptable)value;
            Object[] valueArray = new Object[arr.getIds().length];
            for (Object o : arr.getIds()) 
            {
              int index = (Integer)o;
              valueArray[index] = arr.get(index, null);
            }                        
            String[] assignedLabels = assignedLabel.split(",");
            if (assignedLabels.length == valueArray.length)
            {              
              for (int i = 0; i < assignedLabels.length; i++)
              {
                if (valueArray[i] != null)
                {
                  Property prop = new Property();  
                  prop.setDescription(assignedLabels[i]);
                  prop.setValue(String.valueOf(valueArray[i]));
                  result.add(prop);                  
                }
              }
            }
            else
            {
              //Nothing here -> The property is not added
            }
          }
          else //single value
          {
            Property prop = new Property();
            prop.setDescription(assignedLabel);
            prop.setValue(String.valueOf(value));
            result.add(prop);
          }
        }
        catch (Exception ex)
        {
          
        }        
      }
      else
      {
        Property prop = dumpProperty(object, type, propName, assignedLabel);
        result.add(prop);
      }      
    }

    return result;
  }
  
  private Property dumpProperty(Object object, Type type, String propName, 
    String assignedLabel)
  {
    Property prop = new Property();
    prop.setName(propName);
    String[] props = propName.split("\\.");
    if (props.length == 1)
    {      
      prop.setDescription(
        assignedLabel != null ? assignedLabel : getPropertyDescription(type, propName));          
      String propValue = getPropertyValue(object, propName);
      prop.setValue(formatValue(type, propName, propValue));
    }
    else
    {
      Type auxType = null;
      Object aux = object;
      for (int i = 0; i < props.length; i++)
      {
        if (aux == null) return null;
        auxType = getType(aux);
        aux = PojoUtils.getStaticProperty(aux, props[i]);
      }
      propName = props[props.length - 1];
      String propDesc = propName;
      String propValue = String.valueOf(aux);
      if (auxType != null)
      {
        propDesc = getPropertyDescription(auxType, propName);
        propValue = formatValue(auxType, propName, propValue);
      }
      prop.setDescription(assignedLabel != null ? assignedLabel : propDesc);
      prop.setValue(propValue);
    }
    return prop;
  }
  
  public Map<String, String> dumpAsMap(Object object, Type type)
  {
    HashMap<String,String> result = new HashMap();
    List<Property> properties = dump(object, type);
    for (Property property : properties)
    {
      result.put(property.getName(), property.getValue());
    }
    return result;
  }

  //Private
  
  //TODO Use regular expression instead
  private List<String> captureProperties(String propertiesString)
  {
    List<String> params = new ArrayList();
    boolean jsCapture = false;
    StringBuilder sbParam = new StringBuilder();
    int i = 0;
    int openPar = 0;
    char[] cArray = propertiesString.toCharArray();
    while (i < cArray.length)
    {
      if (jsCapture)
      {
        if (cArray[i] == '(') openPar++;
        else if (cArray[i] == ')')
        {
          openPar--;
          if (openPar == 0) jsCapture = false;
        }
        sbParam.append(cArray[i]);
        i = i + 1;
      }
      else //normal capture
      {
        if (cArray[i] == ';')
        {
          params.add(sbParam.toString().trim());
          sbParam = new StringBuilder();          
          i = i + 1;
        }
        else if ((cArray.length >= i + 3) && cArray[i] == 'j' && cArray[i+1] == 's' && cArray[i+2] == '(')
        {
          jsCapture = true;            
          openPar = 1;
          sbParam.append("js(");
          i = i + 3;
        }
        else if (cArray[i] == '\n' || cArray[i] == '\t')
        {
          i++;
        }
        else //normal char
        {            
          sbParam.append(cArray[i]);
          i++;
        }
      }
    }
    if (sbParam.length() > 0)
    {
      params.add(sbParam.toString().trim());
    }
    return params;
  }
  
  private String getPropertyDescription(Type type, String propName)
  {
    String description = propName;

    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(description);
      if (pd != null)
        description = pd.getDescription();
    }

    return description;
  }

  private String getPropertyValue(Object object, String propName)
  {
    String value = "";

    try
    {
      if (PojoUtils.hasStaticProperty(object.getClass(), propName))
      {
        Object objvalue = PojoUtils.getStaticProperty(object, propName);
        if (objvalue != null)
          value = String.valueOf(objvalue);
      }
      else
      {
        Method getPropertyMethod =
          object.getClass().getMethod("getProperty", new Class[]{});
        if (getPropertyMethod != null)
        {
          List properties = (List)getPropertyMethod.invoke(object, new Object[]{});
          List<String> values =
            (List<String>)PojoUtils.getDynamicProperty(properties, propName);
          if (values != null && values.size() == 1)
            value = values.get(0);
          else if (values != null && values.size() > 1)
            value = String.valueOf(values);
        }
      }
    }
    catch (Exception ex)
    {
    }

    return value;
  }

  private String formatValue(Type type, String propName, String value)
  {
    String formatValue = value;

    if (value != null && value.startsWith("[") && value.endsWith("]") && value.contains(",") && type != null)
    {
      value = value.substring(1, value.length() - 1);
      String[] values = value.split(",");
      String aux = "";
      for (int i = 0; i < values.length; i++)
      {
        aux = aux + formatValue(type, propName, values[i].trim());
        if (i != values.length - 1)
          aux = aux + ",";
      }
      formatValue = aux;
    }
    else if (value != null && !value.equals("") && type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(propName);
      if (pd != null)
      {
        PropertyType pt = pd.getType();
        if (PropertyType.DATE.equals(pt))
        {
          String date =
            TextUtils.formatDate(TextUtils.parseInternalDate(value),
              pd.getSize() == 14 ? "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy");
          if (date != null) formatValue = date;
        }
        else if (PropertyType.TEXT.equals(pt))
        {
          if (propName.endsWith("TypeId"))
          {
            Type formatedType = TypeCache.getInstance().getType(value);
            if (formatedType != null)
              formatValue = formatedType.getDescription();
          }
        }
        
        if (pd.getEnumTypeId() != null)
        {
          long now = System.currentTimeMillis();
          String mapKey = pd.getEnumTypeId() + ";" + value;
          if (enumTypeMap.containsKey(mapKey))
          {
            Object[] mapItem = enumTypeMap.get(mapKey);
            if (now > (Long)mapItem[1] + ENUM_TYPE_REFRESH)
              enumTypeMap.remove(mapKey); //Discard item if it's too old
            else
              formatValue = (String)mapItem[0];
          }
          
          if (!enumTypeMap.containsKey(mapKey))
          {
            EnumTypeItemFilter filter = new EnumTypeItemFilter();
            filter.setEnumTypeId(pd.getEnumTypeId());
            filter.setValue(formatValue);
            try
            {
              List<EnumTypeItem> items = getPort().findEnumTypeItems(filter);
              formatValue = items.get(0).getLabel();    
            }
            catch (Exception ex)
            {
              formatValue = value;
            }
            Object[] mapItem = {formatValue, now};
            enumTypeMap.put(mapKey, mapItem);
          }
          
        }
      }
    }

    return formatValue;
  }
  
  private DictionaryManagerPort getPort() throws Exception
  {
    if (port != null)
      return port;
    else
      return DictionaryConfigBean.getPort();      
  }

  private List<String> getObjectPropertyNames(Type objectType)
  {
    ArrayList<String> result = new ArrayList<String>();
    
    List<PropertyDefinition> pdList = new ArrayList<PropertyDefinition>();
    List<Type> superTypes = objectType.getSuperTypes();
    for (Type superType : superTypes)
    {
      pdList.addAll(superType.getPropertyDefinition());
    }    
    pdList.addAll(objectType.getPropertyDefinition());
    for (PropertyDefinition pd : pdList)
    {
      result.add(pd.getName());
    }

    return result;
  }
  
  private Type getType(Object object)
  {
    Type type = null;

    ObjectPropertiesConverter converter = new ObjectPropertiesConverter(object);
    HashMap<String, Object> properties = new HashMap<String, Object>();
    try
    {
      converter.toPropertiesMap(properties);
      Set<String> propNames = properties.keySet();
      for (String propName : propNames)
      {
        if (propName.endsWith("TypeId"))
        {
          String typeId = (String) properties.get(propName);
          return TypeCache.getInstance().getType(typeId);
        }
      }
    }
    catch (Exception ex)
    {
    }

    return type;
  }
  
  private Object getJSPropertyValue(String jsCode, Object object, Type type) 
    throws Exception
  {
    Set<String> propertyNameSet = getPropertyNames(jsCode);
    Map<String, String> propertyMap = 
      getPropertyMap(propertyNameSet, object, type);    
    jsCode = jsCode.replaceAll("\\.(?=[^{}]*\\})", "__"); //object.property -> object__property 
    return getJSCodeValue(jsCode, propertyMap);
  }
  
  private Map<String, String> getPropertyMap(Set<String> propertyNameSet, 
    Object object, Type type) throws Exception
  {
    Map<String, String> propertyMap = new HashMap();    
    for (String propName : propertyNameSet)
    {      
      Property prop = dumpProperty(object, type, propName, "");
      if (prop.getValue() == null || prop.getValue().trim().isEmpty())
      {
        throw new Exception(); //no null values allowed in query
      }      
      propertyMap.put(prop.getName().replace(".", "__"), prop.getValue());
    }
    return propertyMap;
  }
  
  private Set<String> getPropertyNames(String text)
  {
    Set<String> result = new HashSet();    
    String patternString = "\\$\\{.*?\\}";
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find())
    {
      String sAux = matcher.group();
      String propertyName = sAux.substring(2, sAux.length() - 1).trim();
      if (!propertyName.isEmpty())
      {
        result.add(propertyName);
      }
    }
    return result;
  }
  
  private Object getJSCodeValue(String jsCode, Map<String, String> propertyMap) 
    throws Exception
  {
    Context cx = ContextFactory.getGlobal().enterContext();
    Object result = null;
    try
    {
      String mcode = WebTemplate.create(jsCode).merge(propertyMap);
      Scriptable scope = new WebScriptableBase(cx, propertyMap);      
      result = cx.evaluateString(scope, mcode, "", 1, null);      
    }
    catch (Exception ex)
    {
      throw ex;
    }
    finally
    {
      Context.exit();
    }
    return (result != null ? result : "");
  }

  public class Property implements Serializable
  {
    String name;
    String description;
    String value;

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }
  }

}
