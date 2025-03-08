<template>
  <div>

    <el-row
        type="flex"
        justify="center"
        class="toolbar-container"
    >
      <el-button
          :type="currentTool === 'distance' ? 'primary' : 'default'"
          @click="switchTool('distance')"
      >
        测距
      </el-button>
      <el-button
          :type="currentTool === 'area' ? 'primary' : 'default'"
          @click="switchTool('area')"
      >
        测面积
      </el-button>
      <!-- 确认按钮 -->
      <el-button
          v-if="currentTool === 'area' && markers.length >= 3"
          type="success"
          @click="confirmAreaMeasurement"
      >
        确认
      </el-button>

      <el-button
          :type="currentTool === 'landmarker' ? 'primary' : 'default'"
          @click="switchTool('landmarker')"
      >
        添加地标
      </el-button>

      <el-select v-model="selectedUnit" placeholder="选择计量单位" style="width: 100px;">
        <el-option label="米 (meters)" value="meters"></el-option>
        <el-option label="千米 (kilometers)" value="kilometers"></el-option>
        <el-option label="英里 (miles)" value="miles"></el-option>
        <el-option label="英尺 (feet)" value="feet"></el-option>
      </el-select>
      <el-button type="danger" @click="clearAllDatas">
        清空标绘
      </el-button>
    </el-row>

    <!-- 地图部分 -->
    <div id="mapCon"></div>
    <!-- 添加地标对话框 -->
    <el-dialog title="添加地标" :visible.sync="dialogVisible" width="30%">
      <el-form :model="landmarkForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="landmarkForm.title"></el-input>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="landmarkForm.description" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false;currentTool= 'none'">取消</el-button>
        <el-button type="primary" @click="addLandMarkerWithInfo">确认</el-button>
      </span>
    </el-dialog>

    <!-- 地标信息 -->
    <div v-if="showInfo" class="landmark-info" :style="infoStyle">
      <el-card shadow="hover" :body-style="{ maxWidth: '400px', maxHeight: '600px', overflow: 'auto' }">
        <div slot="header">
          <strong>{{ currentLandmark.title }}</strong>
        </div>
        <p>{{ currentLandmark.description }}</p>
        <p>经度: {{ currentLandmark.lngLat.lng.toFixed(4) }}, 纬度: {{ currentLandmark.lngLat.lat.toFixed(4) }}</p>
      </el-card>
    </div>

    <!-- 地标操作 -->
    <div v-if="showActions" class="landmark-actions" :style="actionsStyle">
      <el-card shadow="hover" :body-style="{ maxWidth: '400px', maxHeight: '600px', overflow: 'auto' }">
        <div slot="header">
          <span>地标操作</span>
        </div>
        <el-button @click="editLandmark(currentLandmark)">编辑</el-button>
        <el-button type="danger" @click="deleteLandmark(currentLandmark)">删除</el-button>
      </el-card>
    </div>


  </div>
</template>

