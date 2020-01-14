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
public class AuthenticateWS extends MobileIdWS
{
  public AuthenticateWS(String endpoint, KeyStore keyStore, 
    char[] keyStorePassword) throws Exception
  {
    super(endpoint, keyStore, keyStorePassword);
  }

  public Map<String, String> authenticateUser(int docType, String docNum, 
    String subject, int source, int level, int minAge) throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("docType", docType);
    variables.put("docNum", docNum);
    variables.put("subject", subject);
    variables.put("source", source);
    variables.put("level", level);
    variables.put("minAge", minAge);
    return post("authenticateUser", variables);
  }
  
  public Map<String, String> checkAuthenticate(String ticket) throws Exception
  {
    HashMap variables = new HashMap();
    variables.put("ticket", ticket);
    return post("checkAuthenticate", variables);
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
      AuthenticateWS client;
      client = new AuthenticateWS(
        "https://int.mobileid.cat:443/ridm/services/authenticateWS", ks, keyStorePassword);
      System.out.println(client.checkAuthenticate("reZ5cAbB"));      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
}
