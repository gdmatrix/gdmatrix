package org.santfeliu.misc.mapviewer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.activation.DataHandler;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.misc.mapviewer.io.SLDReader;
import org.santfeliu.misc.mapviewer.io.SLDWriter;
import org.santfeliu.misc.mapviewer.sld.SLDRoot;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;

/**
 *
 * @author realor
 */
public class SLDStore
{
  public static final String SLD_TYPEID = "SLD";
  public static final String SLD_PROPERTY_NAME = "sld_name";  

  public static SLDRoot createSld(List<String> layers, List<String> styles)
    throws Exception
  {
    InputStream is = SLDStore.class.getResourceAsStream("io/sld_template.xml");
    SLDReader reader = new SLDReader();
    SLDRoot root = reader.read(is);
    root.addNamedLayers(layers, styles);
    return root;
  }

  public static SLDRoot loadSld(String sldName) throws Exception
  {
    Document document = getSldDocument(sldName, true);    
    if (document == null) return null;    
    InputStream is = document.getContent().getData().getInputStream();
    SLDReader reader = new SLDReader();
    return (SLDRoot)reader.read(is);
  }

  public static void storeSld(String sldName, SLDRoot sld,
    String userId, String password) throws Exception
  {
    Document document = getSldDocument(sldName, true);
    if (document == null)
    {
      document = new Document();
      document.setDocTypeId(SLD_TYPEID);
      Property property = new Property();
      property.setName(SLD_PROPERTY_NAME);
      property.getValue().add(sldName);
      document.getProperty().add(property);
    }
    document.setTitle("Styled Layer Descriptor: " + sldName);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SLDWriter writer = new SLDWriter();
    writer.write(sld, bos);
    byte[] bytes = bos.toByteArray();
    MemoryDataSource ds = new MemoryDataSource(bytes, "sld", "text/xml");
    DataHandler dh = new DataHandler(ds);
    Content content = new Content();
    content.setData(dh);
    document.setContent(content);
    DocumentManagerClient client =
      new DocumentManagerClient(userId, password);
    client.storeDocument(document);
  }

  public static Document getSldDocument(String sldName, boolean content)
  {
    Document document = null;
    if (!StringUtils.isBlank(sldName))
    {
      DocumentManagerClient client = new DocumentManagerClient();
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(SLD_TYPEID);
      Property property = new Property();
      property.setName(SLD_PROPERTY_NAME);
      property.getValue().add(sldName);
      filter.getProperty().add(property);
      List<Document> documents = client.findDocuments(filter);
      if (!documents.isEmpty())
      {
        document = documents.get(0);
        if (content)
        {
          document = client.loadDocument(document.getDocId(), 0,
            ContentInfo.ALL);
        }
      }
    }
    return document;
  }

  public static String getSldURL(String sldName)
  {
    if (!StringUtils.isBlank(sldName))
    {
      Document document = getSldDocument(sldName, false);
      if (document != null)
      {
        String host = MatrixConfig.getProperty("org.santfeliu.web.hostname");
        if (host == null)
        {
          try
          {
            host = java.net.InetAddress.getLocalHost().getHostAddress();
          }
          catch (Exception ex)
          {
            host = "localhost";
          }
        }
        String contentId = document.getContent().getContentId();
        StringBuilder buffer = new StringBuilder();
        buffer.append("http://");
        buffer.append(host);
        buffer.append(":");
        buffer.append(MatrixConfig.getProperty("org.santfeliu.web.defaultPort"));
        buffer.append(MatrixConfig.getProperty("contextPath"));
        buffer.append("/documents/");
        buffer.append(contentId);
        buffer.append("/");
        try
        {
          buffer.append(URLEncoder.encode(sldName, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
          buffer.append(sldName);
        }
        buffer.append(".sld");
        return buffer.toString();
      }
    }
    return null;
  }
}
