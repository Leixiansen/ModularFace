package com.uppermac;

import com.uppermac.entity.TbCompanyUser;

import java.util.Map;

public interface FaceDevDelete {

    //海景设备删除
    void HJDelete(Map<String, String> modelMap, TbCompanyUser companyUser);

    //海康门禁删除
    void HKGuardDelete(Map<String, String> modelMap, TbCompanyUser companyUser);

    //海康摄像头删除
    void HKCameraDelete(Map<String, String> modelMap, TbCompanyUser companyUser);

}