<script>
import mapboxgl from "../../utils/mapbox";
import * as turf from "@turf/turf";
export default {
  name: "MapComponent",
  data() {
    return {
      selectedUnit: 'meters',//单位
      map: null,//地图实例
      markers: [],//标记数组
      landmarkers:[],
      currentTool: "none", // 当前工具
      distance: 0,//距离
      area: 0,//面积
      dialogVisible: false, // 对话框是否可见
      landmarkForm: { // 地标表单
        title: '',
        description: ''
      },
      currentInfoEl: null,
      landmarkLngLat:null,
      showInfo: false, // 是否显示地标信息
      showActions: false, // 是否显示地标操作
      infoStyle: {}, // 地标信息样式
      actionsStyle: {}, // 地标操作样式
      currentLandmark:null

    };
  },
  mounted() {
    this.initMap();//初始化
  },
  methods: {
    //初始化
    initMap() {
      // 创建地图
      this.map = new mapboxgl.Map({
        container: "mapCon",
        style: "mapbox://styles/mapbox/streets-v11",
        center: [114.41703647375107, 23.10750961303711],
        zoom: 12,
      });

      // 添加地图控件
      this.map.addControl(new mapboxgl.NavigationControl(), "top-left");

      // 监听地图点击事件，处理不同工具的功能
      this.map.on("click", (e) => {
        if (this.currentTool === "distance") {
          this.handleDistanceMeasurement(e);
        } else if (this.currentTool === "area") {
          this.handleAreaMeasurement(e);
        } else if (this.currentTool === "landmarker") {
          this.showAddLandmarkDialog(e.lngLat);
        }
      });
    },

    //切换功能模块
    switchTool(tool) {
      this.currentTool = tool;
      this.clearAllDatas();
    },

    //添加距离图标，diatance
    handleDistanceMeasurement(e) {
      //添加图标
      this.addMarker(e.lngLat);

      if (this.markers.length === 2) {
        // 计算距离
        const line = turf.lineString([this.markers[0].getLngLat().toArray(), this.markers[1].getLngLat().toArray()]);
        const distance = turf.length(line, { units: this.selectedUnit });
        this.distance = distance.toFixed(2);

        //画线
        this.drawLineBetweenPoints(this.markers[0].getLngLat().toArray(), this.markers[1].getLngLat().toArray(),this.distance);
        this.currentTool = "none";
      }
    },

    //添加面积图标
    handleAreaMeasurement(e) {

      this.addMarker(e.lngLat);
      // 如果已经有三个点，绘制多边形
      if (this.markers.length >= 3) {
        //this.drawPolygon(this.drawPoints);
      }

    },

    // 显示添加地标对话框
    showAddLandmarkDialog(lngLat) {
      this.dialogVisible = true;
      this.landmarkLngLat = lngLat; // 保存点击位置
    },

    // 绘制多边形
    drawPolygon(polygon) {
      // 创建一个符合 GeoJSON 格式的对象
      const geojson = {
        type: "FeatureCollection",
        features: [
          {
            type: "Feature",
            geometry: {
              type: "Polygon", // 这里定义为多边形
              coordinates: [polygon], // 注意是二维数组
            },
            properties: {},
          },
        ],
      };

      // 在地图上添加数据源
      this.map.addSource("custom-polygon", {
        type: "geojson",
        data: geojson, // 这里传递的是一个有效的 GeoJSON 对象
      });

      // 添加图层，绘制多边形
      this.map.addLayer({
        id: "custom-polygon-layer",
        type: "fill",
        source: "custom-polygon",
        paint: {
          "fill-color": "rgba(0, 255, 0, 0.4)", // 设置填充颜色
          "fill-outline-color": "rgba(0, 255, 0, 1)", // 设置边框颜色
        },
      });
    },

    //测量面积
    confirmAreaMeasurement() {

      if (this.markers.length >= 3) {
        const coordinates = this.markers.map(marker => marker.getLngLat().toArray());
        // 闭合多边形（把第一个点再添加到末尾）
        coordinates.push(coordinates[0]);
        this.drawPolygon(coordinates);
        const polygon = turf.polygon([coordinates]);
        let area = turf.area(polygon);
        // 根据选择的单位进行转换
        if (this.selectedUnit === "kilometers") {
          area = area / 1000000; // 转换为平方千米
        } else if (this.selectedUnit === "miles") {
          area = area / 2.589988; // 转换为平方英里
        } else if (this.selectedUnit === "feet") {
          area = area * 10.7639; // 转换为平方英尺
        }
        this.area = area.toFixed(2);

        // 显示面积，单位会根据 selectedUnit 变化
        this.$message.success(`所选区域的面积为 ${this.area} ${this.selectedUnit === 'meters' ? '平方米' : this.selectedUnit === 'kilometers' ? '平方千米' : this.selectedUnit === 'miles' ? '平方英里' : '平方英尺'}`);
        this.currentTool = "none";
      }
    },

    //添加标记
    addMarker(e) {
      let el = document.createElement("div")
      el.innerHTML='📍';
      el.id = 'marks';
      el.style.width = '10px';
      el.style.height = '10px';
      el.style.display = 'flex'; // 用于居中显示内容
      el.style.justifyContent = 'center'; // 水平居中
      el.style.alignItems = 'center'; // 垂直居中
      el.style.fontSize = '25px'; // 设置字体大小
      //el.style.backgroundColor = '#6699ff';  // 这个可以保留，设置背景颜色
      //el.style.borderRadius = '50%';  // 使其为圆形

      const marker = new mapboxgl.Marker(el)
          .setLngLat(e)
          .addTo(this.map)
       //   .getElement().addEventListener('mouseenter', )
        //  .getElement().addEventListener('mouseleave', )
       //   .getElement().addEventListener('click', )
      ;
      this.markers.push(marker);
    },

    //添加地标并关闭对话框
    addLandMarkerWithInfo() {

      if (this.landmarkForm.title && this.landmarkForm.description) {
        this.addLandMarker(this.landmarkLngLat, this.landmarkForm.title, this.landmarkForm.description);
        this.dialogVisible = false;
        this.currentTool = 'none';
      }
      else {
        this.$message.error('请输入标题和说明');
      }
    },

    //添加标记并记录信息
    addLandMarker(lngLat, title, description) {
      let el = document.createElement("div");
      el.innerHTML = '📌';
      el.id = 'marks';
      el.style.width = '10px';
      el.style.height = '10px';
      el.style.display = 'flex'; // 用于居中显示内容
      el.style.justifyContent = 'center'; // 水平居中
      el.style.alignItems = 'center'; // 垂直居中
      el.style.fontSize = '25px'; // 设置字体大小

      const marker = new mapboxgl.Marker(el)
          .setLngLat(lngLat)
          .addTo(this.map);

      // 保存地标信息
      const landmark = {
        marker: marker,
        title: title,
        description: description,
        lngLat: lngLat
      };

      this.landmarkers.push(landmark);

      console.log('Marker Element:', marker.getElement());

      // 添加鼠标悬停事件
      marker.getElement().addEventListener('mouseenter', () => {
        console.log('Mouse Enter Event Triggered');
        this.showLandmarkInfo(landmark);
      });

      // 添加鼠标离开事件
      marker.getElement().addEventListener('mouseleave', () => {
        console.log('Mouse Leave Event Triggered');
        this.hideLandmarkInfo();
      });

      // 添加鼠标点击事件
      marker.getElement().addEventListener('click', () => {
        console.log('Click Event Triggered');
        this.showLandmarkActions(landmark);
      });
    },

    // 显示地标信息
    showLandmarkInfo(landmark) {
      console.log('Showing Landmark Info:', landmark);

      const { x, y } = this.map.project(landmark.lngLat);
      console.log('Landmark position:', { x, y });

      this.infoStyle = {
        left: `${x + 150}px`,
        top: `${y -60}px`
      };


      this.currentLandmark = landmark;
      this.showInfo = true;

    },

    // 隐藏地标信息
    hideLandmarkInfo() {
      this.showInfo = false;
      this.showActions = false;
      this.currentLandmark = null;
    },

    // 显示地标操作（删除、编辑）
    showLandmarkActions(landmark) {
      // 创建一个包含地标操作的 div 元素
      const actionsEl = document.createElement('div');
      actionsEl.className = 'landmark-actions';
      actionsEl.innerHTML = `
    <el-button class="landmark-action-btn" @click="editLandmark(landmark)">编辑</el-button>
    <el-button type="danger" class="landmark-action-btn" @click="deleteLandmark(landmark)">删除</el-button>
  `;
      actionsEl.style.position = 'absolute';
      actionsEl.style.backgroundColor = 'white';
      actionsEl.style.border = '1px solid #ccc';
      actionsEl.style.padding = '5px';
      actionsEl.style.boxShadow = '0 2px 5px rgba(0, 0, 0, 0.2)';
      actionsEl.style.zIndex = '1000';

      // 计算地标操作框的位置
      const { x, y } = this.map.project(landmark.lngLat);
      actionsEl.style.left = `${x}px`;
      actionsEl.style.top = `${y - 60}px`;

      // 将地标操作框添加到地图的 canvas 上
      this.map.getCanvas().appendChild(actionsEl);

      // 保存当前的操作元素
      this.currentActionsEl = actionsEl;

    },

    // 编辑地标
    editLandmark(landmark) {
      console.log('Editing landmark:', landmark);
      this.landmarkForm.title = landmark.title;
      this.landmarkForm.description = landmark.description;
      this.dialogVisible = true;

    },

    // 删除地标
    deleteLandmark(landmark) {
      console.log('Deleting landmark:', landmark);
      this.landmarkers = this.landmarkers.filter(lm => lm !== landmark);
      landmark.marker.remove();
      this.hideLandmarkInfo();

    },

    // 隐藏地标操作
    hideLandmarkActions() {
      if (this.currentActionsEl) {
        this.currentActionsEl.remove();
        this.currentActionsEl = null;
      }
    },


    // 封装的绘制线段方法
    drawLineBetweenPoints(point1,point2,label) {

      const coordinates = [point1, point2];

      // 添加 GeoJSON 数据源
      this.map.addSource('custom-line', {
        type: 'geojson',
        data: {
          type: 'Feature',
          geometry: {
            type: 'LineString',
            coordinates: coordinates // 使用 markers 中的经纬度
          }
        }
      });

      // 添加线段图层
      this.map.addLayer({
        id: 'custom-line-layer',
        type: 'line',
        source: 'custom-line',
        paint: {
          'line-color': 'rgba(92,225,69,0.72)', // 设置线段颜色
          'line-width': 4 // 设置线段宽度
        }
      });

      //文本
      this.map.addLayer({
        id: 'custom-label-layer',
        type: 'symbol',
        source: 'custom-line',
        layout: {
          'symbol-placement': 'line',
          'text-field': label, // 显示的文本
          'text-size': 16,
          'text-anchor': 'center',
          'text-allow-overlap': true,
          'text-offset': [0, 0], // 竖直方向和水平方向都居中
        },
        paint: {
          'text-color': '#f10f07', // 设置文本颜色
        },
      });
    },

    // 删除所有数据
    clearAllDatas() {
      // 移除标记
      this.markers.forEach(marker => marker.remove());
      this.markers = [];

      // 移除自定义图层和数据源
      // 获取所有图层
      const layers = this.map.getStyle().layers;

      if (layers) {
        const sourceUsageCount = {};

        // 统计每个数据源被多少图层使用
        layers.forEach(layer => {
          if (layer.source) {
            sourceUsageCount[layer.source] = (sourceUsageCount[layer.source] || 0) + 1;
          }
        });

        // 删除图层
        layers.forEach(layer => {
          if (layer.id.startsWith("custom-")) {  // 仅删除自定义图层
            // 删除图层
            if (this.map.getLayer(layer.id)) {
              this.map.removeLayer(layer.id);
            }

            // 减少数据源的使用计数
            if (layer.source && sourceUsageCount[layer.source]) {
              sourceUsageCount[layer.source]--;
            }
          }
        });

        // 删除数据源：只有当该数据源不再被任何图层使用时，才删除
        Object.keys(sourceUsageCount).forEach(sourceId => {
          if (sourceUsageCount[sourceId] === 0 && this.map.getSource(sourceId)) {
            this.map.removeSource(sourceId);
          }
        });
      }


    }
  }
};
</script>

<style scoped>
  #mapCon {
    height: calc(100vh - 120px);
    width: 100%;
  }
  .toolbar-container {
    position: absolute;
    top: 50px;
    left: 70px; /* 左边对齐 */
    right: 0; /* 右边对齐 */
    margin: 0 auto; /* 水平居中 */
    background: rgba(255, 255, 255, 0.9); /* 半透明白色背景 */
    padding: 10px;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    display: flex;
    gap: 10px;
    z-index: 1000; /* 确保在地图控件之上 */
    width: fit-content; /* 根据内容自动调整宽度 */
  }


  .landmark-action-btn {
    cursor: pointer;
    margin-right: 5px;
  }

  /* 地标元素样式 */
  #marks {
    border: 2px solid red; /* 临时添加边框以便更容易看到地标 */
    cursor: pointer; /* 改变鼠标指针样式 */
  }

  /* 地标信息样式 */
  .landmark-info {
    position: absolute;
    background-color: white;
    border: 1px solid #ccc;
    padding: 5px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    z-index: 9999; /* 确保地标信息在最上层 */
  }


</style>
