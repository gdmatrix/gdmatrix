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
package org.santfeliu.web.servlet.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.PojoUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public class ProxyConfig
{
  private static final String CLASS_PACKAGE = "org.santfeliu.web.servlet.proxy.";
  private File proxyFile;
  private long lastModified;
  private final HashMap<String, String> urlReplacements = new HashMap<>();
  private final HashMap<String, List<ProxyRule>> hosts = new HashMap<>();
  private static final Logger LOGGER = Logger.getLogger("ProxyConfig");

  public ProxyConfig()
  {
    File matrixDir = MatrixConfig.getDirectory();
    proxyFile = new File(matrixDir, "proxy-config.xml");
  }

  public String getActualURL(String url)
  {
    updateConfig();
    String actualUrl = urlReplacements.get(url);
    return actualUrl != null ? actualUrl : url;
  }

  public List<ProxyRule> getRules(String host)
  {
    updateConfig();
    return hosts.get(host);
  }

  public File getProxyFile()
  {
    return proxyFile;
  }

  private synchronized void updateConfig()
  {
    if (proxyFile.exists() && proxyFile.lastModified() != lastModified)
    {
      readConfig(proxyFile);
      lastModified = proxyFile.lastModified();
    }
  }

  private void readConfig(File file)
  {
    LOGGER.log(Level.INFO, "Reading proxy configuration from {0}",
      file.getAbsolutePath());
    try
    {
      urlReplacements.clear();
      hosts.clear();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new FileInputStream(file));

      Node node = document.getFirstChild();
      while (!(node instanceof Element)) node = node.getNextSibling();
      if ("proxy-config".equals(node.getNodeName()))
      {
        Element root = (Element)node;
        node = root.getFirstChild();
        while (node != null)
        {
          if (node instanceof Element)
          {
            String nodeName = node.getNodeName();
            if ("replace".equals(nodeName))
            {
              Element replaceElem = (Element)node;
              String url = replaceElem.getAttribute("url");
              String by = replaceElem.getAttribute("by");
              if (url != null && by != null)
              {
                urlReplacements.put(url, by);
              }
            }
            else if ("host".equals(nodeName))
            {
              Element hostElem = (Element)node;
              String hostName = hostElem.getAttribute("name");
              if (hostName != null)
              {
                ArrayList<ProxyRule> rules = new ArrayList<>();
                hosts.put(hostName, rules);
                readRules(hostElem, rules);
              }
            }
          }
          node = node.getNextSibling();
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Error reading ProxyConfig", ex);
    }
  }

  private void readRules(Element hostElem, List<ProxyRule> rules)
    throws Exception
  {
    Node node = hostElem.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        if (node.getNodeName().equals("rule"))
        {
          Element ruleElem = (Element)node;
          String type = ruleElem.getAttribute("type");
          if (type != null)
          {
            if (!type.contains("."))
              type = CLASS_PACKAGE + type + "Rule";
            Class cls = Class.forName(type);
            ProxyRule rule = (ProxyRule)cls.newInstance();
            String stop = ruleElem.getAttribute("stop");
            rule.setStop("true".equals(stop));
            setAttributes(rule, ruleElem);
            rules.add(rule);
            readActions(ruleElem, rule.getActions());
          }
        }
      }
      node = node.getNextSibling();
    }
  }

  private void readActions(Element ruleElem, List<ProxyAction> actions)
    throws Exception
  {
    Node node = ruleElem.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        if (node.getNodeName().equals("action"))
        {
          Element actionElem = (Element)node;
          String type = actionElem.getAttribute("type");
          if (type != null)
          {
            if (!type.contains("."))
              type = CLASS_PACKAGE + type + "Action";
            Class cls = Class.forName(type);
            ProxyAction action = (ProxyAction)cls.newInstance();
            setAttributes(action, actionElem);
            actions.add(action);
          }
        }
      }
      node = node.getNextSibling();
    }
  }

  private void setAttributes(Object object, Element ruleElem)
  {
    NamedNodeMap attributes = ruleElem.getAttributes();
    int count = attributes.getLength();
    for (int i = 0; i < count; i++)
    {
      Node item = attributes.item(i);
      String name = item.getNodeName();
      String value = item.getNodeValue();
      PojoUtils.setStaticProperty(object, name, value);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      ProxyConfig config = new ProxyConfig();
      config.proxyFile = new File(MatrixConfig.getDirectory(), "proxy-config.xml");
      List<ProxyRule> rules = config.getRules("localhost");
      if (rules == null) return;
      for (ProxyRule rule : rules)
      {
        System.out.println("Rule " + rule);
        List<ProxyAction> actions = rule.getActions();
        for (ProxyAction action : actions)
        {
          System.out.println("  Action " + action);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
