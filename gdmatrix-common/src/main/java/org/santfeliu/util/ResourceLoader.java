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
package org.santfeliu.util;

import java.util.ArrayList;

import javax.swing.SwingUtilities;


/**
 *
 * @author unknown
 */
public class ResourceLoader extends Thread
{
  private static int maxLoaders = 5;
  private static ArrayList loaders = new ArrayList();

  private String resourceRef;
  private ResourceProvider provider;
  private ResourceConsumer consumer;
  private boolean edt;
  private boolean fail = false;
  private boolean abort = false;

  public static synchronized ResourceLoader requestResource(String resourceRef,
    ResourceProvider provider, ResourceConsumer consumer, boolean edt)
  {
    ResourceLoader loader = null;
    if (loaders.size() < maxLoaders)
    {
      loader = new ResourceLoader(resourceRef, provider, consumer, edt);
      loaders.add(loader);
      loader.start();
    }
    return loader;
  }

  public static synchronized void abortAllRequests(boolean immediate)
  {
    Object[] loadersArray = loaders.toArray();
    for (int i = 0; i < loadersArray.length; i++)
    {
      ResourceLoader loader = (ResourceLoader)loadersArray[i];
      loader.abort(immediate);
    }
  }

  public static void setMaxLoaders(int maxLoaders)
  {
    ResourceLoader.maxLoaders = maxLoaders;
  }

  public static int getMaxLoaders()
  {
    return maxLoaders;
  }

  public ResourceLoader(String resourceRef, 
    ResourceProvider provider, ResourceConsumer consumer, boolean edt)
  {
    this.resourceRef = resourceRef;
    this.provider = provider;
    this.consumer = consumer;
    this.edt = edt;
    this.setPriority(Thread.MIN_PRIORITY);
  }

  @Override
  public void run()
  {
    long t0 = System.currentTimeMillis();
    
    System.out.println("ResourceLoader(" + resourceRef + ") started...");
    Object resource = null;
    Exception exception = null;
    try
    {
      resource = provider.loadResource(resourceRef);
    }
    catch (InterruptedException ex)
    {
      abort = true;
    }
    catch (Exception ex)
    {
      fail = true;
      exception = ex;
    }
    
    // dispatch to consumer 
    if (abort)
    {
      consumer.loadAborted(resourceRef);
    }
    else if (fail)
    {
      consumer.loadFailed(resourceRef, exception);
    }
    else // loadCompleted
    {
      if (edt)
      {
        final Object r = resource;
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            consumer.loadCompleted(resourceRef, r);
          }
        });
      }
      else
      {
        consumer.loadCompleted(resourceRef, resource);
      }
    }
    loaders.remove(this);
    long t1 = System.currentTimeMillis();
    System.out.println("ResourceLoader(" + resourceRef + 
      ") terminated in " + (t1 - t0) + " msec.");
  }

  public void abort(boolean immediate)
  {
    System.out.println("abort: " + immediate);
    abort = true;
    if (immediate) interrupt();
  }
}
