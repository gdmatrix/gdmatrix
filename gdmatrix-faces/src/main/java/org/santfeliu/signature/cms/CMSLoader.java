package org.santfeliu.signature.cms;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
@Deprecated
public class CMSLoader
{
  public String getCMSContentId(String id, String propertyName)
    throws Exception
  {
    String contentId = null;
    DocumentManagerClient client = getDocumentManagerClient();
    if ("contentId".equals(propertyName))
    {
      contentId = id;
    }
    else if ("docId".equals(propertyName))
    {
      String docId = id;
      Document document = client.loadDocument(docId, 0, ContentInfo.ID);
      contentId = DocumentUtils.getContentId(document);
    }
    else
    {
      // find by property
      Document document = 
        client.loadDocumentByName(null, propertyName, id, null, 0);
      contentId = DocumentUtils.getContentId(document);
    }
    return contentId;
  }

  public CMSData loadCMSByContentId(String contentId) throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    Content content = client.loadContent(contentId);
    if (content == null || content.getData() == null)
      throw new Exception("Document not found");

    DataHandler dh;

    String contentType = content.getContentType();
    if ("application/pdf".equals(contentType))
    {
      TransformationRequest request = new TransformationRequest();
      request.setTargetContentType("application/p7m");
      dh = TransformationManager.transform(content, request);
    }
    else
    {
      dh = content.getData();
    }
    InputStream is = new BufferedInputStream(dh.getInputStream());
    return new CMSData(is);
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }

  public static void main(String[] args)
  {
    try
    {
      CMSLoader l = new CMSLoader();
      System.out.println(l.loadCMSByContentId("29decc99-dd79-4223-80cb-c4d4d238f6a3"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
