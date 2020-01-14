package org.santfeliu.sign.web;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.Transformation;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import org.santfeliu.doc.transform.Transformer;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentReader;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.servlet.WorkServlet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class SignatureValidatorBean extends WebBean
{
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY =
    "header.docId";

  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY =
    "header.render";

  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY =
    "footer.docId";

  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY =
    "footer.render";

  @CMSProperty
  public static final String SIGNATURE_TRANSFORMATION_NAME_PROPERTY =
    "signatureTransformationName";

  public static final String SIGID_PARAM = "sigid";

  private String sigId;
  private transient Document document;
  private transient String signatures;
  private transient String viewUrl;

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

  public String getDownloadUrl()
  {
    return DocumentUrlBuilder.getDocumentUrl(document, true);
  }

  public String getViewUrl()
  {
    return viewUrl;
  }

  public String getSignatures()
  {
    return signatures;
  }

  public String getHeaderUrl()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String headerDocId = menuItem.getProperty(HEADER_DOCID_PROPERTY);
    return getDocumentServletURL() + headerDocId;
  }

  public String getFooterUrl()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String footerDocId = menuItem.getProperty(FOOTER_DOCID_PROPERTY);
    return getDocumentServletURL() + footerDocId;
  }

  public boolean isHeaderRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }

  public boolean isFooterRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FOOTER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
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

  @CMSAction
  public String show()
  {
    Map parameters = getExternalContext().getRequestParameterMap();
    sigId = (String)parameters.get(SIGID_PARAM);
    if (sigId != null)
    {
      validate();
    }
    return "signature_validator";
  }

  public String validate()
  {
    try
    {
      if (sigId != null && sigId.trim().length() > 0)
      {
        sigId = sigId.trim();
        DocumentManagerClient client = getDocumentManagerClient();
        document = client.loadDocumentByName(null, "sigId", sigId, null, 0);
        if (document == null) warn("doc:DOCUMENT_NOT_FOUND");
        else
        {
          findViewUrl();
          readSignatures();
        }
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

  private void readSignatures()
  {
    signatures = null;
    String transformationName = getSignatureTransformationName();
    if (transformationName == null) return;
    Content content = document.getContent();
    ArrayList<Transformer> transformers = new ArrayList<Transformer>();
    Transformation pattern = new Transformation(null, transformationName,
      content.getContentType(), content.getFormatId(), 
      "text/html", null, null, null);
    TransformationManager.findTransformers(pattern, transformers);
    Iterator<Transformer> iter = transformers.iterator();
    while (iter.hasNext() && signatures == null)
    {
      Transformer transformer = iter.next();
      try
      {
        // perform document transformation
        DataHandler data =
          transformer.transform(document, transformationName, null);

        // parse result with Tidy
        Tidy tidy = new Tidy();
        tidy.setInputEncoding("UTF-8");
        org.w3c.dom.Document dom =
          tidy.parseDOM(data.getInputStream(), System.out);
        Node node = dom.getElementsByTagName("body").item(0);

        // convert to String
        TransformerFactory transFactory = TransformerFactory.newInstance();
        javax.xml.transform.Transformer xmlTransformer =
          transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        xmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
          Node child = list.item(i);
          xmlTransformer.transform(
            new DOMSource(child), new StreamResult(buffer));
        }
        signatures = buffer.toString();
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void findViewUrl()
  {
    Content content = document.getContent();
    String contentType = content.getContentType();
    if ("application/pdf".equals(contentType))
    {
      viewUrl = DocumentUrlBuilder.getDocumentUrl(document);
    }
    else
    {
      // transformation to pdf
      TransformationRequest request = new TransformationRequest();
      request.setTargetContentType("application/pdf");
      Transformation tr = new Transformation(document, request);
      Transformation transformation =
        TransformationManager.findTransformations(tr, null);
      if (transformation != null)
      {
        String transformerId = transformation.getTransformerId();
        String transformationName = transformation.getName();
        String contentId = content.getContentId();
        viewUrl = WorkServlet.URL_PATTERN + getDocumentServletURL() +
          contentId + "?" + DocumentReader.TRANSFORM_WITH_PARAM + "=" +
          transformerId + "/" + transformationName;
      }
      else viewUrl = null;
    }
  }

  private String getDocumentServletURL()
  {
    return getExternalContext().getRequestContextPath() + "/documents/";
  }

  public String getSignatureTransformationName()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(SIGNATURE_TRANSFORMATION_NAME_PROPERTY);
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }
}
