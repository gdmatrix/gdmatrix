package org.santfeliu.web;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.matrix.security.SecurityConstants;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.web.LoginBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.ActionsScriptClient;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ExternalEditable;

public final class UserSessionBean extends FacesBean implements Serializable
{
  public static final String ACTION_SCRIPT_PREFIX = "script";  
  
  public static final String FRAME = "frame";
  public static final String TEMPLATE = "template";
  public static final String LAYOUT = "layout";
  public static final String SECTION = "section";
  public static final String THEME = "theme";
  public static final String LANGUAGE = "language";
  public static final String NODE_CSS = "nodeCSS";  
  public static final String INHERIT_NODE_CSS_PATH = "inheritNodeCSSPath";
  public static final String ACTION = "action";
  public static final String ERROR_OUTCOME = "error";
  public static final String BLANK_OUTCOME = "blank";
  public static final String TRANSLATION_ENABLED = "translationEnabled";
  
  public static final String DEFAULT_FRAME = "default";
  public static final String DEFAULT_TEMPLATE = "default";
  public static final String DEFAULT_SECTION = "default";
  public static final String DEFAULT_THEME = "default";

  public static final String MENU_ADMIN_ROLE = "WEBMASTER";
  public static final String CMS_ADMIN_ROLE = "CMS_ADMIN";

  public static final String RENDER_VIEW = "RENDER";
  public static final String EDIT_VIEW = "EDIT";
  public static final String CSS_VIEW = "CSS";
  public static final String SYNC_VIEW = "SYNC";
  public static final String SEARCH_VIEW = "SEARCH";
  
  public static final String VIEW_MODE_PARAM = "viewMode";
  public static final String VIEW_MODE_EDIT = "edit";  
  
  private static final String BEAN_NAME = "userSessionBean";
  
  public static final String LOGIN_PASSWORD = "PASSWORD";
  public static final String LOGIN_CERTIFICATE = "CERTIFICATE";
  
  private static List<String> intranetRoles;
  
  private String userId;
  private String password;
  private String displayName;
  private String givenName;
  private String surname;
  private String NIF;
  private String CIF;
  private String organizationName;
  private String email;
  private Set roles;
  private Map attributes;
  private String workspaceId;
  private String view;
  private String lastPageLanguage;
  private String theme = DEFAULT_THEME;
  private String browserType;
  private String loginMethod;
  private String logoutAction;

  private UserPreferences userPreferences;

  private transient String selectedMid;
  private transient MenuModel menuModel;  

  public UserSessionBean()
  {
    System.out.println(">> new UserSessionBean");
    initDefaultWorkspaceId();
    view = RENDER_VIEW;
    attributes = new HashMap();
    autoLogin();    
  }

