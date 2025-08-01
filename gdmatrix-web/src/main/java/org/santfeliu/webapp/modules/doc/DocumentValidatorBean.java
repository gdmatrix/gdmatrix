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
package org.santfeliu.webapp.modules.doc;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */

@Named
@RequestScoped
public class DocumentValidatorBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY = "header.render";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY = "footer.render";
  
  private static final String OUTCOME = "/pages/doc/document_validator.xhtml";
  private static final String SIGID_PARAM = "sigid";  
  
  private String sigId;
  private transient Document document;

  public String getSigId()
  {
    return sigId;
  }

  public void setSigId(String sigId)
  {
    this.sigId = sigId;
  }

  public Document getDocument()
  {
    return document;
  }

  public void setDocument(Document document)
  {
    this.document = document;
  }
  
  public String getHeaderURL()
  {
    String headerDocId = getProperty(HEADER_DOCID_PROPERTY);
    return getDocumentServletURL() + headerDocId;
  }

  public String getFooterURL()
  {
    String footerDocId = getProperty(FOOTER_DOCID_PROPERTY);
    return getDocumentServletURL() + footerDocId;
  } 
  
  public boolean isHeaderRender()
  {
    return (getProperty(HEADER_DOCID_PROPERTY) != null);
  }
  
  public boolean isFooterRender()
  {
    return (getProperty(FOOTER_DOCID_PROPERTY) != null);
  } 
  
  public String getContent()
  {
    return OUTCOME;
  }    
    
  public String getDownloadURL()
  {
    return document != null ? getAuthcopyURL(true) : null;
  }

  public String getViewURL()
  {
    return document != null ? getAuthcopyURL(false) : null;
  }
  
  public String getSourceURL()
  {
    if (document == null)
      return null;
    
    String title = document.getTitle();
    Content content = document.getContent();
    return getDocumentServletURL() + content.getContentId() + "/" +
      DocumentUtils.getFilename(title, content.getContentType()) +
      "&saveas=" + DocumentUtils.getFilename(title, content.getContentType());
  }

  public String getLanguage()
  {
    Content content = document.getContent();
    if (content == null) return null;
    
    String language = content.getLanguage();
    if (language == null) return null;
    
    if (TranslationConstants.UNIVERSAL_LANGUAGE.equals(language))
      return null;
    
    Locale locale = new Locale(language);
    return locale.getDisplayLanguage(
      getFacesContext().getViewRoot().getLocale());
  }

  public String getSize()
  {
    Content content = document.getContent();
    return content == null ? "0" : 
      DocumentUtils.getSizeString(content.getSize());
  }  
  
  // action methods
  @CMSAction
  public String show()
  {
    try
    {
      Map parameters = getExternalContext().getRequestParameterMap();
      sigId = (String) parameters.get(SIGID_PARAM);
      if (sigId != null)
        validate(); 
      
      String template = UserSessionBean.getCurrentInstance().getTemplate();
      return "/templates/" + template + "/template.xhtml";      
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }
  
  public String validate()
  {
    try
    {
      if (sigId != null && !sigId.isBlank())
      {
        sigId = sigId.trim();
        DocumentManagerClient client = DocModuleBean.getClient(true);
        document = client.loadDocumentByName(null, "xmlSigId", sigId, null, 0);
        if (document == null)
          document = client.loadDocumentByName(null, "sigId", sigId, null, 0);
        if (document == null) warn("doc:DOCUMENT_NOT_FOUND");
      }
      else
      {
        warn("VALUE_NOT_SPECIFIED");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  private String getDocumentServletURL()
  {
    return getExternalContext().getRequestContextPath() + "/documents/";
  }  
  
  private String getAuthcopyURL(boolean downloadable)
  {  
    if (sigId == null || document == null)
      return null;
    
    String url = getContextPath() + "/reports/authcopy.pdf?sigId=" + sigId;
    if (downloadable)
    {
      String filename = DocumentUtils.getFilename(document.getTitle());
      url += "&saveas=" + filename + ".pdf";
    }
    return url;
  }    
}
