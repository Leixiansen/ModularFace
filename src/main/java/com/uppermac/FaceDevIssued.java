package com.uppermac;

import java.util.Map;

public interface FaceDevIssued {

    //人脸下发
    //海景设备
    void HJs(Map<String, String> modelMap) throws Exception;

    //海康门禁
    void HKGuards(Map<String, String> modelMap) throws Exception;

    //海康摄像头
    void HKCameras(Map<String, String> modelMap) throws Exception;
}
