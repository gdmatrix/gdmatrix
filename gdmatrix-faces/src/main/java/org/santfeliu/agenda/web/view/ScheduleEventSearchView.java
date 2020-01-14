package org.santfeliu.agenda.web.view;

import java.util.Date;

/**
 * Interface that views with Tomahawk Schedule component must implement
 *
 * @author blanquepa
 */
public interface ScheduleEventSearchView extends EventSearchView
{
  public void setSelectedDate(Date selectedDate);

  public Date getSelectedDate();
}
