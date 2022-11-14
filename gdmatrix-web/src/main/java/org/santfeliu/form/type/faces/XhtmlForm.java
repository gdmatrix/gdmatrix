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
package org.santfeliu.form.type.faces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;

/**
 *
 * @author blanquepa
 */
public class XhtmlForm implements Form
{
  private final String id;
  private String title;
  private String language;
  Map<String, Object> properties = new HashMap();  
  private String code;
  private Map context;
  private String lastModified;
  
  public XhtmlForm()
  {
    id = UUID.randomUUID().toString();
  }

  @Override
  public String getId()
  {
    return id;
  }

  @Override
  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Override
  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public void setProperty(String name, Object value)
  {
    properties.put(name, value);
  }

  @Override
  public Object getProperty(String name)
  {
    return properties.get(name);
  }

  @Override
  public Collection<String> getPropertyNames()
  {
    return Collections.emptyList();
  }

  @Override
  public Collection<Field> getFields()
  {
    return Collections.emptyList();    
  }

  @Override
  public Field getField(String reference)
  {
    return null;
  }

  @Override
  public View getRootView()
  {
    return null;
  }

  @Override
  public View getView(String reference)
  {
    return null;
  }

  @Override
  public boolean validate(String reference, Object value, List errors, Locale locale)
  {
    return true;
  }

  @Override
  public boolean validate(Map data, List errors, Locale locale)
  {
    return true;
  }

  @Override
  public Form evaluate(Map context) throws Exception
  {
    return this;
  }

  @Override
  public Map getContext()
  {
    return context;
  }
  
  @Override
  public String getLastModified()
  {
    return lastModified;
  }

  @Override
  public void setLastModified(String lastModified)
  {
    this.lastModified = lastModified;
  }  

  @Override
  public boolean isOutdated()
  {
    return true; //TODO: implement strategy to detect source changes
  }

  @Override
  public void submit(Map data)
  {
  }

  @Override
  public Map read(InputStream is) throws IOException
  {
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, "UTF-8");
    code = writer.toString();
    
    return null;
  }

  @Override
  public void write(OutputStream os, Map data) throws IOException
  {
    StringReader reader = new StringReader(code);
    IOUtils.copy(reader, os, StandardCharsets.UTF_8);
  }

  public String getCode()
  {
    return code;
  }
  
}
