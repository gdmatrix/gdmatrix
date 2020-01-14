package cat.mobileid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author realor
 */
public class PdfSignatureWS extends MobileIdWS
{
  public PdfSignatureWS(String endpoint, KeyStore keyStore, 
    char[] keyStorePassword) throws Exception
  {
    super(endpoint, keyStore, keyStorePassword);
  }  

  public Map<String, String> signPdf(int policy, int docType, String docNum, 
    String subject, int source, File pdf)
    throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("policy", policy);
    variables.put("docType", docType);
    variables.put("docNum", docNum);
    variables.put("subject", subject);
    variables.put("source", source);
    byte[] base64 = Base64.encodeBase64(
      IOUtils.toByteArray(new FileInputStream(pdf)));
    variables.put("pdfb64", new String(base64));
    return post("signPdf", variables);
  }

  public Map<String, String> checkSignPdf(String ticket) throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("ticket", ticket);
    return post("checkSignPDF", variables);    
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
      PdfSignatureWS client = new PdfSignatureWS(
        "https://int.mobileid.cat:443/ridm/services/signatureServiceWS", ks, keyStorePassword);

      Map<String, String> result = client.signPdf(25, MobileIdWS.DOCTYPE_NIF, 
        "NNNNNNNNX", "prova de signatura", 1, new File("c:/test.pdf"));
      String ticket = result.get("ticket");
      System.out.println(result);
      System.out.println("ENTER PIN... (waiting for 30 seconds)");

      Thread.sleep(30000);
      result = client.checkSignPdf(ticket);
      System.out.println(result);
      System.out.println("URL: " + client.getSignedDocumentUrl(result));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
