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
package org.santfeliu.web.obj;

import java.io.Serializable;

import java.util.Stack;

/**
 *
 * @author unknown
 */
public class ReturnStack implements Serializable
{
  private Stack<Entry> stack;

  public ReturnStack()
  {
    stack = new Stack<Entry>();
  }

  public void push(String searchMid, String returnMid, 
    String objectId, String valueBinding, Object beans)
  {
    stack.push(new Entry(searchMid, returnMid, objectId, valueBinding, beans));
  }

  public Entry peek()
  {
    return stack.peek();
  }

  public Entry pop()
  {
    return stack.pop();
  }
  
  public boolean isEmpty()
  {
    return stack.isEmpty();
  }
  
  public class Entry implements Serializable
  {
    private String searchMid;
    private String returnMid;
    private String objectId;
    private String valueBinding;
    private Object beans;

    Entry(String searchMid, String returnMid, 
      String objectId, String valueBinding, Object beans)
    {
      this.searchMid = searchMid;
      this.returnMid = returnMid;
      this.objectId = objectId;
      this.valueBinding = valueBinding;
      this.beans = beans;
    }
    
    public String getSearchMid()
    {
      return searchMid;
    }

    public String getReturnMid()
    {
      return returnMid;
    }
    
    public String getObjectId()
    {
      return objectId;
    }

    public String getValueBinding()
    {
      return valueBinding;
    }
    
    public Object getBeans()
    {
      return beans;
    }
  }
}
