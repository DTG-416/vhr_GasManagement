<template>
  <div>

    <div>
      <div style="display: flex;justify-content: space-between">
        <!--搜索选项-->
        <div>
          <el-input placeholder="请输入资料关键字进行搜索，可以直接回车搜索..." prefix-icon="el-icon-search"
                    clearable
                    @clear="initGasDates"
                    style="width: 350px;margin-right: 10px" v-model="keyword"
                    @keydown.enter.native="initGasDates" :disabled="showAdvanceSearchView"></el-input>
          <el-button icon="el-icon-search" type="primary" @click="initGasDates" :disabled="showAdvanceSearchView">
            搜索
          </el-button>
          <el-button type="primary" @click="showAdvanceSearchView = !showAdvanceSearchView">
            <i :class="showAdvanceSearchView?'fa fa-angle-double-up':'fa fa-angle-double-down'"
               aria-hidden="true"></i>
            高级搜索
          </el-button>
        </div>

        <!--文件按钮-->
        <div>
            <el-button type="success" @click="downloadSelectedFiles" icon="el-icon-download">
              批量下载
            </el-button>

            <el-button type="primary" icon="el-icon-upload2" @click="dialogVisible = true">
              上传文件
            </el-button>

        </div>
      </div>

      <!--搜索框的定义，高级搜索选项-->
      <transition name="slide-fade">
        <div v-show="showAdvanceSearchView"
             style="border: 1px solid #409eff;border-radius: 5px;box-sizing: border-box;padding: 5px;margin: 10px 0px;"
        >
          <el-row>
            <el-input placeholder="请输入资料名或者关键字" prefix-icon="el-icon-search"
                      clearable
                      style="width: 350px;margin-right: 10px" v-model="searchValue.originalname"
                      >
            </el-input>
          </el-row>
          <el-row>
            <el-col :span="9">
              序号区间：
              <el-input-number v-model="searchValue.beginnum" controls-position="right" :min="0" :max="10000"></el-input-number>
              <span>至</span>
              <el-input-number v-model="searchValue.endnum" controls-position="right" :min="0" :max="10000"></el-input-number>
            </el-col>
            <el-col :span="10">
              资料上传日期:
              <el-date-picker
                  v-model="searchValue.dateScope"
                  type="daterange"
                  size="mini"
                  unlink-panels
                  value-format="yyyy-MM-dd"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期">
              </el-date-picker>
            </el-col>
            <el-col :span="5" :offset="19">
              <el-button size="mini" @click="showAdvanceSearchView = false">取消</el-button>
              <el-button size="mini" icon="el-icon-search" type="primary" @click="initGasDates('advanced')">搜索</el-button>
            </el-col>
          </el-row>
        </div>
      </transition>
    </div>

    <!--表格的定义-->
    <div style="margin-top: 10px">
      <!--表格定义-->
      <el-table
          :data="gasdates"
          stripe
          border
          @selection-change="handleSelectionChange"
          v-loading="loading"
          element-loading-text="正在加载..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(0, 0, 0, 0.8)"
          style="width: 100%"
          max-height="600px">
        <!--表格的列定义-->
        <!--复选框-->
        <el-table-column
            type="selection"
            width="40">
        </el-table-column>

        <el-table-column
            prop="originalName"
            fixed
            align="center"
            label="文件名"
            min-width="50">
        </el-table-column>

        <el-table-column
            prop="uploadDate"
            min-width="8"
            align="center"
            label="上传日期">
        </el-table-column>

        <el-table-column
            prop="fileExtension"
            align="center"
            min-width="6"
            label="文件类型">
        </el-table-column>

        <el-table-column
            prop="uploaderId"
            label="上传人员工号"
            align="center"
            min-width="7">
        </el-table-column>

        <el-table-column
            fixed="right"
            align="center"
            min-width="15"
            label="操作">
          <template slot-scope="scope">
            <el-button @click="downloadFile(scope.row)" style="padding: 3px" size="mini">下载</el-button>
            <el-button style="padding: 3px" size="mini" type="primary">查看</el-button>
            <el-button @click="deleteEmp(scope.row)" style="padding: 3px" size="mini" type="danger">删除
            </el-button>
          </template>
        </el-table-column>

      </el-table>

      <!--分页框-->
      <div style="display: flex;justify-content: flex-end">
        <el-pagination
            background
            @current-change="currentChange"
            @size-change="sizeChange"
            layout="sizes, prev, pager, next, jumper, ->, total, slot"
            :total="total">
        </el-pagination>
      </div>
    </div>

    <!--对话框的定义-->
    <el-dialog
        title="文件上传"
        :visible.sync="dialogVisible"
        width="400px"
    >
      <!--multiple可以多选，drag允许拖拽-->
      <!--actiond调用后台的java方法-->
      <el-upload
          action="/gas/basic/upload"
          multiple
          drag
          :show-file-list="true"
          :before-upload="beforeUpload"
          :on-success="onSuccess"
          :on-error="onError"
          :disabled="importDataDisabled"
      >
      <i class="el-icon-upload"></i>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "GasDataManage",
  files:[],//文件数组
  data() {
    return {
      //高级搜索中的信息
      searchValue: {
        originalname:null,
        beginnum:0,
        endnum:0,
        dateScope:null,
      },
      importDataDisabled: false,
      showAdvanceSearchView: false,//是否显示高级搜索栏
      gasdates: [],//查询的数据
      fileList: [],
      multipleSelection: [], // 存储选中的文件
      loading: false,
      dialogVisible: false,
      total: 0,
      page: 1,
      keyword:"",
      size: 10,
      nations: [],
      positions: [],
      rules: {
      }
    }
  },
  mounted() {
    this.initGasDates();
    this.initData();
  },

  methods: {
    // 处理选中项变化
    handleSelectionChange(selection) {
      this.multipleSelection = selection;
    },

    /**********文件上传********************/
    onError(err, file, fileList) {
      this.importDataBtnText = '导入数据';
      this.importDataBtnIcon = 'el-icon-upload2';
      this.importDataDisabled = false;
    },
    onSuccess(response, file, fileList) {
      this.importDataBtnText = '导入数据';
      this.importDataBtnIcon = 'el-icon-upload2';
      this.importDataDisabled = false;
      this.initGasDates();
    },
    beforeUpload(file) {
      console.log("uploadFiles() 被触发"); // 先检查是否触发
      this.importDataBtnText = '正在导入';
      this.importDataBtnIcon = 'el-icon-loading';
      this.importDataDisabled = true;
      /*
      this.fileList = [...this.fileList, file];

      this.$nextTick(() => {
        this.uploadFiles(); // 确保 Vue 处理完 fileList 后触发上传
        // return false; // 阻止 el-upload 自己发请求
      });
      return false;
      */
    },
    uploadFiles(params) {
      let formData = new FormData();

      this.fileList.forEach(file => {
        formData.append("files", file.raw);
      });

      postRequest("/gas/basic/upload", formData)
          .then(response => {
            if (response.data.success && response.data.success.length > 0) {
              this.$message.success("上传成功: " + response.data.success.join(", "));
            }
            if (response.data.failed && response.data.failed.length > 0) {
              this.$message.error("失败文件: " + response.data.failed.join(", "));
            }
            this.fileList = []; // 清空文件列表
          })
          .catch(error => {
            console.error("上传失败:", error);
            this.$message.error("文件上传失败");
          });
    },
    /***************数据库查询*********************/
    deleteEmp(data) {
      this.$confirm('此操作将永久删除【' + data.name + '】, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleteRequest("/employee/basic/" + data.id).then(resp => {
          if (resp) {
            this.initGasDates();
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //初始化数据方法，从后端获取数据并存入，
    initData() {
      //判断sessionStorage是否已有“nations数据”
      if (!window.sessionStorage.getItem("nations")) {
        //如果没有，发送请求获取民族数据
        this.getRequest('/employee/basic/nations').then(resp => {
          if (resp) {
            this.nations = resp;//存入vue组件变量
            //存入seeeionStorage
            window.sessionStorage.setItem("nations", JSON.stringify(resp));
          }
        })
      } else {
        //如果有，直接解析存入vue
        this.nations = JSON.parse(window.sessionStorage.getItem("nations"));
      }
    },
    sizeChange(currentSize) {
      this.size = currentSize;
      this.initGasDates();
    },
    currentChange(currentPage) {
      this.page = currentPage;
      this.initGasDates('advanced');
    },
    //初始化数据
    initGasDates(type) {
      this.loading = true;
      let url = '/gas/basic/?page=' + this.page + '&size=' + this.size;
      if (type && type == 'advanced') {

        if (this.searchValue.originalname) {
          url += '&originalname' + this.searchValue.originalname;
        }
        //判断序号区间是否有效
        if (this.searchValue.endnum>this.searchValue.beginnum) {
          url += '&beginNum=' + this.searchValue.beginnum;
          url += '&endNum=' + this.searchValue.endnum;
        }
        if (this.searchValue.dateScope) {
          url += `&DateScope=${this.searchValue.dateScope}`;
        }
      } else {
        url += "&originalname=" + this.keyword;
      }
      this.getRequest(url).then(resp => {
        this.loading = false;
        if (resp) {
          this.gasdates = resp.data;
          this.total = resp.total;
        }
      });

    },

    /*************文件下载********************/
    //下载单个文件
    async downloadFile(file) {
      try {
        const response = await this.getRequest("/gas/basic/download",
            {
              uniqueName: file.uniqueName,
              fileExtension: file.fileExtension,
              originalName: file.originalName,
              filePath: file.filePath
            }, "blob");
        const blob = new Blob([response.data]);
        this.$saveAs(blob, `${file.originalName}.${file.fileExtension}`);
      } catch (error) {
        console.error("文件下载失败:", error);
      }
    },
    //批量下载
    async downloadSelectedFiles() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning("请选择文件");
        return;
      }

      try {
        if (this.multipleSelection.length === 1) {
          await this.downloadFile(this.multipleSelection[0]); // 处理单个文件下载
        } else {
          // 定义一个空数组来保存流的所有数
          await this.postRequest("/gas/basic/batchDownload", this.multipleSelection, "blob").then(
              response => {
                const blob = response.data;
                // 创建下载链接并触发下载
                this.$saveAs(blob, "batch_download.zip");
              });
        }
      } catch (error) {
        console.error("批量下载失败:", error);
      }
    }
  }
}
</script>

<style>
/* 可以设置不同的进入和离开动画 */
/* 设置持续时间和动画函数 */
  .slide-fade-enter-active {
    transition: all .8s ease;
  }

  .slide-fade-leave-active {
    transition: all .8s cubic-bezier(1.0, 0.5, 0.8, 1.0);
  }

  .slide-fade-enter, .slide-fade-leave-to
  /* .slide-fade-leave-active for below version 2.1.8 */
  {
    transform: translateX(10px);
    opacity: 0;
  }
</style>
