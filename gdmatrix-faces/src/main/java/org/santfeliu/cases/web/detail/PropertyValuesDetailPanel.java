package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.web.obj.DetailBean;

/**
 *
 * @author blanquepa
 */
public class PropertyValuesDetailPanel extends TabulatedDetailPanel
{
  public static final String MAIN_PROPERTY_NAME = "mainPropertyName";
  public static final String INTERSECT_ENUMTYPEID = "intersectEnumTypeId";

  private List<EnumTypeItem> mainEnumTypeItems;
  private List<EnumTypeItem> intersectEnumTypeItems;

  private List<String> values;

  @Override
  public void loadData(DetailBean detailBean)
  {
    values = new ArrayList();
    CaseDetailBean caseDetailBean = (CaseDetailBean) detailBean;
    Case cas = caseDetailBean.getCase();
    if (cas != null)
    {
      String mainPropertyName = getProperty(MAIN_PROPERTY_NAME);
      Property mainProperty = DictionaryUtils.getProperty(cas, mainPropertyName);
      if (mainProperty != null && !mainProperty.getValue().isEmpty())
      {
        String caseTypeId = cas.getCaseTypeId();
        if (caseTypeId != null)
        {
          Type type = TypeCache.getInstance().getType(caseTypeId);
          if (type != null)
          {
            PropertyDefinition pd = type.getPropertyDefinition(mainPropertyName);
            String enumTypeId = pd.getEnumTypeId();
            mainEnumTypeItems = getEnumTypeItems(enumTypeId);

            intersectEnumTypeItems =
              getEnumTypeItems(getProperty(INTERSECT_ENUMTYPEID));

            for (String value : mainProperty.getValue())
            {
              if (enumTypeId != null)
              {
                EnumTypeItem enumType = getEnumTypeItemByValue(mainEnumTypeItems, value);
                if (enumType != null)
                  value = enumType.getLabel();
              }
              values.add(value);
            }
          }
        }
      }
    }
  }

  public List<String> getValues()
  {
    return values;
  }

  public void setValues(List<String> values)
  {
    this.values = values;
  }

  @Override
  public boolean isRenderContent()
  {
    return values != null && !values.isEmpty();
  }

  @Override
  public String getType()
  {
    return "property_values";
  }

  public boolean isIntersectedValue()
  {
    String row = (String)getValue("#{row}");
    if (row != null)
    {
      EnumTypeItem item = getEnumTypeItemByLabel(intersectEnumTypeItems, row);
      if (item != null)
        return true;
    }
    return false;
  }

  private List<EnumTypeItem> getEnumTypeItems(String enumTypeId)
  {
    List<EnumTypeItem> result = new ArrayList();
    if (enumTypeId != null)
    {
      EnumTypeItemFilter filter = new EnumTypeItemFilter();
      filter.setEnumTypeId(enumTypeId);
      try
      {
        result = DictionaryConfigBean.getPort().findEnumTypeItems(filter);
      }
      catch (Exception ex)
      {
      }
    }
    return result;
  }

  private EnumTypeItem getEnumTypeItemByValue(List<EnumTypeItem>items, String name)
  {
    if (name != null && items != null)
    {
      for (EnumTypeItem item : items)
      {
        if (item.getValue().equals(name))
          return item;
      }
    }

    return null;
  }

  private EnumTypeItem getEnumTypeItemByLabel(List<EnumTypeItem>items, String label)
  {
    if (label != null && items != null)
    {
      for (EnumTypeItem item : items)
      {
        if (item.getLabel().equals(label))
          return item;
      }
    }

    return null;
  }

}
