package org.santfeliu.util.iarxiu.ant;

import java.io.File;
import java.util.HashMap;
import org.santfeliu.ant.AntLauncher;

/**
 *
 * @author realor
 */
public class Test
{
  public static void main(String[] args)
  {
    try
    {
      AntLauncher.execute(
        new File("c:/matrix/conf/ant/executor.xml"), "execute", new HashMap());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
