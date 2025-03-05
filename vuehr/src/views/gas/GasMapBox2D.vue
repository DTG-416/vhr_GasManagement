<template>
  <div>
    <div id="mapCon"></div>
    <!-- 控制面板 -->
    <div class="selectInfo">
      <button @click="toggleDraw('point')">绘制点</button>
      <button @click="toggleDraw('line')">绘制线</button>
      <button @click="toggleDraw('polygon')">绘制面</button>
      <button @click="startDistanceMeasure">测量距离</button>
      <button @click="startAreaMeasure">测量面积</button>
      <button @click="clearMap">清空标绘</button>
      <div v-if="distanceResult">
        <p>测量距离: {{ distanceResult }} 米</p>
      </div>
      <div v-if="areaResult">
        <p>测量面积: {{ areaResult }} 平方米</p>
      </div>
    </div>
  </div>
</template>

<script>
// 引入 Mapbox 和相关工具库
import mapboxgl from '../../utils/mapbox';
import MapboxDraw from '@mapbox/mapbox-gl-draw';
import '@mapbox/mapbox-gl-draw/dist/mapbox-gl-draw.css';
import turf from '@turf/turf';

export default {
  name: 'GasMapBox3D',
  data() {
    return {
      map: null, // Mapbox 地图实例
      draw: null, // 绘制工具实例
      distanceResult: null, // 存储测量的距离
      areaResult: null, // 存储测量的面积
      distanceCoordinates: [], // 存储距离测量的坐标点
      areaCoordinates: [], // 存储面积测量的坐标点
    };
  },
  mounted() {
    // 初始化地图
    this.map = new mapboxgl.Map({
      container: 'mapCon',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [114.41703647375107, 23.10750961303711],
      zoom: 12
    });

    // 初始化绘制工具
    this.draw = new MapboxDraw({
      displayControlsDefault: false,
      controls: {
        point: true,
        line_string: true,
        polygon: true,
        trash: true
      }
    });

    this.map.on('load', () => {
      this.map.addControl(this.draw);
    });
  },
  methods: {
    // 切换绘制模式
    toggleDraw(type) {
      this.draw.changeMode(type); // 切换绘制模式
    },

    // 开始测量距离
    startDistanceMeasure() {
      this.distanceCoordinates = [];
      this.distanceResult = null; // 清除上次测量结果
      this.map.on('click', this.handleDistanceClick);
    },

    // 距离测量点击事件
    handleDistanceClick(e) {
      this.distanceCoordinates.push(e.lngLat);
      if (this.distanceCoordinates.length > 1) {
        // 计算两点间的距离
        const from = this.distanceCoordinates[this.distanceCoordinates.length - 2];
        const to = this.distanceCoordinates[this.distanceCoordinates.length - 1];
        const distance = turf.distance([from.lng, from.lat], [to.lng, to.lat]);
        this.distanceResult = distance.toFixed(2); // 显示到两位小数
        this.map.off('click', this.handleDistanceClick); // 停止监听
      }
    },

    // 开始测量面积
    startAreaMeasure() {
      this.areaCoordinates = [];
      this.areaResult = null; // 清除上次测量结果
      this.map.on('click', this.handleAreaClick);
    },

    // 面积测量点击事件
    handleAreaClick(e) {
      this.areaCoordinates.push(e.lngLat);
      if (this.areaCoordinates.length > 2) {
        // 创建一个闭合的多边形
        const polygon = turf.polygon([this.areaCoordinates.map(coord => [coord.lng, coord.lat])]);
        const area = turf.area(polygon); // 计算面积
        this.areaResult = area.toFixed(2); // 显示到两位小数
        this.map.off('click', this.handleAreaClick); // 停止监听
      }
    },

    // 清空地图上的所有标绘
    clearMap() {
      this.draw.deleteAll();
      this.distanceCoordinates = [];
      this.areaCoordinates = [];
      this.distanceResult = null;
      this.areaResult = null;
    }
  }
}
</script>

<style scoped>
#mapCon {
  height: calc(100vh - 120px);
  width: 100%;
}

.selectInfo {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.8);
  padding: 10px;
  border-radius: 5px;
}

button {
  margin: 5px;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

p {
  margin: 5px 0;
}
</style>
