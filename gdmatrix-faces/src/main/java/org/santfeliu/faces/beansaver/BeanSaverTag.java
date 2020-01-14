package org.santfeliu.faces.beansaver;

import javax.faces.webapp.UIComponentTag;

public class BeanSaverTag extends UIComponentTag
{
  public String getComponentType()
  {
    return UIBeanSaver.COMPONENT_TYPE;
  }

  public String getRendererType()
  {
    return null;
  }
}
