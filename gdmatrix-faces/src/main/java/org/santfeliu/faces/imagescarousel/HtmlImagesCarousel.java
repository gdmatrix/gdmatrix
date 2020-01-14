package org.santfeliu.faces.imagescarousel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.news.NewDocument;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.misc.widget.WidgetCache;
import org.santfeliu.news.web.NewsConfigBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Utilities;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class HtmlImagesCarousel extends UIComponentBase
{
  public static final String IMAGE_SERVLET_PATH = "/imgscale/";
  public static final String DOC_SERVLET_PATH = "/documents/";
  
  private String _caseId;
  private String _newId;
  @Deprecated
  private List _docId;
  private Integer _thumbnailCount;
  private Integer _thumbnailWindow;
  private Integer _transitionTime;
  private Integer _continueTime;  
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _var;
  private String _mainImageWidth;
  private String _mainImageHeight;
  private String _mainImageCrop;
  private String _thumbnailWidth;
  private String _thumbnailHeight;
  private String _thumbnailCrop;
  private Boolean _renderMainImage;
  private Boolean _renderThumbnails;
  private Boolean _renderNavLinks;
  private String _thumbnailShiftMode;
  private String _thumbnailHoverMode;
  private String _thumbnailClickMode;
  private String _mainImageClickMode;
  private List _imageId;

  public HtmlImagesCarousel()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "ImagesCarousel";
  }

  public String getCaseId()
  {
    if (_caseId != null) return _caseId;
    ValueBinding vb = getValueBinding("caseId");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setCaseId(String _caseId)
  {
    this._caseId = _caseId;
  }

  public String getNewId()
  {
    if (_newId != null) return _newId;
    ValueBinding vb = getValueBinding("newId");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setNewId(String _newId)
  {
    this._newId = _newId;
  }
  
  @Deprecated
  public List getDocId()
  {
    if (_docId != null) return _docId;
    ValueBinding vb = getValueBinding("docId");
    return vb != null ? (List)vb.getValue(getFacesContext()) : null;
  }

  @Deprecated
  public void setDocId(List _docId)
  {
    this._docId = _docId;
  }  
  
  public List getImageId()
  {
    if (_imageId != null) return _imageId;
    ValueBinding vb = getValueBinding("imageId");
    return vb != null ? (List)vb.getValue(getFacesContext()) : null;
  }

  public void setImageId(List _imageId)
  {
    this._imageId = _imageId;
  }   
  
  public int getThumbnailCount()
  {
    if (_thumbnailCount != null) return _thumbnailCount.intValue();
    ValueBinding vb = getValueBinding("thumbnailCount");
    if (vb != null)    
    {
      try
      {
        String sNumber = (String)vb.getValue(getFacesContext());      
        return Integer.parseInt(sNumber);
      }
      catch (NumberFormatException ex) { }
    }
    return 10;
  }

  public void setThumbnailCount(int _thumbnailCount)
  {
    this._thumbnailCount = _thumbnailCount;
  }

  public int getThumbnailWindow()
  {
    if (_thumbnailWindow != null) return _thumbnailWindow.intValue();
    ValueBinding vb = getValueBinding("thumbnailWindow");
    if (vb != null)    
    {
      try
      {
        String sNumber = (String)vb.getValue(getFacesContext());      
        return Integer.parseInt(sNumber);
      }
      catch (NumberFormatException ex) { }
    }
    return 4;
  }

  public void setThumbnailWindow(int _thumbnailWindow)
  {
    this._thumbnailWindow = _thumbnailWindow;
  }

  public int getTransitionTime()
  {
    if (_transitionTime != null) return _transitionTime.intValue();
    ValueBinding vb = getValueBinding("transitionTime");
    if (vb != null)    
    {
      try
      {
        String sNumber = (String)vb.getValue(getFacesContext());      
        return Integer.parseInt(sNumber);
      }
      catch (NumberFormatException ex) { }
    }
    return 0;           
  }

  public void setTransitionTime(int _transitionTime)
  {
    this._transitionTime = _transitionTime;
  }

  public int getContinueTime()
  {
    if (_continueTime != null) return _continueTime.intValue();
    ValueBinding vb = getValueBinding("continueTime");
    if (vb != null)    
    {
      try
      {
        String sNumber = (String)vb.getValue(getFacesContext());      
        return Integer.parseInt(sNumber);
      }
      catch (NumberFormatException ex) { }
    }
    return 0;           
  }

  public void setContinueTime(int _continueTime)
  {
    this._continueTime = _continueTime;
  }
  
  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueBinding vb = getValueBinding("translator");
    return vb != null? (Translator) vb.getValue(getFacesContext()): null;
  }

  public void setTranslator(Translator _translator)
  {
    this._translator = _translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "imagesCarousel";
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public String getMainImageHeight()
  {
    if (_mainImageHeight != null) return _mainImageHeight;
    ValueBinding vb = getValueBinding("mainImageHeight");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "0";
  }

  public void setMainImageHeight(String _mainImageHeight)
  {
    this._mainImageHeight = _mainImageHeight;
  }

  public String getMainImageWidth()
  {
    if (_mainImageWidth != null) return _mainImageWidth;
    ValueBinding vb = getValueBinding("mainImageWidth");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "0";
  }

  public void setMainImageWidth(String _mainImageWidth)
  {
    this._mainImageWidth = _mainImageWidth;
  }

  public String getMainImageCrop()
  {
    if (_mainImageCrop != null) return _mainImageCrop;
    ValueBinding vb = getValueBinding("mainImageCrop");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "auto";
  }

  public void setMainImageCrop(String _mainImageCrop)
  {
    this._mainImageCrop = _mainImageCrop;
  }

  public String getThumbnailHeight()
  {
    if (_thumbnailHeight != null) return _thumbnailHeight;
    ValueBinding vb = getValueBinding("thumbnailHeight");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "0";
  }

  public void setThumbnailHeight(String _thumbnailHeight)
  {
    this._thumbnailHeight = _thumbnailHeight;
  }

  public String getThumbnailWidth()
  {
    if (_thumbnailWidth != null) return _thumbnailWidth;
    ValueBinding vb = getValueBinding("thumbnailWidth");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "0";
  }

  public void setThumbnailWidth(String _thumbnailWidth)
  {
    this._thumbnailWidth = _thumbnailWidth;
  }

  public String getThumbnailCrop()
  {
    if (_thumbnailCrop != null) return _thumbnailCrop;
    ValueBinding vb = getValueBinding("thumbnailCrop");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "auto";
  }

  public void setThumbnailCrop(String _thumbnailCrop)
  {
    this._thumbnailCrop = _thumbnailCrop;
  }
  
  public Boolean isRenderMainImage()
  {
    if (_renderMainImage != null) return _renderMainImage;
    ValueBinding vb = getValueBinding("renderMainImage");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v : Boolean.TRUE;
  }

  public void setRenderMainImage(Boolean _renderMainImage)
  {
    this._renderMainImage = _renderMainImage;
  }

  public Boolean isRenderThumbnails()
  {
    if (_renderThumbnails != null) return _renderThumbnails;
    ValueBinding vb = getValueBinding("renderThumbnails");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v : Boolean.TRUE;
  }

  public void setRenderThumbnails(Boolean _renderThumbnails)
  {
    this._renderThumbnails = _renderThumbnails;
  }  

  public Boolean isRenderNavLinks()
  {
    if (_renderNavLinks != null) return _renderNavLinks;
    ValueBinding vb = getValueBinding("renderNavLinks");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v : Boolean.TRUE;
  }

  public void setRenderNavLinks(Boolean _renderNavLinks)
  {
    this._renderNavLinks = _renderNavLinks;
  }

  public String getThumbnailShiftMode()
  {
    if (_thumbnailShiftMode != null) return _thumbnailShiftMode;
    ValueBinding vb = getValueBinding("thumbnailShiftMode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "thumbnail";
  }

  public void setThumbnailShiftMode(String _thumbnailShiftMode)
  {
    this._thumbnailShiftMode = _thumbnailShiftMode;
  }

  public String getThumbnailHoverMode()
  {
    if (_thumbnailHoverMode != null) return _thumbnailHoverMode;
    ValueBinding vb = getValueBinding("thumbnailHoverMode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "select";
  }

  public void setThumbnailHoverMode(String _thumbnailHoverMode)
  {
    this._thumbnailHoverMode = _thumbnailHoverMode;
  }

  public String getThumbnailClickMode()
  {
    if (_thumbnailClickMode != null) return _thumbnailClickMode;
    ValueBinding vb = getValueBinding("thumbnailClickMode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "selectAndOpen";
  }

  public void setThumbnailClickMode(String _thumbnailClickMode)
  {
    this._thumbnailClickMode = _thumbnailClickMode;
  }

  public String getMainImageClickMode()
  {
    if (_mainImageClickMode != null) return _mainImageClickMode;
    ValueBinding vb = getValueBinding("mainImageClickMode");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "open";
  }

  public void setMainImageClickMode(String _mainImageClickMode)
  {
    this._mainImageClickMode = _mainImageClickMode;
  }
  
  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    /*
    if (_caseId != null)
    {
      ValueBinding vb = getValueBinding("caseId");
      if (vb != null)
      {
        if (!vb.isReadOnly(context))
        {
          vb.setValue(context, _caseId);
          _caseId = null;
        }
      }
    }
    if (_newId != null)
    {
      ValueBinding vb = getValueBinding("newId");
      if (vb != null)
      {
        if (!vb.isReadOnly(context))
        {
          vb.setValue(context, _newId);
          _newId = null;
        }
      }
    }
    */
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    //String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      List<ImageItem> itemList;
      String nodeId = (String)getAttributes().get("nodeId");
      if (nodeId != null)
      {
        String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();        
        String username = UserSessionBean.getCurrentInstance().getUsername();        
        itemList = WidgetCache.getInstance().getWidgetObjects(workspaceId, nodeId, username);
        if (itemList == null)
        {
          itemList = getImageItemList();
          WidgetCache.getInstance().setWidgetObjects(workspaceId, nodeId, username, itemList);
        }
      }
      else
      {
        itemList = getImageItemList();      
      }
            
      if (itemList != null && itemList.size() > 0)
      {
        Translator translator = getTranslator();
        writer.startElement("div", this); 
        String style = getStyle();
        if (style != null) writer.writeAttribute("style", style, null);
        String styleClass = getStyleClass();
        if (styleClass != null) writer.writeAttribute("class", styleClass, null);
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeAttribute("src", "/plugins/imagescarousel/imagescarousel.js", null);
        writer.endElement("script");
        if (isRenderMainImage())
        {
          encodeImageArea(writer, translator, itemList.get(0));
        }
        if (isRenderThumbnails())
        {
          encodeThumbnailsArea(writer, translator, itemList);
        }
        if (isRenderNavLinks() && itemList.size() > 1)
        {
          encodeNavLinks(writer);
        }        
        encodeJavaScriptInit(writer, itemList);
        writer.endElement("div");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_IMAGES", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[26];
    values[0] = super.saveState(context);
    values[1] = _caseId;
    values[2] = _newId;
    values[3] = _thumbnailCount;
    values[4] = _thumbnailWindow;
    values[5] = _transitionTime;
    values[6] = _continueTime;
    values[7] = _translator;
    values[8] = _translationGroup;
    values[9] = _style;
    values[10] = _styleClass;
    values[11] = _var;
    values[12] = _mainImageWidth ;
    values[13] = _mainImageHeight;
    values[14] = _mainImageCrop;
    values[15] = _thumbnailWidth;
    values[16] = _thumbnailHeight;
    values[17] = _thumbnailCrop;
    values[18] = _renderMainImage;
    values[19] = _renderThumbnails;
    values[20] = _renderNavLinks;    
    values[21] = _thumbnailShiftMode;
    values[22] = _thumbnailHoverMode;
    values[23] = _thumbnailClickMode;
    values[24] = _mainImageClickMode;
    values[25] = _imageId;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _caseId = (String)values[1];
    _newId = (String)values[2];
    _thumbnailCount = (Integer)values[3];
    _thumbnailWindow = (Integer)values[4];
    _transitionTime = (Integer)values[5];
    _continueTime = (Integer)values[6];
    _translator = (Translator)values[7];
    _translationGroup = (String)values[8];
    _style = (String)values[9];
    _styleClass = (String)values[10];
    _var = (String)values[11];
    _mainImageWidth = (String)values[12];
    _mainImageHeight = (String)values[13];
    _mainImageCrop = (String)values[14];    
    _thumbnailWidth = (String)values[15];
    _thumbnailHeight = (String)values[16];
    _thumbnailCrop = (String)values[17];    
    _renderMainImage = (Boolean)values[18];    
    _renderThumbnails = (Boolean)values[19];
    _renderNavLinks = (Boolean)values[20];    
    _thumbnailShiftMode = (String)values[21];    
    _thumbnailHoverMode = (String)values[22];
    _thumbnailClickMode = (String)values[23];
    _mainImageClickMode = (String)values[24];   
    _imageId = (List)values[25];
  }

//Private
  
  private void encodeImageArea(ResponseWriter writer, Translator translator, ImageItem item) throws IOException
  {
    Locale locale = getFacesContext().getViewRoot().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.imagescarousel.resources.ImagesCarouselBundle", locale);     
    writer.startElement("div", this);
    writer.writeAttribute("class", "mainImageArea", null);
    if ("open".equals(getMainImageClickMode()))
    {      
      writer.startElement("a", this);
      writer.writeAttribute("id", "imagesCarouselMainLink" + "_" + getId(), null);
      writer.writeAttribute("href", item.getURL(), null);
      writer.writeAttribute("target", "_blank", null);
    }    
    writer.startElement("img", this);
    writer.writeAttribute("id", "imagesCarouselMainImage" + "_" + getId(), null);
    writer.writeAttribute("src", item.getMainURL(), null);
    
    String imageAltText = ("open".equals(getMainImageClickMode()) ? 
      bundle.getString("openNewTab") : getTranslation(item.getDescription(), translator));   
    writer.writeAttribute("alt", imageAltText, null);    
    writer.endElement("img");
    if ("open".equals(getMainImageClickMode()))
    {  
      writer.endElement("a");
    }    
    writer.endElement("div");
  }

  private void encodeThumbnailsArea(ResponseWriter writer, Translator translator, List<ImageItem> itemList) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "thumbnailsArea", null);
    int index = 1;
    for (ImageItem item : itemList)
    {
      writer.startElement("div", this);
      writer.writeAttribute("id", "imagesCarouselThumbnailDiv" + index + "_" + getId(), null);      
      writer.writeAttribute("class", "thumbnail", null);
      writer.writeAttribute("onmouseover", getJSVarName() + ".thumbnailMouseOver(" + index + ");", null);
      writer.writeAttribute("onmouseout", getJSVarName() + ".thumbnailMouseOut();", null);
      writer.writeAttribute("onclick", getJSVarName() + ".thumbnailMouseClick(" + index + ");", null);      
      if ("selectAndOpen".equals(getThumbnailClickMode()))
      {
        writer.startElement("a", this);
        writer.writeAttribute("href", item.getURL(), null);
        writer.writeAttribute("target", "_blank", null);
      }
      writer.startElement("img", this);
      writer.writeAttribute("id", "imagesCarouselThumbnailImg" + index + "_" + getId(), null);
      writer.writeAttribute("src", item.getThumbnailURL(), null);      
      writer.writeAttribute("alt", getTranslation(item.getDescription(), translator), null);
      writer.endElement("img"); 
      if ("selectAndOpen".equals(getThumbnailClickMode()))
      {  
        writer.endElement("a");
      }
      writer.endElement("div");
      index++;
    }
    writer.endElement("div");  
  }
  
  private void encodeNavLinks(ResponseWriter writer) throws Exception
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "goLeft", null);
    writer.writeAttribute("onclick", getJSVarName() + ".goLeft();", null);    
    writer.endElement("div");
    writer.startElement("div", this);
    writer.writeAttribute("class", "goRight", null);
    writer.writeAttribute("onclick", getJSVarName() + ".goRight();", null);    
    writer.endElement("div");
  }
  
  private void encodeJavaScriptInit(ResponseWriter writer, List<ImageItem> itemList) throws Exception
  {
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("var " + getJSVarName() + " = new ImagesCarousel('" + getId() + "');", null);    
    writer.writeText(getJSVarName() + ".thumbnailCount = " + itemList.size() + ";", null);
    writer.writeText(getJSVarName() + ".thumbnailWindow = " + getCurrentThumbnailWindow(itemList) + ";", null);
    writer.writeText(getJSVarName() + ".intervalId = " + System.currentTimeMillis() + ";", null);
    writer.writeText(getJSVarName() + ".index = 1;", null);
    writer.writeText(getJSVarName() + ".thumbnailFirstIndex = 1;", null);
    writer.writeText(getJSVarName() + ".thumbnailLastIndex = " + getCurrentThumbnailWindow(itemList) + ";", null);
    writer.writeText(getJSVarName() + ".transitionTime = " + getTransitionTime() + ";", null);
    writer.writeText(getJSVarName() + ".continueTime = " + getContinueTime() + ";", null);
    writer.writeText(getJSVarName() + ".thumbnailShiftMode = '" + getThumbnailShiftMode() + "';", null);    
    writer.writeText(getJSVarName() + ".thumbnailHoverMode = '" + getThumbnailHoverMode() + "';", null);    
    writer.writeText(getJSVarName() + ".thumbnailClickMode = '" + getThumbnailClickMode() + "';", null);    
    writer.writeText(getJSVarName() + ".mainImageClickMode = '" + getMainImageClickMode() + "';", null);    
    writer.writeText(getJSVarName() + ".renderMainImage = " + String.valueOf(isRenderMainImage()) + ";", null);    
    writer.writeText(getJSVarName() + ".renderThumbnails = " + String.valueOf(isRenderThumbnails()) + ";", null);    
    writer.writeText(getJSVarName() + ".mainImageURLArray = [];", null);
    writer.writeText(getJSVarName() + ".externalImageURLArray = [];", null);
    //writer.writeText("var imagesCarouselThumbnailURLArray = [];", null);
    int i = 0;
    for (ImageItem item : itemList)
    {
      writer.writeText(getJSVarName() + ".mainImageURLArray[" + i + "] = '" +
        item.getMainURL() + "';", null);
      writer.writeText(getJSVarName() + ".externalImageURLArray[" + i + "] = '" +
        item.getURL() + "';", null);
//      writer.writeText("imagesCarouselThumbnailURLArray[" + i + "] = '" +
//        item.getThumbnailURL() + "';", null);
      i++;
    }
    writer.writeText(getJSVarName() + ".changeImage(1);", null);
    if (isRenderThumbnails())
    {
      writer.writeText(getJSVarName() + ".updateThumbnailsVisibility(1, " + getCurrentThumbnailWindow(itemList) + ");", null);    
    }    
    writer.writeText(getJSVarName() + ".go();", null);
    writer.endElement("script");    
  }  
  
  private String getJSVarName()
  {
    return "imagesCarousel_" + getId();
  }
  
  private int getCurrentThumbnailWindow(List<ImageItem> itemList)
  {
    return (itemList.size() >= getThumbnailWindow() ? getThumbnailWindow() : itemList.size());
  }
  
  private List<ImageItem> getImageItemList() throws Exception
  {
    List<ImageItem> itemList = new ArrayList<ImageItem>();
    if (getCaseId() != null)
    {
      itemList = getCaseImages();
    }
    else if (getNewId() != null)
    {
      itemList = getNewImages();
    }
    else if (getDocId() != null)
    {
      itemList = getDocIdImages();
    }
    else if (getImageId() != null)
    {
      itemList = getImageIdImages();
    }
    else
    {
      throw new Exception("INVALID_COMPONENT_CONFIG");
    }
    return itemList;
  }
  
  private List<ImageItem> getCaseImages() throws Exception
  {
    List<ImageItem> itemList = new ArrayList<ImageItem>();    

    CaseDocumentFilter filter = new CaseDocumentFilter();
    filter.setCaseId(getCaseId());    

    List<CaseDocumentView> cdvList =
      CaseConfigBean.getPort().findCaseDocumentViews(filter);
    
    Iterator it = cdvList.iterator();
    while (it.hasNext() && itemList.size() < getThumbnailCount())
    {
      CaseDocumentView cdv = (CaseDocumentView)it.next();
      if (isImage(cdv))
      {        
        ImageItem item = new ImageItem();
        item.setDocId(cdv.getDocument().getDocId());
        item.setDescription(cdv.getComments()); //TODO
        itemList.add(item);
      }      
    }
    return itemList;
  }
  
  private List<ImageItem> getNewImages() throws Exception
  {
    List<ImageItem> itemList = new ArrayList<ImageItem>();    

    List<NewDocument> ndList = NewsConfigBean.getPort().findNewDocumentsFromCache(
          getNewId(), null);

    Iterator it = ndList.iterator();
    while (it.hasNext() && itemList.size() < getThumbnailCount())
    {
      NewDocument nd = (NewDocument)it.next();
      if (isImage(nd))
      {        
        ImageItem item = new ImageItem();
        item.setDocId(nd.getDocumentId());
        item.setDescription(nd.getTitle()); //TODO
        itemList.add(item);
      }
    }
    return itemList;
  }

  private List<ImageItem> getDocIdImages() throws Exception
  {
    return getDocIdImages(getDocId());
  }
  
  private List<ImageItem> getDocIdImages(List<String> ids) throws Exception
  {
    List<ImageItem> itemList = new ArrayList<ImageItem>();    
    
    DocumentFilter filter = new DocumentFilter();
    filter.getDocId().addAll(ids);
    filter.setIncludeContentMetadata(true);
    List<Document> docList = DocumentConfigBean.getClient().findDocuments(filter);

    Iterator it = docList.iterator();
    while (it.hasNext() && itemList.size() < getThumbnailCount())
    {
      Document doc = (Document)it.next();
      if (isImage(doc))
      {        
        ImageItem item = new ImageItem();
        item.setDocId(doc.getDocId());
        item.setDescription(doc.getTitle()); //TODO
        itemList.add(item);
      }
    }
    return itemList;    
  }
  
  private List<ImageItem> getImageIdImages() throws Exception
  {
    List<ImageItem> itemList = new ArrayList<ImageItem>();    
    
    List<String> ids = getImageId();
    List<String> docIds = new ArrayList();
    if (ids != null && !ids.isEmpty())
    {
      for (String id : ids)
      {
        if (!Utilities.isUUID(id))           
          docIds.add(id);
        else
        {
          ImageItem item = new ImageItem();
          item.setDocId(id);
          itemList.add(item);        
        }
      }
      if (!docIds.isEmpty())
        itemList.addAll(getDocIdImages(docIds));
    }

    return itemList;    
  }
  
  private boolean isImage(CaseDocumentView cdv)
  {
    return isImage(cdv.getDocument());
  }
  
  private boolean isImage(NewDocument nd)
  {
    String ndTypeId = nd.getNewDocTypeId();
    return (ndTypeId.endsWith(NewsConfigBean.LIST_IMAGE_TYPE))
      ||
      (ndTypeId.endsWith(NewsConfigBean.DETAILS_IMAGE_TYPE))
      ||
      (ndTypeId.endsWith(NewsConfigBean.LIST_AND_DETAILS_IMAGE_TYPE))
      ||
      (ndTypeId.endsWith(NewsConfigBean.CAROUSEL_IMAGE_TYPE))
      ||
      (ndTypeId.endsWith(NewsConfigBean.CAROUSEL_AND_DETAILS_IMAGE_TYPE));      
  }  

  private boolean isImage(Document document)
  {    
    if (document != null)
    {
      Content content = document.getContent();          
      if (content != null)
        return (content.getContentType() != null &&
          content.getContentType().startsWith("image"));
    }
    return false;
  }  
  
  private String getTranslation(String text, Translator translator) throws IOException
  {
    if (text != null)
    {      
      if (translator != null)
      {
        String userLanguage = FacesUtils.getViewLanguage();
        String translationGroup = getTranslationGroup();
        StringWriter sw = new StringWriter();
        translator.translate(new StringReader(text), sw, "text/plain",
          userLanguage, translationGroup);
        return sw.toString();
      }
      else
      {
        return text;
      }
    }
    else
    {
      return "";
    }    
  }
  
  class ImageItem
  {
    private String docId;
    private String description;

    public String getDocId()
    {
      return docId;
    }

    public void setDocId(String docId)
    {
      this.docId = docId;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }
    
    public String getURL()
    {
      if ((docId != null) && (docId.length() != 0))      
      {
        return MatrixConfig.getProperty("contextPath") +
          DOC_SERVLET_PATH + docId;
      }
      return "";
    }
    
    public String getMainURL()
    {
      if ((docId != null) && (docId.length() != 0))      
      {
        return MatrixConfig.getProperty("contextPath") +
          IMAGE_SERVLET_PATH + docId +
          "?width=" + getMainImageWidth() + 
          "&height=" + getMainImageHeight() + 
          "&crop=" + getMainImageCrop();      
      }
      return "";
    }
        
    public String getThumbnailURL()
    {
      if ((docId != null) && (docId.length() != 0))      
      {
        return MatrixConfig.getProperty("contextPath") +
          IMAGE_SERVLET_PATH + docId +
          "?width=" + getThumbnailWidth() + 
          "&height=" + getThumbnailHeight() + 
          "&crop=" + getThumbnailCrop();      
      }
      return "";
    }
  }

}
