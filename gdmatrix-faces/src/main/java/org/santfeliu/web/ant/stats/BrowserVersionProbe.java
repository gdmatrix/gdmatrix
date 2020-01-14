package org.santfeliu.web.ant.stats;

public class BrowserVersionProbe extends CounterProbe
{
  @Override
  public void processLine(Line line)
  {
    increment(line.getBrowserVersion());
  }
}
