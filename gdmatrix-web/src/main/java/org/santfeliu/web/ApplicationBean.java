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
package org.santfeliu.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.Property;
import org.matrix.cms.Workspace;
import org.matrix.cms.WorkspaceFilter;
import org.matrix.util.WSDirectory;

import org.santfeliu.cms.CMSCache;
import org.santfeliu.cms.CNode;
import org.santfeliu.cms.CWorkspace;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.matrix.MatrixInfo;
import org.santfeliu.translation.StreamTranslator;
import org.santfeliu.translation.TranslatorFactory;
import org.santfeliu.translation.stream.TextTranslator;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author realor
 */
public class ApplicationBean
{
  private static final Logger LOGGER = Logger.getLogger("ApplicationBean");
  private static final String BUNDLE_TRANSLATION_PREFIX = "$$";
  private static final String BUNDLE_TRANSLATION_SEPARATOR = ":";

  private static final String BEAN_NAME = "applicationBean";
  private static final String PARAM_RESOURCES_VERSION = "resourcesVersion";

  private CMSCache cmsCache;
  private List<Locale> locales;
  private org.santfeliu.faces.Translator translator = new WebTranslator();

  public ApplicationBean()
  {
    try
    {
      LOGGER.info("ApplicationBean init");
      String userId =
        MatrixConfig.getProperty("adminCredentials.userId");
      String password =
        MatrixConfig.getProperty("adminCredentials.password");
      Integer cWorkspaceCacheMaxSize = getCWorkspaceCacheMaxSize();
      if (cWorkspaceCacheMaxSize != null)
      {
        cmsCache = new CMSCache(WSDirectory.getInstance().getUrl(),
          userId, password, cWorkspaceCacheMaxSize);
      }
      else
      {
        cmsCache = new CMSCache(WSDirectory.getInstance().getUrl(),
          userId, password);
      }
      initCMS();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "ApplicationBean init failed", ex);
      throw new RuntimeException(ex);
    }
  }

  public static ApplicationBean getCurrentInstance()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context == null) // not in FacesContext
    {
      return null;
    }
    else
    {
      Application application = context.getApplication();
      return (ApplicationBean)application.getVariableResolver().
        resolveVariable(context, BEAN_NAME);
    }
  }

  public static ApplicationBean getInstance(ServletContext context)
  {
    ApplicationBean applicationBean =
      (ApplicationBean)context.getAttribute(BEAN_NAME);
    if (applicationBean == null)
    {
      applicationBean = new ApplicationBean();
      context.setAttribute(BEAN_NAME, applicationBean);
    }
    return applicationBean;
  }

  public static String getInitParameter(String parameter)
  {
    return FacesContext.getCurrentInstance().
      getExternalContext().getInitParameter(parameter);
  }

  public String getStartMid(boolean isMobileAgent)
  {
    CWorkspace cWorkspace = cmsCache.getWorkspace(getDefaultWorkspaceId());
    if (cWorkspace == null)
    {
      return null;
    }
    else
    {
      if (!isMobileAgent) // desktop agent
      {
        return getDefaultNodeId(cWorkspace);
      }
      else //mobile agent
      {
        String auxNodeId = cWorkspace.getMobileMainNodeId();
        if (CWorkspace.NULL_MAIN_NODE.equals(auxNodeId))
        {
          auxNodeId = getDefaultNodeId(cWorkspace);
        }
        return auxNodeId;
      }
    }
  }

  public String getResourcesVersion()
  {
    StringBuilder result = new StringBuilder();
    result.append(MatrixInfo.getRevision());
    result.append("_");
    String resVersion;
    try
    {
      CWorkspace cWorkspace = cmsCache.getWorkspace(getDefaultWorkspaceId());
      String nodeId = getDefaultNodeId(cWorkspace);
      CNode node = cWorkspace.getNode(nodeId);
      resVersion = node.getSinglePropertyValue(PARAM_RESOURCES_VERSION);
      if (resVersion == null) resVersion = "0";
    }
    catch (Exception ex)
    {
      resVersion = "0";
    }
    result.append(resVersion);
    return result.toString();
  }

  public synchronized List<Locale> getSupportedLocales()
  {
    if (locales == null)
    {
      locales = new ArrayList<>();
      Iterator iter = FacesContext.getCurrentInstance().
        getApplication().getSupportedLocales();
      while (iter.hasNext()) locales.add((Locale)iter.next());
    }
    return locales;
  }

  public String getContextPath()
  {
    return MatrixConfig.getProperty("contextPath");
  }

  public String getDefaultPort()
  {
    String port =
      MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    return port == null ? "80" : port;
  }

  public String getServerSecurePort()
  {
    String port =
      MatrixConfig.getProperty("org.santfeliu.web.serverSecurePort");
    return port == null ? "443" : port;
  }

  public String getClientSecurePort()
  {
    String port =
      MatrixConfig.getProperty("org.santfeliu.web.clientSecurePort");
    return port == null ? "8443" : port;
  }

  public MenuModel createMenuModel(String workspaceId)
  {
    try
    {
      CWorkspace cWorkspace = cmsCache.getWorkspace(workspaceId);
      MenuModel menuModel = new MenuModel();
      menuModel.setRootMid(getDefaultNodeId(cWorkspace));
      menuModel.setCWorkspace(cWorkspace);
      return menuModel;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public String getDefaultWorkspaceId()
  {
    return MatrixConfig.getProperty("org.santfeliu.web.defaultWorkspaceId");
  }

  public CMSCache getCmsCache()
  {
    return cmsCache;
  }

  public String getVersion()
  {
    return MatrixInfo.getFullVersion();
  }

  public String getServerName()
  {
    try
    {
      InetAddress addr = InetAddress.getLocalHost();
      return addr.getHostName();
    }
    catch (UnknownHostException ex)
    {
      return null;
    }
  }

  public String getServerAddress()
  {
    try
    {
      InetAddress addr = InetAddress.getLocalHost();
      return addr.getHostAddress();
    }
    catch (UnknownHostException ex)
    {
      return null;
    }
  }

  public org.santfeliu.faces.Translator getTranslator()
  {
    return translator;
  }

  public String translate(String text)
  {
    return translate(text, null);
  }

  public String translate(String text, String group)
  {
    try
    {
      if (text != null && !text.trim().isEmpty())
      {
        if (text.startsWith(BUNDLE_TRANSLATION_PREFIX))
        {
          String auxText = text.substring(BUNDLE_TRANSLATION_PREFIX.length());
          int idx = auxText.indexOf(BUNDLE_TRANSLATION_SEPARATOR);
          if (idx >= 0)
          {
            String bundleName = auxText.substring(0, idx);
            String bundleKey = auxText.substring(idx + 
              BUNDLE_TRANSLATION_SEPARATOR.length());
            ResourceBundle bundle =
              ResourceBundle.getBundle(bundleName, FacesUtils.getViewLocale());
            return bundle.getString(bundleKey);
          }
          else 
          {
            return text;
          }
        }
        else
        {
          StringWriter sw = new StringWriter();
          getTranslator().translate(new StringReader(text), sw, "text/plain",
            FacesUtils.getViewLanguage(), group);
          return sw.toString();
        }
      }
      else return "";
    }
    catch (Exception ex)
    {
      return text;
    }
  }

  public String evalExpression(String expression)
  {
    String value;
    if (expression == null)
    {
      value = null;
    }
    else if (expression.startsWith("#{")) // value expression
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Application application = facesContext.getApplication();
      try
      {
        ValueBinding vb = application.createValueBinding(expression);
        Object result = vb.getValue(facesContext);
        value = (result == null) ? null : result.toString();
      }
      catch (Exception ex)
      {
        value = null;
      }
    }
    else value = expression;
    return value;
  }

  private Integer getCWorkspaceCacheMaxSize()
  {
    try
    {
      String value =
        MatrixConfig.getProperty("org.santfeliu.cms.cWorkspaceCacheMaxSize");
      if (value != null)
      {
        return Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {

    }
    return null;
  }

  private String getDefaultNodeId(CWorkspace cWorkspace)
  {
    String nodeId = cWorkspace.getMainNodeId();
    if (CWorkspace.NULL_MAIN_NODE.equals(nodeId))
    {
      nodeId = cWorkspace.getSmallestRootNodeId();
    }
    return nodeId;
  }

  private void initCMS()
  {
    CMSManagerPort port = cmsCache.getPort();
    WorkspaceFilter filter = new WorkspaceFilter();
    int workspaceCount = port.countWorkspaces(filter);
    if (workspaceCount == 0) //No workspaces found
    {
      //Create main workspace
      Workspace workspace = new Workspace();
      workspace.setName("Main workspace");
      workspace = port.storeWorkspace(workspace);
      String workspaceId = workspace.getWorkspaceId();
      //Create root node
      Node node = new Node();
      node.setWorkspaceId(workspaceId);
      Property property = new Property();
      property.setName("label");
      property.getValue().add("Home");
      node.getProperty().add(property);
      property = new Property();
      property.setName("action");
      property.getValue().add("blank");
      node.getProperty().add(property);
      port.storeNode(node);
    }
  }

  public class WebTranslator
    implements org.santfeliu.faces.Translator, Serializable
  {
    @Override
    public String getDefaultLanguage()
    {
      return TranslatorFactory.getDefaultLanguage();
    }

    // binary format translation
    @Override
    public void translate(InputStream in, OutputStream out, String contentType,
      String language, String group) throws IOException
    {
      if (TranslatorFactory.isSupportedLanguage(language))
      {
        StreamTranslator translator =
          TranslatorFactory.getStreamTranslatorForContentType(contentType);
        if (translator != null)
        {
          translator.translate(in, out, language, group);
        }
        else throw new IOException("Unsupported content type");
      }
      else
        IOUtils.copy(in, out);
    }

    // text stream translation
    @Override
    public void translate(Reader reader, Writer writer, String contentType,
      String language, String group) throws IOException
    {
      if (TranslatorFactory.isSupportedLanguage(language))
      {
        StreamTranslator translator =
          TranslatorFactory.getStreamTranslatorForContentType(contentType);
        if (translator instanceof TextTranslator)
        {
          TextTranslator textTranslator = (TextTranslator)translator;
          textTranslator.translate(reader, writer, language, group);
        }
        else throw new IOException("Unsupported content type");
      }
      else
        IOUtils.copy(reader, writer);
    }
  }
}
