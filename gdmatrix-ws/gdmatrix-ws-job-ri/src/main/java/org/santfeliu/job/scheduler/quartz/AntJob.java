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
package org.santfeliu.job.scheduler.quartz;

import java.io.File;
import java.net.URL;
import java.util.List;
import org.apache.tools.ant.Project;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.santfeliu.ant.AntLauncher;
import org.santfeliu.ant.Message;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class AntJob implements org.quartz.Job
{

  @Override
  public void execute(JobExecutionContext context) 
    throws JobExecutionException
  {
    try
    {
      JobDataMap params = context.getJobDetail().getJobDataMap();
      String filename = (String)params.get("filename");
      String target = (String)params.get("target");
      String wsURL = (String)params.get("wsURL");
      String[] filenames = filename.split(",");
      
      if (wsURL == null)
      {
        wsURL = MatrixConfig.getProperty("wsdirectory.url");
        if (wsURL == null)
        {
          String contextPath = MatrixConfig.getProperty("contextPath");
          wsURL = "http://localhost" + contextPath + "/wsdirectory";
        }
      }  
      
      URL wsDirectory = new URL(wsURL);
      File antDir = null;
      String dir = System.getProperty("antDir");
      if (dir != null)
      {
        antDir = new File(dir);
      }
      
      List<Message> messages = AntLauncher.execute(filenames, target,
        params, wsDirectory, 
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"), antDir);
      
      StringBuilder sb = new StringBuilder();      
      for (Message message : messages)
      {
        String level = "";
        switch(message.getLevel())
        {
          case Project.MSG_INFO:
            level = "INFO"; break;
          case Project.MSG_WARN:
            level = "WARN"; break;
          case Project.MSG_ERR:
            level = "ERROR"; break;
          case Project.MSG_DEBUG:
            level = "DEBUG"; break;
          case Project.MSG_VERBOSE:
            level = "VERBOSE"; break;
        }
        sb.append("[").append(level).append("] ");
        sb.append(message.getMessage()).append(";");
      }
      
      context.setResult(sb.toString());
      
    } 
    catch (Exception ex)
    {
      throw new JobExecutionException(ex);
    }
  }
  
}