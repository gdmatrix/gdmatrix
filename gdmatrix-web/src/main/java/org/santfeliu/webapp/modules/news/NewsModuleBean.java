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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.NewView;
import org.matrix.news.NewsManagerPort;
import org.matrix.news.NewsManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class NewsModuleBean
{
  public static final Set<Character> HTML_IGNORED_CHARS =
    getHTMLIgnoredChars();

  public static final String LIST_IMAGE_TYPE =
    "NewDocumentListImage";
  public static final String DETAILS_IMAGE_TYPE =
    "NewDocumentDetailsImage";
  public static final String LIST_AND_DETAILS_IMAGE_TYPE =
    "NewDocumentListAndDetailsImage";
  public static final String CAROUSEL_IMAGE_TYPE =
    "NewDocumentCarouselImage";
  public static final String CAROUSEL_AND_DETAILS_IMAGE_TYPE =
    "NewDocumentCarouselAndDetailsImage";
  public static final String EXTENDED_INFO_TYPE =
    "NewDocumentExtendedInfo";
  
  public static NewsManagerPort getPort(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(NewsManagerService.class);
    return endpoint.getPort(NewsManagerPort.class, userId, password);
  }

  public static NewsManagerPort getPort(boolean asAdmin) throws Exception
  {
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    } 
    else
    {
      userId = UserSessionBean.getCurrentInstance().getUsername();
      password = UserSessionBean.getCurrentInstance().getPassword();
    }
    return getPort(userId, password);
  }

  public static NewsManagerClient getClient() throws Exception
  {
    return new NewsManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static NewsManagerClient getClient(String userId, String password)
    throws Exception
  {
    return new NewsManagerClient(userId, password);
  }

  public static NewsManagerClient getClient(URL wsDirectoryURL, String userId,
    String password) throws Exception
  {
    return new NewsManagerClient(wsDirectoryURL, userId, password);
  }

  public static String formatInputText(String text)
  {
    return putWildCards(text);
  }

  public static List<String> getAllImagesDocId(NewView newRow)
  {
    return getAllImagesId(newRow, false);
  }

  public static String getListImageDocId(NewView newRow)
  {
    return getListImageId(newRow, false);
  }

  public static String getDetailsImageDocId(NewView newRow)
  {
    return getDetailsImageId(newRow, false);
  }

  public static String getCarouselImageDocId(NewView newRow)
  {
    return getCarouselImageId(newRow, false);
  }

  public static List<String> getAllImagesContentId(NewView newRow)
  {
    return getAllImagesId(newRow, true);
  }

  public static String getListImageContentId(NewView newRow)
  {
    return getListImageId(newRow, true);
  }

  public static String getDetailsImageContentId(NewView newRow)
  {
    return getDetailsImageId(newRow, true);
  }

  public static String getCarouselImageContentId(NewView newRow)
  {
    return getCarouselImageId(newRow, true);
  }

  public static List<NewDocument> getExtendedInfoDocList(NewView newRow)
  {
    List<NewDocument> result = new ArrayList<>();
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(EXTENDED_INFO_TYPE))
      {
        result.add(nd);
      }
    }
    return result;
  }

  public static boolean[] checkNewSections(NewsManagerClient client,
    String newId)
  {
    boolean userCanRead = false;
    boolean userCanEdit = false;
    List<NewSection> newSections = client.findNewSectionsFromCache(newId);
    for (NewSection newSection : newSections)
    {
      try
      {
        MenuItemCursor mic = UserSessionBean.getCurrentInstance().
          getMenuModel().getMenuItemByMid(newSection.getSectionId());
        userCanRead = true;
        if (isEditorUser(mic))
          userCanEdit = true;
        if (userCanRead && userCanEdit) break;
      }
      catch (Exception ex)
      {
        //Non visible node
      }
    }
    return new boolean[]{userCanRead, userCanEdit};
  }

  public static boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

  private static List<String> getAllImagesId(NewView newRow, boolean contentId)
  {
    List<String> result = new ArrayList();
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(LIST_IMAGE_TYPE) ||
        nd.getNewDocTypeId().endsWith(LIST_AND_DETAILS_IMAGE_TYPE) ||
        nd.getNewDocTypeId().endsWith(DETAILS_IMAGE_TYPE) ||
        nd.getNewDocTypeId().endsWith(CAROUSEL_AND_DETAILS_IMAGE_TYPE) ||
        nd.getNewDocTypeId().endsWith(CAROUSEL_IMAGE_TYPE))
      {
        result.add(contentId ? nd.getContentId() : nd.getDocumentId());
      }
    }
    return result;
  }

  private static String getListImageId(NewView newRow, boolean contentId)
  {
    String result = null;
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(LIST_IMAGE_TYPE))
      {
        return (contentId ? nd.getContentId() : nd.getDocumentId());
      }
      else if (nd.getNewDocTypeId().endsWith(LIST_AND_DETAILS_IMAGE_TYPE))
      {
        result = (contentId ? nd.getContentId() : nd.getDocumentId());
      }
    }
    return result;
  }

  private static String getDetailsImageId(NewView newRow, boolean contentId)
  {
    String result = null;
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(DETAILS_IMAGE_TYPE))
      {
        return (contentId ? nd.getContentId() : nd.getDocumentId());
      }
      else if (nd.getNewDocTypeId().endsWith(LIST_AND_DETAILS_IMAGE_TYPE))
      {
        result = (contentId ? nd.getContentId() : nd.getDocumentId());
      }
      else if (nd.getNewDocTypeId().endsWith(CAROUSEL_AND_DETAILS_IMAGE_TYPE))
      {
        result = (contentId ? nd.getContentId() : nd.getDocumentId());
      }
    }
    return result;
  }

  private static String getCarouselImageId(NewView newRow, boolean contentId)
  {
    String result = null;
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(CAROUSEL_IMAGE_TYPE))
      {
        return (contentId ? nd.getContentId() : nd.getDocumentId());
      }
      else if (nd.getNewDocTypeId().endsWith(CAROUSEL_AND_DETAILS_IMAGE_TYPE))
      {
        result = (contentId ? nd.getContentId() : nd.getDocumentId());
      }
    }
    return result;
  }

  private static Set<Character> getHTMLIgnoredChars()
  {
    Set<Character> ignoredChars = new HashSet<>();
    ignoredChars.add('<');
    ignoredChars.add('>');
    ignoredChars.add('"');
    ignoredChars.add('&');
    return ignoredChars;
  }

  private static String putWildCards(String text)
  {
    return text.replaceAll(" ", "%");
  }

}
