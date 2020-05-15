package org.santfeliu.util.iarxiu.ant;

import java.io.File;
import org.santfeliu.util.iarxiu.pit.PIT;

/**
 *
 * @author realor
 */
public class LoadPITCommand extends PITCommand
{
  private File file;

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  @Override
  public void execute() throws Exception
  {
    System.out.println("load from " + file);
    PIT pit = getTask().getPIT();
    pit.load(file);
  }
}
