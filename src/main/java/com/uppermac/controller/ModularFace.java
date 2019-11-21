package com.uppermac.controller;

import com.uppermac.FaceDevDelete;
import com.uppermac.FaceDevIssued;
import com.uppermac.data.Constants;
import com.uppermac.entity.Devices;
import com.uppermac.entity.FaceReceive;
import com.uppermac.entity.TbCompanyUser;
import com.uppermac.service.*;
import com.uppermac.utils.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ModularFace implements FaceDevIssued, FaceDevDelete {
    @Autowired
    private FaceReceiveService faceReceiveService;

    @Autowired
    private DevRelatedService devRelatedService;

    @Autowired
    private DevicesService devicesService;

    @Autowired
    private TowerInforService towerInforService;

    @Autowired
    private TbCompanyUserService companyUserService;

    private MyLog logger = new MyLog(ModularFace.class);

    @Autowired
    OkHttpUtil okHttpUtil = new OkHttpUtil();

    @Resource
    private RedisUtils redisUtils;

    @Autowired
    private HCNetSDKService hcNetSDKService;

    //海景设备
    @Override
    public void HJs(Map<String, String> modelMap) throws Exception {
        TbCompanyUser companyUser = new TbCompanyUser();

        // 非正常状态员工不接收
        if (!"normal".equals(companyUser.getCurrentStatus())) {
            return;
        }
        companyUser.setIssued("1");
        companyUser.setIsdel("1");
        companyUserService.save(companyUser);

        if (companyUser.getPhoto() != null) {
            redisUtils.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), companyUser.getPhoto());
            byte[] photoKey = Base64_2.decode(companyUser.getPhoto());
            String fileName = companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
            File fileload = FilesUtils.getFileFromBytes(photoKey, Constants.StaffPath, fileName);
            logger.info("初始化员工存放照片地址" + fileload.getAbsolutePath());
        } else {
            logger.warn(companyUser.getUserName() + "该用户无照片");
            String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                    + towerInforService.findKey();
            //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "", "数据错误",
            //		Constants.errorLogUrl, keysign);
            return;
        }

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }

        String photo = isPhoto(companyUser);
        if (photo == null) {
            return;
        }

        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        System.out.println("共需要下发" + allFaceDecive.size() + "台");
        if (allFaceDecive.size() > 0) {
            String issued = "0";

            for (int i = 0; i < allFaceDecive.size(); i++) {
                logger.info("需下发的人像识别仪器IP为：" + allFaceDecive.get(i));
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                if (null == device) {
                    logger.otherError("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                    continue;
                }
                boolean isSuccess = true;
                //海景设备
                if (device.getDeviceType().equals(modelMap.get(Constants.HJKey))) {
                    isSuccess = companyUserService.sendWhiteList((String) allFaceDecive.get(i), companyUser,
                            companyUser.getPhoto());
                }
                // 针对下发失败的需要登记，待下次冲洗下发，已经下发成功的不在下发
                FaceReceive faceReceive = new FaceReceive();

                if (isSuccess == false) {
                    issued = "1";
                    faceReceive.setOpera("save");
                    faceReceive.setFaceIp(allFaceDecive.get(i));
                    faceReceive.setIdCard(companyUser.getIdNO());
                    faceReceive.setUserName(companyUser.getUserName());
                    faceReceive.setReceiveFlag("1");
                    faceReceive.setUserType("staff");
                    faceReceive.setReceiveTime(getDateTime());
                    faceReceiveService.save(faceReceive);
                    String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                            + towerInforService.findKey();
                    //logger.sendErrorLog(towerInforService.findOrgId(), "下发" + companyUser.getUserName() + "失败，已记录",
                    //		"人脸设备IP" + allFaceDecive.get(i), "下发错误", Constants.errorLogUrl, keysign);
                }
            }
            companyUser.setIssued(issued);
            companyUserService.update(companyUser);
        }
        return;
    }

    //海康门禁
    @Override
    public void HKGuards(Map<String, String> modelMap) throws Exception {
        TbCompanyUser companyUser = new TbCompanyUser();

        // 非正常状态员工不接收
        if (!"normal".equals(companyUser.getCurrentStatus())) {
            return;
        }
        companyUser.setIssued("1");
        companyUser.setIsdel("1");
        companyUserService.save(companyUser);

        if (companyUser.getPhoto() != null) {
            redisUtils.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), companyUser.getPhoto());
            byte[] photoKey = Base64_2.decode(companyUser.getPhoto());
            String fileName = companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
            File fileload = FilesUtils.getFileFromBytes(photoKey, Constants.StaffPath, fileName);
            logger.info("初始化员工存放照片地址" + fileload.getAbsolutePath());
        } else {
            logger.warn(companyUser.getUserName() + "该用户无照片");
            String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                    + towerInforService.findKey();
            //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "", "数据错误",
            //		Constants.errorLogUrl, keysign);
            return;
        }

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }

        String photo = isPhoto(companyUser);
        if (photo == null) {
            return;
        }

        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        System.out.println("共需要下发" + allFaceDecive.size() + "台");
        if (allFaceDecive.size() > 0) {
            String issued = "0";
            for (int i = 0; i < allFaceDecive.size(); i++) {
                logger.info("需下发的人像识别仪器IP为：" + allFaceDecive.get(i));
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                if (null == device) {
                    logger.otherError("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                    continue;
                }
                boolean isSuccess = true;
                //海康门禁
                if (device.getDeviceType().equals(modelMap.get(Constants.HKGuardKey))) {
                    isSuccess = setUser(device, companyUser);
                }
                // 针对下发失败的需要登记，待下次冲洗下发，已经下发成功的不在下发
                FaceReceive faceReceive = new FaceReceive();

                if (isSuccess == false) {
                    issued = "1";
                    faceReceive.setOpera("save");
                    faceReceive.setFaceIp(allFaceDecive.get(i));
                    faceReceive.setIdCard(companyUser.getIdNO());
                    faceReceive.setUserName(companyUser.getUserName());
                    faceReceive.setReceiveFlag("1");
                    faceReceive.setUserType("staff");
                    faceReceive.setReceiveTime(getDateTime());
                    faceReceiveService.save(faceReceive);
                    String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                            + towerInforService.findKey();
                    //logger.sendErrorLog(towerInforService.findOrgId(), "下发" + companyUser.getUserName() + "失败，已记录",
                    //		"人脸设备IP" + allFaceDecive.get(i), "下发错误", Constants.errorLogUrl, keysign);
                }
            }
            companyUser.setIssued(issued);
            companyUserService.update(companyUser);
        }
        return;
    }

    //海康摄像头
    @Override
    public void HKCameras(Map<String, String> modelMap) throws Exception {
        TbCompanyUser companyUser = new TbCompanyUser();

        // 非正常状态员工不接收
        if (!"normal".equals(companyUser.getCurrentStatus())) {
            return;
        }
        companyUser.setIssued("1");
        companyUser.setIsdel("1");
        companyUserService.save(companyUser);

        if (companyUser.getPhoto() != null) {
            redisUtils.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), companyUser.getPhoto());
            byte[] photoKey = Base64_2.decode(companyUser.getPhoto());
            String fileName = companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
            File fileload = FilesUtils.getFileFromBytes(photoKey, Constants.StaffPath, fileName);
            logger.info("初始化员工存放照片地址" + fileload.getAbsolutePath());
        } else {
            logger.warn(companyUser.getUserName() + "该用户无照片");
            String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                    + towerInforService.findKey();
            //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "", "数据错误",
            //		Constants.errorLogUrl, keysign);
            return;
        }

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }

        String photo = isPhoto(companyUser);
        if (photo == null) {
            return;
        }

        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        System.out.println("共需要下发" + allFaceDecive.size() + "台");
        if (allFaceDecive.size() > 0) {
            String issued = "0";
            for (int i = 0; i < allFaceDecive.size(); i++) {
                logger.info("需下发的人像识别仪器IP为：" + allFaceDecive.get(i));
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                if (null == device) {
                    logger.otherError("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                    continue;
                }
                boolean isSuccess = true;
                //海康摄像头
                if (device.getDeviceType().equals(modelMap.get(Constants.HKCameraKey))) {
                    File picAppendData = IPCxmlFile(companyUser);
                    String filePath = Constants.StaffPath + "\\" + companyUser.getUserName() + companyUser.getCompanyId()
                            + ".jpg";
                    File picture = new File(filePath);
                    isSuccess = hcNetSDKService.sendToIPC((String) allFaceDecive.get(i), picture, picAppendData, companyUser, null);

                }
                // 针对下发失败的需要登记，待下次冲洗下发，已经下发成功的不在下发
                FaceReceive faceReceive = new FaceReceive();

                if (isSuccess == false) {
                    issued = "1";
                    faceReceive.setOpera("save");
                    faceReceive.setFaceIp(allFaceDecive.get(i));
                    faceReceive.setIdCard(companyUser.getIdNO());
                    faceReceive.setUserName(companyUser.getUserName());
                    faceReceive.setReceiveFlag("1");
                    faceReceive.setUserType("staff");
                    faceReceive.setReceiveTime(getDateTime());
                    faceReceiveService.save(faceReceive);
                    String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                            + towerInforService.findKey();
                    //logger.sendErrorLog(towerInforService.findOrgId(), "下发" + companyUser.getUserName() + "失败，已记录",
                    //		"人脸设备IP" + allFaceDecive.get(i), "下发错误", Constants.errorLogUrl, keysign);
                }
            }
            companyUser.setIssued(issued);
            companyUserService.update(companyUser);
        }
        return;
    }

    //海景人脸设备删除
    @Override
    public void HJDelete(Map<String, String> modelMap, TbCompanyUser companyUser) {

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }
        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        if (allFaceDecive.size() > 0) {
            for (int i = 0; i < allFaceDecive.size(); i++) {
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                try {
                    //删除海景设备人脸
                    if (device.getDeviceType().equals(modelMap.get(Constants.HJKey))) {
                        //删除redis缓存
                        redisUtils.delete("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO());
                        //删除本地照片
                        File file = new File(Constants.StaffPath + "\\" + companyUser.getUserName() +
                                companyUser.getCompanyId() + ".jpg");
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //海康门禁
    @Override
    public void HKGuardDelete(Map<String, String> modelMap, TbCompanyUser companyUser) {

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }
        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        if (allFaceDecive.size() > 0) {
            for (int i = 0; i < allFaceDecive.size(); i++) {
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                try {
                    //删除海景设备人脸
                    if (device.getDeviceType().equals(modelMap.get(Constants.HKGuardKey))) {
                        //删除redis缓存
                        redisUtils.delete("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO());
                        //删除本地照片
                        File file = new File(Constants.StaffPath + "\\" + companyUser.getUserName() +
                                companyUser.getCompanyId() + ".jpg");
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //海景摄像头删除
    @Override
    public void HKCameraDelete(Map<String, String> modelMap, TbCompanyUser companyUser) {

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }
        List<String> allFaceDecive = devRelatedService.getAllFaceDeviceIP(companyfloor);
        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            return;
        }
        if (allFaceDecive.size() > 0) {
            for (int i = 0; i < allFaceDecive.size(); i++) {
                Devices device = devicesService.findByDeviceIp(allFaceDecive.get(i));
                try {
                    //删除海景设备人脸
                    if (device.getDeviceType().equals(modelMap.get(Constants.HKCameraKey))) {
                        //删除redis缓存
                        redisUtils.delete("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO());
                        //删除本地照片
                        File file = new File(Constants.StaffPath + "\\" + companyUser.getUserName() +
                                companyUser.getCompanyId() + ".jpg");
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String isPhoto(TbCompanyUser companyUser) throws Exception {

        String photo = redisUtils.get("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO());
        if (photo == null) {
            // String filePath = "E:\\sts-space\\photoCache\\staff\\" +
            // companyUser.getUserName()+ companyUser.getCompanyId() + ".jpg";

            String filePath = Constants.StaffPath + "\\" + companyUser.getUserName() + companyUser.getCompanyId()
                    + ".jpg";
            File file = new File(filePath);
            if (!file.exists()) {
                logger.otherError(companyUser.getUserName() + "无照片");
                String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
                        + towerInforService.findKey();
                //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "", "数据错误",
                //		Constants.errorLogUrl, keysign);
                return null;
            } else {
                photo = Base64_2.encode(FilesUtils.getBytesFromFile(file));
                redisUtils.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), photo);
            }
        }
        return photo;
    }

    private boolean setUser(Devices device, TbCompanyUser companyUser) throws UnsupportedEncodingException {
        String strCardNo = "S" + companyUser.getCompanyUserId();
        boolean setCard = hcNetSDKService.setCardInfo(device.getDeviceIp(), companyUser.getCompanyUserId(),
                companyUser.getUserName(), strCardNo, "normal");
        if (!setCard) {
            return false;
        }

        return hcNetSDKService.setFace(device.getDeviceIp(), strCardNo, companyUser);
    }

    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 下发IPC人像时所需照片附加信息文件
     *
     * @param user
     * @return
     */
    public File IPCxmlFile(TbCompanyUser user) {
        // TODO Auto-generated method stub
        String filePath = Constants.StaffPath + "\\" + user.getUserName() + user.getCompanyId() + ".xml";
        File filepath = new File(Constants.StaffPath);
        if (!filepath.exists()) {
            filepath.mkdirs();
        }
        File file = new File(filePath);

        StringBuilder builder = new StringBuilder();
        builder.append("<FaceAppendData><name>S");
        builder.append(user.getUserName());
        builder.append("</name><certificateType>ID</certificateType><certificateNumber>");
        builder.append(user.getCompanyUserId());
        builder.append("</certificateNumber></FaceAppendData>");

        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8");
            StringBuilder outputString = new StringBuilder();
            outputString.append(builder.toString());
            out.write(outputString.toString());

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return file;
    }


}
