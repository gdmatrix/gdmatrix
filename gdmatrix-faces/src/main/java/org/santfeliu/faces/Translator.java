package org.santfeliu.faces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface Translator
{
  // get default language
  public String getDefaultLanguage();

  // translation for binary streams
  public void translate(InputStream is, OutputStream out, String contentType,
    String language, String group) throws IOException;

  // translation for text streams
  public void translate(Reader reader, Writer writer, String contentType,
    String language, String group) throws IOException;
}
