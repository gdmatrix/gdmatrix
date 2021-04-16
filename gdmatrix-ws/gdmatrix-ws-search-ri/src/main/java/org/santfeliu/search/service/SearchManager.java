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
package org.santfeliu.search.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.dic.Property;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.OrderByProperty;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.dic.Type;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.doc.State;
import org.matrix.news.NewView;
import org.matrix.search.AgendaFilter;
import org.matrix.search.Item;
import org.matrix.search.DocFilter;
import org.matrix.search.NewsFilter;
import org.matrix.search.GlobalSearchFilter;
import org.matrix.search.GlobalSearchResults;
import org.matrix.search.ItemType;
import org.matrix.search.SearchManagerPort;
import org.matrix.search.WebFilter;
import org.matrix.security.User;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.SingleInstance;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.search.SearchManagerPort")
@HandlerChain(file="handlers.xml")
@SingleInstance
public class SearchManager implements SearchManagerPort
{
  @Resource
  WebServiceContext wsContext;

  private static final Logger LOGGER = Logger.getLogger("Search");

  private static final int MIN_VALID_CHARS = 3;
  private static final int MIN_CONSONANTS_TO_CLEAN_VOWELS = 2;

  @Initializer
  public void initialize()
  {
    LOGGER.info("SearchManager init");
  }

  @Override
  public GlobalSearchResults search(GlobalSearchFilter filter)
  {
    try
    {
      LOGGER.log(Level.INFO, "Search");
      GlobalSearchResults result = new GlobalSearchResults();
      List<String> textList = tokenizeString(filter.getText());
      if (filter.getAgendaFilter() != null)
      {
        result.getItemList().addAll(doAgendaSearch(textList,
          filter.getAgendaFilter()));
      }
      if (filter.getNewsFilter() != null)
      {
        result.getItemList().addAll(doNewsSearch(textList,
          filter.getNewsFilter()));
      }
      if (filter.getDocFilter() != null)
      {
        result.getItemList().addAll(doDocSearch(textList,
          filter.getDocFilter()));
      }
      if (filter.getWebFilter() != null)
      {
        result.getItemList().addAll(doWebSearch(textList,
          filter.getWebFilter()));
      }
      return result;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Search failed", ex);
      throw new WebServiceException(ex);
    }
  }

