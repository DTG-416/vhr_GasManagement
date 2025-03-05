<template>
  <div>
    <div id="mapCon"></div>

    <!-- 操作面板（右下角） -->
    <div class="control-panel">
      <el-button type="primary" circle icon="el-icon-arrow-up" @click="rotateUp"></el-button>
      <el-button type="primary" circle icon="el-icon-arrow-down" @click="rotateDown"></el-button>
      <el-button type="primary" circle icon="el-icon-arrow-left" @click="rotateLeft"></el-button>
      <el-button type="primary" circle icon="el-icon-arrow-right" @click="rotateRight"></el-button>

      <!-- 光照按钮 -->
      <el-button type="primary" circle icon="el-icon-sunrise" @click="changeLighting"></el-button>
    </div>

    <!-- 经纬度显示（左上角） -->
    <el-card class="coordinates" :body-style="{ padding: '10px' }">
      <div class="main">
        <p>经度: {{ lng }}</p>
        <p>纬度: {{ lat }}</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import mapboxgl from '../../utils/mapbox';

export default {
  name: "GasMapBox3D",
  data() {
    return {
      map: null,
      lng: 0,
      lat: 0,
      pitch: 45,
      bearing: -17.6,
      lightMode: 0,  // 光照模式索引，0表示黑夜，1-5表示五个白天角度
      lightingAngles: [
        { azimuth: 45, altitude: 45, color: 'rgba(255, 255, 255, 1)', intensity: 0.8 },  // 白天1
        { azimuth: 135, altitude: 45, color: 'rgba(255, 255, 204, 1)', intensity: 0.9 }, // 白天2
        { azimuth: 225, altitude: 45, color: 'rgba(255, 204, 204, 1)', intensity: 0.7 }, // 白天3
        { azimuth: 315, altitude: 45, color: 'rgba(204, 204, 255, 1)', intensity: 0.75 }, // 白天4
        { azimuth: 0, altitude: 90, color: 'rgba(255, 255, 255, 1)', intensity: 1.0 },   // 白天5（正上方）
        { azimuth: 0, altitude: -90, color: 'rgba(0, 0, 0, 0)', intensity: 0 }          // 黑夜（没有光照）
      ]
    };
  },
  mounted() {
    this.initMap();
  },
  methods: {
    // 初始化 Mapbox 地图
    initMap() {
      this.map = new mapboxgl.Map({
        container: 'mapCon',
        style: 'mapbox://styles/mapbox/light-v10',
        center: [114.417, 23.107],
        zoom: 15,
        pitch: this.pitch,
        bearing: this.bearing,
        antialias: true
      });

      this.map.on('mousemove', this.showCoordinates);
      this.map.on('load', this.add3DBuildings);
      this.updateLighting();  // 初始设置光照
    },

    // 添加 3D 建筑层
    add3DBuildings() {
      const layers = this.map.getStyle().layers;
      const labelLayerId = layers.find(layer => layer.type === 'symbol' && layer.layout['text-field'])?.id;

      this.map.addLayer({
        id: '3d-buildings',
        source: 'composite',
        'source-layer': 'building',
        filter: ['==', 'extrude', 'true'],
        type: 'fill-extrusion',
        minzoom: 15,
        paint: {
          'fill-extrusion-color': '#aaa',
          'fill-extrusion-height': ['interpolate', ['linear'], ['zoom'], 15, 0, 15.05, ['get', 'height']],
          'fill-extrusion-base': ['interpolate', ['linear'], ['zoom'], 15, 0, 15.05, ['get', 'min_height']],
          'fill-extrusion-opacity': 0.6
        }
      }, labelLayerId);
    },

    // 旋转方向
    rotateUp() {
      if (this.pitch > 0) {
        this.pitch = Math.max(this.pitch - 45, 0);
        this.updateView();
      }
    },
    rotateDown() {
      if (this.pitch < 90) {
        this.pitch = Math.min(this.pitch + 45, 90);
        this.updateView();
      }
    },
    rotateLeft() {
      this.bearing = (this.bearing + 30 + 360) % 360;
      this.updateView();
    },
    rotateRight() {
      this.bearing = (this.bearing - 30) % 360;
      this.updateView();
    },
    updateView() {
      this.map.easeTo({
        pitch: this.pitch,
        bearing: this.bearing,
        duration: 800
      });
    },

    // 显示鼠标位置的经纬度
    showCoordinates(event) {
      this.lng = event.lngLat.lng.toFixed(6);
      this.lat = event.lngLat.lat.toFixed(6);
    },

    // 切换光照模式
    changeLighting() {
      this.lightMode = (this.lightMode + 1) % 6;  // 在0-5之间循环
      this.updateLighting();
    },

    // 更新光照设置
    updateLighting() {
      const currentLighting = this.lightingAngles[this.lightMode];

      if (currentLighting.altitude === -90) {
        // 黑夜：光照不可见
        this.map.setLight({ color: currentLighting.color, intensity: currentLighting.intensity });
      } else {
        // 白天：设置光照方向、强度和颜色
        this.map.setLight({
          color: currentLighting.color,
          intensity: currentLighting.intensity,
          position: [
            Math.cos(currentLighting.azimuth * Math.PI / 180) * Math.cos(currentLighting.altitude * Math.PI / 180),
            Math.sin(currentLighting.azimuth * Math.PI / 180) * Math.cos(currentLighting.altitude * Math.PI / 180),
            Math.sin(currentLighting.altitude * Math.PI / 180)
          ]
        });
      }
    }
  }
};
</script>

<style scoped>
/* 地图容器 */
#mapCon {
  height: calc(100vh - 120px);
  width: 100%;
}

/* 操作面板（右下角） */
.control-panel {
  position: absolute;
  bottom: 40px;
  right: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: row;
  gap: 8px;
}

/* 每个按钮都设置为圆形 */
/*.el-button[shape="circle"] {
  width: 40px;
  height: 40px;
  padding: 0;
  font-size: 20px;
  line-height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
}*/

/* 经纬度显示（左上角） */
.coordinates {
  position: absolute;
  top: 104px;
  left: 220px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 5px;
  font-size: 14px;
}

.coordinates .main {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
</style>
