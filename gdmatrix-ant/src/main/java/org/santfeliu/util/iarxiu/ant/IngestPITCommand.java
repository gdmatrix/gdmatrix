package org.santfeliu.util.iarxiu.ant;

import com.hp.iarxiu.core.schemas._2_0.ingest.ContentTypeHandlingType;
import com.hp.iarxiu.core.schemas._2_0.ingest.ContentTypeHandlingType.Enum;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.santfeliu.util.iarxiu.client.IngestClient;
import org.santfeliu.util.iarxiu.pit.PIT;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author realor
 */
public class IngestPITCommand extends ClientPITCommand
{
  private boolean preservation = false;
  private Enum contentTypeHandling =
    ContentTypeHandlingType.COMPLETE_WITH_INTROSPECTION;

  public Enum getContentTypeHandling()
  {
    return contentTypeHandling;
  }

  public void setContentTypeHandling(Enum contentTypeHandling)
  {
    this.contentTypeHandling = contentTypeHandling;
  }

  public boolean isPreservation()
  {
    return preservation;
  }

  public void setPreservation(boolean preservation)
  {
    this.preservation = preservation;
  }

  @Override
  public void execute() throws Exception
  {
    PIT pit = getTask().getPIT();
    if (pit == null)
      throw new Exception("PIT hasn't data");

    List<File> files = pit.getFiles();
    if (files.size() == 0)
      throw new Exception("No files to ingest");

    getTask().log("Getting client...");
    FileSystemXmlApplicationContext appContext = getApplicationContext();
    IngestClient client = (IngestClient)appContext.getBean("ingestClient");

    //1. GetUploadTicket
    getTask().log("Getting upload ticket...");
    String ticket = client.getUploadTicket();

    //2. Upload invocation
    System.out.println("Generating mets.xml file...");
    File metsFile = createMetsXmlFile(pit);
    System.out.println(metsFile.getAbsolutePath() + " generated");

    ArrayList<File> attachedFiles = new ArrayList<File>();
    attachedFiles.addAll(files);
    System.out.println("Uploading files...");
    if (!(client.uploadFiles(ticket, metsFile, attachedFiles)))
      throw new Exception("Error during file uploading");
    getTask().log("Files uploaded");

    //3. Ingestion
    getTask().log("Request ingestion...");
    String ticket2 =
      client.offlineUploadIngest(ticket, preservation, contentTypeHandling);

    if (ticket2 != null)
    {
      setVariable(resultVar, "OK");
      setVariable(ticketVar, ticket2);
      getTask().log("Ingestion response " + ticket2);
    }
  }

  private File createMetsXmlFile(PIT pit) throws Exception
  {
    File mets = File.createTempFile("mets_", ".xml");
    mets.deleteOnExit();  
    pit.saveAsXml(mets);

    return mets;
  }
}
