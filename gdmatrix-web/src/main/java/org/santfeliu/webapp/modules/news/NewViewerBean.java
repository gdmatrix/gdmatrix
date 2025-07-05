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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.matrix.news.NewsManagerPort;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
@Named
@RequestScoped
public class NewViewerBean extends WebBean implements Serializable
{
  public static final String NEWID_PARAMETER = "newid";

  private static final String OUTCOME = "/pages/news/new_viewer.xhtml";

  New newObject;
  String imageContentId;

  public New getNewObject()
  {
    return newObject;
  }

  public void setNewObject(New newObject)
  {
    this.newObject = newObject;
  }

  public String getImageContentId()
  {
    return imageContentId;
  }

  public void setImageContentId(String imageContentId)
  {
    this.imageContentId = imageContentId;
  }

  public String getContent()
  {
    return OUTCOME;
  }

  public String getStartDate() throws Exception
  {
    try
    {
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat dayHumanFormat = new SimpleDateFormat("dd/MM/yyyy");

      String sysStartDay = newObject.getStartDate();
      if ((sysStartDay == null) || (sysStartDay.length() == 0))
        return "";
      else
        return dayHumanFormat.format(dayFormat.parse(sysStartDay));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  // action methods
  @CMSAction
  public String show()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      ExternalContext externalContext = getExternalContext();
      Map<String, String> parameterMap = externalContext.getRequestParameterMap();
      String newId = parameterMap.get(NEWID_PARAMETER);
      if (newId != null)
      {
        NewsManagerPort port = NewsModuleBean.getPort(
          userSessionBean.getUserId(), userSessionBean.getPassword());
        newObject = port.loadNew(newId);

        List<NewDocument> newDocuments = port.findNewDocuments(newId, null);

        this.imageContentId = null;
        for (NewDocument newDocument : newDocuments)
        {
          if (newDocument.getMimeType().startsWith("image/"))
          {
            this.imageContentId = newDocument.getContentId();
            break;
          }
        }
      }
      else
      {
        newObject = null;
      }
      String template = userSessionBean.getTemplate();
      return "/templates/" + template + "/template.xhtml";
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }
}
