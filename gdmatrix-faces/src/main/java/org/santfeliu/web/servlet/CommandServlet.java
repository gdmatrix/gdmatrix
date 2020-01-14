package org.santfeliu.web.servlet;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author realor
 */
public class CommandServlet extends HttpServlet
{
  HashMap<String, Client> clients = new HashMap<String, Client>();
  HashMap<String, Command> commands = new HashMap<String, Command>();
  HashMap<String, String> info = new HashMap<String, String>();
  private static final long LONG_POLLING_WAIT = 30000;
  private static final float MIN_CLIENT_VERSION = 1.0f;

  public CommandServlet()
  {
    info.put("servletName", "CommandServlet");
    info.put("version", "1.0");
    info.put("minClientVersion", String.valueOf(MIN_CLIENT_VERSION));
  }

  @Override
  protected void doOptions(HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, IOException
  {
    response.setHeader("Allow", "GET,PUT,DELETE,POST,OPTIONS");
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", 
       "GET,PUT,DELETE,POST,OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", 
      "CSESSIONID, Content-Type");
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    String clientId = request.getParameter("clientid");
    if (clientId != null)
    {
      Client client = getClient(clientId, true);
      Command command = client.waitForCommand();
      sendResponse(response, command == null ?
        Collections.EMPTY_MAP : command.properties);
    }
    else
    {
      String commandId = request.getParameter("commandid");
      if (commandId != null)
      {
        Command command = commands.get(commandId);
        if (command == null) throw new IOException("COMMAND_NOT_FOUND");

        boolean completed = command.waitForTermination();
        if (completed)
        {
          commands.remove(commandId);
        }
        sendResponse(response, command.properties);
      }
      else
      {
        sendResponse(response, info);
      }
    }
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    // create command (browser)
    Map properties = readJSON(request);
    String clientId = (String)properties.get("clientId");
    Client client = getClient(clientId, false);
    if (client == null)
    {
      // client not listening!
      sendResponse(response, Collections.EMPTY_MAP);
    }
    else
    {
      Command command = new Command();
      command.properties.putAll(properties);
      commands.put(command.commandId, command);
      client.addCommand(command); // wakeUp
      sendResponse(response, command.properties);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    // end command (client)
    Map properties = readJSON(request);
    System.out.println("POST: " + properties);
    String commandId = (String)properties.get("commandId");
    Command command = commands.get(commandId);
    if (command == null) throw new IOException("COMMAND_NOT_FOUND");
    command.properties.putAll(properties);
    command.wakeUp();
    sendResponse(response, properties);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    // end client (client)
    String clientId = request.getParameter("clientid");
    System.out.println("DELETE Client: " + clientId);
    Client client = getClient(clientId, false);
    if (client != null)
    {
      clients.remove(clientId);
      Iterator<Command> iter = commands.values().iterator();
      while (iter.hasNext())
      {
        Command command = iter.next();
        if (command.getClientId().equals(clientId))
        {
          command.properties.put("status", "terminated");
          command.properties.put("exception", "Aborted");
          command.wakeUp();
          iter.remove();
        }
      }
    }
    sendResponse(response, Collections.EMPTY_MAP);
  }

  private Client getClient(String clientId, boolean create)
  {
    Client client = clients.get(clientId);
    if (client == null)
    {
      if (create)
      {
        client = new Client(clientId);
        clients.put(clientId, client);
      }
    }
    else // client already exists
    {
      if (client.isDisconnected() && !create)
      {
        clients.remove(clientId);
        client = null;
      }
      else
      {
        client.touch();
      }
    }
    return client;
  }

  private Map readJSON(HttpServletRequest request) throws IOException
  {
    JSONParser parser = new JSONParser();
    Reader reader = request.getReader();
    try
    {
      return (Map)parser.parse(reader);
    }
    catch (ParseException ex)
    {
      throw new IOException(ex);
    }
    finally
    {
      reader.close();
    }
  }

  private void sendResponse(HttpServletResponse response, Map map)
    throws IOException
  {
    String json = JSONObject.toJSONString(map);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().print(json);
  }
  
  class Client
  {
    String clientId;
    ArrayList<Command> commands = new ArrayList<Command>();
    long lastRequest;

    Client(String clientId)
    {
      this.clientId = clientId;
      lastRequest = System.currentTimeMillis();
    }

    public synchronized Command waitForCommand()
    {
      if (commands.isEmpty())
      {
        try
        {
          wait(LONG_POLLING_WAIT);
        }
        catch (InterruptedException ex)
        {
        }
      }
      return commands.isEmpty() ? null : commands.remove(0);
    }

    public synchronized void addCommand(Command command)
    {
      commands.add(command);
      notify();
    }

    public void touch()
    {
      lastRequest = System.currentTimeMillis();
    }

    public boolean isDisconnected()
    {
      return System.currentTimeMillis() - lastRequest > LONG_POLLING_WAIT;
    }
  }

  class Command
  {
    final String commandId;
    HashMap properties = new HashMap();
    long lastRequest;

    Command()
    {
      commandId = UUID.randomUUID().toString();
      properties.put("commandId", commandId);
    }

    public String getClientId()
    {
      return (String)properties.get("clientId");
    }

    public synchronized boolean waitForTermination()
    {
      if (isTerminated()) return true;
      try
      {
        wait(LONG_POLLING_WAIT);
      }
      catch (InterruptedException ex)
      {
      }
      return isTerminated();
    }

    public boolean isTerminated()
    {
      return "terminated".equals(properties.get("status"));
    }

    public synchronized void wakeUp()
    {
      notifyAll();
    }
  }
}
