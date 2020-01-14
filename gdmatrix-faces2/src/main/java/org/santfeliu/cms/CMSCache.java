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
package org.santfeliu.cms;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Workspace;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author unknown
 */
public class CMSCache
{
  private static int DEFAULT_CWORKSPACE_CACHE_MAX_SIZE = 1000;

  private URL wsDirectoryURL;
  private String userId;
  private String password;
  private Map<String, CWorkspace> cWorkspaceMap = 
    Collections.synchronizedMap(new HashMap<String, CWorkspace>());
  private int cWorkspaceCacheMaxSize;

  public CMSCache(URL wsDirectoryURL, String userId, String password)
  {
    this(wsDirectoryURL, userId, password, DEFAULT_CWORKSPACE_CACHE_MAX_SIZE);
  }
  
  public CMSCache(URL wsDirectoryURL, String userId, String password, 
    int cWorkspaceCacheMaxSize)
  {
    this.wsDirectoryURL = wsDirectoryURL;
    this.userId = userId;
    this.password = password;
    this.cWorkspaceCacheMaxSize = cWorkspaceCacheMaxSize;
  }

  public CWorkspace getWorkspace(String workspaceId)
  {
    CWorkspace cWorkspace = (CWorkspace)cWorkspaceMap.get(workspaceId);
    if (cWorkspace == null)
    {
      try
      {
        Workspace workspace = getPort().loadWorkspace(workspaceId);
        cWorkspace = new CWorkspace(this, workspace, cWorkspaceCacheMaxSize);
        putWorkspace(cWorkspace);
        JMXUtils.registerMBean("CWorkspace_" + workspaceId,
          cWorkspace.getCacheMBean());
      }
      catch (Exception ex)
      {
        //workspace not found
      }
    }
    return cWorkspace;
  }

  public void putWorkspace(CWorkspace cWorkspace)
  {
    String workspaceId = cWorkspace.getWorkspace().getWorkspaceId();
    cWorkspaceMap.put(workspaceId, cWorkspace);
  }

  public void removeWorkspace(String workspaceId)
  {
    JMXUtils.unregisterMBean("CWorkspace_" + workspaceId);
    cWorkspaceMap.remove(workspaceId);
  }

  public boolean containsWorkspace(String workspaceId)
  {
    return cWorkspaceMap.containsKey(workspaceId);
  }

  public CMSManagerPort getPort()
  {
    try
    {
      WSDirectory wsDir = WSDirectory.getInstance(wsDirectoryURL);
      WSEndpoint endpoint = wsDir.getEndpoint(CMSManagerService.class);
      return endpoint.getPort(CMSManagerPort.class, userId, password);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
