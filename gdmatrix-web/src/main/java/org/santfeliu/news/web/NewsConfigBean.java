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
package org.santfeliu.news.web;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.matrix.news.NewDocument;
import org.matrix.news.NewView;
import org.matrix.news.NewsManagerMetaData;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class NewsConfigBean implements Serializable
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

  private NewsManagerMetaData metaData;

  public NewsConfigBean()
  {
  }

  public NewsManagerMetaData getMetaData() throws Exception
  {
    if (metaData == null)
    {
      metaData = getPort().getManagerMetaData();
    }
    return metaData;
  }

  public static NewsManagerClient getPort() throws Exception
  {
    return new NewsManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());    
  }  
  
  public static NewsManagerClient getPort(String userId, String password) 
    throws Exception
  {
    return new NewsManagerClient(userId, password);
  }

  public static NewsManagerClient getPort(URL wsDirectoryURL, String userId, 
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
    List<NewDocument> result = new ArrayList<NewDocument>();
    for (NewDocument nd : newRow.getNewDocument())
    {
      if (nd.getNewDocTypeId().endsWith(EXTENDED_INFO_TYPE))
      {
        result.add(nd);
      }
    }
    return result;
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
    Set<Character> ignoredChars = new HashSet<Character>();
    ignoredChars.add(Character.valueOf('<'));
    ignoredChars.add(Character.valueOf('>'));
    ignoredChars.add(Character.valueOf('"'));      
    ignoredChars.add(Character.valueOf('&'));      
    return ignoredChars;
  }  

  private static String putWildCards(String text)
  {
    return text.replaceAll(" ", "%");
  }
  
}
