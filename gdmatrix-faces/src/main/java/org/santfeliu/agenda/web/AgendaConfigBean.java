package org.santfeliu.agenda.web;

import java.net.URL;
import java.util.List;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class AgendaConfigBean
{
  public static final String LIST_IMAGE_TYPE =
    "EventDocumentListImage";
  public static final String DETAILS_IMAGE_TYPE =
    "EventDocumentDetailsImage";
  public static final String LIST_AND_DETAILS_IMAGE_TYPE =
    "EventDocumentListAndDetailsImage";
  public static final String EXTENDED_INFO_TYPE =
    "EventDocumentExtendedInfo";

  public AgendaConfigBean()
  {
  }

  public static boolean isRunAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(EventSearchBean.RUN_AS_ADMIN_FOR_PROPERTY);
    return userIdList.contains(userId);
  }
  
  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(EventSearchBean.FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(EventSearchBean.ALL_USERS));
  }  

  public static AgendaManagerClient getPort() throws Exception
  {
    String userId;
    String password;
    if (isRunAsAdmin() || isFindAsAdmin())
    {
       userId = MatrixConfig.getProperty("adminCredentials.userId");
       password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      userId = userSessionBean.getUserId();
      password = userSessionBean.getPassword();
    }    
    return new AgendaManagerClient(userId, password);
  }  
  
  public static AgendaManagerClient getPort(String userId, String password) throws Exception
  {
    return new AgendaManagerClient(userId, password);
  }

  public static AgendaManagerClient getPort(URL wsDirectoryURL, String userId, String password) throws Exception
  {
    return new AgendaManagerClient(wsDirectoryURL, userId, password);
  }
  
  public String getEventTypeDescription(String typeName)
  {
    if (typeName == null) return "";

    String description = typeName;
    Type type =
      TypeCache.getInstance().getType(typeName);
    if (type != null)
      description = type.getDescription();

    return (description != null ? description : typeName);
  }
}
