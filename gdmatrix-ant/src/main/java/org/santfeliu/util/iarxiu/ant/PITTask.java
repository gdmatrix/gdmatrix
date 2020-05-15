package org.santfeliu.util.iarxiu.ant;

import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.santfeliu.ant.js.ScriptableTask;
import org.santfeliu.util.iarxiu.pit.PIT;

/**
 *
 * @author realor
 */
public class PITTask extends ScriptableTask
{
  private String var;
  private List<PITCommand> commands = new ArrayList();
  private PIT pit;
  private String template;
  private boolean newInstance = false;

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public PIT getPIT()
  {
    return pit;
  }

  public void setPit(PIT pit)
  {
    this.pit = pit;
    setVariable(var, pit);
  }

  public String getTemplate()
  {
    return template;
  }

  public void setTemplate(String template)
  {
    this.template = template;
  }
  
  public boolean isNewInstance()
  {
    return newInstance;
  }

  public void setNewInstance(boolean newInstance)
  {
    this.newInstance = newInstance;
  }

  public void addConfigured(PITCommand command)
  {
    command.setTask(this);
    commands.add(command);
  }

  @Override
  public void execute() throws BuildException
  {
    try
    {
      if (var != null && !newInstance)
      {
        Object pitObject = getVariable(var);
        if (pitObject instanceof PIT)
        {
          pit = (PIT)pitObject;
        }
        else
        {
          pit = new PIT();
          setVariable(var, pit);
        }
      }
      else
      {
        pit = new PIT();
        pit.setType(template);
      }
      
      for (PITCommand command : commands)
      {
        command.execute();
      }
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }
}
