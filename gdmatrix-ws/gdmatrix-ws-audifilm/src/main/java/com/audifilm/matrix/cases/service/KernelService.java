package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.PKUtil;
import java.util.List;
import javax.xml.ws.WebServiceException;
import org.matrix.kernel.Address;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;


public class KernelService
{

  KernelManagerPort kernelPort = null;
  WSEndpoint endpoint = null;

  public KernelService(String username, String passwd)
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      this.endpoint = wsDirectory.getEndpoint(KernelManagerService.class);
      this.kernelPort = this.endpoint.getPort( KernelManagerPort.class, username, passwd);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected Person loadPerson(String personId)
  {
      //Invokes kernel WS findPersons
      try
      {
        return kernelPort.loadPerson(personId);
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
  }

  protected PersonAddressView loadPersonAddressView(String personId, String addressId, boolean create)
  {
      try
      {

        PersonAddressFilter filter = new PersonAddressFilter();
        filter.setPersonId(getEndpoint().toGlobalId(Person.class, personId));
        filter.setAddressId(getEndpoint().toGlobalId(Address.class, addressId));

        filter.setFirstResult(0);
        filter.setMaxResults(1);

        List<PersonAddressView> adreces = kernelPort.findPersonAddressViews(filter);

        if (adreces.size()<=0) 
        {
          if (!create) return null;
          String newPersonAddressId = storePersonAddress(personId, addressId);

          if (newPersonAddressId==null) return null;
          return loadPersonAddressView(personId, addressId, false);
        }

        PersonAddressView personAddressView = adreces.get(0);
        return getEndpoint().toLocal(PersonAddressView.class, personAddressView);

      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
  }

  protected String storePersonAddress(String personId, String addressId)
  {
    try
    {
      String globalPersonId = getEndpoint().toGlobalId(Person.class, personId);
      String globalAddressId = getEndpoint().toGlobalId(Address.class, addressId);


      PersonAddressFilter filter = new PersonAddressFilter();
      filter.setPersonId(globalPersonId);
      filter.setAddressId(globalAddressId);
      filter.setFirstResult(0);
      filter.setMaxResults(1);

      int count = kernelPort.countPersonAddresses(filter);
      if (count<=0) {
        PersonAddress personAddress = new PersonAddress();
        personAddress.setPersonId(globalPersonId);
        personAddress.setAddressId(globalAddressId);

        PersonAddress newPersonAddress = kernelPort.storePersonAddress(personAddress);

        return getEndpoint().toLocalId(
                PersonAddress.class, newPersonAddress.getPersonAddressId());
      }
      else
      {
        PersonAddressView personAddressView = loadPersonAddressView(personId, addressId, false);
        return getEndpoint().toLocalId(
                PersonAddressView.class, personAddressView.getPersonAddressId());
      }
    }
    catch (Exception e)
    {
      throw new WebServiceException(e);
    }
  }

  public String findPersonAddressNumber(String personId, String addressId, boolean create)
  {
    if (addressId==null) return null;
    PersonAddressView personAddressView = loadPersonAddressView(personId, addressId, create);

    return (personAddressView!=null?getPersonAddressNumber(personAddressView.getPersonAddressId()):null);
  }

  static public String getPersonAddressNumber(String personAddressId)
  {
    if (personAddressId==null) return null;
    return PKUtil.decomposePK(personAddressId)[1];
  }

  protected WSEndpoint getEndpoint()
  {
    return endpoint;
  }

  protected KernelManagerPort getKernelPort()
  {
    return kernelPort;
  }
}
