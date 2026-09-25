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
package org.santfeliu.webapp.modules.doc;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.loader.Loader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.webapp.data.Data;
import org.santfeliu.webapp.data.ParseDateFilter;
import org.santfeliu.webapp.data.ReverseTextFilter;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class PebbleBean
{
  PebbleEngine engine;
  
  @PostConstruct
  void init()
  {
    engine = new PebbleEngine.Builder()
      .loader(new MatrixLoader())
      .extension(new MatrixExtension())
      .cacheActive(false)
      .build();
  }
  
  public PebbleEngine getEngine()
  {
    return engine;
  }
  
  public Data getData(Credentials credentials)
  {
    return new Data(credentials);
  }
  
  static public class MatrixLoader implements Loader<String>
  {
    public MatrixLoader()
    {      
    }
    
    @Override
    public Reader getReader(String cacheKey)
    { 
      String userId = MatrixConfig.getProperty("adminCredentials.userId");      
      String password = MatrixConfig.getProperty("adminCredentials.password");
      
      DocumentManagerClient client = new DocumentManagerClient(userId, password);
      DocumentFilter filter = new DocumentFilter();
      Property property = new Property();
      property.setName("name");
      property.getValue().add(cacheKey);
      filter.setDocTypeId("PEBBLE");
      filter.getProperty().add(property);
      filter.setMaxResults(1);      
      List<Document> documents = client.findDocuments(filter);
      
      System.out.println("\n\n>>>>> Get page " + cacheKey + " => " + documents.size());

      if (!documents.isEmpty())
      {
        String contentId = documents.get(0).getContent().getContentId();
        Content content = client.loadContent(contentId);
        try
        {
          return new InputStreamReader(content.getData().getInputStream(), "UTF-8");
        }
        catch (Exception ex)
        {          
        }
      }
      return new StringReader("<p>File " + cacheKey + " not found</p>");
    }

    @Override
    public void setCharset(String charset)
    {
    }

    @Override
    public void setPrefix(String prefix)
    {
    }

    @Override
    public void setSuffix(String suffix)
    {
    }

    @Override
    public String resolveRelativePath(String relativePath, String anchorPath)
    {
      return relativePath;
    }

    @Override
    public String createCacheKey(String templateName)
    {
      return templateName;
    }

    @Override
    public boolean resourceExists(String templateName)
    {
      return true;
    }    
  }
  
  static public class MatrixExtension extends AbstractExtension
  {
    public Map<String, Filter> getFilters()   
    {
      Map<String, Filter> filters = new HashMap<>();
      filters.put("reverseText", new ReverseTextFilter());
      filters.put("parseDate", new ParseDateFilter());
      return filters;
    }
  }
}
