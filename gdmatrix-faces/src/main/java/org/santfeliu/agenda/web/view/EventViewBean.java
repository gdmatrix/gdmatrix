package org.santfeliu.agenda.web.view;

import org.santfeliu.agenda.web.EventSearchBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;

/**
 *
 * @author blanquepa
 */
public abstract class EventViewBean extends FacesBean implements EventSearchView, Savable
{
  private EventSearchBean eventSearchBean;

  public EventViewBean()
  {
    eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    eventSearchBean.setEventViewBean(this);
  }

  public EventSearchBean getEventSearchBean()
  {
    return eventSearchBean;
  }

  public void setEventSearchBean(EventSearchBean eventSearchBean)
  {
    this.eventSearchBean = eventSearchBean;
  }
  
}
