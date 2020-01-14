package org.santfeliu.dic.web;

import java.util.Date;
import org.matrix.dic.EnumType;
import org.matrix.dic.PropertyType;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

public class EnumTypeMainBean extends PageBean
{
  private EnumType enumType;
  private String itemTypeInput;

  public EnumTypeMainBean()
  {
    load();
  }

  public EnumType getEnumType()
  {
    return enumType;
  }

  public void setEnumType(EnumType enumType)
  {
    this.enumType = enumType;
  }

  public String getItemTypeInput()
  {
    return itemTypeInput;
  }

  public void setItemTypeInput(String itemTypeInput)
  {
    this.itemTypeInput = itemTypeInput;
  }

  public String show()
  {
    return "enum_type_main";
  }

  @Override
  public String store()
  {
    try
    {
      enumType.setItemType(getItemType());

      String superEnumTypeId = enumType.getSuperEnumTypeId();
      if (superEnumTypeId != null && superEnumTypeId.trim().length() == 0)
      {
        enumType.setSuperEnumTypeId(null);
      }

      enumType = DictionaryConfigBean.getPort().storeEnumType(enumType);
      setObjectId(enumType.getEnumTypeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(enumType.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(enumType.getChangeDateTime());
  }

  private void load()
  {
    if (isNew())
    {
      enumType = new EnumType();
      enumType.setSorted(true);
    }
    else
    {
      try
      {
        enumType = DictionaryConfigBean.getPort().loadEnumType(getObjectId());
        setItemType(enumType.getItemType());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        enumType = new EnumType();
      }
    }
  }

  private void setItemType(PropertyType itemType) throws Exception
  {
    switch (itemType)
    {
      case TEXT: setItemTypeInput("T"); break;
      case NUMERIC: setItemTypeInput("N"); break;
      case BOOLEAN: setItemTypeInput("B"); break;
      case DATE: setItemTypeInput("D"); break;
    }
  }

  private PropertyType getItemType()
  {
    if ("T".equals(itemTypeInput))
      return PropertyType.TEXT;
    else if ("N".equals(itemTypeInput))
      return PropertyType.NUMERIC;
    else if ("B".equals(itemTypeInput))
      return PropertyType.BOOLEAN;
    else if ("D".equals(itemTypeInput))
      return PropertyType.DATE;
    return null;
  }

}
