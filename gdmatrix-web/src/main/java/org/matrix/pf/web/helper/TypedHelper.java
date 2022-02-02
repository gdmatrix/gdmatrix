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
package org.matrix.pf.web.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.pf.web.WebBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;

/**
 *
 * @author blanquepa
 */
  public class TypedHelper extends WebBacking 
    implements Serializable
  {
    private final TypedPage backing;

    public TypedHelper(TypedPage backing)
    {
      this.backing = backing;
    }

    public String getObjectTypeId()
    {
      String objectTypeId = backing.getTypeId();
      return objectTypeId != null ? objectTypeId : backing.getRootTypeId();
    }  

    public List<Type> getAllTypes()
    {
      TypeCache tc = TypeCache.getInstance();
      List<Type> types = new ArrayList();
      List<SelectItem> items = getAllTypeItems();
      for (SelectItem i : items)
      {
        types.add(tc.getType(String.valueOf(i.getValue())));
      }
      return types;
    }

    public List<SelectItem> getAllTypeItems()
    {
      return getAllTypeItems(DictionaryConstants.READ_ACTION,
        DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
    }

    private List<SelectItem> getAllTypeItems(String ... actions)
    {
      try
      {
        TypeBean typeBean = WebUtils.getBacking("typeBean");
        return typeBean.getAllSelectItems(getObjectTypeId(), 
          backing.getAdminRole(), actions, true);
      }
      catch (Exception ex)
      {
        error(ex);
      }   
      return Collections.EMPTY_LIST;
    }  

  }
