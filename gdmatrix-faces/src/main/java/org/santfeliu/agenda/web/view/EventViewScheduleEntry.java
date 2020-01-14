package org.santfeliu.agenda.web.view;

import org.apache.myfaces.custom.schedule.model.DefaultScheduleEntry;

/**
 *
 * @author blanquepa
 */
public class EventViewScheduleEntry extends DefaultScheduleEntry
{
  boolean editable;

  public boolean isEditable()
  {
    return editable;
  }

  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }
}
