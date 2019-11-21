package com.uppermac.service;

import com.uppermac.entity.DeviceRelated;

import java.util.List;

public interface DevRelatedService {

	void save(DeviceRelated deviceRelated);
	
	List<DeviceRelated> findFIPbyFloor(String floor);
	
	List<String> getAllFaceDeviceIP(String companyFloor);
	
	DeviceRelated findByFaceIP(String faceIP);
}
