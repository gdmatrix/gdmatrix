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
package org.santfeliu.jmx;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.State.BLOCKED;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 *
 * @author realor
 */
public class JMXUtils
{
  public static String domain = "org.santfeliu.matrix";
  public static final Logger logger = Logger.getLogger("JMXUtils");

  public static void registerMBean(String name, Object mbean)
  {
    try
    {
      // JMX: register MBean
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = new ObjectName(domain, "name", name);
      mbs.registerMBean(mbean, objectName);
      logger.log(Level.INFO, "MBean {0} registered", name);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING,
        "MBean {0} not registered: {1}", new String[]{name, ex.toString()});
    }
  }

  public static void unregisterMBean(String name)
  {
    try
    {
      // JMX: unregister MBean
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = new ObjectName(domain, "name", name);
      mbs.unregisterMBean(objectName);
      logger.log(Level.INFO, "MBean {0} unregistered", name);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, 
        "MBean {0} not unregistered: {1}", new String[]{name, ex.toString()});
    }
  }
  
  public static void dumpAllThreads() throws IOException
  {
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    ThreadInfo[] threadDump = threadMXBean.dumpAllThreads(true, true);
    Arrays.sort(threadDump, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        ThreadInfo t1 = (ThreadInfo)o1;
        ThreadInfo t2 = (ThreadInfo)o2;
        return (int)(t1.getThreadId() - t2.getThreadId());
      }
    });
    File baseDir = new File(System.getProperty("user.home"));
    long millis = System.currentTimeMillis();
    File dumpFile = new File(baseDir, "threads-" + millis + ".dmp");
    PrintWriter writer = new PrintWriter(dumpFile);
    int blocked = 0;
    int waiting = 0;
    try
    {
      writer.println("Date: " + new Date());
      writer.println("Thread count: " + threadDump.length);
      writer.println();

      for (int i = 0; i < threadDump.length; i++)
      {
        try
        {
          ThreadInfo threadInfo = threadDump[i];
          Thread.State state = threadInfo.getThreadState();
          if (state.equals(BLOCKED)) blocked++;
          else if (state.equals(Thread.State.WAITING)) waiting++;
          
          writer.println("Thread num: " + i);
          writer.println("Thread id: " + threadInfo.getThreadId());
          writer.println("Thread name: " + threadInfo.getThreadName());
          writer.println("Thread state: " + state);
          writer.println("Lock name: " + threadInfo.getLockName());
          writer.println("Lock owner id: " + threadInfo.getLockOwnerId());
          writer.println("Lock owner name: " + threadInfo.getLockOwnerName());
          writer.println("Blocked count: " + threadInfo.getBlockedCount());
          writer.println("Blocked time: " + threadInfo.getBlockedTime());
          writer.println("Waited count: " + threadInfo.getWaitedCount());
          writer.println("Waited time: " + threadInfo.getWaitedTime());
          writer.println("Is in native: " + threadInfo.isInNative());
          writer.println("Is suspended: " + threadInfo.isSuspended());
          MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
          if (lockedMonitors != null)
          {
            writer.println("Locked monitors: ");
            for (MonitorInfo mi : lockedMonitors)
            {
              Object obj = mi.getLockedStackFrame();
              writer.println("  " + (obj == null ? "???" : obj.toString()));
            }
          }
          LockInfo[] lockedSynchronizers = threadInfo.getLockedSynchronizers();
          if (lockedSynchronizers != null)
          {
            writer.println("Locked synchronizers:");
            for (LockInfo li : lockedSynchronizers)
            {
              writer.println("  " + li.toString());
            }
          }
          StackTraceElement[] stackTrace = threadInfo.getStackTrace();
          if (stackTrace != null)
          {
            writer.println("Stack trace:");
            for (StackTraceElement elem : stackTrace)
            {
              writer.println("  " + elem.toString());
            }
          }
        }
        catch (Exception ex)
        {          
        }
        writer.println("----------------------------------------------------");
      }
      writer.println("Total threads: " + threadDump.length);
      writer.println("Blocked threads: " + blocked);
      writer.println("Waiting threads: " + waiting);
      writer.println("Flush completed.");
      writer.flush();
    }
    finally
    {
      writer.close();
    }
  }
}
