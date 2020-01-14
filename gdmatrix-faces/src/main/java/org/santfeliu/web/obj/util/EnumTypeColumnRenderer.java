package org.santfeliu.web.obj.util;

import java.util.List;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.santfeliu.dic.web.DictionaryConfigBean;

/**
 *
 * @author blanquepa
 */
public class EnumTypeColumnRenderer extends DefaultColumnRenderer
{
  private String enumTypeId;
  
  public EnumTypeColumnRenderer(String enumTypeId)
  {
    this.enumTypeId = enumTypeId;
  }
  
  @Override
  public Object getValue(String columnName, Object row)
  {
    Object value = super.getValue(columnName, row);
    if (value != null)
    {
      try
      {
        DictionaryManagerPort port = DictionaryConfigBean.getPort();
        EnumTypeItemFilter filter = new EnumTypeItemFilter();
        filter.setValue(String.valueOf(value));
        filter.setEnumTypeId(enumTypeId);
        List<EnumTypeItem> items = port.findEnumTypeItems(filter);
        if (items != null)
          value = items.get(0).getLabel();
      }
      catch (Exception ex)
      {
        
      }
    }

    return value;
  }
}
