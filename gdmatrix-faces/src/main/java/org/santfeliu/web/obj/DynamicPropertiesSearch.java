package org.santfeliu.web.obj;

import org.santfeliu.web.obj.util.FormFilter;

/**
 *
 * @author blanquepa
 */
public interface DynamicPropertiesSearch
{
  public String getSearchPropertyName();
  
  public String getSearchPropertyValue();
  
  public void setSearchDynamicProperties(FormFilter filter);
}
