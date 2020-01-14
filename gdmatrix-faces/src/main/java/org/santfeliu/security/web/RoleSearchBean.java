package org.santfeliu.security.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class RoleSearchBean extends BasicSearchBean
{
  private List<SelectItem> typeSelectItems;
  private RoleFilter filter;
  private String roleIdInput;
  
  public RoleSearchBean()
  {
    filter = new RoleFilter();
  }

  public RoleFilter getFilter()
  {
    return filter;
  }

  public void setFilter(RoleFilter filter)
  {
    this.filter = filter;
  }

  public String getRoleIdInput()
  {
    return roleIdInput;
  }

  public void setRoleIdInput(String roleIdInput)
  {
    this.roleIdInput = roleIdInput;
  }

  public int countResults()
  {
    try
    {
      setFilterRoleId();
      return SecurityConfigBean.getPort().countRoles(filter);
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
      setFilterRoleId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return SecurityConfigBean.getPort().findRoles(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction
  @Override
  public String show()
  {
    return "role_search";
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      TypeBean typeBean = (TypeBean)getBean("typeBean");
      String[] actions = {DictionaryConstants.READ_ACTION};
      typeSelectItems = typeBean.getAllSelectItems(DictionaryConstants.ROLE_TYPE,
        SecurityConstants.SECURITY_ADMIN_ROLE, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }
  
  public String selectRole()
  {
    Role row = (Role)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String roleId = row.getRoleId();
    return getControllerBean().select(roleId);
  }

  public String showRole()
  {
    return getControllerBean().showObject("Role",
      (String)getValue("#{row.roleId}"));
  }

  private void setFilterRoleId()
  {
    filter.getRoleId().clear();
    for (String roleId : roleIdInput.split(";"))
    {
      filter.getRoleId().add(roleId);
    }
  }

}
