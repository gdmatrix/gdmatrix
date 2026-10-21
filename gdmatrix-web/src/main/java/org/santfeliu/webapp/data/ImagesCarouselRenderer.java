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
package org.santfeliu.webapp.data;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.news.NewDocument;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.Translator;
import org.santfeliu.news.web.NewsConfigBean;
import org.santfeliu.util.MatrixConfig;

import org.santfeliu.util.Utilities;

public class ImagesCarouselRenderer
{

  public static final String IMAGE_SERVLET_PATH = "/imgscale/";
  public static final String DOC_SERVLET_PATH = "/documents/";

  public String render(Map<String, Object> params, Locale locale)
  {
    try
    {
      String caseId = (String) params.get("caseId");
      String newId = (String) params.get("newId");
      List<String> docId = (List<String>) params.get("docId");
      List<String> imageId = (List<String>) params.get("imageId");

      String id = getStringParam(params, "id", "carousel1");
      String style = (String) params.get("style");
      String styleClass = getStringParam(params, "styleClass", "imagesCarousel");

      int thumbnailCount = getIntParam(params, "thumbnailCount", 10);
      int thumbnailWindow = getIntParam(params, "thumbnailWindow", 4);
      int transitionTime = getIntParam(params, "transitionTime", 0);
      int continueTime = getIntParam(params, "continueTime", 0);

      String mainImageHeight = getStringParam(params, "mainImageHeight", "0");
      String mainImageWidth = getStringParam(params, "mainImageWidth", "0");
      String mainImageCrop = getStringParam(params, "mainImageCrop", "auto");
      String thumbnailHeight = getStringParam(params, "thumbnailHeight", "50");
      String thumbnailWidth = getStringParam(params, "thumbnailWidth", "50");
      String thumbnailCrop = getStringParam(params, "thumbnailCrop", "auto");

      boolean renderMainImage = getBooleanParam(params, "renderMainImage", true);
      boolean renderThumbnails = 
        getBooleanParam(params, "renderThumbnails", true);
      boolean renderNavLinks = getBooleanParam(params, "renderNavLinks", true);

      String thumbnailPrevLabel = 
        getStringParam(params, "thumbnailPrevLabel", "Mostrar miniatura anterior");
      String thumbnailNextLabel = 
        getStringParam(params, "thumbnailNextLabel", "Mostrar miniatura següent");
      String thumbnailPrevIconUrl = (String) params.get("thumbnailPrevIconUrl");
      String thumbnailNextIconUrl = (String) params.get("thumbnailNextIconUrl");

      String thumbnailShiftMode = 
        getStringParam(params, "thumbnailShiftMode", "thumbnail");      
      String thumbnailClickMode = 
        getStringParam(params, "thumbnailClickMode", "selectAndOpen");
      String mainImageClickMode = 
        getStringParam(params, "mainImageClickMode", "open");

      Translator translator = (Translator) params.get("translator");
      String translationGroup = (String) params.get("translationGroup");

      // Obtenemos la lista de imágenes usando los mismos criterios
      List<ImageItem> itemList = getImageItemList(caseId, newId, docId, imageId, 
        thumbnailCount, mainImageWidth, mainImageHeight, mainImageCrop, 
        thumbnailWidth, thumbnailHeight, thumbnailCrop);

      if (itemList == null || itemList.isEmpty())
      {
        return "";
      }

      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.faces.imagescarousel.resources.ImagesCarouselBundle", locale);

      StringBuilder html = new StringBuilder();
      html.append("<div");
      if (style != null)
      {
        html.append(" style=\"").append(style).append("\"");
      }
      if (styleClass != null)
      {
        html.append(" class=\"").append(styleClass).append("\"");
      }
      html.append(">");

      html.append("<script type=\"text/javascript\" "
        + "src=\"/plugins/imagescarousel/imagescarousel.js\"></script>");

      String jsVarName = "imagesCarousel_" + id;

      if (renderMainImage)
      {
        html.append(encodeImageArea(itemList.get(0), mainImageClickMode, id, 
          translator, bundle, locale, translationGroup));
      }
      if (renderThumbnails)
      {
        html.append(encodeThumbnailsArea(itemList, thumbnailClickMode, id, 
          jsVarName, translator, bundle, locale, translationGroup));
      }
      if (renderNavLinks && itemList.size() > 1)
      {
        html.append(encodeNavLinks(thumbnailPrevIconUrl, thumbnailNextIconUrl, 
          thumbnailPrevLabel, thumbnailNextLabel, jsVarName, translator, locale, 
          translationGroup));
      }

      html.append(encodeJavaScriptInit(itemList, id, jsVarName, thumbnailWindow, 
        transitionTime, continueTime, renderMainImage, renderThumbnails, 
        thumbnailShiftMode, thumbnailClickMode, mainImageClickMode, translator, bundle, locale, 
        translationGroup));
      
      html.append("</div>");

      return html.toString();

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return "<div class=\"error\">CAN_NOT_SHOW_IMAGES</div>";
    }
  }

