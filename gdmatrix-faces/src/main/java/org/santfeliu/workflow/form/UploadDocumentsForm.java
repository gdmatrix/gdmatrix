package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class UploadDocumentsForm extends Form
{
  public UploadDocumentsForm()
  {
  }

  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    String reference = (String)parameters.get("reference");
    if (reference == null) reference = "";
    {
      set.add(reference + "docid_%");
      set.add(reference + "uuid_%");
      set.add(reference + "desc_%");
      set.add(reference + "count");
    }
    return set;
  }
}
