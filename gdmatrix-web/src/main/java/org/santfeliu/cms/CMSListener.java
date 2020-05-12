/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.cms;

import com.sun.faces.lifecycle.RestoreViewPhase;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.matrix.security.SecurityConstants;
import org.santfeliu.faces.menu.model.MenuException;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebAuditor;

/**
 *
 * @author realor
 */
public class CMSListener implements PhaseListener
{
  public static final String TOPIC_PARAM = "topic";
  public static final String SMID_PARAM = "smid";
  public static final String XMID_PARAM = "xmid";
  public static final String WORKSPACEID_PARAM = "workspaceid";  
  public static final String BROWSER_TYPE_PARAM = "btype"; 
  public static final String LANGUAGE_PARAM = "language";

  public static final String PAGE_USERID_PARAM = "_userid_";
  // Replace by RequestDispatcher.FORWARD_REQUEST_URI in servlet spec 3.0
  public static final String FORWARD_REQUEST_URI = 
    "javax.servlet.forward.request_uri";   
  public static final String CHARSET_ATTR = "javax.faces.request.charset";
  public static final String SECURE_SESSION_ATTR =
    "org.santfeliu.web.SecureSession";
  
  public static final String NEXT_MENU_ITEM_ATTR =
    "org.santfeliu.web.nextMenuItem";  
  
  public static final String FORCED_LANGUAGE_PROP =
    "org.santfeliu.web.forcedLanguage";  
  public static final String CLIENT_SECURE_PORT_PROP =
    "org.santfeliu.web.clientSecurePort";  
  public static final String REDIRECTION_LIMIT_PROP =
    "org.santfeliu.web.redirectionLimit";
  
  public static final String GO_URI = "/go.faces";
  public static final String LOGIN_URI = "/login.faces"; 
  
  public static final String BLANK_VIEWID = "/common/util/blank.faces";
  
  private static final String REDIR_COUNT_PARAM = "_redircount";  
  
  private Application application;  
  
  private String forcedLanguage;
  private Integer redirectionLimit;
  private int clientSecurePort;  
  private final WebAuditor webAuditor = new WebAuditor();  
  private final HashSet<String> pathExceptions = new HashSet<String>();
  
  public CMSListener()
  {
    //Set paths that shouldn't go through CMS lifecycle (full servletPath)
    pathExceptions.add("/apps/ide.faces");
    pathExceptions.add("/apps/client.faces");
    pathExceptions.add("/apps/elections.faces");
  }
  
