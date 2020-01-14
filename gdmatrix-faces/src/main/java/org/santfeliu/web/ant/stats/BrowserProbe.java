package org.santfeliu.web.ant.stats;

public class BrowserProbe extends CounterProbe
{
  @Override
  public void processLine(Line line)
  {
    increment(line.getBrowser());
  }
}
