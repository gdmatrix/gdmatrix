package org.santfeliu.web.ant.stats;

public class OSProbe extends CounterProbe
{
  @Override
  public void processLine(Line line)
  {
    increment(line.getOs());
  }
}
