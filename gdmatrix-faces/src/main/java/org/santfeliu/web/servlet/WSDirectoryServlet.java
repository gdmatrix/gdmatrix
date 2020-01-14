package org.santfeliu.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class WSDirectoryServlet extends HttpServlet
{
  private static final String WSDIRECTORY_FILENAME = "wsdirectory.xml";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    PrintWriter writer  = response.getWriter();

    File baseDir = MatrixConfig.getDirectory();
    File dirFile = new File(baseDir, WSDIRECTORY_FILENAME);
    if (dirFile.exists() && dirFile.isFile())
    {
      HashMap variables = new HashMap();
      String base = HttpUtils.getContextURL(request);
      variables.put("base", base);

      String dirTemplate = readFile(dirFile);
      // apply values to template
      String out = WebTemplate.create(dirTemplate).merge(variables);
      response.setContentType("text/xml");
      writer.print(out);
    }
    else
    {
      response.setContentType("text/plain");
      writer.println("WSDirectory file not found at " +
        dirFile.getAbsolutePath());
    }
  }

  private String readFile(File file) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    FileInputStream is = new FileInputStream(file);
    try
    {
      int numRead = is.read(buffer);
      while (numRead > 0)
      {
        bos.write(buffer, 0, numRead);
        numRead = is.read(buffer);
      }
    }
    finally
    {
      is.close();
    }
    return bos.toString("UTF-8");
  }
}
