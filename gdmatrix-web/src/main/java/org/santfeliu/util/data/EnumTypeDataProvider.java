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
package org.santfeliu.util.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public class EnumTypeDataProvider implements DataProvider
{
  private String enumTypeId;

  public void init(String reference)
  {
    enumTypeId = reference;
  }

  public String getEnumTypeId()
  {
    return enumTypeId;
  }

  public void setEnumTypeId(String enumTypeId)
  {
    this.enumTypeId = enumTypeId;
  }

  public Table getData(Map context)
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(DictionaryManagerService.class);
    DictionaryManagerPort port = endpoint.getPort(DictionaryManagerPort.class);
    EnumTypeItemFilter filter = new EnumTypeItemFilter();
    filter.setEnumTypeId(enumTypeId);
    if (context != null && !context.isEmpty())
    {
      String value = (String) context.get("value");
      if (value != null)
        filter.setValue(value);
    }
    List<EnumTypeItem> items = port.findEnumTypeItems(filter);
    Table table = new Table("value", "label", "title");
    for (EnumTypeItem item : items)
    {
      table.addRow(item.getValue(), item.getLabel(), item.getDescription());
    }
    return table;
  }

  public List<String> getParameters()
  {
    return Collections.EMPTY_LIST;
  }
}
