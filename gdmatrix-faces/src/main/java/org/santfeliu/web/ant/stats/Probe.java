package org.santfeliu.web.ant.stats;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class Probe
{
  private StatisticsTask statistics;
  private String name;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public StatisticsTask getStatistics()
  {
    return statistics;
  }

  public void setStatistics(StatisticsTask statistics)
  {
    this.statistics = statistics;
  }

  public void init()
  {
  }

  public abstract void processLine(Line line);
   
  public void close()
  {    
  }
  
  public void printResult(PrintWriter writer) throws IOException
  {
    printHeader(writer);
    printBody(writer);
    printFooter(writer);
  }

  protected void printHeader(PrintWriter writer) throws IOException
  {
  }

  protected void printBody(PrintWriter writer) throws IOException
  {
  }

  protected void printFooter(PrintWriter writer) throws IOException
  {
  }
}
