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
package org.santfeliu.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.matrix.cms.CMSConstants;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.SecurityConstants;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.cms.CMSListener;
import org.santfeliu.doc.DocumentCache;
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
import org.santfeliu.security.util.StringCipher;
import org.santfeliu.security.web.LoginBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Utilities;
import org.santfeliu.util.script.ActionsScriptClient;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.PageBean;

public final class UserSessionBean extends FacesBean implements Serializable
{
  public static final String ACTION_SCRIPT_PREFIX = "script";

  public static final String FRAME = "frame";
  public static final String TEMPLATE = "template";
  public static final String LAYOUT = "layout";
  public static final String SECTION = "section";
  public static final String THEME = "theme";
  public static final String PRIMEFACES_THEME = "primefacesTheme";

  public static final String LANGUAGE = "language";
  public static final String NODE_CSS = "nodeCSS";
  public static final String INHERIT_NODE_CSS_PATH = "inheritNodeCSSPath";
  public static final String ACTION = "action";
  public static final String ERROR_OUTCOME = "error";
  public static final String BLANK_OUTCOME = "blank";
  public static final String TRANSLATION_ENABLED = "translationEnabled";
  public static final String INTRANET_ROOT = "intranetRoot";

  public static final String DEFAULT_FRAME = "default";
  public static final String DEFAULT_TEMPLATE = "default";
  public static final String DEFAULT_SECTION = "default";
  public static final String DEFAULT_THEME = "default";
  public static final String DEFAULT_FONT_SIZE = "12";
  public static final String DEFAULT_PRIMEFACES_THEME = "smoothness";
  public static final String EDIT_PRIMEFACES_THEME = "saga";

  public static final String MATRIX_INFO_VIEW = "MATRIX_INFO";
  public static final String RENDER_VIEW = "RENDER";
  public static final String EDIT_VIEW = "EDIT";
  public static final String SYSTEM_INFO_VIEW = "SYSTEM_INFO";
  public static final String REDIR_VIEW = "REDIR";

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
  private Boolean representant;
  private String organizationName;
  private String email;
  private Set roles;
  private Map attributes;
  private String workspaceId;
  private String view;
  private String lastPageLanguage;
  private String theme = DEFAULT_THEME;
  private String pfTheme = DEFAULT_PRIMEFACES_THEME;
  private String fontSize = DEFAULT_FONT_SIZE;
  private String browserType;
  private String loginMethod;
  private String logoutAction;

  private Locale viewLocale;

  private UserPreferences userPreferences;
  private Integer failedLoginAttempts;
  private String lastSuccessLoginDateTime;
  private String lastFailedLoginDateTime;
  private String lastIntrusionDateTime;

  private transient String selectedMid;
  private transient MenuModel menuModel;
  private Throwable unhandledError;

