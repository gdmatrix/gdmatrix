package org.santfeliu.faces.beansaver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

public class UIBeanSaver extends UIParameter
{
  public static final String COMPONENT_TYPE = "BeanSaver";
  public static final String COMPONENT_FAMILY = "javax.faces.Parameter";

  public UIBeanSaver()
  {
  }

  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    Map requestMap = context.getExternalContext().getRequestMap();
    values[1] = saveRequestBeans(requestMap);
    return values;
  }

  public void restoreState(FacesContext context, Object state)
  {
    Object values[] = (Object[])state;
    super.restoreState(context, values[0]);
    Map requestMap = context.getExternalContext().getRequestMap();
    restoreRequestBeans(requestMap, values[1]);
  }

  private Object saveRequestBeans(Map requestMap)
  {
    ArrayList beanList = new ArrayList();
    Iterator iter = requestMap.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      Object value = entry.getValue();
      if (value instanceof Savable)
      {
        String name = String.valueOf(entry.getKey());
        beanList.add(name);
        beanList.add(value);
      }
    }
    return beanList.toArray();
  }

  private void restoreRequestBeans(Map requestMap, Object state)
  {
    Object[] beans = (Object[])state;
    for (int i = 0; i < beans.length; i += 2)
    {      
      Object name = beans[i];
      Object bean = beans[i + 1];
      requestMap.put(name, bean);
    }
  }
}
