package org.santfeliu.util.iarxiu.ant;

import java.io.File;
import org.santfeliu.util.iarxiu.pit.PIT;

/**
 *
 * @author realor
 */
public class SavePITCommand extends PITCommand
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
    PIT pit = getTask().getPIT();
    System.out.println("save to " + file + " pit: " + pit);

    String filename = file.getName();
    if (filename.endsWith("zip"))
      pit.saveAsZip(file);
    else
      pit.saveAsXml(file);
  }
}