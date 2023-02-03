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
package org.santfeliu.webapp.modules.dic;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@ManualScoped
@Named
public class TypeObjectBean extends ObjectBean
{
  private Type type = new Type();
  
  @Inject
  TypeTypeBean typeTypeBean;
  
  @Inject
  TypeFinderBean typeFinderBean;

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }
  
  @Override
  public TypeBean getTypeBean()
  {
    return typeTypeBean;
  }

  @Override
  public FinderBean getFinderBean()
  {
    return typeFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.TYPE_TYPE;
  }
  
  @Override
  public Type getObject()
  {
    return isNew() ? null : type;
  }
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : type.getDescription();
  }  
  
  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      type = DicModuleBean.getPort(false).loadType(objectId);
    else 
      type = new Type();
  }  
  
  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/dic/type_main.xhtml"));
    }
  }  

  @Override
  public String show()
  {
    return "/pages/dic/type.xhtml";
  }
  
  //Person selection
  public SelectItem getSuperTypeSelectItem()
  {
    String superTypeId = type.getSuperTypeId();
    if (superTypeId != null 
      && !superTypeId.equals(DictionaryConstants.TYPE_TYPE))
    {
      String description = typeTypeBean.getDescription(superTypeId);
      return new SelectItem(superTypeId, description);
    }
    else
      return new SelectItem("", "");
  }
  
  public void setSuperTypeSelectItem(SelectItem item)
  {
    if (item != null)
      type.setSuperTypeId((String) item.getValue());
  }  
  
  public List<SelectItem> complete(String query)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    String baseTypeId = baseTypeInfo.getBaseTypeId();
    if (typeTypeBean.getRootTypeId().equals(baseTypeId))
      baseTypeId = null;    
    return typeTypeBean.getSelectItems(query, baseTypeId, true, true);
  }
  
}
