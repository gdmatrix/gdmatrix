package org.santfeliu.misc.iarxiu.ant;

/**
 *
 * @author realor
 */
public abstract class PITCommand
{
  private PITTask task;

  public PITTask getTask()
  {
    return task;
  }

  public void setTask(PITTask task)
  {
    this.task = task;
  }

  public Object getVariable(String name)
  {
    Object variable = null;
    if (this.task != null)
      variable = this.task.getVariable(name);

    return variable;
  }

  public void setVariable(String name, Object value)
  {
    if (this.task != null)
      this.task.setVariable(name, value);
  }

  public abstract void execute() throws Exception;
}
