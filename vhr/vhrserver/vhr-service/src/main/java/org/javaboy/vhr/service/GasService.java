package org.javaboy.vhr.service;

import org.springframework.stereotype.Service;
import org.javaboy.vhr.mapper.*;
import org.javaboy.vhr.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Service
public class GasService {
    @Autowired
    GasDatesMapper gasDatesMapper;

    //上传的资料信息写入数据库
    public Integer addGasDataInfo(GasDateInfo gasDate) {
        return gasDatesMapper.insertGasDateInfo(gasDate);
    }

    //查询页面
    public RespPageBean getGasDatesByPage(Integer page,Integer size, String originalname,Integer beginNum ,Integer endNum, Date[] DateScope){
        //判断页数与大小是否存在
        if (page != null && size != null) {
            page = (page - 1) * size;
        }
        int kk=0;
        //按页数查询数据
        List<GasDateInfo> data = gasDatesMapper.getGasDatesByPage(page, size,originalname,beginNum,endNum, DateScope);
        //获得总数
        Long total = gasDatesMapper.getTotal(originalname,beginNum,endNum, DateScope);
        //创建数据返回结构
        RespPageBean bean = new RespPageBean();
        bean.setData(data);
        bean.setTotal(total);
        return bean;
    }

    //删除
    public Integer deleteGasDataByid(Integer id) {
        return gasDatesMapper.deleteByPrimaryKey(id);
    }
    @Autowired
    GasEquManageMapper deviceManageMapper;

    public List<GasEqu> getAllDeviceManage() {
        return deviceManageMapper.getAllDeviceManage();
    }

    public Integer addDeviceManage(GasEqu deviceManage) {
        return deviceManageMapper.insertSelective(deviceManage);
    }

    public Integer deleteDeviceManageById(Integer id) {
        return deviceManageMapper.deleteByPrimaryKey(id);
    }

    public Integer updateDeviceManageById(GasEqu deviceManage) {
        return deviceManageMapper.updateByPrimaryKeySelective(deviceManage);
    }

}
