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
package org.santfeliu.util.script.function;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import javax.xml.ws.Service;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.news.client.NewsManagerClient;

/**
 *
 * @author realor
 */

/*
 * Usage: wsport(String module, String userId, String password, String dirUrl)
 *
 * returns: a module port with userId/password/directotyURL
 *   userId, password and dirURL are optional
 *
 * Example:
 *
 *   ${wsport("cases").loadCase("65").title} => "case title"
 *
 */

public class WSPortFunction extends BaseFunction
{
  private static final HashMap<String, Class<? extends Service>> services =
    new HashMap();
  private static final HashMap<String, Class> ports = new HashMap();

  static
  {
    services.put("agenda", org.matrix.agenda.AgendaManagerService.class);
    ports.put("agenda", org.matrix.agenda.AgendaManagerPort.class);

    services.put("cases", org.matrix.cases.CaseManagerService.class);
    ports.put("cases", org.matrix.cases.CaseManagerPort.class);

    services.put("classif", org.matrix.classif.ClassificationManagerService.class);
    ports.put("classif", org.matrix.classif.ClassificationManagerPort.class);

    services.put("cms", org.matrix.cms.CMSManagerService.class);
    ports.put("cms", org.matrix.cms.CMSManagerPort.class);

    services.put("dic", org.matrix.dic.DictionaryManagerService.class);
    ports.put("dic", org.matrix.dic.DictionaryManagerPort.class);

    services.put("doc", org.matrix.doc.DocumentManagerService.class);
    ports.put("doc", org.matrix.doc.DocumentManagerPort.class);

    services.put("edu", org.matrix.edu.EducationManagerService.class);
    ports.put("edu", org.matrix.edu.EducationManagerPort.class);

    services.put("elections", org.matrix.elections.ElectionsManagerService.class);
    ports.put("elections", org.matrix.elections.ElectionsManagerPort.class);

    services.put("feed", org.matrix.feed.FeedManagerService.class);
    ports.put("feed", org.matrix.feed.FeedManagerPort.class);

    services.put("forum", org.matrix.forum.ForumManagerService.class);
    ports.put("forum", org.matrix.forum.ForumManagerPort.class);

    services.put("kernel", org.matrix.kernel.KernelManagerService.class);
    ports.put("kernel", org.matrix.kernel.KernelManagerPort.class);
    
    services.put("job", org.matrix.job.JobManagerService.class);
    ports.put("job", org.matrix.job.JobManagerPort.class);    

    services.put("news", org.matrix.news.NewsManagerService.class);
    ports.put("news", org.matrix.news.NewsManagerPort.class);

    services.put("policy", org.matrix.policy.PolicyManagerService.class);
    ports.put("policy", org.matrix.policy.PolicyManagerPort.class);

    services.put("presence", org.matrix.presence.PresenceManagerService.class);
    ports.put("presence", org.matrix.presence.PresenceManagerPort.class);      
    
    services.put("report", org.matrix.report.ReportManagerService.class);
    ports.put("report", org.matrix.report.ReportManagerPort.class);

    services.put("search", org.matrix.search.SearchManagerService.class);
    ports.put("search", org.matrix.search.SearchManagerPort.class);

    services.put("security", org.matrix.security.SecurityManagerService.class);
    ports.put("security", org.matrix.security.SecurityManagerPort.class);

    services.put("signature", org.matrix.signature.SignatureManagerService.class);
    ports.put("signature", org.matrix.signature.SignatureManagerPort.class);

    services.put("sql", org.matrix.sql.SQLManagerService.class);
    ports.put("sql", org.matrix.sql.SQLManagerPort.class);

    services.put("survey", org.matrix.survey.SurveyManagerService.class);
    ports.put("survey", org.matrix.survey.SurveyManagerPort.class);

    services.put("translation", org.matrix.translation.TranslationManagerService.class);
    ports.put("translation", org.matrix.translation.TranslationManagerPort.class);

    services.put("workflow", org.matrix.workflow.WorkflowManagerService.class);
    ports.put("workflow", org.matrix.workflow.WorkflowManagerPort.class);
  }

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    Object port = null;

    if (args.length >= 1)
    {
      String module = null;      
      String userId = null;
      String password = null;
      String url = null;

      module = (String)Context.toString(args[0]);
      if (args.length >= 2)
      {
        userId = (String)Context.toString(args[1]);
        if (args.length >= 3)
        {
          password = (String)Context.toString(args[2]);
          if (args.length >= 4)
          {
            url = (String)Context.toString(args[3]);
          }
        }
      }

      if ("news".equals(module)) 
      {
        port = getManagerClient(NewsManagerClient.class, url, userId, password);
      }
      else if ("feed".equals(module)) 
      {
        port = getManagerClient(FeedManagerClient.class, url, userId, password);
      }      
      else if ("agenda".equals(module)) 
      {
        port = getManagerClient(AgendaManagerClient.class, url, userId, password);
      }      
      else if (services.containsKey(module))
      {
        WSDirectory directory;
        try
        {
          if (url == null) directory = WSDirectory.getInstance();
          else directory = WSDirectory.getInstance(new URL(url));
        }
        catch (MalformedURLException ex)
        {
          throw new RuntimeException("Malformed URL: " + url);
        }

        Class<? extends Service> serviceClass = services.get(module);
        Class portClass = ports.get(module);

        WSEndpoint endpoint = directory.getEndpoint(serviceClass);
        if (userId == null || userId.length() == 0)
        {
          port = endpoint.getPort(portClass); // anonymous call
        }
        else
        {
          port = endpoint.getPort(portClass, userId, password);
        }        
      }
      else
      {
        throw new RuntimeException("Unsupported module: " + module);
      }
    }
    return port;
  }
  
  private Object getManagerClient(Class clientClass, String url, String userId, String password)
  {
    Object client;
    try
    {
      Class[] argTypes;
      Object[] arguments;
      if (url == null)
      {
        argTypes = new Class[]{String.class, String.class};
        arguments = new Object[]{userId, password};
      }
      else
      {        
        argTypes = new Class[]{URL.class, String.class, String.class};
        arguments = new Object[]{new URL(url), userId, password};
      }
      Constructor constructor = clientClass.getDeclaredConstructor(argTypes); 
      client = constructor.newInstance(arguments); 
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }                  
    return client;
  }  
  
}