  private String getStringParam(Map<String, Object> params, String key, 
    String defaultValue)
  {
    Object val = params.get(key);
    return val != null ? val.toString() : defaultValue;
  }

  private int getIntParam(Map<String, Object> params, String key, 
    int defaultValue)
  {
    Object val = params.get(key);
    if (val instanceof Number)
    {
      return ((Number) val).intValue();
    }
    try
    {
      if (val != null)
      {
        return Integer.parseInt(val.toString());
      }
    }
    catch (Exception ignored)
    {
    }
    return defaultValue;
  }

  private boolean getBooleanParam(Map<String, Object> params, String key, 
    boolean defaultValue)
  {
    Object val = params.get(key);
    if (val instanceof Boolean)
    {
      return (Boolean) val;
    }
    if (val != null)
    {
      return Boolean.parseBoolean(val.toString());
    }
    return defaultValue;
  }

  // Get data methods
  private List<ImageItem> getImageItemList(String caseId, String newId, 
    List<String> docId, List<String> imageId, int thumbnailCount, String mw, 
    String mh, String mc, String tw, String th, String tc) throws Exception
  {
    List<ImageItem> itemList = new ArrayList<>();
    if (caseId != null)
    {
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      List<CaseDocumentView> cdvList = 
        CaseConfigBean.getPort().findCaseDocumentViews(filter);
      for (CaseDocumentView cdv : cdvList)
      {
        if (itemList.size() >= thumbnailCount)
        {
          break;
        }
        if (cdv.getDocument() != null && cdv.getDocument().getContent() != null
          && cdv.getDocument().getContent().getContentType() != null
          && cdv.getDocument().getContent().getContentType().startsWith("image"))
        {
          ImageItem item = new ImageItem(cdv.getDocument().getDocId(), 
            cdv.getComments(), mw, mh, mc, tw, th, tc);
          itemList.add(item);
        }
      }
    }
    else if (newId != null)
    {
      List<NewDocument> ndList = 
        NewsConfigBean.getPort().findNewDocumentsFromCache(newId, null);
      for (NewDocument nd : ndList)
      {
        if (itemList.size() >= thumbnailCount)
        {
          break;
        }
        String ndTypeId = nd.getNewDocTypeId();
        if (ndTypeId != null && (ndTypeId.endsWith(NewsConfigBean.LIST_IMAGE_TYPE)
          || ndTypeId.endsWith(NewsConfigBean.DETAILS_IMAGE_TYPE)
          || ndTypeId.endsWith(NewsConfigBean.LIST_AND_DETAILS_IMAGE_TYPE)
          || ndTypeId.endsWith(NewsConfigBean.CAROUSEL_IMAGE_TYPE)
          || ndTypeId.endsWith(NewsConfigBean.CAROUSEL_AND_DETAILS_IMAGE_TYPE)))
        {
          ImageItem item = 
            new ImageItem(nd.getDocumentId(), nd.getTitle(), mw, mh, mc, tw, th, tc);
          itemList.add(item);
        }
      }
    }
    else if (docId != null && !docId.isEmpty())
    {
      itemList = getDocIdImages(docId, thumbnailCount, mw, mh, mc, tw, th, tc);
    }
    else if (imageId != null && !imageId.isEmpty())
    {
      List<String> docIds = new ArrayList<>();
      for (String id : imageId)
      {
        if (!Utilities.isUUID(id))
        {
          docIds.add(id);
        }
        else
        {
          itemList.add(new ImageItem(id, null, mw, mh, mc, tw, th, tc));
        }
      }
      if (!docIds.isEmpty())
      {
        itemList.addAll(getDocIdImages(docIds, thumbnailCount, mw, mh, mc, tw, th, tc));
      }
    }
    else
    {
      throw new Exception("INVALID_COMPONENT_CONFIG");
    }
    return itemList.size() <= thumbnailCount ? itemList : itemList.subList(0, thumbnailCount);
  }

