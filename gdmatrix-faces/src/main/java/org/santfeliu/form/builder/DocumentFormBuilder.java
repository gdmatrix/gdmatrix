package org.santfeliu.form.builder;

import java.util.Collections;
import java.util.List;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class DocumentFormBuilder extends MatrixFormBuilder
{
  public static final String PREFIX = "doc";

  public List<FormDescriptor> findForms(String selector)
  {
    String docId = getDocId(selector);
    if (docId != null)
    {
      try
      {
        DocumentManagerClient client = getDocumentManagerClient();
        Document document = client.loadDocument(docId, 0, ContentInfo.ID);
        // check document is a form
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setTitle(document.getTitle());
        descriptor.setSelector(selector);
        return Collections.singletonList(descriptor);
      }
      catch (Exception ex)
      {
      }
    }
    return Collections.EMPTY_LIST;
  }

  public Form getForm(String selector)
  {
    String docId = getDocId(selector);
    if (docId != null)
    {
      try
      {
        DocumentManagerClient client = getDocumentManagerClient();
        Document document = client.loadDocument(docId, 0, ContentInfo.ID);
        // check document is a form
        String contentId = document.getContent().getContentId();
        HtmlForm form = new HtmlForm();
        form.read(getDocumentStream(contentId));
        setup(form);
        return form;
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  private String getDocId(String selector)
  {
    if (selector.startsWith(PREFIX + ":"))
    {
      String docId = selector.substring(PREFIX.length() + 1);
      return docId;
    }
    return null;
  }
}
