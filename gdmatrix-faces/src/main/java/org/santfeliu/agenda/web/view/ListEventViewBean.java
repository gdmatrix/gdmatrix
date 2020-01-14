package org.santfeliu.agenda.web.view;

import java.util.List;
import org.matrix.agenda.EventView;

/**
 *
 * @author realor
 */
public class ListEventViewBean extends TableEventViewBean
{
  public String getEventStyleClass()
  {
    EventView eventView = (EventView)getValue("#{row}");
    String eventTypeId = eventView.getEventTypeId();
    return "et_" + eventTypeId.replace(':', '_').replace('.', '_');
  }
  
  public boolean isDayHeaderRendered()
  {
    try
    {
      int rowIndex = (Integer)getValue("#{rowIndex}");
      if (rowIndex % super.getPageSize() == 0) return true;
      else
      {
        List rows = getRows();
        EventView eventView1 = (EventView)rows.get(rowIndex - 1);
        EventView eventView2 = (EventView)rows.get(rowIndex);
        String date1 = eventView1.getStartDateTime().substring(0, 8);
        String date2 = eventView2.getStartDateTime().substring(0, 8);
        return !date1.equals(date2);
      }
    }
    catch (Exception ex)
    {
    }
    return false;
  }
}
