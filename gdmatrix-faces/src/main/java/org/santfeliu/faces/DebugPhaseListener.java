package org.santfeliu.faces;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 *
 * @author realor
 */
public class DebugPhaseListener implements PhaseListener
{
  public void beforePhase(PhaseEvent pe)
  {
    System.out.println(">>>>>> Before " + pe.getPhaseId());
  }

  public void afterPhase(PhaseEvent pe)
  {
    System.out.println(">>>>>> After " + pe.getPhaseId());
  }

  public PhaseId getPhaseId()
  {
    return PhaseId.ANY_PHASE;
  }
}
