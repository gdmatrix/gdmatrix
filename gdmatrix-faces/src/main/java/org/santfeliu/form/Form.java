package org.santfeliu.form;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author realor
 */
public interface Form
{
  // Description
  String getId();
  String getTitle();
  String getLanguage();
  Object getProperty(String name);
  Collection<String> getPropertyNames();

  // Model
  Collection<Field> getFields(); // input and output fields (read only)
  Field getField(String reference);

  // View
  View getRootView();
  View getView(String reference);

  // Validation
  boolean validate(String reference, Object value, List errors, Locale locale);
  boolean validate(Map data, List errors, Locale locale);

  // Evaluation
  Form evaluate(Map context) throws Exception;
  Map getContext();

  // Change control
  boolean isOutdated();

  // submit
  void submit(Map data);

  // Persistence
  Map read(InputStream is) throws IOException;
  void write(OutputStream os, Map data) throws IOException;
}
