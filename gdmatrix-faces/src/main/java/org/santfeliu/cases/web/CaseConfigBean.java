package org.santfeliu.cases.web;


import java.io.Serializable;
import java.util.List;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.dic.Type;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;


public class CaseConfigBean implements Serializable
{
  public CaseConfigBean()
  {
  }
  
  public static CaseManagerPort getPort() throws Exception
  {
    String userId;
    String password;      
    if (isFindAsAdmin())
    {
       userId = MatrixConfig.getProperty("adminCredentials.userId");
       password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);
    return endpoint.getPort(CaseManagerPort.class, userId, password);
  }
  
  public String getCaseTypeDescription(String typeName)
  {
    if (typeName == null) return null;
    
    Type type = TypeCache.getInstance().getType(typeName);
    if (type != null)
      return type.getDescription() != null ? type.getDescription() : typeName;
    else
      return typeName;
  }
  
  public static boolean isFindAsAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    List<String> userIdList = 
      cursor.getMultiValuedProperty(CaseSearchBean.FIND_AS_ADMIN_FOR_PROPERTY);
    return (userIdList.contains(userId) || userIdList.contains(CaseSearchBean.ALL_USERS));
  }  
  
}
