package org.santfeliu.dic.web;

import org.matrix.dic.EnumType;
import org.santfeliu.web.obj.ObjectBean;

public class EnumTypeBean extends ObjectBean
{
  public EnumTypeBean()
  {
  }

  public String getObjectTypeId()
  {
    return "EnumType";
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        DictionaryConfigBean.getPort().removeEnumType(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return getControllerBean().show();
  }
  
  @Override
  public String getDescription()
  {
    EnumTypeMainBean enumTypeMainBean = (EnumTypeMainBean)getBean("enumTypeMainBean");
    EnumType enumType = enumTypeMainBean.getEnumType();
    return enumType.getName();
  }   
  
  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      EnumType enumType = DictionaryConfigBean.getPort().loadEnumType(oid);
      description = enumType.getName();
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
}
