package com.uppermac.repository;

import java.util.List;

import com.uppermac.entity.DeviceRelated;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DevRelatedRepository extends JpaRepository<DeviceRelated, Integer>{

	@Query("select a from DeviceRelated a where a.contralFloor LIKE CONCAT('%|',:floor,'|%')")
	List<DeviceRelated> findFIPbyFloor(@Param("floor") String floor);
	
	@Query("select a from DeviceRelated a where a.faceIP = ?1")
	List<DeviceRelated> findByFaceIP(String faceIP);

	@Query("select a from DeviceRelated a where a.faceIP !=''")
	List<DeviceRelated> findAllFaceRelated();
}