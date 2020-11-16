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

import java.util.logging.Level;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author blanquepa
 */
public class JavascriptJob extends AbstractJob
{

  @Override
  public void doExecute(JobExecutionContext context) throws JobExecutionException
  {
    try
    {
      JobDataMap params = context.getJobDetail().getJobDataMap();
      String filename = (String)params.get("filename");
      
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");      
      ScriptClient client = new ScriptClient(userId, password);
      client.put("logger", logger);
      for (String key : params.keySet())
      {
        client.put(key, params.get(key));
      }      

      log(Level.INFO, 
        "Executing " + filename + " script with parameters: " + params);
      Object result = client.executeScript(filename);
      log(Level.INFO, (String) result);
      
      context.setResult(result);
    } 
    catch (Exception ex)
    {
      throw new JobExecutionException(ex);
    }
  }
  
}
