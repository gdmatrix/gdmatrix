package org.santfeliu.web.ant.stats;

/**
 *
 * @author realor
 */
public class UserProbe extends CounterProbe
{
  @Override
  public void processLine(Line line)
  {
    increment(line.getUserId());
  }
}
