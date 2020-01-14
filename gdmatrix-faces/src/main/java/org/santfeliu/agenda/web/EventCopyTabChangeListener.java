package org.santfeliu.agenda.web;

import javax.faces.event.AbortProcessingException;
import org.apache.myfaces.custom.tabbedpane.TabChangeEvent;
import org.apache.myfaces.custom.tabbedpane.TabChangeListener;
import org.santfeliu.faces.FacesBean;

/**
 *
 * @author blanquepa
 */
public class EventCopyTabChangeListener extends FacesBean implements TabChangeListener
{
  public void processTabChange(TabChangeEvent tce) throws AbortProcessingException
  {
    EventCopyBean eventCopyBean = (EventCopyBean)getBean("eventCopyBean");
    eventCopyBean.setFrequencyMode(tce.getNewTabIndex());
    eventCopyBean.setSelectedIndex(tce.getNewTabIndex());
  }
}
