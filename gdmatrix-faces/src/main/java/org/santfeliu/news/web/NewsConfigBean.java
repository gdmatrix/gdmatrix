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