  public static UserSessionBean getCurrentInstance()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context == null) // not in FacesContext
    {
      return null;
    }
    else
    {
      Application application = context.getApplication();
      return (UserSessionBean)application.getVariableResolver().
        resolveVariable(context, BEAN_NAME);
    }
  }
  
  public static UserSessionBean getInstance(HttpSession session)
  {
    // can return null
    return (UserSessionBean)session.getAttribute(BEAN_NAME);
  }  
  
  /* this method can be called outside faces context */
  public static Credentials getCredentials(HttpServletRequest request)
  {
    Credentials credentials = null;
    HttpSession session = request.getSession(false);
    if (session != null)
    {
      UserSessionBean userSessionBean =
        (UserSessionBean)session.getAttribute(BEAN_NAME);
      if (userSessionBean != null)
      {
        credentials = new Credentials(
          userSessionBean.getUserId(), userSessionBean.getPassword());
      }
    }
    if (credentials == null)
    {
      credentials = new Credentials();
    }
    return credentials;
  }

  //*** UserBean properties ****

  public String getUserId()
  {
    return userId;
  }
  
  public String getUsername()
  {
    return userId;
  }

  public String getPassword()
  {
    return password;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getGivenName()
  {
    return givenName;
  }

  public void setGivenName(String givenName)
  {
    this.givenName = givenName;
  }

  public String getSurname()
  {
    return surname;
  }

  public void setSurname(String surname)
  {
    this.surname = surname;
  }

  public String getNIF()
  {
    return NIF;
  }

  public void setNIF(String NIF)
  {
    this.NIF = NIF;
  }

  public String getCIF()
  {
    return CIF;
  }

  public void setCIF(String CIF)
  {
    this.CIF = CIF;
  }

  public String getOrganizationName()
  {
    return organizationName;
  }

  public void setOrganizationName(String organizationName)
  {
    this.organizationName = organizationName;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public Set getRoles()
  {
    return roles;
  }

  public String getViewMode()
  {
    return view;
  }

  public void setViewMode(String viewMode)
  {
    this.view = viewMode;
  }

  public String getBrowserType()
  {
    return browserType;
  }

  public void setBrowserType(String browserType)
  {
    if ("desktop".equals(browserType) || "mobile".equals(browserType))
    {
      this.browserType = browserType;
    }
  }

  public Credentials getCredentials()
  {
    return new Credentials(userId, password);
  }

  public String getLoginMethod()
  {
    return loginMethod;
  }
  
  public boolean isAnonymousUser()
  {
    return SecurityConstants.ANONYMOUS.equals(userId) ||
      isAutoLoginUser();
  }

  public boolean isCertificateUser()
  {
    return LOGIN_CERTIFICATE.equals(loginMethod);
  }

  public boolean isAuthenticatedUser()
  {
    return userId.startsWith(SecurityConstants.AUTH_USER_PREFIX);
  }
  
  public boolean isAutoLoginUser()
  {
    String autoLoginUserId =
      MatrixConfig.getProperty("org.santfeliu.web.autoLogin.userId");
    return userId.equals(autoLoginUserId);
  }
  
  public boolean isMatrixClientEnabled()
  {
    return isUserInRole("MATRIX_CLIENT");
  }

  public String getRemoteAddress()
  {
    ExternalContext externalContext = 
      FacesContext.getCurrentInstance().getExternalContext();
    HttpServletRequest request = 
      (HttpServletRequest)externalContext.getRequest();
    
    return HttpUtils.getRemoteAddress(request);
  }
  
  public MenuModel getMenuModel()
  {
    if (menuModel == null) createMenuModel(workspaceId, selectedMid);
    return menuModel;
  }

  public MenuItemCursor getSelectedMenuItem()
  {
    return getMenuModel().getSelectedMenuItem();
  }

  public String getSelectedMid()
  {
    return getMenuModel().getSelectedMid();
  }

  public void setSelectedMid(String mid)
  {
    getMenuModel().setSelectedMid(mid);
  }

  public String getWorkspaceId()
  {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId)
  {
    if (!workspaceId.equals(this.workspaceId))
    {
      CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
      if (cmsCache.getWorkspace(workspaceId) != null)
      {
        this.workspaceId = workspaceId;
        if (menuModel != null)
        {
          selectedMid = menuModel.getSelectedMid();
        }
        createMenuModel(workspaceId, selectedMid, false); // update menuModel
      }
    }
  }

  public boolean isDefaultWorkspaceSelected()
  {
    String defaultWorkspaceId =
      MatrixConfig.getProperty("org.santfeliu.web.defaultWorkspaceId");
    if (defaultWorkspaceId == null)
    {
      return false;
    }
    else
    {
      return defaultWorkspaceId.equals(getWorkspaceId());
    }
  }

  /**
   * Returns de frame name defined in forest or 'default' if not defined.
   * @return the frame name.
   */
  public String getFrame()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    String frame = (String)cursor.getProperties().get(FRAME);
    if (frame == null) frame = DEFAULT_FRAME;

    return frame;
  }

  public String getTemplate()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    String template = cursor.getBrowserSensitiveProperty(TEMPLATE);
    if (template == null) 
    {
      template = (String)cursor.getProperties().get(SECTION);
    }
    if (template == null) template = DEFAULT_TEMPLATE;
    return template;
  }

  public String getLayout()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    return (String)cursor.getProperties().get(LAYOUT);
  }
  /**
   * Returns de section name defined in forest or 'default' if not defined.
   * @return the section name.
   */
  public String getSection()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    String section = (String)cursor.getProperties().get(SECTION);
    if (section == null) section = DEFAULT_SECTION;
    
    return section;
  }

  public void setTheme(String theme)
  {
    this.theme = theme;
  }

  public String getTheme()
  {
    List<String> themes = getSelectedMenuItem().getMultiValuedProperty(THEME);
    
    if (themes.isEmpty() || themes.contains(theme)) return theme;  
    
    Iterator iter = themes.iterator();
    if (iter.hasNext())
    {
      return (String)iter.next();
    }
    else
    {
      return theme;
    }
  }

  public UserPreferences getUserPreferences()
  {
    if (userPreferences == null)
    {
      userPreferences = new UserPreferences(userId);
    }
    return userPreferences;
  }

  public void setUserPreferences(UserPreferences userPreferences)
  {
    this.userPreferences = userPreferences;
  }

  public List<SelectItem> getThemes()
  {
    ArrayList<SelectItem> selectItems = new ArrayList<SelectItem>();

    List<String> themes = getSelectedMenuItem().getMultiValuedProperty(THEME);

    for (String th : themes)
    {      
      if (th != null)
      {
        if (th.length() > 0)
        {
          selectItems.add(new SelectItem(th));
        }
      }
    }
    return selectItems;
  }

  public boolean isThemeSelectionEnabled()
  {
    List<String> themes = getSelectedMenuItem().getMultiValuedProperty(THEME);

    return themes.size() > 1;    
  }

  public List<String> getNodeCSS()
  {
    List<String> result = new ArrayList<String>();
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    if (isInheritNodeCSSPath())
    {
      while (!cursor.isNull())
      {
        String id = (String)cursor.getDirectProperty(NODE_CSS);
        if (id != null)
        {
          String nodeCSS = "/documents/" + id;
          result.add(0, nodeCSS);
        }
        cursor = cursor.getParent();
      }
    }
    else
    {
      // nodeCSS is the docId or UUID of a css document
      String id = (String)cursor.getProperties().get(NODE_CSS);
      if (id != null)
      {
        // TODO: depends on document servlet mapping
        String nodeCSS = "/documents/" + id;
        result.add(nodeCSS);
      }
    }
    if (result.isEmpty()) //use default node.css
    {
      String template = getTemplate();
      result.add("/templates/" + template + "/css/node.css?v=" + 
        ApplicationBean.getCurrentInstance().getResourcesVersion());
    }
    return result;
  }

  public boolean isNodeWithAction()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (!cursor.isNull())
    {
      String action = (String)cursor.getAction();
      return (action != null);
    }
    return false;
  }

  public boolean isNodeWithCSS()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (!cursor.isNull())
    {
      String action = (String)cursor.getDirectProperties().get(NODE_CSS);
      return (action != null);
    }
    return false;
  }

  public boolean isAdministrator()
  {
    return roles.contains(MENU_ADMIN_ROLE);
  }

  public boolean isCmsAdministrator()
  {
    return roles.contains(CMS_ADMIN_ROLE);
  }

  public boolean isIntranetUser()
  {
    return isUserInRole(getIntranetRoles()) || isAutoLoginUser();
  }
  
  public List<String> getIntranetRoles()
  {
    if (intranetRoles == null)
    {
      String rolesString =
        MatrixConfig.getProperty("org.santfeliu.web.intranetRoles");
      if (rolesString == null)
      { 
        intranetRoles = Collections.EMPTY_LIST;
      }
      else
      {
        String[] rolesArray = rolesString.split(",");
        intranetRoles = Arrays.asList(rolesArray);
      }
    }
    return intranetRoles;
  }
  
  public HttpSession getSession()
  {
    return (HttpSession)getExternalContext().getSession(true);
  }
  
  /**
   * 
   * @param role
   * @return true if user is in role <code>role</code>, or false otherwise.
   */
  public boolean isUserInRole(String role)
  {
    return roles.contains(role);
  }

  /**
   *
   * @param role
   * @return true if user is in any role of <code>checkRoles</code>, or false
   * otherwise.
   */
  public boolean isUserInRole(Collection checkRoles)
  {
    if (checkRoles == null) return false;
    boolean isInRole = false;
    Iterator<String> iter = checkRoles.iterator();
    while (!isInRole && iter.hasNext())
    {
      String role = iter.next();
      isInRole = roles.contains(role);
    }
    return isInRole;
  }

  /* Translation methods */

  public String getViewLanguage()
  {
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    return viewRoot.getLocale().getLanguage();
  }

  public String getLastPageLanguage()
  {
    return lastPageLanguage;
  }

  public void setLastPageLanguage(String lastPageLanguage)
  {
    this.lastPageLanguage = lastPageLanguage;
  }

  public boolean isLanguageSelectionEnabled()
  {
    List<String> languages =
      getSelectedMenuItem().getMultiValuedProperty(LANGUAGE);

    return languages.size() > 1;
  }

  public List<Locale> getSupportedLocales()
  {
    List<Locale> locales =
      ApplicationBean.getCurrentInstance().getSupportedLocales();

    List<Locale> locales2 = new ArrayList<Locale>();

    List<String> languages =
      getSelectedMenuItem().getMultiValuedProperty(LANGUAGE);

    for (String lang : languages)
    {
      if (lang != null)
      {
        if (lang.length() > 0)
        {
          Locale locale = new Locale(lang);
          if (locales.contains(locale))
          {
            locales2.add(locale);
          }
        }
      }
    }
    if (locales2.size() > 0)
    {
      locales = locales2;
    }
    return locales;
  }

  public String getCurrentLocaleDescription()
  {
    Locale currentLocale =
      FacesContext.getCurrentInstance().getViewRoot().getLocale();
    return getLocaleDescription(currentLocale);
  }

  public String getLocaleRowDescription()
  {
    Locale locale = (Locale)getExternalContext().getRequestMap().get("locale");
    return getLocaleDescription(locale);
  }

  public List<Locale> getUnselectedLocales()
  {
    Locale currentLocale =
      FacesContext.getCurrentInstance().getViewRoot().getLocale();
    List<Locale> auxLocales = new ArrayList(getSupportedLocales());
    auxLocales.remove(currentLocale);
    return auxLocales;
  }

  public void changeLanguage()
  {
    Locale locale = (Locale)getExternalContext().getRequestMap().get("locale");
    FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    FacesContext.getCurrentInstance().renderResponse();
  }

  public Translator getTranslator()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull())
    {
      cursor = getMenuModel().getRootMenuItem();
    }
    String translationEnabled =
      (String)cursor.getProperties().get(TRANSLATION_ENABLED);
    if ("true".equalsIgnoreCase(translationEnabled))
    {
      return ApplicationBean.getCurrentInstance().getTranslator();
    }
    return null;
  }

  public String getTranslationGroup()
  {
    return "jsp:" + getSelectedMid();
  }

  public String translate(String text)
  {
    return translate(text, getTranslationGroup());
  }

  public String translate(String text, String group)
  {
    Translator translator = getTranslator();
    if (translator != null)
    {
      try
      {
        String userLanguage = getViewLanguage();
        StringWriter sw = new StringWriter();
        translator.translate(new StringReader(text), sw, "text/plain",
          userLanguage, group);
        return sw.toString();
      }
      catch (Exception ex)
      {
      }
    }
    return text;
  }
  
  public String translateProperty(String propertyName)
  {
    return translateProperty(propertyName, getTranslationGroup());
  }

  public String translateProperty(String propertyName, String group)
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    return translateProperty(propertyName, group, cursor);
  }  
  
  public String translateProperty(String propertyName, String group, 
    MenuItemCursor cursor)
  {
    String label = (String)cursor.getProperties().get(propertyName);
    if (label != null)
    {
      label = translate(label, group);
    }
    return label;
  }  
  
  public String getTranslatedTitleProperty()
  {
    MenuItemCursor cursor = (MenuItemCursor)getRequestMap().get("item");
    if (cursor != null)
    {
      return translateProperty("title", getTranslationGroup(), cursor);
    }
    return null;
  }
  
  /* session attributes */

  public void setAttributes(Map attributes)
  {
    this.attributes = attributes;
  }

  public Map getAttributes()
  {
    return attributes;
  }

  public void setAttribute(String name, Object value)
  {
    attributes.put(name, value);
  }

  public Object getAttribute(String name)
  {
    return attributes.get(name);
  }
  
  public String getSystemTime()
  {
    Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    SimpleDateFormat df = new SimpleDateFormat("EEEE, d/M/yyyy", locale);
    return df.format(new java.util.Date());
  }

  @Override
  public String getContextURL()
  {
    return super.getContextURL();
  }

  public String getServerAddress()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    return request.getLocalAddr();
  }
  
  public boolean isRenderDisabled()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull()) return true;
    String action = cursor.getAction();
    return (MenuUtils.URL_ACTION.equals(action));
  }

  /**** JSF actions ****/

  public String showRenderView()
  {
    view = RENDER_VIEW;
    executeSelectedMenuItem();
    return null;
  }

  public String showEditView()
  {
    if (isRenderViewSelected())
    {
      view = EDIT_VIEW;
      return "node_edit";
    }
    else
    {
      view = EDIT_VIEW;
      return null;
    }
  }

  public String showCssView()
  {
    if (isRenderViewSelected())
    {
      view = CSS_VIEW;
      return "node_edit";
    }
    else
    {
      view = CSS_VIEW;
      return null;
    }
  }

  public String showSyncView()
  {
    if (isRenderViewSelected())
    {
      view = SYNC_VIEW;
      return "node_edit";
    }
    else
    {
      view = SYNC_VIEW;
      return null;
    }
  }

  public String showSearchView()
  {
    if (isRenderViewSelected())
    {
      view = SEARCH_VIEW;
      return "node_edit";
    }
    else
    {
      view = SEARCH_VIEW;
      return null;
    }
  }

  public boolean isRenderViewSelected()
  {
    return RENDER_VIEW.equals(view);
  }

  public boolean isEditViewSelected()
  {
    return EDIT_VIEW.equals(view);
  }

  public boolean isCssViewSelected()
  {
    return CSS_VIEW.equals(view);
  }

  public boolean isSyncViewSelected()
  {
    return SYNC_VIEW.equals(view);
  }

  public boolean isSearchViewSelected()
  {
    return SEARCH_VIEW.equals(view);
  }

  /**** special methods ****/

  public void executeAction(String action)
  {
    String outcome = null;
    String fromAction = null;

    FacesContext context = getFacesContext();
    Application application = context.getApplication();
    if (action == null)
    {
      outcome = BLANK_OUTCOME; // show blank page
    }
    else if (action.startsWith("#{") && action.endsWith("}"))
    {
      MethodBinding methodBinding = application.
        createMethodBinding(action, new Class[0]);
      fromAction = methodBinding.getExpressionString();
      try
      {
        outcome = (String)methodBinding.invoke(context, null);
      }
      catch (Exception ex)
      {
        outcome = ERROR_OUTCOME;
      }
    }
    else if (action.startsWith(ACTION_SCRIPT_PREFIX + ":"))
    {
      try
      {
        Object result = executeScriptAction(action);
        if (result != null)
          outcome = (String)result;
      }
      catch (Exception ex)
      {
        outcome = null;
        error(ex);
      }
    }
    else if (action.equals(MenuUtils.URL_ACTION))
    {
      outcome = null;
      // The execution of an url action has
      // the same efect than selecting it.
    }
    else
    {
      outcome = action; // action is a outcome
    }
    System.out.println(">>>>> Action outcome=" + outcome);
    NavigationHandler navigationHandler = application.getNavigationHandler();
    navigationHandler.handleNavigation(context,
                                       fromAction,
                                       outcome);
    context.renderResponse();
  }

  public void executeSelectedMenuItem()
  {
    executeMenuItem(getMenuModel().getSelectedMenuItem());
  }

  public void executeMenuItem(MenuItemCursor menuItem)
  {
    if (menuItem == null || menuItem.isNull())
      menuItem = getMenuModel().getRootMenuItem();

    if (isAdministrator())
    {
      FacesContext facesContext = getFacesContext();
      HttpServletRequest request = 
        (HttpServletRequest)facesContext.getExternalContext().getRequest();
      boolean editMode = 
        VIEW_MODE_EDIT.equals((String)request.getParameter(VIEW_MODE_PARAM));
      if (editMode)
      {
        menuItem.select();
        showEditView();
        NavigationHandler navigationHandler = 
          facesContext.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(facesContext, null, "node_edit");
        facesContext.renderResponse();
        return;
      }      
    }
    
    // skip menuItems with null action if possible.
    menuItem = getExecutableMenuItem(menuItem);
    if (menuItem.getAction() == null)
    {
      MenuItemCursor rootMenuItem = getMenuModel().getRootMenuItem();
      menuItem = getExecutableMenuItem(rootMenuItem);
    }
    
    // TODO: reference to ControllerBean!!
    ControllerBean controllerBean = ControllerBean.getCurrentInstance();
    if (controllerBean.getSearchBean(menuItem) != null)
    {
      MenuItemCursor selectedMenuItem = getSelectedMenuItem();
      String oldTypeId = controllerBean.getActualTypeId(selectedMenuItem);
      controllerBean.getPageHistory().visit(menuItem.getMid(), null, oldTypeId);
    }

    String action = menuItem.getAction();

    System.out.println(">>>>> Executing mid:" + menuItem.getMid() +
      " action:" + action);
    
    // select MenuItem
    menuItem.select();

    setViewMode(UserSessionBean.RENDER_VIEW);

    // execute action of menuItem
    executeAction(action);
  }

  public void showLoginPage()
  {
    showLoginPage((String)null);
  }

  public void showLoginPage(Exception ex)
  {
    showLoginPage(ex.getMessage());
  }

  public void showLoginPage(String messageId)
  {
    LoginBean loginBean = (LoginBean)getBean("loginBean");
    if (messageId != null)
    {
      FacesMessage message = FacesUtils.getFacesMessage(messageId, null, 
        FacesMessage.SEVERITY_ERROR);
      loginBean.setLoginMessage(message.getSummary());
    }
    else
    {
      loginBean.setLoginMessage(null);
    }
    String outcome = loginBean.showLogin();

    Application application = getApplication();
    FacesContext context = getFacesContext();
    NavigationHandler navigationHandler = application.getNavigationHandler();
    navigationHandler.handleNavigation(context, null, outcome);
    context.renderResponse();
  }

  public void login(String userId, String password)
    throws Exception
  {
    User user = UserCache.login(userId, password);
    loadProfile(user);
    getMenuModel().setRoles(roles);
    loginMethod = userId.startsWith(SecurityConstants.AUTH_USER_PREFIX) ?
      LOGIN_CERTIFICATE : LOGIN_PASSWORD;
  }

  public void loginCertificate() throws Exception
  {
    ExternalContext externalContext = 
      FacesContext.getCurrentInstance().getExternalContext();

    HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();
    
    byte[] cert = HttpUtils.getUserCertificate(request);

    User user = UserCache.loginCertificate(cert);
    loadProfile(user);
    getMenuModel().setRoles(roles);
    loginMethod = LOGIN_CERTIFICATE;
  }
  
  public void loginMobile(String idNumber, String mobilePlatform) 
    throws Exception
  {
    loginMobile(idNumber, mobilePlatform, null);
  }
  
  public void loginMobile(String idNumber, String mobilePlatform, 
    String logoutAction) throws Exception
  {
    userId = SecurityConstants.AUTH_USER_PREFIX + idNumber;
    password = MatrixConfig.getProperty(
      "org.santfeliu.security.service.SecurityManager.masterPassword");
    User user = UserCache.login(userId, password);
    loadProfile(user);
    getMenuModel().setRoles(roles);
    this.loginMethod = mobilePlatform;
    this.logoutAction = logoutAction;
  }

  public void logout()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ExternalContext extContext = facesContext.getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    
    // method to execute clean up tasks
    if (logoutAction != null)
    {
      MethodBinding methodBinding = getApplication().
        createMethodBinding(logoutAction, new Class[0]);
      methodBinding.invoke(facesContext, new Object[0]);
    }

    // redirect to page
    String mid;
    MenuItemCursor menuItem = getSelectedMenuItem();
    if (MenuModel.CLIENT_CERTIFICATE.equals(menuItem.getCertificateRequired()))
    {
      mid = menuModel.getRootMid();
    }
    else
    {
      mid = menuItem.getMid();
    }
    
    String logoutURL = HttpUtils.getDefaultURL(request, FacesServlet.GO_URI,
      FacesServlet.XMID_PARAM + "=" + mid + "&workspaceid=" + getWorkspaceId() + 
      "&btype=" + getBrowserType() + "&language=" + getLastPageLanguage());
    
    System.out.println("Redirecting to " + logoutURL);

    try
    {
      request.getSession().invalidate();
      extContext.redirect(logoutURL);
      facesContext.responseComplete();
    }
    catch (Exception ex)
    {
    }
  }

  public void redirectSelectedMenuItem()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ExternalContext extContext = facesContext.getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    // redirect to current node
    String redirectURL;
    if (isAnonymousUser())
    {
      redirectURL = HttpUtils.getDefaultURL(request,
      FacesServlet.GO_URI, FacesServlet.XMID_PARAM + "=" +
        getSelectedMid() + "&workspaceid=" + getWorkspaceId());
    }
    else
    {
      redirectURL = HttpUtils.getServerSecureURL(request,
      FacesServlet.GO_URI, FacesServlet.XMID_PARAM + "=" +
        getSelectedMid() + "&workspaceid=" + getWorkspaceId());
    }
    try
    {
      System.out.println(">>> Redirecting to " + redirectURL);
      extContext.redirect(redirectURL);
      facesContext.responseComplete();
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public String toString()
  {
    return "UserSessionBean(" + userId + ")";
  }

  public boolean isFacesMessagesQueued()
  {
    return getFacesContext().getMessages().hasNext();
  }
  
  public void initDefaultWorkspaceId()
  {
    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    workspaceId = applicationBean.getDefaultWorkspaceId();
  }

  public IconMap getIcons()
  {
    String iconsPath = "/themes/" + getTheme() + "/images/";
    return new IconMap(iconsPath);
  }

  public IconMap getObjectIcons()
  {
    String iconsPath = "/themes/" + getTheme() + "/images/object/";
    return new IconMap(iconsPath);
  }
  
  public void clearWSCallCaches()
  {    
    FeedManagerClient.getCache().clear();
    NewsManagerClient.getCache().clear();
    AgendaManagerClient.getCache().clear();
  }
  
  
  public void setDesktopBrowserType()
  {
    changeBrowserTypeTo("desktop");
  }
  
  public void setMobileBrowserType()
  {
    changeBrowserTypeTo("mobile");    
  }
  
  /**** private methods ****/

  private void changeBrowserTypeTo(String browserType)
  {
    setBrowserType(browserType);
    try
    {
      FacesContext.getCurrentInstance().getExternalContext().redirect(
        "/go.faces?xmid=" + menuModel.getSelectedMid());      
    }
    catch (Exception ex)
    {
    }
  }

  private boolean isInheritNodeCSSPath()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (cursor.isNull()) return true;
    String value = cursor.getProperty(INHERIT_NODE_CSS_PATH);
    return (value == null ? true : "true".equals(value));
  }

  private void loginAsAnonymous()
  {
    userId = SecurityConstants.ANONYMOUS;
    password = "";
    displayName = userId;
    givenName = null;
    surname = null;
    NIF = null;
    CIF = null;
    organizationName = null;
    email = null;
    attributes = new HashMap();
    roles = new HashSet();
    roles.add(SecurityConstants.EVERYONE_ROLE);
    roles.add(SecurityConstants.SELF_ROLE_PREFIX +
      SecurityConstants.ANONYMOUS + SecurityConstants.SELF_ROLE_SUFFIX);
    getMenuModel().setRoles(roles);
  }

  private void autoLogin()
  {
    String ipAddress = getRemoteAddress();
    System.out.println("autoLogin =>" + ipAddress);
    String ipPattern =
      MatrixConfig.getProperty("org.santfeliu.web.autoLogin.ipPattern");
    if (ipAddress.matches(ipPattern))
    {
      String autoLoginUserId =
        MatrixConfig.getProperty("org.santfeliu.web.autoLogin.userId");
      String passw =
        MatrixConfig.getProperty("org.santfeliu.web.autoLogin.password");
      try
      {
        User user = UserCache.getUser(autoLoginUserId, passw);
        loadProfile(user);
        getMenuModel().setRoles(roles);
      }
      catch (Exception ex)
      {
        System.out.println("Invalid autoLogin for: " + autoLoginUserId);
        loginAsAnonymous();
      }
    }
    else
    {
      loginAsAnonymous();
    }
  }

  private void createMenuModel(String workspaceId, String selectedMid)
  {
    createMenuModel(workspaceId, selectedMid, true); // fast update menuModel
  }

  private void createMenuModel(String workspaceId, String selectedMid,
    boolean fastUpdate)
  {
    System.out.println(">>> creating menuModel for " + workspaceId + "/" + selectedMid);
    // create new MenuModel
    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    if (applicationBean == null) return;

    menuModel = applicationBean.createMenuModel(workspaceId);
    if (menuModel == null)
      throw new RuntimeException("MenuModel creation failed");
    
    FacesContext facesContext = getFacesContext();
    HttpServletRequest request = 
      (HttpServletRequest)facesContext.getExternalContext().getRequest();
    
    if (browserType == null)
    {
      String navMode = 
        UserAgentDetector.isMobile(request) ? "mobile" : "desktop";
      setBrowserType(navMode);      
    }
        
    menuModel.setRoles(roles);
    menuModel.setIpAddress(getRemoteAddress());
    if (fastUpdate)
    {
      menuModel.getCWorkspace().fastPurge();
    }
    else
    {
      menuModel.getCWorkspace().purge();
    }

    // set selectedMenuItem
    if (selectedMid == null)
    {
      // rootMenuItem must be visible for everyone
      menuModel.setSelectedMid(menuModel.getRootMid());
    }
    else
    {
      // verify access to selectedMenuItem
      MenuItemCursor cursor = menuModel.getMenuItem(selectedMid);
      if (cursor.isNull()) // not visible for this user
      {
        menuModel.setSelectedMid(menuModel.getRootMid());
      }
      else
      {
        menuModel.setSelectedMid(selectedMid);
      }
    }
  }

  private MenuItemCursor getExecutableMenuItem(MenuItemCursor menuItem)
  {
    boolean stop = false;

    while (!stop)
    {
      if (menuItem.getAction() == null)
      {
        if (menuItem.hasChildren())
        {
          menuItem = menuItem.getFirstChild();
        }
        else stop = true;
      }
      else // menuItem executable
      {
        stop = true;
      }
    }
    return menuItem;
  }

  private void loadProfile(User user)
  {
    userId = user.getUserId();
    password = user.getPassword();
    displayName = user.getDisplayName();
    if (displayName == null) displayName = userId;
    givenName = user.getGivenName();
    surname = user.getSurname();
    NIF = user.getNIF();
    CIF = user.getCIF();
    organizationName = user.getOrganizationName();
    email = user.getEmail();
    roles = user.getRoles();
    userPreferences = null;
    
    try
    {
      String language;    
      try
      {
        language = getUserPreferences().getDefaultLanguage();
      }
      catch (Exception ex)
      {
        language = getLastPageLanguage();
      }
      Locale locale = new Locale(language);
      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      setLastPageLanguage(language);
    }
    catch (Exception ex) { } //no default language defined
    
    try
    {
      setTheme(getUserPreferences().getDefaultTheme());
    }
    catch (Exception ex) { } //no default theme defined
  }

  private String getLocaleDescription(Locale locale)
  {
    return locale.getDisplayLanguage(locale).toLowerCase();
  }  
  
  // Serialization methods

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException
  {
    out.writeObject(userId);
    out.writeObject(password);
    out.writeObject(displayName);
    out.writeObject(givenName);
    out.writeObject(surname);
    out.writeObject(NIF);
    out.writeObject(CIF);
    out.writeObject(organizationName);
    out.writeObject(email);
    out.writeObject(roles);
    out.writeObject(attributes);
    out.writeObject(theme);
    out.writeObject(lastPageLanguage);
    out.writeObject(workspaceId);
    String mid = null;
    if (menuModel != null)
    {
      mid = menuModel.getSelectedMid();
    }
    if (mid == null) mid = "1"; // dummy nodeId
    out.writeObject(mid);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    userId = (String)in.readObject();
    password = (String)in.readObject();
    displayName = (String)in.readObject();
    givenName = (String)in.readObject();
    surname = (String)in.readObject();
    NIF = (String)in.readObject();
    CIF = (String)in.readObject();
    organizationName = (String)in.readObject();
    email = (String)in.readObject();
    roles = (Set)in.readObject();
    attributes = (Map)in.readObject();
    theme = (String)in.readObject();
    lastPageLanguage = (String)in.readObject();
    workspaceId = (String)in.readObject();
    selectedMid = (String)in.readObject(); // lazy loading
  }
  
  public String jumpToObject()
  {
    String outcome = null;
    String[] parts = jumpCommand.split("\\|");
    if (parts[0].equals("type"))
    {
      String typeId = parts[1];
      String objectId = parts[2];
      outcome = ControllerBean.getCurrentInstance().showObject(typeId, objectId);
    }
    else if (parts[0].equals("tab"))
    {
      String mid = parts[1];
      String objectId = parts[2];
      String subObjectId = parts[3];
      outcome = ControllerBean.getCurrentInstance().show(mid, objectId);
      String beanName = getSelectedMenuItem().getProperty("oc.pageBean");
      ExternalEditable editableBean = (ExternalEditable)getBean(beanName);
      editableBean.editObject("new".equals(subObjectId) ? null : subObjectId);
    }
    return outcome;
  }
  
  String jumpCommand;

  public String getJumpCommand()
  {
    return jumpCommand;
  }

  public void setJumpCommand(String jumpCommand)
  {
    this.jumpCommand = jumpCommand;
  }
  
  //Script actions
  String actionToExecute;

  public String getActionToExecute()
  {
    return actionToExecute;
  }

  public void setActionToExecute(String actionToExecute)
  {
    this.actionToExecute = actionToExecute;
  }  
  
  public void executeAction()
  {
    if (this.actionToExecute != null)
      executeAction(actionToExecute);
  }
  
  public Object executeScriptAction(String action)
    throws Exception
  {
    Object result = null;    

    if (action != null)
    {
      ActionsScriptClient client = new ActionsScriptClient(userId, password);
      client.put("userSessionBean", this);
      client.put("facesContext", getFacesContext());
      client.put("externalContext", getExternalContext());
      client.put("request", getExternalContext().getRequest());
      client.put("application", getApplication());
      result = client.executeScript(action);
    }

    return result;    
  }
}
