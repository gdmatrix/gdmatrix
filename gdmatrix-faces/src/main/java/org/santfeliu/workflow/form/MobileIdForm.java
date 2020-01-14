package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class MobileIdForm extends Form
{
  public MobileIdForm()
  {
  }

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    set.add("signedPDFDocId");
    return set;
  }
}
