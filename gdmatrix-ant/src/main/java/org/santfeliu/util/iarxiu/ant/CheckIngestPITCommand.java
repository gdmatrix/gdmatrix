package org.santfeliu.util.iarxiu.ant;

import com.hp.iarxiu.core.schemas._2_0.ingest.OfflineIngestInfoType;
import org.santfeliu.util.iarxiu.client.IngestClient;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author blanquepa
 */
public class CheckIngestPITCommand extends ClientPITCommand
{
  @Override
  public void execute() throws Exception
  {
    String ticket = (String)getVariable(ticketVar);
    if (ticket != null)
    {
      FileSystemXmlApplicationContext appContext = getApplicationContext();
      IngestClient client = (IngestClient)appContext.getBean("ingestClient");
      OfflineIngestInfoType ingestInfo = client.getOfflineIngestStatus(ticket);

      if (OfflineIngestInfoType.Status.OK.equals(ingestInfo.getStatus()))
        setVariable(ticketVar , ingestInfo.getId());
      else if (OfflineIngestInfoType.Status.ERROR.equals(ingestInfo.getStatus()))
        setVariable(ticketVar , ingestInfo.getErrorCode());
      String status = ingestInfo.getStatus().toString();
        setVariable(resultVar, status.toUpperCase());
    }
  }
}