  private List<Item> doAgendaSearch(List<String> textList,
    AgendaFilter filter) throws Exception
  {
    Map<String, Item> elementMap = new HashMap<>();
    EventFilter eventFilter = new EventFilter();
    if (filter.getSearchDays() != null)
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(new Date());
      cal.add(Calendar.DAY_OF_YEAR, -filter.getSearchDays());
      DatatypeFactory factory = DatatypeFactory.newInstance();
//      eventFilter.setInitTime(factory.newXMLGregorianCalendar(cal));
      eventFilter.setStartDateTime(
        TextUtils.formatDate(cal.getTime(), "yyyyMMddHHmmss"));
    }
    OrderByProperty orderByProperty = new OrderByProperty();
    orderByProperty.setName("startDateTime");
    orderByProperty.setDescending(false);
    eventFilter.getOrderBy().add(orderByProperty);
    eventFilter.getThemeId().addAll(filter.getThemeList());
    eventFilter.setMaxResults(filter.getMaxRows());
    for (String text : textList)
    {
      String searchString = getSearchString(text);
      if (searchString != null)
      {
        String regEx = getVowelsRegEx(text);
        eventFilter.setContent(searchString);
        List<Event> eventList = getAgendaPort().findEventsFromCache(eventFilter);
        for (Event event : eventList)
        {
          int score = matchRegEx(event.getDescription() + "," +
            event.getSummary(), regEx);
          if (score > 0)
          {
            String eventId = event.getEventId();
            if (!elementMap.containsKey(eventId))
            {
              Item item = new Item();
              item.setType(ItemType.AGENDA);
              item.setId(eventId);
              item.setDate(event.getStartDateTime());
              if (event.getEventTypeId() != null)
              {
                Type type = TypeCache.getInstance().getType(event.getEventTypeId());
                if (type != null)
                  item.setInfo1(type.getDescription() + ": " + event.getSummary());
                else
                  item.setInfo1(event.getEventTypeId() + ": " + event.getSummary());
              }
              else
                item.setInfo1(event.getSummary());
              item.setInfo2(event.getDescription());
              item.setScore(0);
              elementMap.put(eventId, item);
            }
            Item item = elementMap.get(eventId);
            item.setScore(item.getScore() + score);
          }
        }
      }
    }
    return sortElementMap(elementMap);
  }

  private List<Item> doNewsSearch(List<String> textList,
    NewsFilter filter) throws Exception
  {
    Map<String, Item> elementMap = new HashMap<>();
    org.matrix.news.NewsFilter newsFilter = new org.matrix.news.NewsFilter();
    newsFilter.getSectionId().addAll(filter.getSectionList());
    newsFilter.setExcludeDrafts(true);
    newsFilter.setExcludeNotPublished(true);
    newsFilter.setMinPubDateTime(getPriorDateString(filter.getSearchDays()));
    newsFilter.setMaxResults(filter.getMaxRows());
    for (String text : textList)
    {
      String searchString = getSearchString(text);
      if (searchString != null)
      {
        String regEx = getVowelsRegEx(text);
        newsFilter.setContent(searchString);
        List<NewView> newViewList =
          getNewsPort().findNewViewsFromCache(newsFilter);
        for (NewView nv : newViewList)
        {
          int score = matchRegEx(nv.getHeadline() + "," + nv.getSummary(),
            regEx);
          if (score > 0)
          {
            String newId = nv.getNewId();
            if (!elementMap.containsKey(newId))
            {
                Item item = new Item();
                item.setType(ItemType.NEWS);
                item.setId(newId);
                item.setDate(nv.getStartDate() + nv.getStartTime());
                item.setInfo1(nv.getHeadline());
                item.setInfo2(nv.getSummary());
                item.setScore(0);
                elementMap.put(newId, item);
            }
            Item item = elementMap.get(newId);
            item.setScore(item.getScore() + score);
          }
          if (nv.getKeywords() != null)
          {
            score = matchRegEx(nv.getKeywords(), regEx);
            if (score > 0)
            {
              String newId = nv.getNewId();
              if (!elementMap.containsKey(newId))
              {
                  Item item = new Item();
                  item.setType(ItemType.NEWS);
                  item.setId(newId);
                  item.setDate(nv.getStartDate() + nv.getStartTime());
                  item.setInfo1(nv.getHeadline());
                  item.setInfo2(nv.getSummary());
                  item.setScore(0);
                  elementMap.put(newId, item);
              }
              Item item = elementMap.get(newId);
              item.setScore(item.getScore() + score * 10);
            }
          }
        }
      }
    }
    return sortElementMap(elementMap);
  }

  private List<Item> doDocSearch(List<String> textList, DocFilter filter)
    throws Exception
  {
    List<Item> itemList = new ArrayList<>();
    DocumentFilter docFilter = new DocumentFilter();
    docFilter.setStartDate(getPriorDateString(filter.getSearchDays()));
    docFilter.setVersion(0);
    Property p = new Property();
    p.setName("searchable");
    p.getValue().add("true");
    docFilter.getProperty().add(p);
    docFilter.setMaxResults(filter.getMaxRows());
    docFilter.setIncludeContentMetadata(true);
    String searchExpression = "";
    for (int i = 0; i < textList.size(); i++)
    {
      String text = textList.get(i);
      if (text.length() >= MIN_VALID_CHARS)
      {
        if (searchExpression.length() > 0) searchExpression += ",";
        searchExpression += text;
      }
    }
    if (searchExpression.length() > 0)
    {
      docFilter.setContentSearchExpression(searchExpression);
      docFilter.getStates().add(State.DRAFT);
      docFilter.getStates().add(State.COMPLETE);
      docFilter.getStates().add(State.RECORD);
      DocumentUtils.setOrderByProperty(docFilter, "score", true);
      DocumentUtils.setOrderByProperty(docFilter, "captureDate", true);
      List<Document> docList = getDocPort().findDocuments(docFilter);
      for (Document doc : docList)
      {
        String docId = doc.getDocId();
        Item item = new Item();
        item.setType(ItemType.DOC);
        item.setId(docId);
        item.setDate(doc.getCaptureDateTime());
        if (doc.getContent() != null)
        {
          item.setInfo1(doc.getContent().getContentType());
        }
        String prefix = "";
        Type docType = TypeCache.getInstance().getType(doc.getDocTypeId());
        if (docType != null && !docType.getDescription().equals("Document"))
        {
          prefix = docType.getDescription() + ": ";
        }
        item.setInfo2(prefix + doc.getTitle());
        item.setScore(1);
        itemList.add(item);
      }
    }
    return itemList;
  }

  private List<Item> doWebSearch(List<String> textList, WebFilter filter)
    throws Exception
  {
    Map<String, Item> elementMap = new HashMap<>();
    NodeFilter nodeFilter = new NodeFilter();
    nodeFilter.setChangeDateTime1(getPriorDateString(filter.getSearchDays()));
    nodeFilter.setPropertyCaseSensitive(false);
    nodeFilter.getWorkspaceId().add(filter.getWorkspaceId());
    nodeFilter.setMaxResults(filter.getMaxRows());
    for (String text : textList)
    {
      String searchString = getSearchString(text);
      if (searchString != null)
      {
        String regEx = getVowelsRegEx(text);
        //"label%" property search
        nodeFilter.getProperty().clear();
        org.matrix.cms.Property property = new org.matrix.cms.Property();
        property.setName("label%");
        property.getValue().add("%" + searchString + "%");
        nodeFilter.getProperty().add(property);
        List<Node> nodeList = getWebPort().findNodes(nodeFilter);
        for (Node node : nodeList)
        {
          int score = matchRegEx(getNodeLabel(node), regEx);
          if (score > 0)
          {
            String nodeId = node.getNodeId();
            if (!elementMap.containsKey(nodeId))
            {
              Item item = new Item();
              item.setType(ItemType.WEB);
              item.setId(nodeId);
              item.setDate(node.getChangeDateTime());
              item.setInfo1(getNodeLabel(node));
              if ("url".equals(getNodeAction(node)))
              {
                item.setInfo2(getNodeURLProperty(node));
              }
              item.setScore(0);
              elementMap.put(nodeId, item);
            }
            Item item = elementMap.get(nodeId);
            item.setScore(item.getScore() + score);
          }
        }
        //"Keyword" property search
        nodeList.clear();
        nodeFilter.getProperty().clear();
        property = new org.matrix.cms.Property();
        property.setName("keyword");
        property.getValue().add(searchString);
        nodeFilter.getProperty().add(property);
        nodeList.addAll(getWebPort().findNodes(nodeFilter));
        for (Node node : nodeList)
        {
          int score = matchRegEx(getNodeKeywordList(node), regEx);
          if (score > 0)
          {
            String nodeId = node.getNodeId();
            if (!elementMap.containsKey(nodeId))
            {
              Item item = new Item();
              item.setType(ItemType.WEB);
              item.setId(nodeId);
              item.setDate(node.getChangeDateTime());
              item.setInfo1(getNodeLabel(node));
              item.setScore(0);
              elementMap.put(nodeId, item);
            }
            Item item = elementMap.get(nodeId);
            item.setScore(item.getScore() + score * 10);
          }
        }
      }
    }
    return sortElementMap(elementMap);
  }

  String getPriorDateString(int days)
  {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    cal.add(Calendar.DAY_OF_YEAR, -days);
    SimpleDateFormat bigFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    return bigFormat.format(cal.getTime());
  }

  private List<String> tokenizeString(String text)
  {
    List<String> result = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(text, " '`´,.");
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      token = removeRareChars(token);
      result.add(token);
    }
    return result;
  }

  private String getSearchString(String text)
  {
    String result = parseVowels(text);
    if (result != null && result.length() >= MIN_VALID_CHARS)
    {
      return result;
    }
    return null;
  }

  private String removeRareChars(String text)
  {
    StringBuilder sbResult = new StringBuilder();
    for (char c : text.toCharArray())
    {
      if (isVowel(c) ||
          ((c >= '0') && (c <= '9')) ||
          ((c >= 'a') && (c <= 'z')) ||
          ((c >= 'A') && (c <= 'Z')) ||
          (c == '·') || (c == '`') || (c == '´') ||
          (c == '\'') || (c == 'ª') || (c == 'º') ||
          (c == 'ñ') || (c == 'Ñ') || (c == 'ç') ||
          (c == 'Ç') || (c == '@'))
      {
        sbResult.append(c);
      }
    }
    return sbResult.toString().trim();
  }

  private boolean isVowel(char cIn)
  {
    char c = Character.toLowerCase(cIn);
    if ((c == 'a') || (c == 'e') || (c == 'i') || (c == 'o') || (c == 'u') ||
        (c == 'à') || (c == 'ä') || (c == 'á') ||
        (c == 'è') || (c == 'ë') || (c == 'é') ||
        (c == 'ì') || (c == 'ï') || (c == 'í') ||
        (c == 'ò') || (c == 'ö') || (c == 'ó') ||
        (c == 'ù') || (c == 'ü') || (c == 'ú'))
    {
      return true;
    }
    return false;
  }

  private String parseVowels(String text)
  {
    if (countNonVowels(text) >= MIN_CONSONANTS_TO_CLEAN_VOWELS)
    {
      text = cleanVowels(text);
    }
    if (text.matches("_*"))
    {
      text = "";
    }
    return text;
  }

  private int countNonVowels(String text)
  {
    int result = 0;
    for (char c : text.toCharArray())
    {
      if (!isVowel(c)) result++;
    }
    return result;
  }

  private String cleanVowels(String text)
  {
    StringBuilder sbResult = new StringBuilder();
    for (char c : text.toCharArray())
    {
      if (isVowel(c))
      {
        sbResult.append('_');
      }
      else sbResult.append(c);
    }
    return sbResult.toString();
  }

  private String getVowelsRegEx(String text)
  {
    StringBuilder sb = new StringBuilder();
    for (char c : text.toCharArray())
    {
      if (!isVowel(c))
      {
        sb.append(c);
      }
      else
      {
        c = Character.toLowerCase(c);
        if ((c == 'a') || (c == 'à') || (c == 'ä') || (c == 'á'))
        {
          sb.append("[aàäá]");
        }
        else if ((c == 'e') || (c == 'è') || (c == 'ë') || (c == 'é'))
        {
          sb.append("[eèëé]");
        }
        else if ((c == 'i') || (c == 'ì') || (c == 'ï') || (c == 'í'))
        {
          sb.append("[iìïí]");
        }
        else if ((c == 'o') || (c == 'ò') || (c == 'ö') || (c == 'ó'))
        {
          sb.append("[oòöó]");
        }
        else if ((c == 'u') || (c == 'ù') || (c == 'ü') || (c == 'ú'))
        {
          sb.append("[uùüú]");
        }
      }
    }
    return sb.toString();
  }

  private int matchRegEx(String text, String regEx)
  {
    Pattern p = Pattern.compile(regEx.toLowerCase());
    Matcher m = p.matcher(text.toLowerCase());
    int score = 0;
    if (m.find())
    {
      score = 10;
      while (m.find()) score += 1;
    }
    return score;
  }

  private int matchRegEx(List<String> textList, String regEx)
  {
    int score = 0;
    for (String text : textList)
    {
      score += matchRegEx(text, regEx);
    }
    return score;
  }

  private List<Item> sortElementMap(Map<String, Item> elementMap)
  {
    List<Item> itemList = new ArrayList<>();
    Iterator<Item> it = elementMap.values().iterator();
    while (it.hasNext())
    {
      itemList.add(it.next());
    }
    return sortItemListByScore(itemList);
  }

  private List<Item> sortItemListByScore(List<Item> itemList)
  {
    if (itemList == null || itemList.size() <= 1) return itemList;
    List<Item> result = new ArrayList<>();
    List<Item> lessAuxList = new ArrayList<>();
    List<Item> equalAuxList = new ArrayList<>();
    List<Item> greaterAuxList = new ArrayList<>();
    Item pivotItem = itemList.remove(0);
    equalAuxList.add(pivotItem);
    for (Item auxItem : itemList)
    {
      if (auxItem.getScore() < pivotItem.getScore())
      {
        lessAuxList.add(auxItem);
      }
      else if (auxItem.getScore() == pivotItem.getScore())
      {
        equalAuxList.add(auxItem);
      }
      else
      {
        greaterAuxList.add(auxItem);
      }
    }
    result.addAll(sortItemListByScore(greaterAuxList));
    result.addAll(sortItemListByDate(equalAuxList));
    result.addAll(sortItemListByScore(lessAuxList));
    return result;
  }

  private List<Item> sortItemListByDate(List<Item> itemList)
  {
    if (itemList == null || itemList.size() <= 1) return itemList;
    List<Item> result = new ArrayList<>();
    List<Item> lessAuxList = new ArrayList<>();
    List<Item> greaterAuxList = new ArrayList<>();
    Item pivotItem = itemList.remove(0);
    for (Item auxItem : itemList)
    {
      if (auxItem.getDate().compareTo(pivotItem.getDate()) <= 0)
      {
        lessAuxList.add(auxItem);
      }
      else
      {
        greaterAuxList.add(auxItem);
      }
    }
    result.addAll(sortItemListByDate(greaterAuxList));
    result.add(pivotItem);
    result.addAll(sortItemListByDate(lessAuxList));
    return result;
  }

  private String getNodeLabel(Node node)
  {
    String nodeDescription = getNodeProperty(node, "description");
    if (nodeDescription != null)
    {
      return nodeDescription;
    }
    else
    {
      String nodeLabel = getNodeProperty(node, "label");
      if (nodeLabel != null)
      {
        return nodeLabel;
      }
    }
    return node.getNodeId();
  }

  private String getNodeAction(Node node)
  {
    return getNodeProperty(node, "action");
  }

  private String getNodeURLProperty(Node node)
  {
    return getNodeProperty(node, "url");
  }

  private String getNodeProperty(Node node, String propertyName)
  {
    for (org.matrix.cms.Property property : node.getProperty())
    {
      if (propertyName.equals(property.getName()))
      {
        return property.getValue().get(0);
      }
    }
    return null;
  }

  private List<String> getNodeKeywordList(Node node)
  {
    for (org.matrix.cms.Property property : node.getProperty())
    {
      if ("keyword".equals(property.getName()))
      {
        return property.getValue();
      }
    }
    return null;
  }

  private AgendaManagerClient getAgendaPort() throws Exception
  {
    User user = UserCache.getUser(wsContext);

    return new AgendaManagerClient(user.getUserId(), user.getPassword());
  }

  private NewsManagerClient getNewsPort() throws Exception
  {
    User user = UserCache.getUser(wsContext);
    return new NewsManagerClient(user.getUserId(), user.getPassword());
  }

  private DocumentManagerPort getDocPort()
  {
    User user = UserCache.getUser(wsContext);
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(DocumentManagerService.class);
    return endpoint.getPort(DocumentManagerPort.class,
      user.getUserId(), user.getPassword());
  }

  private CMSManagerPort getWebPort()
  {
    User user = UserCache.getUser(wsContext);
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CMSManagerService.class);
    return endpoint.getPort(CMSManagerPort.class,
      user.getUserId(), user.getPassword());
  }

}