  public UserSessionBean()
  {
    System.out.println(">> new UserSessionBean");
    initDefaultWorkspaceId();
    view = RENDER_VIEW;
    attributes = new HashMap();
    viewLocale = FacesContext.getCurrentInstance().
      getExternalContext().getRequestLocale();
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

  public Boolean isRepresentant()
  {
    return representant;
  }

  public void setRepresentant(Boolean representant)
  {
    this.representant = representant;
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

  public boolean isPersistentUser()
  {
    return !SecurityConstants.ANONYMOUS.equals(userId) &&
      !isAutoLoginUser() && !isCertificateUser();
  }

  public Integer getFailedLoginAttempts()
  {
    return failedLoginAttempts;
  }

  public void setFailedLoginAttempts(Integer failedLoginAttempts)
  {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public String getLastSuccessLoginDateTime()
  {
    return lastSuccessLoginDateTime;
  }

  public void setLastSuccessLoginDateTime(String lastSuccessLoginDateTime)
  {
    this.lastSuccessLoginDateTime = lastSuccessLoginDateTime;
  }

  public String getLastFailedLoginDateTime()
  {
    return lastFailedLoginDateTime;
  }

  public void setLastFailedLoginDateTime(String lastFailedLoginDateTime)
  {
    this.lastFailedLoginDateTime = lastFailedLoginDateTime;
  }

  public String getLastIntrusionDateTime()
  {
    return lastIntrusionDateTime;
  }

  public void setLastIntrusionDateTime(String lastIntrusionDateTime)
  {
    this.lastIntrusionDateTime = lastIntrusionDateTime;
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

  public Locale getViewLocale()
  {
    if (viewLocale == null)
    {
      String language = MatrixConfig.getProperty(
        "org.santfeliu.translation.defaultLanguage");
      viewLocale = new Locale(language);
    }
    return viewLocale;
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
    ArrayList<SelectItem> selectItems = new ArrayList<>();

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
    List<String> result = new ArrayList<>();
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

  public Throwable getUnhandledError()
  {
    return unhandledError;
  }

  public void setUnhandledError(Throwable unhandledError)
  {
    this.unhandledError = unhandledError;
  }

  public String getUnhandledErrorTrace()
  {
    if (unhandledError == null) return null;

    StringWriter sw = new StringWriter();
    PrintWriter writer = new PrintWriter(sw);
    unhandledError.printStackTrace(writer);
    return sw.toString();
  }

  public boolean isAdministrator()
  {
    return roles.contains(CMSConstants.MENU_ADMIN_ROLE);
  }

  public boolean isCmsAdministrator()
  {
    return roles.contains(CMSConstants.CMS_ADMIN_ROLE);
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

  public boolean inIntranetNode()
  {
    MenuItemCursor cursor = getMenuModel().getSelectedMenuItem();
    if (!cursor.isNull())
    {
      return "true".equals(cursor.getProperty(INTRANET_ROOT));
    }
    return false;
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

  public void setViewLanguage(String language)
  {
    viewLocale = new Locale(language);
    FacesContext.getCurrentInstance().getViewRoot().setLocale(viewLocale);
  }

  public String getViewLanguage()
  {
    return getViewLocale().getLanguage();
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
    //FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    setViewLanguage(locale.getLanguage());
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

  public String showMatrixInfoView()
  {
    view = MATRIX_INFO_VIEW;
    return "matrix_info";
  }

  public String showRenderView()
  {
    view = RENDER_VIEW;
    executeSelectedMenuItem();
    return null;
  }

  public String showEditView()
  {
    view = EDIT_VIEW;
    return "node_edit";
  }

  public String showSystemInfoView()
  {
    view = SYSTEM_INFO_VIEW;
    return "system_info";
  }

  public String showRedirView()
  {
    view = REDIR_VIEW;
    return "redir_edit";
  }

  public boolean isMatrixInfoViewSelected()
  {
    return MATRIX_INFO_VIEW.equals(view);
  }

  public boolean isRenderViewSelected()
  {
    return RENDER_VIEW.equals(view);
  }

  public boolean isEditViewSelected()
  {
    return EDIT_VIEW.equals(view);
  }

  public boolean isSystemInfoViewSelected()
  {
    return SYSTEM_INFO_VIEW.equals(view);
  }

  public boolean isRedirViewSelected()
  {
    return REDIR_VIEW.equals(view);
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
      try
      {
        ELContext elContext = context.getELContext();
        MethodExpression methodExpression = application.getExpressionFactory()
          .createMethodExpression(elContext, action, String.class, new Class[0]);
        outcome = (String)methodExpression.invoke(elContext, new Object[0]);
      }
      catch (Exception ex)
      {
        unhandledError = ex;
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

    if (isAdministrator() || isCmsAdministrator())
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

    unhandledError = null;

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

    HttpServletRequest request =
      (HttpServletRequest)externalContext.getRequest();

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
    String secret = MatrixConfig.getProperty(
      "org.santfeliu.security.authUserPasswordCipher.secret");
    StringCipher cipher = new StringCipher(secret);
    password = cipher.encrypt(userId);

    User user = UserCache.login(userId, password);
    loadProfile(user);
    getMenuModel().setRoles(roles);
    this.loginMethod = mobilePlatform;
    this.logoutAction = logoutAction;
  }

  public void logout()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    ExternalContext extContext = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();

    // method to execute clean up tasks
    if (logoutAction != null)
    {
      ELContext elContext = context.getELContext();
      MethodExpression methodExpression = application.getExpressionFactory()
        .createMethodExpression(elContext, logoutAction, Void.class, new Class[0]);
      methodExpression.invoke(elContext, new Object[0]);
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

    String logoutURL = HttpUtils.getDefaultURL(request, CMSListener.GO_URI,
      CMSListener.XMID_PARAM + "=" + mid + "&workspaceid=" + getWorkspaceId() +
      "&btype=" + getBrowserType() + "&language=" + getLastPageLanguage());

    System.out.println("Redirecting to " + logoutURL);

    try
    {
      request.getSession().invalidate();
      extContext.redirect(logoutURL);
      context.responseComplete();
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
      CMSListener.GO_URI, CMSListener.XMID_PARAM + "=" +
        getSelectedMid() + "&workspaceid=" + getWorkspaceId());
    }
    else
    {
      redirectURL = HttpUtils.getServerSecureURL(request,
      CMSListener.GO_URI, CMSListener.XMID_PARAM + "=" +
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

  public List<SelectItem> getPrimefacesThemeSelectItems()
  {
    List<SelectItem> items = new ArrayList<>();
    List<String> pfThemes =
      getSelectedMenuItem().getMultiValuedProperty(PRIMEFACES_THEME);

    if (pfThemes.isEmpty())
    {
      pfThemes = Collections.singletonList(DEFAULT_PRIMEFACES_THEME);
    }

    for (String pft : pfThemes)
    {
      String label = pft.substring(0, 1).toUpperCase() + pft.substring(1);
      SelectItem item = new SelectItem(pft, label);
      items.add(item);
    }
    return items;
  }

  public void setPrimefacesTheme(String pfTheme)
  {
    this.pfTheme = pfTheme;
  }

  public String getPrimefacesTheme()
  {
    List<String> pfThemes =
      getSelectedMenuItem().getMultiValuedProperty(PRIMEFACES_THEME);

    String currentTheme;
    if (isEditViewSelected() || isRedirViewSelected())
    {
      currentTheme = EDIT_PRIMEFACES_THEME;
    }
    else if (pfThemes.contains(pfTheme))
    {
      currentTheme = pfTheme;
    }
    else if (!pfThemes.isEmpty())
    {
      currentTheme = pfThemes.get(0);
    }
    else currentTheme = DEFAULT_PRIMEFACES_THEME;

    // load theme resource
    try
    {
      String library = "primefaces-" + currentTheme;
      Resource resource = getFacesContext().getApplication()
        .getResourceHandler().createResource("theme.css", library);
      if (resource != null)
        return currentTheme;
    }
    catch (Exception ex)
    {
      currentTheme = DEFAULT_PRIMEFACES_THEME;
    }
    return currentTheme;
  }

  public void setFontSize(String fontSize)
  {
    this.fontSize = fontSize;
  }

  public String getFontSize()
  {
    return fontSize;
  }

  public void updateFontSize()
  {
    fontSize = getExternalContext().getRequestParameterMap().get("fontSize");
  }

  //Action executed from showObject command in common_script.js
  public String jumpToObject()
  {
    String outcome = null;
    PageBean pageBean = ControllerBean.getCurrentInstance().getPageBean();
    if (pageBean != null)
      outcome = pageBean.jshow();
    return outcome;
  }

  public String getJumpCommand()
  {
    return null;
  }

  //Value set in showObject command in common_script.js
  public void setJumpCommand(String jumpCommand)
  {
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
      ActionsScriptClient client = new ActionsScriptClient();
      client.put("userSessionBean", this);
      client.put("facesContext", getFacesContext());
      client.put("externalContext", getExternalContext());
      client.put("request", getExternalContext().getRequest());
      client.put("application", getApplication());
      result = client.executeAction(action);
    }

    return result;
  }

  public static String toUuid(String id)
  {
    String preStr = "";
    String postStr = "";
    String auxId = id;
    if (auxId.startsWith("/documents/")) //document servlet
    {
      preStr = "/documents/";
      auxId = auxId.substring("/documents/".length());
    }
    if (auxId.contains("?")) //parameters
    {
      postStr = auxId.substring(auxId.indexOf("?"));
      auxId = auxId.substring(0, auxId.indexOf("?"));
    }
    if (auxId.contains("/")) //file name
    {
      postStr = auxId.substring(auxId.indexOf("/")) + postStr;
      auxId = auxId.substring(0, auxId.indexOf("/"));
    }
    try
    {
      Integer.parseInt(auxId); //check if Integer (docId)
      try
      {
        auxId = DocumentCache.getDocument(auxId,
          DocumentConstants.UNIVERSAL_LANGUAGE,
          getCurrentInstance().getCredentials().getUserId(),
          getCurrentInstance().getCredentials().getPassword(),
          60 * 60 * 1000);
      }
      catch (Exception ex)
      {
        return id;
      }
    }
    catch (NumberFormatException ex) //contentId?
    {
      if (!Utilities.isUUID(auxId))
      {
        return id;
      }
    }
    return preStr + auxId + postStr;
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
    representant = user.isRepresentant();
    organizationName = user.getOrganizationName();
    email = user.getEmail();
    roles = user.getRoles();
    failedLoginAttempts = user.getFailedLoginAttempts();
    lastSuccessLoginDateTime = user.getLastSuccessLoginDateTime();
    lastFailedLoginDateTime = user.getLastFailedLoginDateTime();
    lastIntrusionDateTime = user.getLastIntrusionDateTime();
    userPreferences = getUserPreferences();

    try
    {
      String language;
      try
      {
        language = userPreferences.getDefaultLanguage();
      }
      catch (Exception ex)
      {
        language = getLastPageLanguage();
      }
      setViewLanguage(language);
      setLastPageLanguage(language);
    }
    catch (Exception ex) { } // no language defined

    try
    {
      setTheme(userPreferences.getDefaultTheme());
    }
    catch (Exception ex) { } // no theme defined

    try
    {
      setPrimefacesTheme(userPreferences.getPrimefacesTheme());
    }
    catch (Exception ex) { } // no primefaces theme defined

    try
    {
      setFontSize(userPreferences.getFontSize());
    }
    catch (Exception ex) { } // no font size defined
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
    out.writeObject(representant);
    out.writeObject(organizationName);
    out.writeObject(email);
    out.writeObject(roles);
    out.writeObject(attributes);
    out.writeObject(loginMethod);
    out.writeObject(logoutAction);
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
    representant = (Boolean)in.readObject();
    organizationName = (String)in.readObject();
    email = (String)in.readObject();
    roles = (Set)in.readObject();
    attributes = (Map)in.readObject();
    loginMethod = (String)in.readObject();
    logoutAction = (String)in.readObject();
    theme = (String)in.readObject();
    lastPageLanguage = (String)in.readObject();
    workspaceId = (String)in.readObject();
    selectedMid = (String)in.readObject(); // lazy loading
  }


}
