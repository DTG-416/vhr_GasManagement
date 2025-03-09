package org.javaboy.vhr.mapper;

import org.javaboy.vhr.model.GasEqu;

import java.util.List;

public interface GasEquManageMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(GasEqu record);//静态插入
    int insertSelective(GasEqu record);//动态插入，防止null
    GasEqu selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(GasEqu record);//静态更新
    int updateByPrimaryKeySelective(GasEqu record);//动态更新，防止null
    List<GasEqu> getAllDeviceManage();
}
