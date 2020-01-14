package org.santfeliu.doc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MemoryDataSource;

/**
 *
 * @author realor
 */
public class XMLFixer
{
  // some XML files may have a trailing line like this (due to web transfer)
  // uuid: xxxx-xxxx-xxxx-xxxx-xxxx
  // fix: trim to last symbol '>' (ascii 62)
    
  private final int size;
  private final byte[] bytes;
  
  public XMLFixer(InputStream is) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.writeToStream(is, bos);
    bytes = bos.toByteArray();
    int i = bytes.length - 1;
    while (i >= 0 && bytes[i] != 62)
    {
      i--;
    }
    size = i + 1;
  }
  
  public int getFixedSize()
  {
    return size;
  }
  
  public InputStream getFixedStream()
  {
    return new ByteArrayInputStream(bytes, 0, size);
  }
  
  public MemoryDataSource getFixedDataSource()
  {
    return new MemoryDataSource(bytes, 0, size, "xml", "text/xml");
  }
}