  private List<ImageItem> getDocIdImages(List<String> ids, int thumbnailCount, 
    String mw, String mh, String mc, String tw, String th, String tc) 
    throws Exception
  {
    List<ImageItem> itemList = new ArrayList<>();
    DocumentFilter filter = new DocumentFilter();
    filter.getDocId().addAll(ids);
    filter.setIncludeContentMetadata(true);
    List<Document> docList = DocumentConfigBean.getClient().findDocuments(filter);
    for (Document doc : docList)
    {
      if (itemList.size() >= thumbnailCount)
      {
        break;
      }
      if (doc != null && doc.getContent() != null
        && doc.getContent().getContentType() != null
        && doc.getContent().getContentType().startsWith("image"))
      {
        ImageItem item = 
          new ImageItem(doc.getDocId(), doc.getTitle(), mw, mh, mc, tw, th, tc);
        itemList.add(item);
      }
    }
    return itemList;
  }

  private String getTranslation(String text, Translator translator, 
    Locale locale, String translationGroup) throws IOException
  {
    if (text == null)
    {
      return "";
    }
    if (translator != null)
    {
      String userLanguage = locale != null ? locale.getLanguage() : "es";
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain", 
        userLanguage, translationGroup);
      return sw.toString();
    }
    return text;
  }

  private String encodeImageArea(ImageItem item, String mainImageClickMode, 
    String id, Translator translator, ResourceBundle bundle, Locale locale, 
    String group) throws IOException
  {
    String linkDescription = 
      getTranslation(item.getDescription(), translator, locale, group).trim();
    if ("open".equals(mainImageClickMode))
    {
      linkDescription = bundle.getString("openNewTab") + 
        (linkDescription.isEmpty() ? "" : (": " + linkDescription));
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"mainImageArea\">");
    if ("open".equals(mainImageClickMode))
    {
      sb.append("<a id=\"imagesCarouselMainLink_")
        .append(id).append("\" href=\"")
        .append(item.getURL())
        .append("\" target=\"_blank\" title=\"")
        .append(linkDescription).append("\">");
    }
    sb.append("<img id=\"imagesCarouselMainImage_")
      .append(id).append("\" src=\"")
      .append(item.getMainURL())
      .append("\" alt=\"")
      .append(linkDescription)
      .append("\" />");
    
    if ("open".equals(mainImageClickMode))
    {
      sb.append("</a>");
    }
    
    sb.append("</div>");
    return sb.toString();
  }

  private String encodeThumbnailsArea(List<ImageItem> itemList, 
    String thumbnailClickMode, String id, String jsVarName, 
    Translator translator, ResourceBundle bundle, Locale locale, String group) 
    throws IOException
  {
    String openNewWindowLabel = 
      MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
    openNewWindowLabel = getTranslation(openNewWindowLabel, translator, locale, group);
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"thumbnailsArea\">");
    int index = 1;
    for (ImageItem item : itemList)
    {
      String desc = 
        getTranslation(item.getDescription(), translator, locale, group).trim();
      String title = bundle.getString("selectImage") + " " + index 
        + (desc.isEmpty() ? "" : (": " + desc));
      if ("selectAndOpen".equals(thumbnailClickMode))
      {
        title = title + " (" + openNewWindowLabel + ")";
      }
      sb.append("<a id=\"imagesCarouselThumbnailDiv")
        .append(index).append("_")
        .append(id)
        .append("\" class=\"thumbnail\" title=\"").append(title).append("\" ")
        .append("onmouseover=\"").append(jsVarName)
        .append(".thumbnailMouseOver(").append(index).append(");\" ")
        .append("onfocus=\"").append(jsVarName)
        .append(".thumbnailMouseOver(").append(index).append(");\" ")
        .append("onmouseout=\"").append(jsVarName)
        .append(".thumbnailMouseOut();\" ")
        .append("onblur=\"").append(jsVarName).append(".thumbnailMouseOut();\" ")
        .append("onclick=\"").append(jsVarName)
        .append(".thumbnailMouseClick(")
        .append(index).append(");\"");
      if ("selectAndOpen".equals(thumbnailClickMode))
      {
        sb.append(" href=\"").append(item.getURL())
          .append("\" target=\"_blank\"");
      }
      sb.append(">");
      sb.append("<img id=\"imagesCarouselThumbnailImg")
        .append(index).append("_")
        .append(id)
        .append("\" src=\"")
        .append(item.getThumbnailURL())
        .append("\" alt=\"")
        .append(title)
        .append("\" />");
      sb.append("</a>");
      index++;
    }
    sb.append("</div>");
    return sb.toString();
  }

  private String encodeNavLinks(String prevIcon, String nextIcon, 
    String prevLabelText, String nextLabelText, String jsVarName, 
    Translator translator, Locale locale, String group) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    if (prevIcon != null)
    {
      String prevLabel = getTranslation(prevLabelText, translator, locale, group);
      sb.append("<a class=\"goLeft\" title=\"").append(prevLabel)
        .append("\" onclick=\"").append(jsVarName).append(".goLeft();\">")
        .append("<img src=\"").append(prevIcon).append("\" alt=\"")
        .append(prevLabel).append("\" /></a>");
    }
    if (nextIcon != null)
    {
      String nextLabel = getTranslation(nextLabelText, translator, locale, group);
      sb.append("<a class=\"goRight\" title=\"").append(nextLabel)
        .append("\" onclick=\"").append(jsVarName).append(".goRight();\">")
        .append("<img src=\"").append(nextIcon).append("\" alt=\"")
        .append(nextLabel).append("\" /></a>");
    }
    return sb.toString();
  }

  private String encodeJavaScriptInit(List<ImageItem> itemList, String id, 
    String jsVarName, int thumbnailWindow, int transitionTime, int continueTime, 
    boolean renderMainImage, boolean renderThumbnails, String thumbnailShiftMode, 
    String thumbnailClickMode, String mainImageClickMode, Translator translator, 
    ResourceBundle bundle, Locale locale, String group) throws IOException
  {
    int windowSize = itemList.size() >= thumbnailWindow ? thumbnailWindow : itemList.size();
    StringBuilder sb = new StringBuilder();
    sb.append("<script type=\"text/javascript\">");
    sb.append("var ").append(jsVarName).append(" = new ImagesCarousel('")
      .append(id).append("');");
    sb.append(jsVarName).append(".thumbnailCount = ").append(itemList.size())
      .append(";");
    sb.append(jsVarName).append(".thumbnailWindow = ").append(windowSize)
      .append(";");
    sb.append(jsVarName).append(".intervalId = ")
      .append(System.currentTimeMillis()).append(";");
    sb.append(jsVarName).append(".index = 1;");
    sb.append(jsVarName).append(".thumbnailFirstIndex = 1;");
    sb.append(jsVarName).append(".thumbnailLastIndex = ")
      .append(windowSize).append(";");
    sb.append(jsVarName).append(".transitionTime = ")
      .append(transitionTime).append(";");
    sb.append(jsVarName).append(".continueTime = ")
      .append(continueTime).append(";");
    sb.append(jsVarName).append(".thumbnailClickMode = '")
      .append(thumbnailClickMode).append("';");    
    sb.append(jsVarName).append(".thumbnailShiftMode = '")
      .append(thumbnailShiftMode).append("';");
    sb.append(jsVarName).append(".mainImageClickMode = '")
      .append(mainImageClickMode).append("';");
    sb.append(jsVarName).append(".renderMainImage = ")
      .append(renderMainImage).append(";");
    sb.append(jsVarName).append(".renderThumbnails = ")
      .append(renderThumbnails).append(";");
    sb.append(jsVarName).append(".mainImageArray = [];");

    int i = 0;
    for (ImageItem item : itemList)
    {
      String title = getMainImageItemTitle(item, mainImageClickMode, translator, bundle, locale, group);
      sb.append(jsVarName).append(".mainImageArray[").append(i).append("] = {};");
      sb.append(jsVarName).append(".mainImageArray[")
        .append(i).append("].docURL = '").append(item.getMainURL()).append("';");
      sb.append(jsVarName).append(".mainImageArray[")
        .append(i).append("].externalURL = '").append(item.getURL()).append("';");
      sb.append(jsVarName).append(".mainImageArray[")
        .append(i).append("].title = '").append(title.replace("'", "\\'"))
        .append("';");
      i++;
    }
    sb.append(jsVarName).append(".changeImage(1);");
    if (renderThumbnails)
    {
      sb.append(jsVarName).append(".updateThumbnailsVisibility(1, ")
        .append(windowSize).append(");");
    }
    sb.append(jsVarName).append(".go();");
    sb.append("</script>");
    return sb.toString();
  }

  private String getMainImageItemTitle(ImageItem item, 
    String mainImageClickMode, Translator translator, ResourceBundle bundle, 
    Locale locale, String group) throws IOException
  {
    String title = 
      getTranslation(item.getDescription(), translator, locale, group).trim();
    if ("open".equals(mainImageClickMode))
    {
      title = 
        bundle.getString("openNewTab") + (title.isEmpty() ? "" : (": " + title));
    }
    return title;
  }

  public static class ImageItem
  {
    private String docId;
    private String description;
    private String mw, mh, mc, tw, th, tc;

    public ImageItem(String docId, String description, String mw, String mh, 
      String mc, String tw, String th, String tc)
    {
      this.docId = docId;
      this.description = description;
      this.mw = mw;
      this.mh = mh;
      this.mc = mc;
      this.tw = tw;
      this.th = th;
      this.tc = tc;
    }

    public String getDocId()
    {
      return docId;
    }

    public String getDescription()
    {
      return description != null ? description : "";
    }

    public String getURL()
    {
      if (docId != null && !docId.isEmpty())
      {
        return MatrixConfig.getProperty("contextPath") 
          + DOC_SERVLET_PATH + docId;
      }
      return "";
    }

    public String getMainURL()
    {
      if (docId != null && !docId.isEmpty())
      {
        return MatrixConfig.getProperty("contextPath") 
          + IMAGE_SERVLET_PATH + docId
          + "?width=" + mw + "&height=" + mh + "&crop=" + mc;
      }
      return "";
    }

    public String getThumbnailURL()
    {
      if (docId != null && !docId.isEmpty())
      {
        return MatrixConfig.getProperty("contextPath") 
          + IMAGE_SERVLET_PATH + docId
          + "?width=" + tw + "&height=" + th + "&crop=" + tc;
      }
      return "";
    }
  }
}
