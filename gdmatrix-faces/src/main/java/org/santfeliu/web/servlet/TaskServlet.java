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
