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
package org.santfeliu.ant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.IOUtils;

/**
 *
 * @author blanquepa
 * @author realor
 */
public class AntLauncher
{
  public static final String WS_DIR_PROPERTY = "wsDir";
  public static final String WS_USERID_PROPERTY = "wsUserId";
  public static final String WS_PASSWORD_PROPERTY = "wsPassword";

  public static final String VERBOSE = "verbose";
  public static final String FILE_PREFIX = "-file:";
  public static final String TARGET_PREFIX = "-target:";
  public static final String PROPERTY_PREFIX = "-property:";
  public static final String WS_DIR_PREFIX = "-" + WS_DIR_PROPERTY + ":";
  public static final String WS_USERID_PREFIX = "-" + WS_USERID_PROPERTY + ":";
  public static final String WS_PASSWORD_PREFIX = "-" + WS_PASSWORD_PROPERTY + ":";

  public static List<Message> execute(String[] files, String target,
    Map properties, URL wsDirectory, String userId, String password,
    File antDir, Logger logger) throws Exception
  {
    if (antDir == null)
    {
      antDir = new File(System.getProperty("user.home") + "/ant");
    }
    antDir.mkdirs();
    if (wsDirectory != null)
    {
      properties.put(WS_DIR_PROPERTY, wsDirectory.toString());
      properties.put(WS_USERID_PROPERTY, userId);
      properties.put(WS_PASSWORD_PROPERTY, password);

      // download ant files from doc service
      for (String file : files)
      {
        try
        {
          downloadFile(file, userId, password, antDir, wsDirectory);
        }
        catch (Exception ex)
        {
          System.out.println(ex.toString());
        }
      }
    }
    String mainFile = files[0];
    if (!mainFile.endsWith(".xml"))
      mainFile = mainFile + ".xml";
    File mainTaskFile = new File(antDir, mainFile);

    return execute(mainTaskFile, target, properties, logger);
  }
  
  public static List<Message> execute(String[] files, String target,
    Map properties, URL wsDirectory, String userId, String password,
    File antDir) throws Exception
  {
    return execute(files, target, properties, wsDirectory, userId, password, 
      antDir, null);
  }  

  public static List<Message> execute(File file, String target, Map properties,
    Logger logger)
    throws Exception
  {
    final int verbose;
    Object vb = properties.get(VERBOSE);
    if (vb != null)
      verbose = Integer.parseInt((String)vb);
    else
      verbose = 0;

    final List<Message> messages = new ArrayList();
    Project p = new Project();
    p.addBuildListener(new BuildListener()
    {
      @Override
      public void buildStarted(BuildEvent be)
      {
      }

      @Override
      public void buildFinished(BuildEvent be)
      {
      }

      @Override
      public void targetStarted(BuildEvent be)
      {
      }

      @Override
      public void targetFinished(BuildEvent be)
      {
      }

      @Override
      public void taskStarted(BuildEvent be)
      {
      }

      @Override
      public void taskFinished(BuildEvent be)
      {
      }

      @Override
      public void messageLogged(BuildEvent be)
      {     
        if (be.getPriority() >= verbose)
        {
          if (logger != null)
          {
            Level level = Level.ALL;
            switch(be.getPriority())
            {
              case Project.MSG_ERR:
                level = Level.SEVERE; break;    
              case Project.MSG_WARN:
                level = Level.WARNING; break;                
              case Project.MSG_INFO:
                level = Level.INFO; break;
              case Project.MSG_VERBOSE:
                level = Level.FINE; break;
              case Project.MSG_DEBUG:
                level = Level.ALL; break;
            }
            logger.log(level, be.getMessage()); 
          }
          else
            System.out.println(be.getMessage()); 
          
          messages.add(new Message(be.getMessage(), be.getPriority()));
        }
      }
    });

    p.setUserProperty("ant.file", file.getAbsolutePath());
    Set<Map.Entry> entries = properties.entrySet();
    for (Map.Entry entry : entries)
    {
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String)
        p.setUserProperty(key, (String)value);
    }

