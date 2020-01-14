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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.List;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public abstract class FormFilter implements Serializable
{
  private Backup backup;

  public FormFilter()
  {
  }

  //actions
  public void restore()
  {
    if (backup != null)
      copy(backup.getFormFilter(), this);
    backup = null;
  }

  public void backup()
  {
    backup = new Backup(this);
  }

  public void setProperty(String propName, List<String> propValues)
  {
    if (propName != null)
    {
      //set input properties
      DictionaryUtils.setProperty(this, propName, propValues);
      if (!DictionaryUtils.containsProperty(this, propName))
      {
        //set inner object filter
        DictionaryUtils.setProperty(getObjectFilter(), propName, propValues);
      }
    }
  }

  protected abstract Object getObjectFilter();

  public abstract void setMaxResults(int value);

  public abstract void setFirstResult(int value);

  protected abstract void copy(FormFilter src, FormFilter dst);

  protected abstract void clearAll();

  public abstract boolean isEmpty();

  private class Backup implements Serializable
  {
    private FormFilter formFilter;

    public Backup(FormFilter formFilter)
    {
      copy(formFilter, this.formFilter);
    }

    public FormFilter getFormFilter()
    {
      return formFilter;
    }
  }
}
