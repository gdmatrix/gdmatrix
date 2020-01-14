package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class ProcedureCatalogueBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String CATALOGUE_MID_PROPERTY = "catalogueMid";
  @CMSProperty
  public static final String EXPAND_LEVELS_PROPERTY = "expandLevels";
  @CMSProperty
  public static final String INSTANCE_MID_PROPERTY = "instanceMid";
  public static final String ACCESS_TOKEN_PARAM = "access_token";
  public static final String LOGIN_ACCESS_TOKEN_SEPARATOR = "$";

  // Catalogue properties
  public static final String TRANSACT = "tramitar";
  public static final String SIMULATE = "simular";
  public static final String DOCUMENT = "document";
  public static final String WORKFLOW = "workflow";
  public static final String CERTIFICATE = "certificate";
  public static final String SIGNATURE = "signature";
  public static final String URL = "url";

  private Set expandedMenuItems;

  public ProcedureCatalogueBean()
  {
  }

  public void setExpandedMenuItems(Set expandedMenuItems)
  {
    this.expandedMenuItems = expandedMenuItems;
  }

  public Set getExpandedMenuItems()
  {
    return expandedMenuItems;
  }

  public String getCatalogueMid()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();

    String catalogueMid = (String)menuModel.getSelectedMenuItem().
      getProperties().get(CATALOGUE_MID_PROPERTY);
    if (catalogueMid == null)
    {
      catalogueMid = menuModel.getRootMid();
    }
    return catalogueMid;
  }

  public boolean isCertificateRequired()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return false; 
    return "true".equals(cursor.getDirectProperties().get(CERTIFICATE));
  }

  public boolean isSignatureRequired()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return false;
    return "true".equals(cursor.getDirectProperties().get(SIGNATURE));
  }

  public boolean isTransactEnabled()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return false;
    return "true".equals(cursor.getDirectProperties().get(TRANSACT));
  }

  public boolean isSimulateEnabled()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return false;    
    return "true".equals(cursor.getDirectProperties().get(SIMULATE));
  }

  public boolean isDocumentEnabled()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return false;    
    return cursor.getDirectProperties().get(DOCUMENT) != null;
  }

  public String getExternalURL()
  {
    MenuItemCursor cursor = getProcedureCursor();
    if (cursor == null) return null;
    return (String)cursor.getDirectProperties().get(URL);
  }

  /* actions */
  @CMSAction
  public String showCatalogue()
  {
    String outcome = "procedure_catalogue";  
    try
    {
      Map parameters =
       getFacesContext().getExternalContext().getRequestParameterMap();
      String procedureMid = (String)parameters.get("procedureMid");
      String instanceId = (String)parameters.get("instanceid");
      if (procedureMid != null)
      {
        ProcedureInfoBean procedureInfoBean =
          (ProcedureInfoBean)getBean("procedureInfoBean");
        outcome = procedureInfoBean.showProcedureInfo(procedureMid);
        if (outcome != null) return outcome;
      }
      else if (instanceId != null)
      {
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        instanceBean.setInstanceId(instanceId);
        String accessToken = instanceBean.getAccessToken();
        if (accessToken != null)
        {
          if (!accessToken.equals(parameters.get(ACCESS_TOKEN_PARAM)))
            throw new Exception("INVALID_INSTANCE_TOKEN");

          int index = accessToken.indexOf(LOGIN_ACCESS_TOKEN_SEPARATOR);
          if (index != -1)
          {
            String formVariable = accessToken.substring(index + 1);
            instanceBean.login(formVariable);
          }
          outcome = instanceBean.forward();
        }
        else if (!UserSessionBean.getCurrentInstance().isAnonymousUser())
        {
          // allow access if user is logged in, for backward compatibility
          outcome = instanceBean.forward();          
        }
      }
      // show catalogue
      if (expandedMenuItems == null)
      {
        expandedMenuItems = new HashSet();
        // expand catalogue root
        String catalogueMid = getCatalogueMid();
        expandedMenuItems.add(catalogueMid);

        MenuModel menuModel =
          UserSessionBean.getCurrentInstance().getMenuModel();
        MenuItemCursor cursor = menuModel.getSelectedMenuItem();
        String value =
          (String)cursor.getProperties().get(EXPAND_LEVELS_PROPERTY);
        if (value != null)
        {
          try
          {
            int expandLevels = Integer.parseInt(value);
            cursor = menuModel.getMenuItem(catalogueMid);
            expandCatalogue(cursor, expandLevels);
          }
          catch (NumberFormatException ex)
          {            
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return outcome;
  }

  public String startProcedure()
  {
    try
    {
      MenuItemCursor cursor = getProcedureCursor();
      String workflowName = (String)cursor.getDirectProperties().get(WORKFLOW);
      if (workflowName != null)
      {
        String description = cursor.getLabel();
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        String outcome =
          instanceBean.createInstance(workflowName, description, false);
        goInstanceMid();
        return outcome;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String simulateProcedure()
  {
    try
    {
      MenuItemCursor cursor = getProcedureCursor();
      String workflowName = (String)cursor.getDirectProperties().get(WORKFLOW);
      if (workflowName != null)
      {
        String description = cursor.getLabel();
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        String outcome =
          instanceBean.createInstance(workflowName, description, true);
        goInstanceMid();
        return outcome;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String certStartProcedure()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return null;
    }

    try
    {
      return startProcedure();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String certSimulateProcedure()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return null;
    }

    try
    {
      return simulateProcedure();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getProcedureInfoURL()
  {
    MenuItemCursor procedureCursor = getProcedureCursor();
    String procedureMid = procedureCursor.getMid();
    String currentMid = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem().getMid();
    return getContextPath() + "/go.faces?xmid=" + currentMid +
      "&procedureMid=" + procedureMid;
  }
  
  private void goInstanceMid()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    String instanceMid = (String)menuModel.
      getSelectedMenuItem().getProperties().get(INSTANCE_MID_PROPERTY);
    if (instanceMid != null)
    {
      menuModel.setSelectedMid(instanceMid);
    }
  }

  private MenuItemCursor getProcedureCursor()
  {
    MenuItemCursor cursor = (MenuItemCursor)getValue("#{item}");
    return cursor;
  }

  private void expandCatalogue(MenuItemCursor cursor, int expandLevels)
  {
    if (expandLevels > 0)
    {
      MenuItemCursor currentCursor = cursor.getFirstChild();
      while (!currentCursor.isNull())
      {
        expandedMenuItems.add(currentCursor.getMid());
        if (!currentCursor.isLeaf())
        {
          expandCatalogue(currentCursor, expandLevels - 1);
        }
        currentCursor.moveNext();
      }
    }
  }
}
