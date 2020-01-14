package cat.mobileid;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.santfeliu.util.template.Template;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public abstract class MobileIdWS
{
	// document types
  public static final int DOCTYPE_NIF = 0;
	public static final int DOCTYPE_CIF = 1;
	public static final int DOCTYPE_OTHERS = 2;

  // source
	public static final int SOURCE_INTEGRATIONCLIENT = 1;
  
	// Levels of accreditation
	public static final int LEVEL_REGISTERED = 0;
	public static final int LEVEL_RECOGNIZED = 1;
	public static final int LEVEL_CERTIFICATED = 2;
   
  protected String endpoint = "https://int.mobileid.cat:443/ridm/services/authenticateWS";
  protected final SSLSocketFactory sslSocketFactory;
  
  public MobileIdWS(String endpoint, KeyStore keyStore, char[] keyStorePassword) 
    throws Exception
  {
    this.endpoint = endpoint;

    final SSLContext sc = SSLContext.getInstance("TLSv1.2");
    final KeyManagerFactory kmf = KeyManagerFactory.getInstance(
      KeyManagerFactory.getDefaultAlgorithm());
    
    kmf.init(keyStore, keyStorePassword);
    sc.init(kmf.getKeyManagers(), null, null);
    this.sslSocketFactory = sc.getSocketFactory();
  }

  protected Map<String, String> post(String operation, Map values) 
    throws Exception
  {
    Map<String, String> result = new HashMap<String, String>();

    Reader reader = new InputStreamReader(getClass().
      getResourceAsStream(operation + ".xml"), "UTF-8");

    Template template = Template.create(reader);
    String soap = template.merge(values);
    System.out.println(soap);

    URL url = new URL(endpoint);
    HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    conn.setSSLSocketFactory(sslSocketFactory);
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("SOAPAction", operation);
    conn.setRequestProperty("Content-Type", "text/xml;charset=\"utf-8\"");      
    OutputStream os = conn.getOutputStream();
    os.write(soap.getBytes("UTF-8"));
    os.flush();
    InputStream in = conn.getInputStream();
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      Document document = docBuilder.parse(in);
      Node child = document.getDocumentElement();
      int i = 0;
      while (child != null && i < 4)
      {
        child = child.getFirstChild();
        i++;
      }
      while (child != null)
      {
        if (child instanceof Element)
        {
          Element element = (Element)child;
          String name = element.getNodeName();
          String value = element.getTextContent();
          result.put(name, value);
        }
        child = child.getNextSibling();
      }
    }
    finally
    {
      in.close();
    }
    return result;
  }
  
  public String getSignedDocumentUrl(Map<String, String> result)
  {
    if ("0".equals(result.get("error")))
    {
      String url = result.get("urlSignedFile");
      if (url.startsWith("/"))
      {
        int index = endpoint.indexOf("//");
        if (index != -1)
        {
          index = endpoint.indexOf("/", index + 2);
          if (index != -1)
          {
            url = endpoint.substring(0, index) + "/public/ridm" + url;
          }
        }
      }
      return url;
    }
    return null;
  }  
}
