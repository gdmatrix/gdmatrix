package org.santfeliu.form.builder;

import java.io.InputStream;
import org.matrix.doc.Content;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public abstract class MatrixFormBuilder extends AbstractFormBuilder
{
  public static final String FORM_TYPEID = "FORM";

  protected DocumentManagerClient getDocumentManagerClient() throws Exception
  {
    return getDocumentManagerClient(null, null);
  }

  protected DocumentManagerClient getDocumentManagerClient(
    String userId, String password) throws Exception
  {
    if (userId == null)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    return new DocumentManagerClient(userId, password);
  }

  protected InputStream getDocumentStream(String contentId)
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    Content content = client.loadContent(contentId);
    return content.getData().getInputStream();
  }
}
