/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.signature.xmldsig;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.xml.utils.URI;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;

/**
 * A simple ResourceResolver for HTTP requests.
 *
 * This resolver is only used in SignedDocument version 1.0.
 * Newer SignedDocument versions have not references with http URIs.
 */
/**
 *
 * @author realor
 */
public class HTTPResolver extends ResourceResolverSpi
{
  /** {@link org.apache.commons.logging} logging facility */
  static org.apache.commons.logging.Log log =
    org.apache.commons.logging.LogFactory.getLog(HTTPResolver.class.getName());
  /** Field properties[] */
  private static final String properties[] = {};

  @Override
  public boolean engineIsThreadSafe()
  {
    return true;
  }

  @Override
  public String[] engineGetPropertyKeys()
  {
    return (String[])HTTPResolver.properties.clone();
  }

  /**
   * Method resolve
   *
   * @param rrc, the resource resolver context
   *
   * @throws ResourceResolverException
   * @return
   * $todo$ calculate the correct URI from the attribute and the BaseURI
   */
  @Override
  public XMLSignatureInput engineResolveURI(ResourceResolverContext rrc)
    throws ResourceResolverException
  {
    Attr uri = rrc.attr;
    String baseUri = rrc.baseUri;

    try
    {
      // calculate new URI
      URI uriNew = getNewURI(uri.getNodeValue(), baseUri);

      // if the URI contains a fragment, ignore it
      URI uriNewNoFrag = new URI(uriNew);

      uriNewNoFrag.setFragment(null);

      URL url = new URL(uriNewNoFrag.toString());

      InputStream inputStream = new ResetableInputStream(url);

      XMLSignatureInput result = new XMLSignatureInput(inputStream)
      {
        /**
         * Gets a resetable input stream.
         *
         * In order to avoid loading the url content into memory with the
         * current xmlsec version, an input stream that implements mark/reset
         * methods is returned.
         *
         * @return a resetable input stresm
         * @throws IOException
         */
        protected InputStream getResetableInputStream()
          throws IOException
        {
          return getOctetStreamReal();
        }
      };

      result.setSourceURI(uriNew.toString());
      result.setMIMEType(((ResetableInputStream)result.getOctetStreamReal())
        .getMimeType());
      return result;
    }
    catch (MalformedURLException ex)
    {
      throw new ResourceResolverException("generic.EmptyMessage",
        uri.getNodeValue(), baseUri);
    }
    catch (IOException ex)
    {
      throw new ResourceResolverException("generic.EmptyMessage",
        uri.getNodeValue(), baseUri);
    }
  }


  /**
   * We resolve http URIs <I>without</I> fragment...
   *
   * @param rrc, the resource resolver context
   *  @return true if can be resolved
   */
  @Override
  public boolean engineCanResolveURI(ResourceResolverContext rrc)
  {
    Attr uri = rrc.attr;
    String baseUri = rrc.baseUri;

    if (uri == null)
    {
      log.debug("quick fail, uri == null");

      return false;
    }

    String uriNodeValue = uri.getNodeValue();

    if (uriNodeValue.equals("") || (uriNodeValue.charAt(0) == '#'))
    {
      log.debug("quick fail for empty URIs and local ones");

      return false;
    }

    if (log.isDebugEnabled())
    {
      log.debug("I was asked whether I can resolve " + uriNodeValue);
    }

    if (uriNodeValue.startsWith("http:") ||
       (baseUri != null && baseUri.startsWith("http:")))
    {
      if (log.isDebugEnabled())
      {
        log.debug("I state that I can resolve " + uriNodeValue);
      }

      return true;
    }

    if (log.isDebugEnabled())
    {
      log.debug("I state that I can't resolve " + uriNodeValue);
    }
    return false;
  }

  public class ResetableInputStream extends InputStream
  {
    private final URL url;
    private InputStream inputStream;
    private String mimeType;

    public ResetableInputStream(URL url) throws IOException
    {
      this.url = url;
      openStream();
    }

    @Override
    public boolean markSupported()
    {
      return true;
    }

    @Override
    public synchronized void reset() throws IOException
    {
      try
      {
        inputStream.close();
      }
      catch (IOException ex)
      {
      }
      openStream();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
      if (readlimit > 0) throw new RuntimeException("Unsupported operation");
    }

    @Override
    public void close() throws IOException
    {
      inputStream.close();
    }

    @Override
    public int available() throws IOException
    {
      return inputStream.available();
    }

    @Override
    public long skip(long n) throws IOException
    {
      return inputStream.skip(n);
    }

    @Override
    public int read() throws IOException
    {
      return inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      return inputStream.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException
    {
      return inputStream.read(b);
    }

    public String getMimeType()
    {
      return mimeType;
    }

    private void openStream() throws IOException
    {
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(3600000); // 1 hour
      conn.setReadTimeout(3600000); // 1 hour
      this.mimeType = conn.getHeaderField("Content-Type");
      this.inputStream = conn.getInputStream();
    }
  }

  private URI getNewURI(String uri, String BaseURI)
          throws URI.MalformedURIException
  {
    if ((BaseURI == null) || "".equals(BaseURI))
    {
      return new URI(uri);
    }
    return new URI(new URI(BaseURI), uri);
  }
}
