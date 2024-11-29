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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Country;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class CountryObjectBean extends TerritoryObjectBean
{
  @Inject
  CountryFinderBean countryFinderBean;

  @Inject
  CountryTypeBean countryTypeBean;

  @Inject
  NavigatorBean navigatorBean;

  @Override
  public FinderBean getFinderBean()
  {
    return countryFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.COUNTRY_TYPE;
  }

  @Override
  public Country getObject()
  {
    return isNew() ? null : country;
  }

  @Override
  public CountryTypeBean getTypeBean()
  {
    return countryTypeBean;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(country.getCountryId());
  }

  public String getDescription(String countryId)
  {
    return getTypeBean().getDescription(countryId);
  }

  @Override
  public void createObject()
  {
    country = new Country();
    setObjectId(NavigatorBean.NEW_OBJECT_ID);
  }

  @Override
  public void loadObject() throws Exception
  {
    if (objectId != null && !isNew())
    {
      country = KernelModuleBean.getPort(false).loadCountry(objectId);
    }
    else
    {
      createObject();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    country = KernelModuleBean.getPort(false).storeCountry(country);
    setObjectId(country.getCountryId());
    countryFinderBean.outdate();
  }

  @Override
  public void removeObject() throws Exception
  {
    KernelModuleBean.getPort(false).removeCountry(objectId);
    countryFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return country;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.country = (Country)country;
  }
}
