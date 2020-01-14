package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.dic.service.address.AddressType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import org.matrix.kernel.Address;
import org.matrix.util.WSEndpoint;

import org.santfeliu.util.TextUtils;


public class DBAddress extends DBEntityBase
{
  private String addressId;
  private String countryId;
  private String provinceId;
  private String cityId;
  private String streetId;
  private String wayId;
  private String addressTypeId;
  private String gisReference;
  private String postalCode;
  private String postOfficeBox;
  private String entranceHall;
  private String door;
  private String comments;
  private String stair;
  private String number1;
  private String bis1;
  private String number2;
  private String bis2;
  private Integer km;
  private Integer hm;
  private String block;
  private String floor;
  private String domtloc;
  private String stdapladd;
  private String stdaplmod;
  private String baixasw;
  private String valdata;
  
  private DBStreet street;
  
  public DBAddress()
  {
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getCountryId()
  {
    return countryId;
  }

  public void setProvinceId(String provinceId)
  {
    this.provinceId = provinceId;
  }

  public String getProvinceId()
  {
    return provinceId;
  }

  public void setCityId(String cityId)
  {
    this.cityId = cityId;
  }

  public String getCityId()
  {
    return cityId;
  }

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setWayId(String wayId)
  {
    this.wayId = wayId;
  }

  public String getWayId()
  {
    return wayId;
  }

  public void setAddressTypeId(String addressTypeId)
  {
    this.addressTypeId = addressTypeId;
  }

  public String getAddressTypeId()
  {
    return addressTypeId;
  }

  public void setGisReference(String gisReference)
  {
    this.gisReference = gisReference;
  }

  public String getGisReference()
  {
    return gisReference;
  }

  public void setPostalCode(String postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getPostalCode()
  {
    return postalCode;
  }

  public void setPostOfficeBox(String postOfficeBox)
  {
    this.postOfficeBox = postOfficeBox;
  }

  public String getPostOfficeBox()
  {
    return postOfficeBox;
  }

  public void setEntranceHall(String entranceHall)
  {
    this.entranceHall = entranceHall;
  }

  public String getEntranceHall()
  {
    return entranceHall;
  }

  public void setDoor(String door)
  {
    this.door = door;
  }

  public String getDoor()
  {
    return door;
  }

  public void setComments(String comments)
  {
    this.comments = comments;
  }

  public String getComments()
  {
    return comments;
  }

  public void setStair(String stair)
  {
    this.stair = stair;
  }

  public String getStair()
  {
    return stair;
  }

  public void setNumber1(String number1)
  {
    this.number1 = number1;
  }

  public String getNumber1()
  {
    return number1;
  }

  public void setBis1(String bis1)
  {
    this.bis1 = bis1;
  }

  public String getBis1()
  {
    return bis1;
  }

  public void setNumber2(String number2)
  {
    this.number2 = number2;
  }

  public String getNumber2()
  {
    return number2;
  }

  public void setBis2(String bis2)
  {
    this.bis2 = bis2;
  }

  public String getBis2()
  {
    return bis2;
  }

  public void setKm(Integer km)
  {
    this.km = km;
  }

  public Integer getKm()
  {
    return km;
  }

  public void setHm(Integer hm)
  {
    this.hm = hm;
  }

  public Integer getHm()
  {
    return hm;
  }

  public void setBlock(String block)
  {
    this.block = block;
  }

  public String getBlock()
  {
    return block;
  }

  public void setFloor(String floor)
  {
    this.floor = floor;
  }

  public String getFloor()
  {
    return floor;
  }

  public void setDomtloc(String domtloc)
  {
    this.domtloc = domtloc;
  }

  public String getDomtloc()
  {
    return domtloc;
  }

  public void setStdapladd(String stdapladd)
  {
    this.stdapladd = stdapladd;
  }

  public String getStdapladd()
  {
    return stdapladd;
  }

  public void setStdaplmod(String stdaplmod)
  {
    this.stdaplmod = stdaplmod;
  }

  public String getStdaplmod()
  {
    return stdaplmod;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  /* relationships */
  public void setStreet(DBStreet street)
  {
    this.street = street;
  }

  public DBStreet getStreet()
  {
    return street;
  }
  
  public void copyFrom(WSEndpoint endpoint, Address address)
  {
    this.addressId = address.getAddressId();
    String ids[] = address.getStreetId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.streetId = ids[3];

    this.addressTypeId = DicTypeAdmin.getInstance(AddressType.class)
              .toLocalId(endpoint, address.getAddressTypeId());


    Double kilometer = address.getKm(); // split in km and hm
    if (kilometer != null)
    {
      this.km = new Integer(kilometer.intValue());
      this.hm = new Integer((int)(kilometer * 10) % 10);
    }
    else
    {
      this.km = null;
      this.hm = null;
    }
    
    this.number1 = TextUtils.leftPadding(address.getNumber1(), "0000");
    this.bis1 = address.getBis1();
    this.number2 = TextUtils.leftPadding(address.getNumber2(), "0000");
    this.bis2 = address.getBis2();

    this.entranceHall = address.getEntranceHall();
    this.block = address.getBlock();
    this.stair = address.getStair();
    this.floor = TextUtils.leftPadding(address.getFloor(), "00");
    this.door = TextUtils.leftPadding(address.getDoor(), "00");

    this.comments = address.getComments();
    this.gisReference = address.getGisReference();
    this.postalCode = address.getPostalCode();
    this.postOfficeBox = address.getPostOfficeBox();
  }

  public void copyTo(WSEndpoint endpoint, Address address)
  {
    address.setAddressId(addressId);
    address.setStreetId(countryId + KernelManager.PK_SEPARATOR + 
      provinceId + KernelManager.PK_SEPARATOR + 
      cityId + KernelManager.PK_SEPARATOR + streetId);
    address.setAddressTypeId(DicTypeAdmin.getInstance(AddressType.class)
              .toGlobalId(endpoint, addressTypeId));

    address.setPostalCode(TextUtils.leftPadding(postalCode, "00000"));
    address.setPostOfficeBox(postOfficeBox);
    address.setEntranceHall(entranceHall);
    address.setDoor(door);
    address.setComments(comments);
    address.setStair(stair);
    address.setNumber1(number1);
    address.setBis1(bis1);
    address.setNumber2(number2);
    address.setBis2(bis2);
    if (km != null)
    {
      Double kilometer = new Double(km);
      if (hm != null) kilometer += 0.1 * hm;
      address.setKm(kilometer);
    }
    else address.setKm(null);
    address.setBlock(block);
    address.setFloor(floor);
    address.setGisReference(gisReference);
  }
}
