package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.List;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.santfeliu.dic.web.DictionaryConfigBean;

/**
 *
 * @author lopezrj
 */
public class MultiFieldsColumnRenderer extends ColumnRenderer implements Serializable
{
  private String fieldName1;
  private String fieldName2;
  private String fieldName3;
  private String fieldName4;
  
  public MultiFieldsColumnRenderer(String fieldName1, String fieldName2, 
    String fieldName3, String fieldName4)
  {
    this.fieldName1 = fieldName1;
    this.fieldName2 = fieldName2;
    this.fieldName3 = fieldName3;
    this.fieldName4 = fieldName4;
  }
  
  @Override
  public Object getValue(String columnName, Object row)
  {    
    StringBuilder sbDiv = new StringBuilder();
    sbDiv.append("<div class='fields'>");
    sbDiv.append(getFieldDiv(fieldName1, row, 1));
    sbDiv.append(getFieldDiv(fieldName2, row, 2));
    sbDiv.append(getFieldDiv(fieldName3, row, 3));
    sbDiv.append(getFieldDiv(fieldName4, row, 4));
    sbDiv.append("</div>");
    return sbDiv.toString();
  }

  private String getFieldDiv(String fieldName, Object row, Integer idx)
  {    
    if (fieldName != null && !fieldName.equals("null"))
    {
      Object value = null;
      if (fieldName.startsWith("enumtype(")) //ex: enumtype(sf:TipusAAA,215) 
      {
        try
        {
          String params = fieldName.substring("enumtype(".length(), fieldName.length() - 1);
          String enumTypeId = params.split(",")[0].trim();
          String field = params.split(",")[1].trim();
          if (enumTypeId.length() > 0 && field.length() > 0)
          {
            DefaultColumnRenderer defaultColumnRenderer = new DefaultColumnRenderer();
            String itemValue = (String)defaultColumnRenderer.getValue(field, row);
            if (itemValue != null)
            {
              DictionaryManagerPort port = DictionaryConfigBean.getPort();
              EnumTypeItemFilter filter = new EnumTypeItemFilter();
              filter.setValue(itemValue);
              filter.setEnumTypeId(enumTypeId);
              List<EnumTypeItem> items = port.findEnumTypeItems(filter);
              if (items != null) value = items.get(0).getLabel();
            }
          }
        }
        catch (Exception ex)
        {
          
        }
      }
      else
      {
        DefaultColumnRenderer defaultColumnRenderer = new DefaultColumnRenderer();
        value = defaultColumnRenderer.getValue(fieldName, row);
      }      
      if (value != null)
      {
        return "<div class='field" + idx + "'>" + value + "</div>";
      }
    }
    return "";
  }
  
  @Override
  public boolean isValueEscaped()
  {
    return false;
  }
  
}