    p.init();
    ProjectHelper helper = ProjectHelper.getProjectHelper();
    p.addReference("ant.projectHelper", helper);
    helper.parse(p, file);
    if (target == null)
    {
      p.executeTarget(p.getDefaultTarget());
    }
    else
    {
      p.executeTarget(target);
    }
    return messages;
  }
  
  public static List<Message> execute(File file, String target, Map properties)
    throws Exception
  {  
    return execute(file, target, properties, null);
  }

  private static File downloadFile(String file, String userId, String password,
    File antDir, URL wsDirectory) throws Exception
  {
    file = file.trim();
    System.out.println("Downloading " + file);

    DocumentManagerClient client;
    if (wsDirectory == null)
      client = new DocumentManagerClient(userId, password);
    else
      client = new DocumentManagerClient(wsDirectory, userId, password);
    if (file.endsWith(".xml"))
      file = file.substring(0, file.length() - 4);
    Document document =
      client.loadDocumentByName("ANT", "ide.ant", file, null, 0);
    if (document == null)
      throw new Exception("Can not load '" + file + "' ant document");
    else
    {
      DataHandler dataHandler = DocumentUtils.getContentData(document);
      if (dataHandler == null)
        throw new Exception("Ant document '" + file + "' has not content");
      return writeToAntFile(dataHandler, file, antDir);
    }
  }

  private static File writeToAntFile(DataHandler dataHandler, String name,
    File directory)
    throws Exception
  {
    if (!name.endsWith(".xml"))
      name = name + ".xml";
    File file = new File(directory, name);
    IOUtils.writeToFile(dataHandler, file);

    return file;
  }

  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0)
      {
        System.out.println("Usage: org.santfeliu.ant.AntLauncher " +
         FILE_PREFIX + "<filename> " +
         TARGET_PREFIX + "<target> " +
         PROPERTY_PREFIX + "<name>:<value> " +
         WS_DIR_PREFIX + "<wsDirectoryURL> " +
         WS_USERID_PREFIX + "<userId> " +
         WS_PASSWORD_PREFIX + "<password>");
      }
      else
      {
        URL wsDirectory = null;
        String userId = null;
        String password = null;
        ArrayList<String> files = new ArrayList<String>();
        String target = null;
        HashMap properties = new HashMap();

        for (String arg : args)
        {
          if (arg.startsWith(WS_DIR_PREFIX))
          {
            String wsDir = arg.substring(WS_DIR_PREFIX.length());
            wsDirectory = new URL(wsDir);
            System.out.println(WS_DIR_PROPERTY + ": " + wsDirectory);
          }
          else if (arg.startsWith(WS_USERID_PREFIX))
          {
            userId = arg.substring(WS_USERID_PREFIX.length());
            System.out.println(WS_USERID_PROPERTY + ": " + userId);
          }
          else if (arg.startsWith(WS_PASSWORD_PREFIX))
          {
            password = arg.substring(WS_PASSWORD_PREFIX.length());
            System.out.println(WS_PASSWORD_PROPERTY + ": " + password);
          }
          else if (arg.startsWith(TARGET_PREFIX))
          {
            target = arg.substring(TARGET_PREFIX.length());
            System.out.println("target: " + target);
          }
          else if (arg.startsWith(FILE_PREFIX))
          {
            String file = arg.substring(FILE_PREFIX.length());
            files.add(file);
            System.out.println("file: " + file);
          }
          else if (arg.startsWith(PROPERTY_PREFIX))
          {
            String varValue = arg.substring(PROPERTY_PREFIX.length());
            int index = varValue.indexOf(":");
            if (index > 0)
            {
              String variable = varValue.substring(0, index);
              String value = varValue.substring(index + 1);
              properties.put(variable, value);
              System.out.println("property: " + variable + "=" + value);
            }
          }
          else
          {
            // old format
            if (files.isEmpty())
            {
              files.add(arg);
              System.out.println("file: " + arg);
            }
            else
            {
              target = arg;
              System.out.println("target: " + target);
            }
          }
        }
        AntLauncher.execute(files.toArray(new String[files.size()]),
          target, properties, wsDirectory, userId, password, new File("."));
      }
    }
    catch (Exception ex)
    {
      System.err.println("ERROR: " + ex.toString());
    }
  }
}
