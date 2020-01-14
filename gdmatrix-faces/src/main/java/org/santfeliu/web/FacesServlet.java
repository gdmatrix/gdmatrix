package org.santfeliu.web;

/**
 *
 * @author realor
 */
import com.sun.faces.lifecycle.RestoreViewPhase;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.matrix.security.SecurityConstants;
import org.santfeliu.faces.menu.model.MenuException;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;

public final class FacesServlet implements Servlet
{
  // Servlet parameter names
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

  // Servlet attributes
  public static final String CONFIG_FILES_ATTR = "javax.faces.CONFIG_FILES";
  public static final String LIFECYCLE_ID_ATTR = "javax.faces.LIFECYCLE_ID";
  public static final String CHARSET_ATTR = "javax.faces.request.charset";
  public static final String SECURE_SESSION_ATTR =
    "org.santfeliu.web.SecureSession";
  public static final String NEXT_MENU_ITEM_ATTR =
    "org.santfeliu.web.nextMenuItem";

  // Servlet constants
  public static final String GO_URI = "/go.faces";
  public static final String LOGIN_URI = "/login.faces";
  public static final String FORCED_LANGUAGE_PROP =
    "org.santfeliu.web.forcedLanguage";
  public static final String CLIENT_SECURE_PORT_PROP =
    "org.santfeliu.web.clientSecurePort";
  
  private Application application;
  private FacesContextFactory facesContextFactory;
  private Lifecycle lifecycle;
  private ServletConfig servletConfig;  
  private int clientSecurePort;
  private String forcedLanguage;
  private final WebAuditor webAuditor = new WebAuditor();

  public FacesServlet()
  {
    application = null;
    facesContextFactory = null;
    lifecycle = null;
    servletConfig = null;
  }

  public ServletConfig getServletConfig()
  {
    return servletConfig;
  }

