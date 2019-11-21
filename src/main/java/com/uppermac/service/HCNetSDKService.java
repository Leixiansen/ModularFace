package com.uppermac.service;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.uppermac.entity.TbCompanyUser;
import com.uppermac.entity.TbVisitor;

public interface HCNetSDKService {


	boolean setCardInfo(String deviceIP, int dwEmployeeNo, String strCardName, String strCardNo, String isdel) throws UnsupportedEncodingException;
	
	boolean setFace(String deviceIP, String strCardNo, TbCompanyUser companyUser) throws UnsupportedEncodingException;
	
	boolean delFace(String deviceIP) throws UnsupportedEncodingException;
	
	boolean getCardInfo(String deviceIP) throws UnsupportedEncodingException;
	
	boolean setVisitorCard(String deviceIP, String isdel, TbVisitor visitor);

	boolean setVisitorFace(String deviceIP, TbVisitor visitor);
	
	int initAndLogin(String hcDeviceIP);
	
	void sendAccessRecord(String deviceIP);
	
	boolean sendToIPC(String hcDeviceIP, File picture, File picAppendData, TbCompanyUser companyUser, TbVisitor visitor);
	
	boolean delIPCpicture(String type, String picID);
	
	void createIPCAlarm(String hcDeviceIP);
	
	boolean getIPCRecord(String deviceIP, String dayInfo);
	
}
