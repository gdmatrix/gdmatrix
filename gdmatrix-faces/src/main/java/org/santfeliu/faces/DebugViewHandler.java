package org.santfeliu.faces;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.santfeliu.web.UserSessionBean;


public class DebugViewHandler extends ViewHandler
{
  private ViewHandler _viewHandler;
  private boolean debug = false;
  
  public DebugViewHandler(ViewHandler viewHandler)
  {
    _viewHandler = viewHandler;
  }
  
  public UIViewRoot createView(FacesContext context, String viewId)
  {    
    Map sessionMap = context.getExternalContext().getSessionMap();
    UIViewRoot uiRoot = _viewHandler.createView(context, viewId);

    System.out.println(sessionMap);

    System.out.println("createView " + viewId + ": " + uiRoot + 
      " [" + uiRoot.getLocale() + "]");
    renderTree(uiRoot, "");

    return uiRoot;
  }

  public UIViewRoot restoreView(FacesContext context, String viewId)
  {
    Map sessionMap = context.getExternalContext().getSessionMap();
    UIViewRoot uiRoot = _viewHandler.restoreView(context, viewId);

    System.out.println(sessionMap);
    
    System.out.println("restoreView " + viewId + ": " + uiRoot);
    return uiRoot;
  }
  
  public void renderView(FacesContext context, UIViewRoot uiRoot)
    throws IOException
  {
    String template = 
      UserSessionBean.getCurrentInstance().getFrame() + "." +
      UserSessionBean.getCurrentInstance().getTemplate();
    System.out.println("renderView " + template + uiRoot.getViewId() + 
      ": " + uiRoot);
    _viewHandler.renderView(context, uiRoot);
    uiRoot = context.getViewRoot();
    if (debug) renderTree(uiRoot, "");
  }

  public Locale calculateLocale(FacesContext context)
  {
    return _viewHandler.calculateLocale(context);
  }

  public String calculateRenderKitId(FacesContext context)
  {
    return _viewHandler.calculateRenderKitId(context);
  }

  public String getActionURL(FacesContext context, String viewId)
  {
    return _viewHandler.getActionURL(context, viewId);
  }

  public String getResourceURL(FacesContext context, String viewId)
  {
    return _viewHandler.getResourceURL(context, viewId);
  }

  public void writeState(FacesContext context) throws IOException
  {
    _viewHandler.writeState(context);
  } 
 
  private void renderTree(UIComponent comp, String indent)
  {
    if (comp == null) return;
    System.out.println(indent + comp.getId() + " " + comp);
    List list = comp.getChildren();
    Iterator iter = list.iterator();    
    while (iter.hasNext())
    {
      UIComponent child = (UIComponent)iter.next();     
      renderTree(child, indent + " ");
    }
    iter = comp.getFacets().entrySet().iterator();
    if (iter.hasNext())
    {
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        System.out.println(indent + "FACET " + entry.getKey() + ":");
        UIComponent facet = (UIComponent)entry.getValue();
        renderTree(facet, indent + " ");
      }
    }
  }
}
