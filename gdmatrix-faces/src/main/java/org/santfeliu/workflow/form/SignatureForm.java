package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class SignatureForm extends Form
{
  public SignatureForm()
  {
  }

  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    set.add("result");
    return set;
  }
}
