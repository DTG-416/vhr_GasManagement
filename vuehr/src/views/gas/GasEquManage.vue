<template>
  <div>
    <div style="display: flex;justify-content: space-between">
      <el-button icon="el-icon-plus" type="primary" @click="showAddDeviceView">添加设备信息</el-button>
      <el-button icon="el-icon-refresh" type="success" @click="initDevices"></el-button>
    </div>
    <div style="margin-top: 10px">
      <el-table :data="devices" border stripe>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column width="200" prop="name" label="设备名称"></el-table-column>
        <el-table-column width="150" prop="code" label="设备编号"></el-table-column>
        <el-table-column width="150" prop="processArea" label="工艺区域"></el-table-column>
        <el-table-column width="150" prop="pressureLevel" label="压力等级"></el-table-column>
        <el-table-column width="150" prop="specification" label="设备规格"></el-table-column>
        <el-table-column width="150"  label="备注"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button @click="showEditDeviceView(scope.row)">编辑</el-button>
            <el-button type="danger" @click="deleteDevice(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="50%">
      <div style="display: flex;justify-content: space-around;align-items: center">
        <el-steps direction="vertical" :active="activeItemIndex">
          <el-step :title="itemName" v-for="(itemName,index) in deviceItemName" :key="index"></el-step>
        </el-steps>
        <el-input v-model="device[title]" :placeholder="'请输入'+deviceItemName[index]+'...'"
                  v-for="(value,title,index) in device"
                  :key="index" v-show="activeItemIndex===index" style="width: 200px"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="preStep">{{activeItemIndex===4?'取消':'上一步'}}</el-button>
        <el-button type="primary" @click="nextStep">{{activeItemIndex===4?'完成':'下一步'}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "GasEqManage",
  data() {
    return {
      dialogVisible: false,
      dialogTitle: '添加设备信息',
      devices: [],
      activeItemIndex: 0,
      deviceItemName: [
        '设备名称',
        '设备编号',
        '工艺区域',
        '压力等级',
        '设备规格',
      ],
      device: {
        name: '',
        code: '',
        processArea: '',
        pressureLevel: '',
        specification: ''
      }
    }
  },
  mounted() {
    this.initDevices();
  },
  methods: {
    showEditDeviceView(data) {
      this.dialogTitle = '修改设备信息';
      this.dialogVisible = true;
      this.device.name = data.name;
      this.device.code = data.code;
      this.device.processArea = data.processArea;
      this.device.pressureLevel = data.pressureLevel;
      this.device.specification = data.specification;
      this.device.id = data.id;
      this.activeItemIndex = 0;
    },
    deleteDevice(data) {
      this.$confirm('此操作将删除【' + data.name + '】的信息，是否继续？', '提示', {
        cancelButtonText: '取消',
        confirmButtonText: '确定'
      }).then(() => {
        this.deleteRequest("/deviceManage/sob/" + data.id).then(resp => {
          if (resp) {
            this.initDevices();
          }
        })
      }).catch(() => {
        this.$message.info("取消删除!");
      })
    },
    preStep() {
      if (this.activeItemIndex === 0) {
        return;
      } else if (this.activeItemIndex === 4) {
        //关闭对话框
        this.dialogVisible = false;
        return;
      }
      this.activeItemIndex--;
    },
    nextStep() {
      if (this.activeItemIndex === 4) {
        if (this.device.id) {
          this.putRequest("/deviceManage/sob/", this.device).then(resp=>{
            if (resp) {
              this.initDevices();
              this.dialogVisible = false;
            }
          })
        } else {
          this.postRequest("/deviceManage/sob/", this.device).then(resp => {
            if (resp) {
              this.initDevices();
              this.dialogVisible = false;
            }
          });
        }
        return;
      }
      this.activeItemIndex++;
    },
    showAddDeviceView() {
      //数据初始化
      this.device = {
        name: '',
        code: '',
        processArea: '',
        pressureLevel: '',
        specification: ''
      }
      this.dialogTitle = '添加设备信息';
      this.activeItemIndex = 0;
      this.dialogVisible = true;
    },
    initDevices() {
      this.getRequest("/deviceManage/sob/").then(resp => {
        if (resp) {
          this.devices = resp;
        }
      })

    }
  }
}
</script>

<style scoped>

</style>