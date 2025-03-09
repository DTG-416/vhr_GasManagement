package org.javaboy.vhr.mapper;

import org.apache.ibatis.annotations.Param;
import org.javaboy.vhr.model.Employee;
import org.javaboy.vhr.model.GasDateInfo;
import org.springframework.security.core.parameters.P;

import java.util.Date;
import java.util.List;

public interface GasDatesMapper {
    int insertGasDateInfo(GasDateInfo record);

    List<GasDateInfo> getGasDatesByPage(@Param("page") Integer page,
                                        @Param("size") Integer size,
                                        @Param("originalname") String originalname,
                                        @Param("beginNum") Integer beginNum,
                                        @Param("endNum") Integer endNum,
                                        @Param("DateScope") Date[] DateScope);

    Long getTotal(@Param("originalname") String originalname,
                  @Param("beginNum") Integer beginNum,
                  @Param("endNum") Integer endNum,
                  @Param("DateScope") Date[] DateScope);
}

