package org.santfeliu.faces;

import com.sun.faces.application.StateManagerImpl;


public class StateManager extends StateManagerImpl
{
  public StateManager()
  {
  }

/*
  public javax.faces.application.StateManager.SerializedView 
    saveSerializedView(FacesContext context)
  {
    return super.saveSerializedView(context);
  }

  protected Object getTreeStructureToSave(FacesContext context)
  {
    return super.getTreeStructureToSave(context);
  }

  protected Object getComponentStateToSave(FacesContext context)
  {
    return super.getComponentStateToSave(context);
  }

  public void writeState(FacesContext context, 
    javax.faces.application.StateManager.SerializedView state)
    throws IOException
  {
    System.out.println(">>>>>>>>>>>Write state:" + state);
    super.writeState(context, state);
  }

  public UIViewRoot restoreView(FacesContext context, 
    String viewId, String renderKitId)
  {
    UIViewRoot viewRoot = super.restoreView(context, viewId, renderKitId);
    System.out.println(">>>>>>>>>>>Restore view:" + viewRoot);
    return viewRoot;
  }

  protected UIViewRoot restoreTreeStructure(FacesContext context, 
    String viewId, String renderKitId)
  {
    return super.restoreTreeStructure(context, viewId, renderKitId);
  }

  protected void restoreComponentState(FacesContext context, 
    UIViewRoot view, String renderKitId)
  {
    super.restoreComponentState(context, view, renderKitId);
  }

  public boolean isSavingStateInClient(FacesContext context)
  {
    return super.isSavingStateInClient(context);
  }
*/
}
