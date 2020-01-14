package cat.aoc.valid;

import org.json.simple.JSONObject;

/**
 *
 * @author realor
 */
public class DocumentToSign extends JSONObject
{
  public DocumentToSign()
  {    
  }

  public DocumentToSign(String name, String hash, 
    String algorithm, String metadata)
  { 
    put("name", name);
    put("hash", hash);
    put("algorithm", algorithm);
    put("metadata", metadata);
  }
  
  public String getName()
  {
    return (String)get("name");
  }

  public void setName(String name)
  {
    put("name", name);
  }

  public String getHash()
  {
    return (String)get("hash");
  }

  public void setHash(String hash)
  {
    put("hash", hash);
  }

  public String getAlgorithm()
  {
    return (String)get("algorithm");
  }

  public void setAlgorithm(String algorithm)
  {
    put("algorithm", algorithm);
  }

  public String getMetadata()
  {
    return (String)get("metadata");
  }

  public void setMetadata(String metadata)
  {
    put("metadata", metadata);
  }
}
