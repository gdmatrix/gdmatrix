package org.santfeliu.security.web;

import java.util.List;
import org.matrix.security.User;
import org.matrix.security.UserFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class UserSearchBean extends BasicSearchBean
{
  private UserFilter filter;
  private String userIdInput;
  
  public UserSearchBean()
  {
    filter = new UserFilter();
  }

  public UserFilter getFilter()
  {
    return filter;
  }

  public void setFilter(UserFilter filter)
  {
    this.filter = filter;
  }

  public String getUserIdInput()
  {
    return userIdInput;
  }

  public void setUserIdInput(String userIdInput)
  {
    this.userIdInput = userIdInput;
  }
  
  public int countResults()
  {
    try
    {
      setFilterUserId();
      return SecurityConfigBean.getPort().countUsers(filter);
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
      setFilterUserId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return SecurityConfigBean.getPort().findUsers(filter);
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
    return "user_search";
  }

  public String selectUser()
  {
    User row = (User)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String userId = row.getUserId();
    return getControllerBean().select(userId);
  }

  public String showUser()
  {
    return getControllerBean().showObject("User", 
      (String)getValue("#{row.userId}"));
  }

  private void setFilterUserId()
  {
    filter.getUserId().clear();
    for (String userId : userIdInput.split(";"))
    {
      filter.getUserId().add(userId);
    }
  }

}
