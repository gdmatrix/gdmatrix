package org.santfeliu.web.ant.stats;

import java.util.Calendar;
import java.util.Date;

public class AccessMapProbe extends MapProbe
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
        visits[dia - 1][hour] = new Integer(0);
      }
      int visitCount = (Integer)visits[dia - 1][hour];
      visits[dia - 1][hour] = new Integer(visitCount + 1);
    }
    catch (Exception ex)
    {
    }
  }

  protected int getCellValue(int day, int hour)
  {
    return (Integer)visits[day][hour];
  }

}
