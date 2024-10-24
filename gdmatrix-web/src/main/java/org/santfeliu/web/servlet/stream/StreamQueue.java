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
package org.santfeliu.web.servlet.stream;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author realor
 */
public class StreamQueue
{
  private static final Logger LOGGER = Logger.getLogger("StreamQueue");
  private static final long PURGE_TIME = 10 * 60 * 1000; // 10 minutes
  private static final Map<String, StreamQueue> queues = new HashMap<>();
  private static long lastPurge;

  private final List<Object> items = new ArrayList<>();
  private long lastAccess;

  public static StreamQueue getInstance(String queueId)
  {
    return getInstance(queueId, false);
  }

  public static synchronized StreamQueue getInstance(String queueId, boolean create)
  {
    StreamQueue queue = queues.get(queueId);
    if (queue == null && create)
    {
      queue = new StreamQueue();
      queues.put(queueId, queue);
      LOGGER.log(Level.INFO, "Queue {0} created.", queueId);
    }
    if (queue != null)
    {
      queue.lastAccess = System.currentTimeMillis();
    }
    purge();

    return queue;
  }

  public static synchronized void destroyInstance(String queueId)
  {
    queues.remove(queueId);
    LOGGER.log(Level.INFO, "Queue {0} destroyed.", queueId);
  }

  public static synchronized void purge()
  {
    long now = System.currentTimeMillis();
    if (now - lastPurge > PURGE_TIME)
    {
      lastPurge = now;
      queues.values().removeIf(queue -> now - queue.lastAccess > PURGE_TIME);
    }
  }

  public synchronized void push(Object item)
  {
    items.add(item);
    notifyAll();
  }

  public synchronized void waitForItems(long duration)
  {
    if (items.isEmpty())
    {
      try
      {
        wait(duration);
      }
      catch (InterruptedException ex)
      {
        // ignore
      }
    }
  }

  public synchronized boolean isEmpty()
  {
    return items.isEmpty();
  }

  public synchronized String flush()
  {
    Gson gson = new Gson();
    String json = gson.toJson(items);
    items.clear();
    return json;
  }

  public synchronized void clear()
  {
    items.clear();
  }
}