  @Override
  public String getServletInfo()
  {
    return getClass().getName();
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {
    this.servletConfig = servletConfig;    
    initFacesContextFactory();
    initApplicationFactory();
    initLifecycleFactory();

    forcedLanguage = MatrixConfig.getProperty(FORCED_LANGUAGE_PROP);
    if (forcedLanguage == null) forcedLanguage = "ca";
    
    String value = MatrixConfig.getProperty(CLIENT_SECURE_PORT_PROP);
    if (value != null) clientSecurePort = Integer.parseInt(value);
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
    throws IOException, ServletException
  {
    System.out.println("\n>>>> BEGIN ======================================\n");
    
    HttpServletRequest request = (HttpServletRequest)req;
    HttpServletResponse response = (HttpServletResponse)res;
    HttpSession session = request.getSession();

    // restore request charset encoding
    String charset = (String)session.getAttribute(CHARSET_ATTR);
    if (charset != null) request.setCharacterEncoding(charset);

    // securize session
    enterSecureSession(session, request);

    // create faces context
    FacesContext context = facesContextFactory.getFacesContext(
      servletConfig.getServletContext(), request, response, lifecycle);
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      processFacesRequest(context, userSessionBean);

      if (!context.getResponseComplete())
      {
        response.setHeader(HttpUtils.X_USER_HEADER, userSessionBean.getUserId());
        System.out.println("\n>>>> Render ---------------------------------\n");
        renderFacesResponse(context, userSessionBean);
      }
    }
    catch (Exception e)
    {
      throwException(e);
    }
    finally
    {
      context.release();
    }
    System.out.println("\n>>>> END ========================================\n");
  }

  @Override
  public void destroy()
  {
    application = null;
    facesContextFactory = null;
    lifecycle = null;
    servletConfig = null;
  }

  // ****** private methods ******

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
          for (Entry<String, Object> entry : attributes.entrySet())
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

  private void processFacesRequest(FacesContext context, 
    UserSessionBean userSessionBean) throws Exception
  {
    MenuItemCursor menuItem = getRequestedMenuItem(context, userSessionBean);
    if (menuItem != null) // menuItem execution
    {
      loginFromParameters(context, userSessionBean);
      if (context.getResponseComplete() || context.getRenderResponse()) return;

      loginFromCertificate(context, userSessionBean);
      if (context.getResponseComplete() || context.getRenderResponse()) return;

      redirectSecure(context, userSessionBean, menuItem);
      if (context.getResponseComplete() || context.getRenderResponse()) return;

      requestAuthentication(context, userSessionBean, menuItem);
      if (context.getResponseComplete() || context.getRenderResponse()) return;

      executeRequestedMenuItem(context, userSessionBean, menuItem);
    }
    else // normal faces lifecycle
    {
      userChangeDetection(context, userSessionBean);
      if (context.getResponseComplete()) return;      
      
      lifecycle.execute(context);
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

  private void renderFacesResponse(FacesContext context,
    UserSessionBean userSessionBean) throws Exception
  {
    lifecycle.render(context);

    // save viewRoot Locale
    userSessionBean.setLastPageLanguage(
      context.getViewRoot().getLocale().getLanguage());

    webAuditor.logRequest(userSessionBean, context);
  }

  private MenuItemCursor getRequestedMenuItem(
    FacesContext context, UserSessionBean userSessionBean)
    throws Exception
  {
    ExternalContext externalContext = context.getExternalContext();
    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();

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
          if (mid == null)
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

  private void initFacesContextFactory() throws ServletException
  {
    try
    {
      facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(
        FactoryFinder.FACES_CONTEXT_FACTORY);
    }
    catch (FacesException e)
    {
      throwServletException(e);
    }
  }

  private void initApplicationFactory() throws ServletException
  {
    try
    {
      ApplicationFactory applicationFactory =
        (ApplicationFactory)FactoryFinder.getFactory(
        FactoryFinder.APPLICATION_FACTORY);
      application = applicationFactory.getApplication();
    }
    catch (FacesException e)
    {
      throwServletException(e);
    }
  }

  private void initLifecycleFactory() throws ServletException
  {
    try
    {
      LifecycleFactory lifecycleFactory =
        (LifecycleFactory)FactoryFinder.getFactory(
        FactoryFinder.LIFECYCLE_FACTORY);
      String lifecycleId = servletConfig.getServletContext().
        getInitParameter("javax.faces.LIFECYCLE_ID");
      if (lifecycleId == null)
      {
        lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
      }
      lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
    }
    catch (FacesException e)
    {
      throwServletException(e);
    }
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

  private void createView(FacesContext context,
    UserSessionBean userSessionBean, HttpServletRequest request)
  {
    // create new viewRoot
    String viewId = "/common/util/blank.jsp"; // dummy view
    String renderKitId =
      application.getViewHandler().calculateRenderKitId(context);
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
    UIViewRoot viewRoot = new UIViewRoot();
    viewRoot.setViewId(viewId);
    viewRoot.setRenderKitId(renderKitId);
    viewRoot.setLocale(new Locale(language));
    context.setViewRoot(viewRoot);
  }
  
  private void restoreView(FacesContext context)
  {
    RestoreViewPhase restorePhase = new RestoreViewPhase();
    restorePhase.execute(context);
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
  
  private boolean isAccessibleMenuItem(MenuItemCursor menuItem,
    UserSessionBean userSessionBean)
  {
    List<String> accessRoles = menuItem.getAccessRoles();
    return accessRoles.isEmpty() ||
      userSessionBean.isCmsAdministrator() ||
      userSessionBean.isUserInRole(accessRoles);
  }

  private String getUserId(HttpServletRequest request)
  {
    return request.getParameter(SecurityConstants.USERID_PARAMETER);
  }

  private String getPassword(HttpServletRequest request)
  {
    return request.getParameter(SecurityConstants.PASSWORD_PARAMETER);
  }

  private void throwServletException(FacesException e) throws ServletException
  {
    Throwable rootCause = e.getCause();
    if (rootCause == null)
    {
      throw e;
    }
    else
    {
      throw new ServletException(e.getMessage(), rootCause);
    }
  }

  private void throwException(Exception e)
    throws IOException, ServletException
  {
    Throwable t = e.getCause();
    if (t == null)
    {
      throw new ServletException(e.getMessage(), e);
    }
    if (t instanceof ServletException)
    {
      throw (ServletException) t;
    }
    if (t instanceof IOException)
    {
      throw (IOException) t;
    }
    throw new ServletException(t.getMessage(), t);
  }
}
