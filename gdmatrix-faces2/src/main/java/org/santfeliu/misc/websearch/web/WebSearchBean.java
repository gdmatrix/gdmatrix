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
package org.santfeliu.misc.websearch.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class WebSearchBean extends WebBean implements Savable
{
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String FOUND_MESSAGE_PROPERTY = "webSearch.foundMessage";
  @CMSProperty
  public static final String NOT_FOUND_MESSAGE_PROPERTY = "webSearch.notFoundMessage";
  @CMSProperty
  public static final String WEB_SEARCH_ROOT_PROPERTY = "webSearch.root";
  @CMSProperty
  public static final String WEB_SEARCH_EXCLUDE_ROOT_PROPERTY = "webSearch.excludedRoot";
  @CMSProperty
  public static final String DUMMY_MAX_LENGTH_PROPERTY = "webSearch.dummyMaxLength";
  @CMSProperty
  public static final String DUMMY_WORDS_PROPERTY = "webSearch.dummyWords";
  @CMSProperty
  public static final String NODEID_PROPERTY = "webSearch.nodeId";

  public static final String URL_PROPERTY = "url";

  private String words;
  private String outerWords;
  private List<Result> results = new ArrayList<Result>();
  private List<Link> links = new ArrayList<Link>();
  private transient HashSet<String> includedNodes;
  private transient HashSet<String> excludedNodes;
  private transient HashSet<String> dummyWords;

  public String getWords()
  {
    return words;
  }

  public void setWords(String words)
  {
    this.words = words;
  }

  public String getOuterWords()
  {
    return outerWords;
  }

  public void setOuterWords(String outerWords)
  {
    this.outerWords = outerWords;
  }

  public List<Result> getResults()
  {
    return results;
  }

  public int getResultCount()
  {
    return results.size();
  }

  public List<Link> getLinks()
  {
    return links;
  }

  public int getLinkCount()
  {
    return links.size();
  }

  public String getLinksMessage()
  {
    String message = null;
    if (results.isEmpty())
    {
      message = getProperty(NOT_FOUND_MESSAGE_PROPERTY);
    }
    else
    {
      message = getProperty(FOUND_MESSAGE_PROPERTY);
    }
    if (message != null)
    {
      message = eval(message);
    }
    return message;
  }

  public int getPageSize()
  {
    try
    {
      String value = getProperty("pageSize");
      if (value != null)
      {
        return Integer.parseInt(value);
      }
    }
    catch (NumberFormatException ex)
    {
    }
    return 10;
  }

  // actions
  @CMSAction
  public String show()
  {
    return "web_search";
  }

  public String outerSearch()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String nodeId = getProperty(NODEID_PROPERTY);
    if (nodeId != null)
    {
      userSessionBean.setSelectedMid(nodeId);
    }
    words = outerWords;
    outerWords = null;
    return search();
  }

  @CMSAction
  public String search()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    results.clear();
    links.clear();

    HashMap<String, Result> resultList = new HashMap<String, Result>();    
    CMSManagerPort port = getPort();
    NodeFilter filter = new NodeFilter();
    Property property = new Property();
    filter.setPropertyCaseSensitive(false);
    filter.getWorkspaceId().add(userSessionBean.getWorkspaceId());
    List<String> keywords = getKeywords();
    if (!keywords.isEmpty())
    {
      for (String keyword : keywords)
      {
        System.out.println("NORM_KW>>" + keyword);

        // look in keyword
        property.setName("keyword");
        property.getValue().clear();
        property.getValue().add(keyword);
        filter.getProperty().clear();
        filter.getProperty().add(property);
        addResults(port.findNodes(filter), resultList, 1, null);
      }
      // look in label
      String pattern = getPattern(keywords);
      System.out.println("PAT>>" + pattern);
      if (pattern != null)
      {
        property.setName("label%");
        property.getValue().clear();
        property.getValue().add(pattern);
        filter.getProperty().clear();
        filter.getProperty().add(property);
        addResults(port.findNodes(filter), resultList, 
          2 * keywords.size(), getRegex(keywords));
      }
      sortResultsByScore(resultList);
      updateLinks();
    }
    return show();
  }

  public void updateLinks()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    cursor = cursor.getFirstChild();
    while (!cursor.isNull())
    {
      String label = cursor.getLabel();
      if (label != null)
      {
        label = eval(label);
        String url = cursor.getProperty(URL_PROPERTY);
        if (url != null)
        {
          url = eval(url);
          Link link = new Link();
          link.setLabel(label);
          link.setUrl(url);
          links.add(link);
        }
      }
      cursor = cursor.getNext();
    }
  }

  private String eval(String value)
  {
    if (value.indexOf("#{") != -1 && value.indexOf("}") != -1)
    {
      try
      {
        return String.valueOf(getValue(value));
      }
      catch (Exception ex)
      {
        return null;
      }
    }
    else return value;
  }

  private CMSManagerPort getPort()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    String password = userSessionBean.getPassword();
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CMSManagerService.class);
    return endpoint.getPort(CMSManagerPort.class, userId, password);
  }

  private void sortResultsByScore(HashMap<String, Result> scores)
  {
    results.clear();
    results.addAll(scores.values());
    Collections.sort(results);
  }

  private void addResults(List<Node> nodes,
    HashMap<String, Result> resultList, int score, String regex)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuModel menuModel = userSessionBean.getMenuModel();
    for (Node node : nodes)
    {
      String nodeId = node.getNodeId();
      MenuItemCursor menuItem = menuModel.getMenuItem(nodeId);
      if (isValidNode(menuItem, regex))
      {
        Result result = resultList.get(nodeId);
        if (result == null)
        {
          result = new Result();
          result.setNodeId(nodeId);
          result.setScore(score);
          resultList.put(nodeId, result);
        }
        else
        {
          result.setScore(result.getScore() + score);
        }
      }
    }
  }

  private boolean isValidNode(MenuItemCursor menuItem, String regex)
  {
    if (menuItem.isNull()) return false;

    boolean included = false;
    boolean excluded = false;

    MenuItemCursor pathCursor = menuItem;
    do
    {
      String mid = pathCursor.getMid();
      if (getIncludedNodes().contains(mid)) included = true;
      if (getExcludedNodes().contains(mid)) excluded = true;
      pathCursor = pathCursor.getParent();
    } while (!pathCursor.isNull());

    if (!included || excluded) return false;

    if (regex != null)
    {
      String label = menuItem.getLabel();
      if (label != null)
      {
        label = normalize(label);
        if (!label.matches(regex)) return false;
      }
    }
    return true;
  }

  private List<String> getKeywords()
  {
    List<String> keywords = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(words, " ,'\";:_-‘’", false);
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      if (!isDummy(token)) keywords.add(normalize(token));
    }
    return keywords;
  }

  private HashSet<String> getIncludedNodes()
  {
    if (includedNodes == null)
    {
      includedNodes = new HashSet<String>();
      List<String> values =
         getSelectedMenuItem().getMultiValuedProperty(WEB_SEARCH_ROOT_PROPERTY);
      includedNodes = new HashSet<String>();
      includedNodes.addAll(values);
    }
    return includedNodes;
  }

  private HashSet<String> getExcludedNodes()
  {
    if (excludedNodes == null)
    {
      excludedNodes = new HashSet<String>();
      List<String> values = getSelectedMenuItem().getMultiValuedProperty(
        WEB_SEARCH_EXCLUDE_ROOT_PROPERTY);
      excludedNodes = new HashSet<String>();
      excludedNodes.addAll(values);
    }
    return excludedNodes;
  }

  private HashSet<String> getDummyWords()
  {
    if (dummyWords == null)
    {
      dummyWords = new HashSet<String>();
      List<String> values =
         getSelectedMenuItem().getMultiValuedProperty(DUMMY_WORDS_PROPERTY);
      dummyWords = new HashSet<String>();
      dummyWords.addAll(values);
    }
    return dummyWords;
  }

  private boolean isDummy(String token)
  {
    int dummyMaxLength = 2;
    String value = getProperty(DUMMY_MAX_LENGTH_PROPERTY);
    if (value != null)
    {
      try
      {
        dummyMaxLength = Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    if (token.length() <= dummyMaxLength) return true;
    if (getDummyWords().contains(token)) return true;
    return false;
  }

  private String getPattern(List<String> keywords)
  {
    int consonants = 0;
    StringBuilder pattern = new StringBuilder();    
    for (String keyword : keywords)
    {
      pattern.append("%");
      for (int i = 0; i < keyword.length(); i++)
      {
        char ch = keyword.charAt(i);
        if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u')
        {
          pattern.append("_");
        }
        else
        {
          consonants++;
          pattern.append(ch);
        }
      }
    }
    pattern.append("%");

    return consonants > 2 ? pattern.toString() : null;
  }

  private String getRegex(List<String> keywords)
  {
    StringBuilder buffer = new StringBuilder();
    for (String keyword : keywords)
    {
      buffer.append(".*");
      buffer.append(keyword);
    }
    buffer.append(".*");
    return buffer.toString();
  }

  private String normalize(String keyword)
  {
    return TextUtils.unAccent(keyword.toLowerCase());
  }

  public class Result implements Serializable, Comparable<Result>
  {
    private String nodeId;
    private int score;

    public String getNodeId()
    {
      return nodeId;
    }

    public void setNodeId(String nodeId)
    {
      this.nodeId = nodeId;
    }

    public int getScore()
    {
      return score;
    }

    public void setScore(int score)
    {
      this.score = score;
    }

    public int compareTo(Result result)
    {
      return result.score - score;
    }

    public MenuItemCursor getMenuItem()
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuModel menuModel = userSessionBean.getMenuModel();
      return menuModel.getMenuItem(nodeId);
    }
  }

  public class Link implements Serializable
  {
    private String url;
    private String label;

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getUrl()
    {
      return url;
    }

    public void setUrl(String url)
    {
      this.url = url;
    }
  }
}
