package org.santfeliu.workflow.web;

import java.io.Serializable;
import org.santfeliu.doc.web.DocumentEditor;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;

public class ProcedureInfoBean extends FacesBean implements Serializable
{
  private String procedureMid;
  private String docId;
  private DocumentEditor editor;

  public ProcedureInfoBean()
  {
  }

  public DocumentEditor getEditor()
  {
    return editor;
  }

  public void setEditor(DocumentEditor editor)
  {
    this.editor = editor;
  }

  public String getDocId()
  {
    return docId;
  }

  public String getProcedureMid()
  {
    return procedureMid;
  }

  public String getDocumentURL()
  {
    String url = (docId == null) ?
      "blank" : UserSessionBean.getCurrentInstance().getContextURL() +
      "/documents/" + docId;
    return url;
  }
  
  public boolean isTransactEnabled()
  {
    return "true".equals(getProcedureCursor().getProperties().
      get(ProcedureCatalogueBean.TRANSACT));
  }

  public boolean isSimulateEnabled()
  {
    return "true".equals(getProcedureCursor().getProperties().
      get(ProcedureCatalogueBean.SIMULATE));    
  }

  public boolean isCertificateRequired()
  {
    return "true".equals(getProcedureCursor().getProperties().
      get(ProcedureCatalogueBean.CERTIFICATE));
  }

  public String startProcedure()
  {
    try
    {
      String workflowName = (String)getProcedureCursor().getProperties().
        get(ProcedureCatalogueBean.WORKFLOW);
      if (workflowName != null)
      {
        String description = getProcedureCursor().getLabel();
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
      String workflowName = (String)getProcedureCursor().getProperties().
        get(ProcedureCatalogueBean.WORKFLOW);
      if (workflowName != null)
      {
        String description = getProcedureCursor().getLabel();
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
    try
    {
      return startProcedure();
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
    }
    return null;
  }

  public String certSimulateProcedure()
  {
    try
    {
      return simulateProcedure();
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
    }
    return null;
  }

  public String showProcedureInfo(String procedureMid)
  {
    this.procedureMid = procedureMid;
    this.docId = (String)getProcedureCursor().getProperties().get(
      ProcedureCatalogueBean.DOCUMENT);
    return (docId == null) ? null : "procedure_info";
  }

  public String editProcedureInfo()
  {
    try
    {
      editor = new DocumentEditor(docId);
      editor.editDocument(true);
    }
    catch (DocumentEditor.DocumentLockedByUser dlex)
    {
      error(dlex.getMessage(), new Object[]{dlex.getUserId()});
    }    
    catch (Exception ex)
    {
      error(ex);
    }
    return "procedure_info";
  }

  public String storeProcedureInfo()
  {
    try
    {
      editor.storeDocument();
      editor = null;
    }
    catch(Exception ex)
    {
      error(ex);
    }

    return "procedure_info";
  }

  public String closeProcedureInfo()
  {
    if (editor != null)
    {
      try
      {
        editor.unlockDocument();
      }
      catch (Exception e)
      {
        error(e);
      }
      finally
      {
        editor = null;
      }
    }
    return "procedure_info";
  }

  public boolean isEditing()
  {
    return editor != null && editor.isLockUser();
  }

  public String getLockUserId()
  {
    return (editor != null ? editor.getDocument().getLockUserId() : null);
  }

  public boolean isEditorUser()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(mic.getEditRoles());
  }

  private MenuItemCursor getProcedureCursor()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().
      getMenuItem(procedureMid);
  }
  
  private void goInstanceMid()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    String instanceMid = (String)menuModel.
      getSelectedMenuItem().getProperties().get("instanceMid");
    if (instanceMid != null)
    {
      menuModel.setSelectedMid(instanceMid);
    }
  }
}
