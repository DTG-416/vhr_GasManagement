package org.javaboy.vhr.controller.gas;

import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.model.GasEqu;
import org.javaboy.vhr.service.GasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/deviceManage/sob")
public class GasEquManageController {
    @Autowired
    GasService deviceManageService;

    @GetMapping("/")
    public List<GasEqu> getAllDeviceManage() {
        return deviceManageService.getAllDeviceManage();
    }

    @PostMapping("/")
    public RespBean addDeviceManage(@RequestBody GasEqu deviceManage) {
        if (deviceManageService.addDeviceManage(deviceManage) == 1) {
            return RespBean.ok("添加成功!");
        }
        return RespBean.error("添加失败!");
    }

    @DeleteMapping("/{id}")
    public RespBean deleteDeviceManageById(@PathVariable Integer id) {
        if (deviceManageService.deleteDeviceManageById(id) == 1) {
            return RespBean.ok("删除成功！");
        }
        return RespBean.error("删除失败！");
    }

    @PutMapping("/")
    public RespBean updateDeviceManageById(@RequestBody GasEqu deviceManage) {
        if (deviceManageService.updateDeviceManageById(deviceManage) == 1) {
            return RespBean.ok("更新成功!");
        }
        return RespBean.error("更新失败!");
    }
}

