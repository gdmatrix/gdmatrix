package org.santfeliu.web.ant.stats;

public class ParameterProbe extends CounterProbe
{
  private String parameters;
  private String[] parameterArray;

  public String getParameters()
  {
    return parameters;
  }

  public void setParameters(String parameters)
  {
    this.parameters = parameters;
  }

  @Override
  public void init()
  {
    parameterArray = parameters.split(",");
  }

  @Override
  public void processLine(Line line)
  {
    String lineParameters = line.getParameters();
    if (lineParameters != null)
    {
      for (String p : parameterArray)
      {
        String value = getParameterValue(lineParameters, p);
        if (value != null)
        {
          increment(value);
        }
      }
    }
  }

  private String getParameterValue(String parameters, String parameter)
  {
    int index = parameters.indexOf(parameter + "=");
    if (index != -1)
    {
      index += parameter.length() + 1;
      String word = parameters.substring(index);
      int index2 = word.indexOf("&");
      return index2 == -1 ? word : word.substring(0, index2);
    }
    return null;
  }
}
