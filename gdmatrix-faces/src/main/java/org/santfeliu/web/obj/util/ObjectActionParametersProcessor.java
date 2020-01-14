package org.santfeliu.web.obj.util;

import java.lang.reflect.Method;
import java.util.Map;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 * This ParametersProcessor decides whether to execute the object creation action 
 * or to show the object based on the presenece of NEW_OBJECT_PARAMETER as objectId.
 * 
 * @author blanquepa
 */
public class ObjectActionParametersProcessor extends ParametersProcessor
{
  public static final String NEW_OBJECT_PARAMETER = "new"; //

  private BasicSearchBean searchBean;
  private String idParameterName;
  private String idTabName;
  private String objectTypeId;  
  
  public ObjectActionParametersProcessor(BasicSearchBean searchBean)
  {
    this(searchBean, null, null, null);
  }
  
  public ObjectActionParametersProcessor(BasicSearchBean searchBean, String idParameterName, String objectTypeId)
  {
    this(searchBean, idParameterName, null, objectTypeId);
  }
  
  public ObjectActionParametersProcessor(BasicSearchBean searchBean, String idParameterName, String idTabName, String objectTypeId)
  {
    this.searchBean = searchBean;
    this.idParameterName = idParameterName;
    this.idTabName = idTabName;
    this.objectTypeId = objectTypeId;
  }

  public String getIdParameterName()
  {
    return idParameterName;
  }

  public void setIdParameterName(String idParameterName)
  {
    this.idParameterName = idParameterName;
  }

  public String getIdTabName()
  {
    return idTabName;
  }

  public void setIdTabName(String idTabName)
  {
    this.idTabName = idTabName;
  }

  public String getObjectTypeId()
  {
    return objectTypeId;
  }

  public void setObjectTypeId(String objectTypeId)
  {
    this.objectTypeId = objectTypeId;
  }

  @Override
  public String processParameters(Map requestMap)
  {
    if (searchBean instanceof BasicSearchBean)
    {
      if (idParameterName == null)
        error("ID_PARAMETER_NAME_NOT_FOUND");
      else if(objectTypeId == null)
        error("OBJECT_TYPEID_NOT_FOUND");
      else
      {
        String objectId = (String)requestMap.get(idParameterName);
        if (objectId != null)
        {
          Class pageBeanClass = searchBean.getClass();
          try
          {
            if (objectId.equals(NEW_OBJECT_PARAMETER))
            {
              Method method = pageBeanClass.getMethod("createObject");
              return (String)method.invoke(searchBean, new Object[]{});
            }
            else
            {
              Method method = pageBeanClass.getMethod("showObject", String.class, String.class);                          
              return (String)method.invoke(searchBean, new Object[]{objectTypeId, objectId});
            }
          }
          catch (Exception ex)
          {
            return null;
          }            
        }          
      }
    }
    
    return null;
  }
  
  public boolean isObjectCreation()
  {
    Map requestMap = getExternalContext().getRequestParameterMap();
    String objectId = (String)requestMap.get(idParameterName);
    if (objectId != null)
      return objectId.equals(NEW_OBJECT_PARAMETER);
    else
      return false;
  }  
  
  public Object getParameter(String name)
  {
    return getExternalContext().getRequestParameterMap().get(name);
  }  
  
  public String getObjectId()
  {
    Object value = getParameter(getIdParameterName());
    return value != null ? String.valueOf(value) : null;
  }
  
  public String getTabObjectId()
  {
    Object value = getParameter(getIdTabName());
    return value != null ? String.valueOf(value) : null;
  }  
  
}
