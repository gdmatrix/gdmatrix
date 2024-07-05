package cat.aoc.valid;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author realor
 */
public class ValidClient
{
  private String baseUrl = "https://valid.aoc.cat";
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
    Map<String, String> params = new HashMap<>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("code", authorizationCode);
    params.put("grant_type", "authorization_code");
    params.put("redirect_uri", redirectUrl);

    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, String> param : params.entrySet())
    {
      if (builder.length() != 0) builder.append('&');
      builder.append(URLEncoder.encode(param.getKey(), "UTF-8"));
      builder.append('=');
      builder.append(URLEncoder.encode(param.getValue(), "UTF-8"));
    }
    return postForm(url, builder.toString());
  }

  public JSONObject getUserInfo(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/getUserInfo?AccessToken=" +
      accessToken);
    String result = getResponse(url);
    JSONParser parser = new JSONParser();
    return (JSONObject)parser.parse(result);
  }

  public String revokeAccessToken(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/o/oauth2/revoke?token=" + accessToken);
    return getResponse(url);
  }

  public String logoutAuthorizationCode(String accessToken) throws Exception
  {
    URL url = new URL(baseUrl + "/o/oauth2/logout?token=" + accessToken);
    return getResponse(url);
  }

  public JSONObject getBasicSignature(String accessToken,
    List<DocumentToSign> documents) throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/getBasicSignature");
    Map<String, Object> params = new HashMap<>();
    params.put("accessToken", accessToken);
    params.put("documents", documents);
    String json = JSONObject.toJSONString(params);
    return postData(url, json);
  }

  public JSONObject initBasicSignature(String accessToken,
    List<DocumentToSign> documents) throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/initBasicSignature");
    Map<String, Object> params = new HashMap<>();
    params.put("accessToken", accessToken);
    params.put("redirectUri", redirectUrl);
    params.put("documents", documents);
    String json = JSONObject.toJSONString(params);
    return postData(url, json);
  }

	public String generateBasicSignatureUrl(String signatureCode)
  {
		return baseUrl + "/o/sign?signature_code=" + signatureCode;
	}

  public JSONObject getBasicSignature(String accessToken, String signatureCode)
    throws Exception
  {
    URL url = new URL(baseUrl + "/serveis-rest/getBasicSignature");
    Map<String, Object> params = new HashMap<>();
    params.put("accessToken", accessToken);
    params.put("signatureCode", signatureCode);
    String json = JSONObject.toJSONString(params);
    return postData(url, json);
  }

  /* private methods */

  private String getResponse(URL url) throws Exception
  {
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    String result = readResponse(conn);
    conn.disconnect();
    return result;
  }

  private JSONObject postForm(URL url, String data) throws Exception
  {
    byte[] postDataBytes = data.getBytes("UTF-8");

    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type",
      "application/x-www-form-urlencoded");
    conn.setRequestProperty("Content-Length",
      String.valueOf(postDataBytes.length));
    conn.setDoOutput(true);
    conn.getOutputStream().write(postDataBytes);
    JSONObject result = readJSON(conn);
    conn.disconnect();
    return result;
  }

  private JSONObject postData(URL url, String json)
    throws Exception
  {
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

  private JSONObject readJSON(HttpURLConnection conn) throws Exception
  {
    String result = readResponse(conn);
    JSONParser parser = new JSONParser();
    return (JSONObject)parser.parse(result);
  }

  private String readResponse(HttpURLConnection conn) throws Exception
  {
    try (InputStream is = conn.getInputStream())
    {
      return IOUtils.toString(is, "UTF-8");
    }
  }

	private String urlEncode(final String value)
    throws UnsupportedEncodingException
  {
		return URLEncoder.encode(value, "UTF-8");
	}
}
