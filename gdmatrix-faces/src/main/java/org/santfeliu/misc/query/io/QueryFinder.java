package org.santfeliu.misc.query.io;

import java.io.InputStream;

/**
 *
 * @author realor
 */
public interface QueryFinder
{
  public InputStream getQueryStream(String name) throws Exception;
}
