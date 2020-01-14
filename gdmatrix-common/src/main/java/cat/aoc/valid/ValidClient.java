package cat.aoc.valid;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author realor
 */
public class ValidClient
{
  private String baseUrl = "https://identitats.aoc.cat";
  private String clientId;
  private String clientSecret;
  private String redirectUrl;
  
  public ValidClient()
  {   
  }

  public String getBaseUrl()
  {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  public String getClientId()
  {
    return clientId;
  }

  public void setClientId(String clientId)
  {
    this.clientId = clientId;
  }

  public String getClientSecret()
  {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret)
  {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUrl()
  {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl)
  {
    this.redirectUrl = redirectUrl;
  }
  
	public String generateOAuthLoginUrl(String state) 
    throws UnsupportedEncodingException
  {
		return baseUrl +
			"/o/oauth2/auth?" +
			"scope=autenticacio_usuari" +
			"&redirect_uri=" + urlEncode(redirectUrl) +
			"&response_type=code" +
			"&client_id=" + urlEncode(clientId) +
			"&approval_prompt=auto" +
			"&state=" + state;
	}

  public JSONObject getAccessToken(String authorizationCode) throws Exception
  {
    URL url = new URL(baseUrl + "/o/oauth2/token");
    Map<String, String> params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("code", authorizationCode);
    params.put("grant_type", "authorization_code");
    params.put("redirect_uri", redirectUrl);

    StringBuilder postData = new StringBuilder();
    for (Map.Entry<String, String> param : params.entrySet()) 
    {
      if (postData.length() != 0) postData.append('&');
      postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
      postData.append('=');
      postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
    }
    byte[] postDataBytes = postData.toString().getBytes("UTF-8");

    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
    conn.setDoOutput(true);
    conn.getOutputStream().write(postDataBytes);
    JSONObject result = readJSON(conn);
    conn.disconnect();    
    return result;
  }

  public JSONObject getUserInfo(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/getUserInfo?AccessToken=" + 
      accessToken);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    JSONObject result = readJSON(conn);
    conn.disconnect();
    return result;
  }
  
  public String revokeAccessToken(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/o/oauth2/revoke?token=" + accessToken);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    String result = readResponse(conn);
    conn.disconnect();
    return result;
  }

  public String logoutAuthorizationCode(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/o/oauth2/logout?token=" + accessToken);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    String result = readResponse(conn);
    conn.disconnect();
    return result;
  }

  public JSONObject getBasicSignature(String accessToken, 
    List<DocumentToSign> documents) throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/getBasicSignature");
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("accessToken", accessToken);
    params.put("documents", documents);
    String json = JSONObject.toJSONString(params);
    System.out.println(json);
    byte[] data = json.getBytes("UTF-8");

    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Content-Length", String.valueOf(data.length));
    conn.setDoOutput(true);
    conn.getOutputStream().write(data);
    JSONObject result = readJSON(conn);
    conn.disconnect();
    return result;
  }
  
  /* private methods */
  
  private JSONObject readJSON(HttpURLConnection conn) throws Exception
  {
    String result = readResponse(conn);
    JSONParser parser = new JSONParser();
    return (JSONObject)parser.parse(result);
  }
  
  private String readResponse(HttpURLConnection conn) throws Exception
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    InputStream is = conn.getInputStream();
    try
    {
      int count = is.read(buffer);
      while (count != -1)
      {
        os.write(buffer, 0, count);
        count = is.read(buffer);
      }
    }
    finally
    {
      is.close();    
    }
    return new String(os.toByteArray(), "UTF-8");
  }
  
	private String urlEncode(final String value) 
    throws UnsupportedEncodingException
  {
		return URLEncoder.encode(value, "UTF-8");
	}
  
  public static void main(String[] args)
  {
    ArrayList<DocumentToSign> docsToSign = new ArrayList<DocumentToSign>();
    docsToSign.add(new DocumentToSign("nom", "jkllkjkl", "SHA1", "a=3"));
    docsToSign.add(new DocumentToSign("nom2", "jkllkjk2", "SHA256", null));
    
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("accessToken", "kjhjhkjh");
    params.put("documents", docsToSign);
    String json = JSONObject.toJSONString(params);
    System.out.println(json);
  }
}
