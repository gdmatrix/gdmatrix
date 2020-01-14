package org.santfeliu.web.ant.stats;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class SessionMapProbe extends MapProbe
{

  @Override
  public void processLine(Line line)
  {
    try
    {
      Date dt = df.parse(line.getDate());
      calendar.setTime(dt);
      int dia = calendar.get(Calendar.DAY_OF_WEEK);
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      if (visits[dia - 1][hour] == null)
      {
        visits[dia - 1][hour] = new HashSet();
      }
      ((HashSet)visits[dia - 1][hour]).add(line.getSessionId());
    }
    catch (Exception ex)
    {
    }
  }

  protected int getCellValue(int day, int hour)
  {
    return ((HashSet)visits[day][hour]).size();
  }

}
