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
package org.santfeliu.webapp.modules.geo;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.MapStore;

/**
 *
 * @author realor
 */

@Named
@ApplicationScoped
public class GeoCategoryBean extends WebBean
{
  List<SelectItem> categorySelectItems;

  public void updateCategories()
  {
    getMapStore().purgeCategoryCache();
    categorySelectItems = null;
  }

  public List<SelectItem> getCategorySelectItems()
  {
    if (categorySelectItems == null)
    {
      List<SelectItem> selectItems = new ArrayList<>();

      for (MapStore.MapCategory category : getMapStore().getCategoryList())
      {
        SelectItem selectItem =
          new SelectItem(category.getName(), category.getTitle());
        selectItems.add(selectItem);
      }
      categorySelectItems = selectItems;
    }
    return categorySelectItems;
  }

  public MapStore.MapCategory getCategory(String categoryName)
  {
    return getMapStore().getCategory(categoryName);
  }

  MapStore getMapStore()
  {
    return CDI.current().select(MapStore.class).get();
  }
}

