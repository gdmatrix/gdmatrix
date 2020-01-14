package org.santfeliu.dic.web;

import java.util.List;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class EnumTypeSearchBean extends BasicSearchBean
{
  private String enumTypeIdInput;
  private EnumTypeFilter filter;
  
  public EnumTypeSearchBean()
  {
    filter = new EnumTypeFilter();
  }

  public String getEnumTypeIdInput()
  {
    return enumTypeIdInput;
  }

  public void setEnumTypeIdInput(String enumTypeIdInput)
  {
    this.enumTypeIdInput = enumTypeIdInput;
  }

  public EnumTypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EnumTypeFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterEnumTypeId();
      return DictionaryConfigBean.getPort().countEnumTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      setFilterEnumTypeId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return DictionaryConfigBean.getPort().findEnumTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction  
  public String show()
  {
    return "enum_type_search";
  }

  public String selectEnumType()
  {
    EnumType row = (EnumType)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String enumTypeId = row.getEnumTypeId();
    return getControllerBean().select(enumTypeId);
  }

  public String showEnumType()
  {
    return getControllerBean().showObject("EnumType",
      (String)getValue("#{row.enumTypeId}"));
  }

  private void setFilterEnumTypeId()
  {
    filter.getEnumTypeId().clear();
    if (enumTypeIdInput != null)
    {
      for (String enumTypeId : enumTypeIdInput.split(";"))
      {
        if (!enumTypeId.isEmpty()) filter.getEnumTypeId().add(enumTypeId);
      }
    }
  }
  
}
