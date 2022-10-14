package org.santfeliu.util.iarxiu.ant;

import java.io.File;
import org.santfeliu.util.iarxiu.converter.Converter;
import org.santfeliu.util.iarxiu.converter.DefaultCaseConverter;
import org.santfeliu.util.iarxiu.mets.MetsConstants;
import org.santfeliu.util.iarxiu.pit.PIT;
import org.santfeliu.util.iarxiu.pit.PITCase;

/**
 *
 * @author realor
 */
public class AddCasePITCommand extends PITCommand
{
  private File file;
  private String caseVar;
  private String converterVar;
  private String type = MetsConstants.OTHER_MDTYPE_VALUE;
  private String vocabulary = "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient";

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public String getCaseVar()
  {
    return caseVar;
  }

  public void setCaseVar(String caseVar)
  {
    this.caseVar = caseVar;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getVocabulary()
  {
    return vocabulary;
  }

  public void setVocabulary(String vocabulary)
  {
    this.vocabulary = vocabulary;
  }

  public String getConverterVar()
  {
    return converterVar;
  }

  public void setConverterVar(String converterVar)
  {
    this.converterVar = converterVar;
  }

  @Override
  public void execute() throws Exception
  {
    PIT pit = getTask().getPIT();

    Converter converter;
    if (converterVar == null)
    {
      converter = new DefaultCaseConverter();
    }
    else
    {
      converter = (Converter)getVariable(converterVar);
      if (converter == null)
        throw new Exception("converter not found!");
    }
    if (caseVar != null)
    {
      Object cas = getTask().getVariable(caseVar);
      PITCase pitCase = pit.newCase();
      pitCase.newDmdMetadata(type, vocabulary, cas, converter);
      getTask().log("- Case added to PIT: " + pitCase.getCaseId());
    }
    else if (file != null)
    {
      PITCase pitCase = pit.newCase();
      getTask().log("- Case added to PIT: " + pitCase.getCaseId());
      pitCase.newDmdMetadata(type, vocabulary, file, converter);
    }
    getTask().setPit(pit);
  }
}
