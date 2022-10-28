package org.santfeliu.misc.iarxiu.ant;

import java.io.File;
import java.util.List;
import cat.aoc.iarxiu.converter.DefaultDocumentConverter;
import cat.aoc.iarxiu.mets.MetsConstants;
import cat.aoc.iarxiu.converter.DocumentConverter;
import cat.aoc.iarxiu.pit.PIT;
import cat.aoc.iarxiu.pit.PITCase;
import cat.aoc.iarxiu.pit.PITDocument;

/**
 *
 * @author realor
 */
public class AddDocumentPITCommand extends PITCommand
{
  private File file;
  private String documentVar;
  private String type = MetsConstants.OTHER_MDTYPE_VALUE;
  private String vocabulary = "urn:iarxiu:2.0:vocabularies:catcert:Voc_document";
  private String converterVar;

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public String getDocumentVar()
  {
    return documentVar;
  }

  public void setDocumentVar(String documentVar)
  {
    this.documentVar = documentVar;
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
    PITDocument pitDocument = null;

    if (pit.isComplex())
    {
      List<PITCase> cases = pit.getCases();
      PITCase pitCase = cases.get(cases.size() - 1); // add to last case
      pitDocument = pitCase.newDocument();
    }
    else
    {
      pitDocument = pit.newDocument();
    }
    getTask().setPit(pit);

    DocumentConverter converter;
    if (converterVar == null)
    {
      converter = new DefaultDocumentConverter();
    }
    else
    {
      converter = (DocumentConverter)getVariable(converterVar);
      if (converter == null)
        throw new Exception("converter not found!");
    }
    //Add content and metadata
    if (documentVar != null)
    {
      Object document = getTask().getVariable(documentVar);
      pitDocument.newDmdMetadata(type, vocabulary, document, converter);
      pitDocument.newContent(document, converter);
      getTask().log("- Document added to PIT: " + pitDocument.getDocId());
    }
    else if (file != null)
    {
      pitDocument.newDmdMetadata(type, vocabulary, file, converter);
      pitDocument.newContent(file, converter);
      getTask().log("- Document added to PIT: " + pitDocument.getDocId());
    }
  }
}
