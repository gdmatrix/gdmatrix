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
package org.santfeliu.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.util.Task;

/**
 *
 * @author realor
 */
public class TaskServlet extends HttpServlet
{
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException
  {
    String taskId = req.getPathInfo();
    if (taskId.startsWith("/"))
    {
      taskId = taskId.substring(1);
    }
    Task task = Task.getInstance(taskId);
    if (task == null)
    {
      sendResponse(resp, "{\"result\": \"TASK_NOT_FOUND\"}");
    }
    else
    {
      String wait = req.getParameter("wait");
      if (wait != null)
      {
        task.waitForUpdate(Long.parseLong(wait));
      }
      String json = "{\"result\": \"OK\", \"progress\": " + task.getProgress() + 
        ", \"message\": \"" + task.getMessage() + 
        "\", \"state\": \"" + task.getState() + "\"}";
      sendResponse(resp, json);
    }
  }
  
  protected void sendResponse(HttpServletResponse resp, String json)
    throws IOException
  {
    resp.setContentType("application/json");
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "0");
    resp.getWriter().println(json);
  }
}
