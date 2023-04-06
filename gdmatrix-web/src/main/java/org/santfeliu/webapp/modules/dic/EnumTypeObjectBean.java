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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.EnumType;
import org.matrix.dic.PropertyType;
import org.santfeliu.webapp.FinderBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@ViewScoped
@Named
public class EnumTypeObjectBean extends ObjectBean
{
  private EnumType enumType = new EnumType();

  @Inject
  EnumTypeTypeBean enumTypeTypeBean;

  @Inject
  EnumTypeFinderBean enumTypeFinderBean;

  public EnumType getEnumType()
  {
    return enumType;
  }

  public void setEnumType(EnumType type)
  {
    this.enumType = type;
  }

  @Override
  public TypeBean getTypeBean()
  {
    return enumTypeTypeBean;
  }

  @Override
  public FinderBean getFinderBean()
  {
    return enumTypeFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ENUM_TYPE_TYPE;
  }

  @Override
  public EnumType getObject()
  {
    return isNew() ? null : enumType;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : enumType.getName();
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      enumType = DicModuleBean.getPort(false).loadEnumType(objectId);
    else
      enumType = new EnumType();
  }

  @Override
  public void storeObject() throws Exception
  {
    if (StringUtils.isBlank(enumType.getSuperEnumTypeId()))
      enumType.setSuperEnumTypeId(null);
    enumType = DicModuleBean.getPort(false).storeEnumType(enumType);
    setObjectId(enumType.getEnumTypeId());
    enumTypeFinderBean.outdate();      
  }
  
  @Override
  public void removeObject() throws Exception
  {
    DicModuleBean.getPort(false).removeEnumType(enumType.getEnumTypeId());

    enumTypeFinderBean.outdate();
  }    
  
  public PropertyType[] getPropertyTypes()
  {
    return PropertyType.values();
  }    

}
