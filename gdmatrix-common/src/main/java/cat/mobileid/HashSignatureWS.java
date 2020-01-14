package cat.mobileid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author realor
 */
public class HashSignatureWS extends MobileIdWS
{
  public HashSignatureWS(String endpoint, KeyStore keyStore, 
    char[] keyStorePassword) throws Exception
  {
    super(endpoint, keyStore, keyStorePassword);
  }  

  public Map<String, String> signHash(int policy, int docType, String docNum, 
    String subject, int source, String hash, String urlFile, String imageId)
    throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("policy", policy);
    variables.put("docType", docType);
    variables.put("docNum", docNum);
    variables.put("subject", subject);
    variables.put("source", source);
    variables.put("hash", hash);
    variables.put("urlFile", urlFile);
    variables.put("imageId", imageId);
    return post("signHash", variables);
  }
  
  public Map<String, String> checkSignHash(String ticket) throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("ticket", ticket);
    return post("checkSignHash", variables);
  }
  
  public static void main(String[] args)
  {
    try
    {
      String password = "******";
      char[] keyStorePassword = password.toCharArray();

      File certificateDir = new File("c:/matrix/conf/certificates");
      File certificateFile = new File(certificateDir, "mobileid.p12");
      KeyStore ks = KeyStore.getInstance("PKCS12");
      InputStream is = new FileInputStream(certificateFile);
      try
      {
        ks.load(is, keyStorePassword);
      }
      finally
      {
        is.close();
      }
      HashSignatureWS client = new HashSignatureWS(
        "https://int.mobileid.cat:443/ridm/services/hashSignatureServiceWS", ks, keyStorePassword);

      // NOTE: hash must be SHA1 40 hexadecimal chars length (160 binary bits).
      // urlSignedFile is located at https://int.mobileid.cat/public/ridm/
      Map<String, String> result = client.signHash(5, MobileIdWS.DOCTYPE_NIF,
      "NNNNNNNNX", "prova de signatura", 1, "261C5AD45770CC14875C8F46EAA3ECA42568104A", 
      "http://xxxxxx/signatures/2309915-92265718-0009-41ea-985a-1e1a7cd67d7c", "1");
      String ticket = result.get("ticket");
      System.out.println(result);
      System.out.println("ENTER PIN... (waiting for 30 seconds)");
      
      Thread.sleep(30000);
      result = client.checkSignHash(ticket);
      System.out.println(result);
      System.out.println("URL: " + client.getSignedDocumentUrl(result));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
