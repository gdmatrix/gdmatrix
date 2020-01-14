package org.santfeliu.web.ant.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.Task;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class StatisticsTask extends Task
{
  private List<Probe> probes = new ArrayList<Probe>();
  private File directory;
  private File outputFile;
  private File templateFile;
  
  // filter
  private String startDate;
  private String endDate;
  private boolean processRobots;
  private boolean processInternal;
  private String rootMid;

  // general stats
  private int days;
  private int accessCount;

  public File getDirectory()
  {
    return directory;
  }

  public void setDirectory(File directory)
  {
    this.directory = directory;
  }

  public String getStartDate()
  {
    return startDate;
  }

  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }  
  
  public String getEndDate()
  {
    return endDate;
  }

  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
  }

  public String getRootMid()
  {
    return rootMid;
  }

  public void setRootMid(String rootMid)
  {
    this.rootMid = rootMid;
  }

  public File getOutputFile()
  {
    return outputFile;
  }

  public void setOutputFile(File outputFile)
  {
    this.outputFile = outputFile;
  }

  public File getTemplateFile()
  {
    return templateFile;
  }

  public void setTemplateFile(File templateFile)
  {
    this.templateFile = templateFile;
  }

  public boolean isProcessInternal()
  {
    return processInternal;
  }

  public void setProcessInternal(boolean processInternal)
  {
    this.processInternal = processInternal;
  }

  public boolean isProcessRobots()
  {
    return processRobots;
  }

  public void setProcessRobots(boolean processRobots)
  {
    this.processRobots = processRobots;
  }

  public int getDays()
  {
    return days;
  }

  public int getAccessCount()
  {
    return accessCount;
  }

  @Override
  public void execute()
  {
    try
    {
      initProbes();
      processProbes();
      closeProbes();
      if (outputFile != null) generateOutput();
    }
    catch (Exception ex)
    {
      log(ex.toString());
    }
  }

  public void addConfigured(Probe probe)
  {
    probes.add(probe);
  }

  public void initProbes()
  {
    for (Probe probe : probes)
    {
      probe.setStatistics(this);
      probe.init();
    }
  }

  public void processProbes() throws FileNotFoundException, IOException
  {
    log("processing directory " + directory);
    days = 0;
    accessCount = 0;
    File files[] = directory.listFiles();
    for (File file : files)
    {
      if (mustProcessFile(file))
      {        
        processFile(file);
        days++;
      }
    }
  }

  public void closeProbes()
  {
    for (Probe probe : probes)
    {
      probe.close();
    }
  }

  public void generateOutput() throws IOException
  {
    log("generating output to " + outputFile);

    HashMap variables = new HashMap();
    addFilterParameters(variables);

    for (Probe probe : probes)
    {
      String name = probe.getName();
      if (name != null)
      {
        StringWriter sw = new StringWriter();
        probe.printResult(new PrintWriter(sw));
        variables.put(name, sw.toString());
      }
    }

    Template template = Template.create(templateFile);
    PrintWriter writer = new PrintWriter(outputFile, "utf-8");
    try
    {
      template.merge(variables, writer);
    }
    finally
    {
      writer.close();
    }
  }

  private void processFile(File file) throws IOException
  {
    log("processing file " + file);
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(file)));
    try
    {
      String sline = reader.readLine();
      while (sline != null)
      {
        Line line = new Line(sline);
        if (mustProcessLine(line))
        {
          for (Probe probe : probes)
          {
            probe.processLine(line);            
          }
          accessCount++;
        }
        sline = reader.readLine();
      }
    }
    catch (Exception e)
    {
      log(e.toString());
    }
    finally
    {
      reader.close();
    }
  }

  private void addFilterParameters(Map map)
  {
    map.put("rootMid", rootMid);
    map.put("startDate", startDate);
    map.put("endDate", endDate);
    map.put("processRobots", processRobots);
    map.put("processInternal", processInternal);
    map.put("days", days);
    map.put("accessCount", accessCount);
  }

  private boolean mustProcessFile(File file)
  {
    String fileName = file.getName();
    if (fileName.startsWith("WEB") && fileName.endsWith(".csv"))
    {
      String fileDate = fileName.substring(3, 11);
      if ((startDate == null || startDate.compareTo(fileDate) <= 0) &&
        (endDate == null || fileDate.compareTo(endDate) <= 0))
      {
        return true;
      }
    }
    return false;
  }

  private boolean mustProcessLine(Line line)
  {
    if (processRobots != line.isRobot())
    {
      return false;
    }

    if(!processInternal && line.getIp().startsWith("10."))
    {
      return false;
    }

    if (rootMid != null)
    {
      String path = line.getPath();
      if (path.indexOf("[" + rootMid + "]") == -1) return false;
    }
    return true;
  }
}
