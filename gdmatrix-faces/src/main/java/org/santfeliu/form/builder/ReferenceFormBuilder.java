package org.santfeliu.form.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class ReferenceFormBuilder extends MatrixFormBuilder
{
  private static final Entry[] REFERENCE_TABLE = new Entry[]
  {
    new Entry("html", "workflow.html", FORM_TYPEID, HtmlForm.class),
    new Entry("form", "workflow.form", FORM_TYPEID, HtmlForm.class)
  };

  public List<FormDescriptor> findForms(String selectorBase)
  {
    try
    {
      Entry entry = getEntry(selectorBase);
      if (entry != null)
      {
        String name = entry.getName(selectorBase);
        List<Document> documents = findDocuments(entry, name);
        if (!documents.isEmpty())
        {
          List<FormDescriptor> descriptors = new ArrayList(documents.size());
          for (Document document : documents)
          {
            FormDescriptor descriptor = new FormDescriptor();
            String title = document.getTitle();
            int index = title.indexOf(":");
            String selector = (index == -1) ?
              title : entry.prefix + ":" + title.substring(0, index);
            descriptor.setTitle(title);
            descriptor.setSelector(selector);
            descriptors.add(descriptor);
          }
          return descriptors;
        }
      }
    }
    catch (Exception ex)
    {
      // return empty list
    }
    return Collections.EMPTY_LIST;
  }

  public Form getForm(String selector)
  {
    try
    {
      Entry entry = getEntry(selector);
      if (entry != null)
      {
        String name = entry.getName(selector);
        List<Document> documents = findDocuments(entry, name);
        if (!documents.isEmpty())
        {
          Document document = documents.get(0);
          String contentId = document.getContent().getContentId();

          Form form = (Form)entry.formClass.newInstance();
          form.read(getDocumentStream(contentId));
          setup(form);
          return form;
        }
      }
    }
    catch (Exception ex)
    {
      // return null
    }
    return null;
  }

  private Entry getEntry(String selector)
  {
    if (selector == null) return null;
    
    Entry entry = null;
    int i = 0;
    while (i < REFERENCE_TABLE.length && entry == null)
    {
      Entry current = REFERENCE_TABLE[i];
      if (selector.startsWith(current.prefix + ":")) entry = current;
      i++;
    }
    return entry;
  }
  
  private List<Document> findDocuments(Entry entry, String name)
    throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setVersion(0);
    filter.setDocTypeId(entry.docTypeId);
    Property property = new Property();
    property.setName(entry.property);
    property.getValue().add(name);
    filter.getProperty().add(property);
    OrderByProperty orderBy = new OrderByProperty();
    orderBy.setName(DocumentConstants.TITLE);
    orderBy.setDescending(false);
    filter.getOrderByProperty().add(orderBy);
    DocumentManagerClient client = getDocumentManagerClient();
    return client.findDocuments(filter);
  }

  static class Entry
  {
    String prefix;
    String property;
    String docTypeId;
    Class formClass;

    Entry(String prefix, String property, String docTypeId, Class formClass)
    {
      this.prefix = prefix;
      this.property = property;
      this.docTypeId = docTypeId;
      this.formClass = formClass;
    }

    String getName(String selector)
    {
      return selector.substring(prefix.length() + 1);
    }
  }
}