  @Override
  public void beforePhase(PhaseEvent pe)
  {
    if (pe.getPhaseId().equals(PhaseId.RESTORE_VIEW))
    {    
      forcedLanguage = MatrixConfig.getProperty(FORCED_LANGUAGE_PROP);
      if (forcedLanguage == null) forcedLanguage = "ca"; 

      try
      {
        String value = MatrixConfig.getProperty(REDIRECTION_LIMIT_PROP);
        redirectionLimit = (value != null ? Integer.valueOf(value) : 2);
      }
      catch (NumberFormatException ex)
      {
        redirectionLimit = 2; //default value
      }

      String value = MatrixConfig.getProperty(CLIENT_SECURE_PORT_PROP);
      if (value != null) clientSecurePort = Integer.parseInt(value);    

      FacesContext context = FacesContext.getCurrentInstance();
      ExternalContext externalContext = context.getExternalContext();
      application = context.getApplication(); 
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();  

      HttpServletRequest request =
        (HttpServletRequest)externalContext.getRequest();
      HttpSession session = request.getSession();
      
      try
      {
        
        // restore request charset encoding
        String charset = (String)session.getAttribute(CHARSET_ATTR);
        if (charset != null) request.setCharacterEncoding(charset);

        // securize session
        enterSecureSession(session, request);      

        MenuItemCursor mic = getRequestedMenuItem(request, userSessionBean);
        if (mic != null)
        {
          redirectByUrl(context, userSessionBean, mic);
          if (context.getResponseComplete() || context.getRenderResponse()) 
            return;

          loginFromParameters(context, userSessionBean);
          if (context.getResponseComplete() || context.getRenderResponse()) 
            return;

          loginFromCertificate(context, userSessionBean);
          if (context.getResponseComplete() || context.getRenderResponse()) 
            return;

          redirectSecure(context, userSessionBean, mic);
          if (context.getResponseComplete() || context.getRenderResponse()) 
            return;        

          requestAuthentication(context, userSessionBean, mic);
          if (context.getResponseComplete() || context.getRenderResponse()) 
            return;
          
          executeRequestedMenuItem(context, userSessionBean, mic);
        }
        else
        {
          //Normal faces lifecycle
          userChangeDetection(context, userSessionBean);
          if (context.getResponseComplete()) return;    
        }

      }
      catch (Exception ex)
      {
        //TODO: Manage exceptions
        userSessionBean.getMenuModel().setAllVisible(false);
      }
      finally
      {

      }         
    }
    else if (pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      if (!context.getResponseComplete())
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();  
        ExternalContext externalContext = context.getExternalContext();
        HttpServletResponse response =
          (HttpServletResponse)externalContext.getResponse();      
        response.setHeader(HttpUtils.X_USER_HEADER, userSessionBean.getUserId());
        System.out.println("\n>>>> Render ---------------------------------\n");
      }      
    }
  }

  @Override
  public void afterPhase(PhaseEvent pe)
  {
    if (pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      if (!context.getResponseComplete())
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();  
        userSessionBean.setLastPageLanguage(
          context.getViewRoot().getLocale().getLanguage());      
        webAuditor.logRequest(userSessionBean, context);      
      }
    }
  }

  @Override
  public PhaseId getPhaseId()
  {
    return PhaseId.ANY_PHASE;
  }
  
  public boolean isSupportedLanguage(String language)
  {
    boolean supported = false;
    Iterator iterator = application.getSupportedLocales();
    while (iterator.hasNext() && !supported)
    {
      Locale locale = (Locale)iterator.next();
      supported = locale.getLanguage().equals(language);
    }
    return supported;
  } 
  
  private HttpSession enterSecureSession(HttpSession session,
    HttpServletRequest request)
  {
    if (HttpUtils.isSecure(request))
    {
      Object secure = session.getAttribute(SECURE_SESSION_ATTR);
      if (secure == null) // current session is not secure
      {
        if (!session.isNew())
        {
          HashMap<String, Object> attributes = new HashMap<String, Object>();
          Enumeration enu = session.getAttributeNames();
          while (enu.hasMoreElements())
          {
            String name = (String)enu.nextElement();
            Object value = session.getAttribute(name);
            attributes.put(name, value);
          }
          session.invalidate();
          session = request.getSession(true);
          for (Map.Entry<String, Object> entry : attributes.entrySet())
          {
            String name = (String)entry.getKey();
            Object value = entry.getValue();
            session.setAttribute(name, value);
          }
        }
        session.setAttribute(SECURE_SESSION_ATTR, Boolean.TRUE);
      }
    }
    else
    {
      session.removeAttribute(SECURE_SESSION_ATTR);
    }
    return session;
  }  
  
  private MenuItemCursor getRequestedMenuItem(
    HttpServletRequest request, UserSessionBean userSessionBean)
    throws Exception
  {
    // Do not execute menuItem in forward requests
    if (request.getAttribute(FORWARD_REQUEST_URI) != null) return null;

    MenuItemCursor menuItem = null;

    if (userSessionBean.getWorkspaceId() == null)
    {
      userSessionBean.initDefaultWorkspaceId();
    }

    String workspaceId = (String)request.getParameter(WORKSPACEID_PARAM);
    if (workspaceId != null) // change workspace
    {
      userSessionBean.setWorkspaceId(workspaceId);
    }

    String browserType = (String)request.getParameter(BROWSER_TYPE_PARAM);
    if (browserType != null) // change browser type
    {
      userSessionBean.setBrowserType(browserType);
    }

    MenuModel menuModel = userSessionBean.getMenuModel();
    menuModel.setAllVisible(true);

    String mid = null;
    String topic = (String)request.getParameter(TOPIC_PARAM);
    if (topic != null) // by topic
    {
      try
      {
        menuItem = menuModel.getMenuItemByTopic(topic);
      }
      catch (MenuException ex) // TopicNotFound or MenuItemNotFound
      {
        menuItem = userSessionBean.getSelectedMenuItem();
      }
    }
    else // by mid
    {
      mid = (String)request.getParameter(XMID_PARAM);      
      if (mid == null)
      {
        String method = request.getMethod();
        if ("GET".equals(method))
        {
          mid = (String)request.getParameter(SMID_PARAM);
          if (mid == null && !isPathException(request))
          {
            mid = userSessionBean.getSelectedMid();
          }
        }
      }

      if (mid != null)
      {
        try
        {
          menuItem = menuModel.getMenuItemByMid(mid);
        }
        catch (MenuException ex) // MenuItemNotFound
        {
          menuItem = userSessionBean.getSelectedMenuItem();
        }
      }
    }
    // register menuItem in request attribute
    request.setAttribute(NEXT_MENU_ITEM_ATTR, menuItem);

    menuModel.setAllVisible(false);

    return menuItem;
  }

  private void executeRequestedMenuItem(FacesContext context,
    UserSessionBean userSessionBean, MenuItemCursor menuItem) throws Exception
  {
    System.out.println("===> Executing menuItem: " + menuItem);

    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();

    String method = request.getMethod();
    if ("GET".equals(method))
    {
      // menuItem execution
      createView(context, userSessionBean, request);
      userSessionBean.executeMenuItem(menuItem);
    }
    else if ("POST".equals(method))
    {
      // menuItem execution
      restoreView(context); // restore view state
      userSessionBean.executeMenuItem(menuItem);
    }
    else context.responseComplete();
  }
  
  private void createView(FacesContext context,
    UserSessionBean userSessionBean, HttpServletRequest request)
  {

    //language
    String language = request.getParameter(LANGUAGE_PARAM);
    if (language == null || !isSupportedLanguage(language))
    {
      language = userSessionBean.getLastPageLanguage();
      if (language == null)
      {
        language = forcedLanguage;
      }
    }
    
    // create view
    ViewHandler viewHandler = application.getViewHandler();
    UIViewRoot viewRoot = viewHandler.createView(context, BLANK_VIEWID);    
    context.setViewRoot(viewRoot);
    userSessionBean.setViewLanguage(language);    
  }
  
  private void restoreView(FacesContext context)
  {
    RestoreViewPhase restorePhase = new RestoreViewPhase();
    restorePhase.execute(context);
  }
  
  private void loginFromParameters(FacesContext context, 
    UserSessionBean userSessionBean) throws Exception
  {
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();
    HttpServletResponse response =
      (HttpServletResponse)externalContext.getResponse();

    String redirectURI = null;
    String uri = request.getRequestURI();
    if (uri.startsWith(LOGIN_URI))
    {
      String method = request.getMethod();
      if (method.equals("GET"))
      {
        String userId = getUserId(request);
        if (userId != null)
        {
          String password = getPassword(request);
          if (password != null)
          {
            if (!userSessionBean.getUserId().equals(userId))
            {
              try
              {
                userSessionBean.login(userId, password);
                // login successfull, redirect to destination page
                redirectURI = GO_URI;
              }
              catch (Exception ex)
              {
                // login failed, redirect to login form
                redirectURI = LOGIN_URI;
              }
            }
          }
        }
      }
    }
    if (redirectURI != null)
    {
      // redirect to URL without password in secure mode
      String url = HttpUtils.getServerSecureURL(request, redirectURI,
        request.getQueryString());
      response.sendRedirect(url);
      context.responseComplete();
    }
  }
  
  private String getUserId(HttpServletRequest request)
  {
    return request.getParameter(SecurityConstants.USERID_PARAMETER);
  }

  private String getPassword(HttpServletRequest request)
  {
    return request.getParameter(SecurityConstants.PASSWORD_PARAMETER);
  }
  
  private void loginFromCertificate(FacesContext context,
     UserSessionBean userSessionBean) throws Exception
  {
    if (!userSessionBean.isCertificateUser())
    {
      ExternalContext externalContext = context.getExternalContext();
      HttpServletRequest request =
        (HttpServletRequest)externalContext.getRequest();

      String method = request.getMethod();
      if (method.equals("GET"))
      {
        if (HttpUtils.isSecure(request))
        {
          int port = HttpUtils.getServerPort(request);
          if (port == clientSecurePort)
          {
            try
            {
              userSessionBean.loginCertificate();
            }
            catch (Exception ex)
            {
              createView(context, userSessionBean, request);
              userSessionBean.showLoginPage(ex);
            }
          }
        }
      }
    }
  }

  private void redirectSecure(FacesContext context, 
    UserSessionBean userSessionBean, MenuItemCursor menuItem) throws Exception
  {
    userSessionBean.getMenuModel().setAllVisible(true);

    String certificateRequired = menuItem.getCertificateRequired();
    if (certificateRequired != null)
    {
      ExternalContext externalContext = context.getExternalContext();
      HttpServletRequest request =
       (HttpServletRequest)externalContext.getRequest();
      HttpServletResponse response =
        (HttpServletResponse)externalContext.getResponse();
      if (MenuModel.SERVER_CERTIFICATE.equals(certificateRequired) &&
          !HttpUtils.isSecure(request))
      {
        String url = HttpUtils.getServerSecureURL(request, GO_URI,
          request.getQueryString());
        response.sendRedirect(url);
        context.responseComplete();
      }
      else if (MenuModel.CLIENT_CERTIFICATE.equals(certificateRequired) &&
               HttpUtils.getServerPort(request) != clientSecurePort)
      {
        String url = HttpUtils.getClientSecureURL(request, GO_URI,
          request.getQueryString());
        response.sendRedirect(url);
        context.responseComplete();
      }
    }
    userSessionBean.getMenuModel().setAllVisible(false);
  }
    
  private void requestAuthentication(FacesContext context,
    UserSessionBean userSessionBean, MenuItemCursor menuItem) throws Exception
  {
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();
    HttpServletResponse response =
      (HttpServletResponse)externalContext.getResponse();

    userSessionBean.getMenuModel().setAllVisible(true);

    if (mustLogin(request, userSessionBean, menuItem))
    {
      if (HttpUtils.isSecure(request))
      {
        createView(context, userSessionBean, request);
        String messageId = null;
        String uri = request.getRequestURI();
        if (!uri.startsWith(LOGIN_URI) && !userSessionBean.isAnonymousUser())
          messageId = "ACCESS_DENIED";
        userSessionBean.showLoginPage(messageId);
      }
      else
      {
        // redirect to secure mode
        String url = HttpUtils.getServerSecureURL(request, 
          request.getRequestURI(), request.getQueryString());
        response.sendRedirect(url);
        context.responseComplete();
      }
    }
    userSessionBean.getMenuModel().setAllVisible(false);
  }
  
  private boolean mustLogin(HttpServletRequest request,
    UserSessionBean userSessionBean, MenuItemCursor menuItem)
  {
    String uri = request.getRequestURI();
    if (uri.startsWith(LOGIN_URI))
    {
      String userId = getUserId(request);
      if (userId == null) return true;
      else return !userSessionBean.getUserId().equals(userId);
    }
    else
    {
      return !isAccessibleMenuItem(menuItem, userSessionBean);
    }
  }  
  
  private boolean isAccessibleMenuItem(MenuItemCursor menuItem,
    UserSessionBean userSessionBean)
  {
    List<String> accessRoles = menuItem.getAccessRoles();
    return accessRoles.isEmpty() ||
      userSessionBean.isCmsAdministrator() ||
      userSessionBean.isUserInRole(accessRoles);
  }
  
  private void redirectByUrl(FacesContext context, 
    UserSessionBean userSessionBean, MenuItemCursor menuItem) throws Exception
  {
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();
        
    int redirectionCount = getRedirectionCount(request);    
    if (redirectionCount >= redirectionLimit) return; 
    //no more redirections allowed
    
    userSessionBean.getMenuModel().setAllVisible(true);
    String action = menuItem.getAction();
    if (MenuUtils.URL_ACTION.equals(action))
    {
      String url = menuItem.getURL();
      if (url != null && (url.startsWith("/") || url.startsWith("http://") || 
        url.startsWith("https://")))
      {
        url = getRedirectionUrl(url, redirectionCount + 1);
        HttpServletResponse response =
          (HttpServletResponse)externalContext.getResponse();        
        response.sendRedirect(url);
        context.responseComplete();
      }
    }
    userSessionBean.getMenuModel().setAllVisible(false);
  }

  private int getRedirectionCount(HttpServletRequest request)
  {
    try
    {
      String redirectionCount = request.getParameter(REDIR_COUNT_PARAM);
      return (redirectionCount != null ? 
        Math.max(0, Integer.valueOf(redirectionCount)) : 0);
    }
    catch (NumberFormatException ex)
    {
      return 0;
    }
  }

  private String getRedirectionUrl(String url, int redirectionCount)
  {
    if (url.contains("?"))
    {
      return url + "&" + REDIR_COUNT_PARAM + "=" + redirectionCount;
    }
    else    
    {
      return url + "?" + REDIR_COUNT_PARAM + "=" + redirectionCount;
    }
  }
  
  private void userChangeDetection(FacesContext context,
    UserSessionBean userSessionBean)
  {
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();

    String pageUserId = request.getParameter(PAGE_USERID_PARAM);
    if (pageUserId != null && !userSessionBean.getUserId().equals(pageUserId))
    {
      // user changed due to session lost, session recreation or relogin
      userSessionBean.redirectSelectedMenuItem();
    }
  } 
  
  private boolean isPathException(HttpServletRequest request)
  {
    String servletPath = request.getServletPath();
    if (servletPath != null)
      return pathExceptions.contains(servletPath);
    else
      return false;
  }
  
}
