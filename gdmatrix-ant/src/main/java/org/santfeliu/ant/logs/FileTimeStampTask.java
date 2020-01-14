/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.ant.logs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Iterator;
import org.apache.commons.net.util.Base64;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 *
 * @author realor
 */
public class FileTimeStampTask extends Task
{
  private URL url;
  private File message;
  private FileSet fileSet;
  private String extension = "ts";
  private String digestMethod = "SHA-1";
  private String check;
  private boolean ignoreErrors = false;

  public URL getUrl()
  {
    return url;
  }

  public void setUrl(URL url)
  {
    this.url = url;
  }

  public File getMessage()
  {
    return message;
  }

  public void setMessage(File message)
  {
    this.message = message;
  }

  public String getExtension()
  {
    return extension;
  }

  public void setExtension(String extension)
  {
    this.extension = extension;
  }

  public String getDigestMethod()
  {
    return digestMethod;
  }

  public void setDigestMethod(String digestMethod)
  {
    this.digestMethod = digestMethod;
  }

  public String getCheck()
  {
    return check;
  }

  public void setCheck(String check)
  {
    this.check = check;
  }

  public boolean isIgnoreErrors()
  {
    return ignoreErrors;
  }

  public void setIgnoreErrors(boolean ignoreErrors)
  {
    this.ignoreErrors = ignoreErrors;
  }

  @Override
  public void execute() throws BuildException
  {
    validateInput();
    try
    {
      String messageTemplate = readFile(message);
      Iterator iter = fileSet.iterator();
      while (iter.hasNext())
      {
        File file = ((FileResource)iter.next()).getFile();
        File tsFile = new File(file.getPath() + "." + extension);
        if (!tsFile.exists())
        {
          generateTimeStamp(messageTemplate, file, tsFile);
        }
      }
    }
    catch (Exception ex)
    {
      if (!ignoreErrors) throw new BuildException(ex);
    }
  }

  public void add(FileSet fileSet)
  {
    this.fileSet = fileSet;
  }

  protected void validateInput()
  {
    if (url == null)
      throw new BuildException("Attribute 'url' is required");
    if (message == null)
      throw new BuildException("Attribute 'message' is required");
    if (fileSet == null)
      throw new BuildException("Nested element 'fileset' is required");
  }

  private void generateTimeStamp(String messageTemplate, File in, File out)
    throws Exception
  {
    String digestValue = digest(in);
    log(digestMethod + " digest for " + in.getName() +
      ": " + digestValue, Project.MSG_VERBOSE);
    String messageToSend =
      messageTemplate.replace("${DIGEST_VALUE}", digestValue);
    String result = post(messageToSend);
    if (check != null)
    {
      if (result.contains(check))
      {
        writeFile(result, out);
      }
      else
      {
        log("Invalid result for file " + in, Project.MSG_ERR);
      }
    }
    else
    {
      writeFile(result, out);
    }
  }

  private String readFile(File in) throws Exception
  {
    log("Reading file " + in, Project.MSG_VERBOSE);
    StringBuilder sb = new StringBuilder();
    InputStream is = new FileInputStream(in);
    try
    {
      byte[] buffer = new byte[4096];
      int numRead = is.read(buffer);
      while (numRead != -1)
      {
        sb.append(new String(buffer, 0, numRead, "UTF-8"));
        numRead = is.read(buffer);
      }
    }
    finally
    {
      is.close();
    }
    return sb.toString();
  }

  private void writeFile(String result, File out) throws Exception
  {
    log("Writing file " + out, Project.MSG_VERBOSE);
    FileOutputStream fos = new FileOutputStream(out);
    try
    {
      fos.write(result.getBytes("UTF-8"));
    }
    finally
    {
      fos.close();
    }
  }

  private String digest(File file) throws Exception
  {
    MessageDigest md = MessageDigest.getInstance(digestMethod);
    InputStream is = new FileInputStream(file);
    try
    {
      byte buffer[] = new byte[4096];
      int nr = is.read(buffer);
      while (nr != -1)
      {
        md.update(buffer, 0, nr);
        nr = is.read(buffer);
      }
      byte[] digest = md.digest();
      return Base64.encodeBase64String(digest);
    }
    finally
    {
      is.close();
    }
  }

  private String post(String messageToSend) throws Exception
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    URLConnection conn = url.openConnection();
    conn.setDoOutput(true);
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);
    OutputStream os = conn.getOutputStream();
    try
    {
      os.write(messageToSend.getBytes("UTF-8"));
      os.flush();

      // Get the response
      InputStream is = conn.getInputStream();
      try
      {
        try
        {
          byte buffer[] = new byte[4096];
          int nr = is.read(buffer);
          while (nr != -1)
          {
            bos.write(buffer, 0, nr);
            nr = is.read(buffer);
          }
        }
        finally
        {
          bos.close();
        }
      }
      finally
      {
        is.close();
      }
    }
    finally
    {
      os.close();
    }
    return bos.toString("UTF-8");
  }
}
