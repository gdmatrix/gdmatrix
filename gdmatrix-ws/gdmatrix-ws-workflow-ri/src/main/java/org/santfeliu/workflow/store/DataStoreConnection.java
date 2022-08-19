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
package org.santfeliu.workflow.store;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.workflow.InstanceEvent;
import org.matrix.workflow.InstanceFilter;
import org.santfeliu.util.Table;
import org.santfeliu.workflow.WorkflowEvent;

/**
 *
 * @author realor
 */
public interface DataStoreConnection
{
  public String createInstance(String agentName)
    throws Exception;

  public boolean destroyInstance(String instanceId)
    throws Exception;

  public String findProcessableInstance(String agentName)
    throws Exception;

  public void lockInstance(String instanceId)
    throws Exception;

  public void setInstanceProcessable(String instanceId, boolean processable)
    throws Exception;

  public void pushEvent(WorkflowEvent event)
    throws Exception;

  public WorkflowEvent popEvent(String instanceId)
    throws Exception;

  public void loadVariables(String instanceId, Map variables)
    throws Exception;

  public void storeVariables(String instanceId, Map variables)
    throws Exception;

  public Table findInstances(InstanceFilter filter, Set roles)
    throws Exception;

  public List<InstanceEvent> getInstanceEvents(String instanceId)
    throws Exception;

  public void programTimer(String instanceid, String dateTime)
    throws Exception;

  public void removeTimer(String instanceId, String dateTime)
    throws Exception;

  public Table getTimers(String dateTime)
    throws Exception;

  public void rollback()
    throws Exception;

  public void commit()
    throws Exception;

  public void close()
    throws Exception;
}
