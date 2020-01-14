package org.santfeliu.test.control;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 *
 * @author blanquepa
 */
public class ViewController implements PhaseListener
{
  public void afterPhase(PhaseEvent pe)
  {
    System.out.println("After Phase: " + pe.getPhaseId().toString());
    if (pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();
      if (uiViewRoot != null)
        System.out.println(uiViewRoot.getId() + " " + uiViewRoot);
//      renderTree(uiViewRoot, "");
    }
  }

  public void beforePhase(PhaseEvent pe)
  {
    System.out.println("Before Phase: " + pe.getPhaseId().toString());
    if (pe.getPhaseId().equals(PhaseId.RESTORE_VIEW)
      || pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();
//      renderTree(uiViewRoot, "");
      if (uiViewRoot != null)
        System.out.println(uiViewRoot.getId() + " " + uiViewRoot);
    }
  }

  public PhaseId getPhaseId()
  {
    return PhaseId.ANY_PHASE;
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
