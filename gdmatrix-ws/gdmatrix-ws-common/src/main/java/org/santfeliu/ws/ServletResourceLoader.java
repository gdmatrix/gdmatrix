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
package org.santfeliu.ws;

import com.sun.xml.ws.transport.http.ResourceLoader;

import javax.servlet.ServletContext;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Set;

/**
 * {@link ResourceLoader} backed by {@link ServletContext}.
 *
 * @author Kohsuke Kawaguchi
 */
final class ServletResourceLoader implements ResourceLoader
{
  private final ServletContext context;

  public ServletResourceLoader(ServletContext context)
  {
    this.context = context;
  }

  @Override
  public URL getResource(String path) throws MalformedURLException
  {
    return context.getResource(path);
  }

  @Override
  public URL getCatalogFile() throws MalformedURLException
  {
    return getResource("/WEB-INF/jax-ws-catalog.xml");
  }

  @Override
  public Set<String> getResourcePaths(String path)
  {
    return context.getResourcePaths(path);
  }
}
