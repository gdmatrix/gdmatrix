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
package org.santfeliu.misc.query.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.QueryInstance;
import org.santfeliu.misc.query.QueryInstance.Operator;
import org.santfeliu.misc.query.Query.Predicate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author realor
 */
public class QueryReader
{
  private QueryFinder queryFinder;
  private boolean readInstances = true;
  private HashSet<String> queryNames;
  
  public QueryReader()
  {
  }

  public QueryFinder getQueryFinder()
  {
    return queryFinder;
  }

  public void setQueryFinder(QueryFinder queryFinder)
  {
    this.queryFinder = queryFinder;
  }
  
  public boolean isReadInstances()
  {
    return readInstances;
  }

  public void setReadInstances(boolean readInstances)
  {
    this.readInstances = readInstances;
  }
  
  public Query readQuery(InputStream is) throws Exception
  {
    Query query = new Query();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);
    NodeList list = document.getElementsByTagName("query");
    if (list.getLength() == 1)
    {
      Element queryElement = (Element)list.item(0);
      readQuery(query, queryElement);
    }
    return query;
  }
  
  private void readQuery(Query query, Element queryElement) throws Exception
  {
    String queryName = queryElement.getAttribute("name");
    query.setName(queryName);

    if (queryNames == null) queryNames = new HashSet();
    else if (queryNames.contains(queryName))
      throw new Exception("CYCLIC_QUERY_DEFINTION");
    queryNames.add(queryName);

    String base = null;
    Element element = getChild(queryElement, "base");
    if (element != null)
    {
      base = element.getTextContent();
      query.setBase(base);
    }
    
    element = getChild(queryElement, "title");
    if (element != null) query.setTitle(element.getTextContent());
    
    element = getChild(queryElement, "description");
    if (element != null) query.setDescription(element.getTextContent());

    element = getChild(queryElement, "label");
    if (element != null) query.setLabel(element.getTextContent());

    element = getChild(queryElement, "sql");
    if (element != null) query.setSql(element.getTextContent());

    element = getChild(queryElement, "connection");
    if (element != null) readConnection(query, element);

    List<Element> elements;
    elements = getChildren(queryElement, "parameter");
    for (Element paramElement : elements)
    {
      readParameter(query, paramElement);
    }

    elements = getChildren(queryElement, "predicate");
    for (Element predicateElement : elements)
    {
      readPredicate(query, predicateElement);
    }

    elements = getChildren(queryElement, "output");
    for (Element outputElement : elements)
    {
      readOutput(query, outputElement);
    }
    
    if (!StringUtils.isBlank(base) && queryFinder != null)
    {
      QueryReader reader = new QueryReader();
      reader.queryFinder = queryFinder;
      reader.readInstances = false;
      reader.queryNames = queryNames;
      Query baseQuery = reader.readQuery(queryFinder.getQueryStream(base));
      
      // merge fields
      if (StringUtils.isBlank(query.getDescription()))
      {
        query.setDescription(baseQuery.getDescription());
      }
      if (StringUtils.isBlank(query.getSql()))
      {
        query.setSql(baseQuery.getSql());
      }
      if (StringUtils.isBlank(query.getLabel())) 
      {
        query.setLabel(baseQuery.getLabel());
      }
      
      Query.Connection connection = query.getConnection();
      Query.Connection baseConnection = baseQuery.getConnection();
      if (StringUtils.isBlank(connection.getDriver()))
      {
        connection.setDriver(baseConnection.getDriver());
      }
      if (StringUtils.isBlank(connection.getUrl()))
      {
        connection.setUrl(baseConnection.getUrl());
      }
      if (StringUtils.isBlank(connection.getUsername()))
      {
        connection.setUsername(baseConnection.getUsername());
      }
      if (StringUtils.isBlank(connection.getPassword()))
      {
        connection.setPassword(baseConnection.getPassword());
      }
      
      for (Query.Parameter baseParameter : baseQuery.getParameters())
      {
        if (query.getParameter(baseParameter.getName()) == null)
        {
          Query.Parameter parameter = query.addParameter();
          parameter.setName(baseParameter.getName());
          parameter.setDescription(baseParameter.getDescription());
          parameter.setFormat(baseParameter.getFormat());
          parameter.setSize(baseParameter.getSize());
          parameter.setSql(baseParameter.getSql());
          parameter.setDefaultValue(baseParameter.getDefaultValue());
          parameter.setInherited(true);
        }
      }
      
      for (Query.Predicate basePredicate : baseQuery.getPredicates())
      {
        if (query.getPredicate(basePredicate.getName()) == null)
        {
          Query.Predicate predicate = query.addPredicate();
          predicate.setLabel(basePredicate.getLabel());
          predicate.setName(basePredicate.getName());
          predicate.setSql(basePredicate.getSql());
          predicate.setInherited(true);
        }
      }
      
      for (Query.Output baseOutput : baseQuery.getOutputs())
      {
        if (query.getOutput(baseOutput.getName()) == null)
        {
          Query.Output output = query.addOutput();
          output.setLabel(baseOutput.getLabel());
          output.setName(baseOutput.getName());
          output.setDescription(baseOutput.getDescription());
          output.setSql(baseOutput.getSql());
          output.setInherited(true);
        }
      }
    }
    
    element = getChild(queryElement, "parametersOrder");
    if (element != null) 
    {
      String parametersOrder = element.getTextContent();
      if (parametersOrder != null)
      {
        query.sortParameters(parametersOrder);
      }      
    }

    element = getChild(queryElement, "predicatesOrder");
    if (element != null) 
    {
      String predicatesOrder = element.getTextContent();    
      if (predicatesOrder != null)
      {
        query.sortPredicates(predicatesOrder);     
      }
    }
     
    element = getChild(queryElement, "outputsOrder");
    if (element != null) 
    {
      String outputsOrder = element.getTextContent();        
      if (outputsOrder != null)
      {
        query.sortOutputs(outputsOrder);     
      }
    }
    
    // read instances
    if (readInstances)
    {
      elements = getChildren(queryElement, "instance");
      for (Element instanceElement : elements)
      {
        readQueryInstance(query, instanceElement);
      }
    }
  }

  private void readConnection(Query query, Element connElement)
  {
    Query.Connection conn = query.getConnection();
    Element element;

    element = getChild(connElement, "driver");
    if (element != null) conn.setDriver(element.getTextContent());

    element = getChild(connElement, "url");
    if (element != null) conn.setUrl(element.getTextContent());

    element = getChild(connElement, "username");
    if (element != null) conn.setUsername(element.getTextContent());

    element = getChild(connElement, "password");
    if (element != null) conn.setPassword(element.getTextContent());
  }

  private void readParameter(Query query, Element paramElement)
  {
    Query.Parameter parameter = query.addParameter();
    parameter.setName(paramElement.getAttribute("name"));

    Element element;
    element = getChild(paramElement, "description");
    if (element != null) parameter.setDescription(element.getTextContent());

    element = getChild(paramElement, "format");
    if (element != null) parameter.setFormat(element.getTextContent());

    element = getChild(paramElement, "size");
    if (element != null)
      parameter.setSize(Integer.parseInt(element.getTextContent()));

    element = getChild(paramElement, "defaultValue");
    if (element != null) parameter.setDefaultValue(element.getTextContent());  
    
    element = getChild(paramElement, "sql");
    if (element != null) parameter.setSql(element.getTextContent());
  }

  private void readPredicate(Query query, Element predicateElement)
  {
    Query.Predicate predicate = query.addPredicate();
    predicate.setName(predicateElement.getAttribute("name"));

    Element element;
    element = getChild(predicateElement, "label");
    if (element != null) predicate.setLabel(cleanText(element.getTextContent()));

    element = getChild(predicateElement, "sql");
    if (element != null) predicate.setSql(element.getTextContent());    
  }

  private void readOutput(Query query, Element outputElement)
  {
    Query.Output output = query.addOutput();
    output.setName(outputElement.getAttribute("name"));

    Element element;
    element = getChild(outputElement, "label");
    if (element != null) output.setLabel(element.getTextContent());

    element = getChild(outputElement, "description");
    if (element != null) output.setDescription(element.getTextContent());    
    
    element = getChild(outputElement, "sql");
    if (element != null) output.setSql(element.getTextContent());
  }

  private void readQueryInstance(Query query, Element instanceElement)
  {
    QueryInstance queryInstance = query.addInstance();
    queryInstance.setName(instanceElement.getAttribute("name"));

    Element element;
    element = getChild(instanceElement, "description");
    if (element != null) queryInstance.setDescription(element.getTextContent());

    element = getChild(instanceElement, "maxResults");
    if (element != null)
    {
      try
      {
        queryInstance.setMaxResults(Integer.parseInt(element.getTextContent()));
      }
      catch (NumberFormatException ex)
      {
        queryInstance.setMaxResults(QueryInstance.DEFAULT_MAX_RESULTS);
      }
    }
    else
    {
      queryInstance.setMaxResults(QueryInstance.DEFAULT_MAX_RESULTS);
    }
    
    element = getChild(instanceElement, "expression");
    if (element != null)
      readExpression(queryInstance.getRootExpression(), element);

    List<Element> elements = getChildren(instanceElement, "output");
    for (Element outputElement : elements)
    {
      queryInstance.addOutput(outputElement.getAttribute("name"));
    }

    // global parameters
    elements = getChildren(instanceElement, "parameter");
    for (Element paramElement : elements)
    {
      String paramName = paramElement.getAttribute("name");
      String paramValue = paramElement.getTextContent();
      queryInstance.getGlobalParameterValuesMap().put(paramName, paramValue);
    }
  }

  private void readExpression(QueryInstance.Expression expression,
    Element element)
  {
    if (expression instanceof QueryInstance.Predicate)
    {
      QueryInstance.Predicate predicate = (QueryInstance.Predicate)expression;
      // local parameters
      NodeList list = element.getElementsByTagName("parameter");
      for (int i = 0; i < list.getLength(); i++)
      {
        Element paramElem = (Element)list.item(i);
        String paramName = paramElem.getAttribute("name");
        String paramValue = paramElem.getTextContent();
        predicate.getParameterValuesMap().put(paramName, paramValue);
      }      
    }
    else // Operator
    {
      Operator operator = (Operator)expression;
      Node child = element.getFirstChild();
      while (child != null)
      {
        if (child instanceof Element)
        {
          Element childElement = (Element)child;
          if (childElement.getNodeName().equals("predicate"))
          {
            QueryInstance.Predicate predicate = 
              operator.addPredicate(childElement.getAttribute("name"));
            if (predicate != null)
            {
              readExpression(predicate, childElement);
            }
          }
          else // Operator
          {
            Operator subOp = operator.addOperator(childElement.getNodeName());
            if (subOp != null)
            {
              readExpression(subOp, childElement);
            }
          }
        }
        child = child.getNextSibling();
      }
    }
  }

  private String cleanText(String text)
  {
    StringBuilder buffer = new StringBuilder();
    boolean space = true;
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      if (ch == '\n' || ch == '\r' || ch == '\t' || ch == ' ')
      {
        if (!space)
        {
          buffer.append(' ');
          space = true;
        }
      }
      else
      {
        buffer.append(ch);
        space = false;
      }
    }
    return buffer.toString().trim();
  }
  
  private Element getChild(Element element, String name)
  {
    boolean found = false;
    Element childElement = null;
    Node childNode = element.getFirstChild();
    while (childNode != null && !found)
    {
      if (childNode instanceof Element)
      {
        childElement = (Element)childNode;
        found = childElement.getNodeName().equals(name);
      }
      childNode = childNode.getNextSibling();
    }
    return found? childElement : null;
  }

  private List<Element> getChildren(Element element, String name)
  {
    List<Element> childElements = new ArrayList();
    Node childNode = element.getFirstChild();
    while (childNode != null)
    {
      if (childNode instanceof Element)
      {
        Element childElement = (Element)childNode;
        if (childElement.getNodeName().equals(name))
        {
          childElements.add(childElement);
        }
      }
      childNode = childNode.getNextSibling();
    }
    return childElements;
  }
  
  public static void main(String[] args)
  {
    try
    {
      QueryReader reader = new QueryReader();
      InputStream is = reader.getClass().getResourceAsStream("query.xml");
      Query query = reader.readQuery(is);
      System.out.println(query.getName());
      System.out.println(query.getDescription());
      System.out.println(query.getLabel());
      System.out.println(query.getSql());
      System.out.println(query.getConnection().getDriver());
      System.out.println(query.getConnection().getUrl());
      System.out.println(query.getConnection().getUsername());
      System.out.println(query.getConnection().getPassword());
      for (Predicate predicate : query.getPredicates())
      {
        System.out.println(predicate.getLabel());
      }
      System.out.println(query.getOutputs());
      QueryInstance queryInstance = query.getInstances().get(0);
      System.out.println(queryInstance.generateSql());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
