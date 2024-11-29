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
package org.santfeliu.webapp.modules.news;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.Source;
import org.matrix.dic.DictionaryConstants;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class SourceObjectBean extends ObjectBean
{
  private Source source = new Source();

  @Inject
  SourceTypeBean sourceTypeBean;

  @Inject
  SourceFinderBean sourceFinderBean;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.SOURCE_TYPE;
  }

  @Override
  public SourceTypeBean getTypeBean()
  {
    return sourceTypeBean;
  }

  @Override
  public Source getObject()
  {
    return isNew() ? null : source;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(source.getId());
  }

  public String getDescription(String sourceId)
  {
    return getTypeBean().getDescription(sourceId);
  }

  @Override
  public SourceFinderBean getFinderBean()
  {
    return sourceFinderBean;
  }

  public Source getSource()
  {
    return source;
  }

  public void setSource(Source source)
  {
    this.source = source;
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      source = NewsModuleBean.getPort(false).loadSource(objectId);
    }
    else source = new Source();
  }

  @Override
  public void storeObject()
  {
    try
    {
      source = NewsModuleBean.getPort(false).storeSource(source);
      setObjectId(source.getId());
      sourceFinderBean.outdate();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void removeObject() throws Exception
  {
    NewsModuleBean.getPort(false).removeSource(source.getId());
    sourceFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return source;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.source = (Source)state;
  }

}
