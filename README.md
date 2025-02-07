# cordova-plugin-ths-baidulocation

百度定位Cordova插件，支持Android平台。本插件基于百度定位SDK，提供了获取位置信息、停止定位以及配置定位参数等功能。

## 安装

```bash
cordova plugin add cordova-plugin-ths-baidulocation --variable BD_AK=你的百度地图AK密钥
```

或者通过git地址安装：

```bash
cordova plugin add https://github.com/THS-FE/cordova-plugin-ths-baidulocation.git --variable BD_AK=你的百度地图AK密钥
```

## 支持平台

- Android 4.0.0 及以上版本

## API说明

### 获取位置信息

```javascript
navigator.baidulocation.get(successCallback, errorCallback, isSingle)
```

#### 参数说明
- `successCallback`: 成功获取数据的回调函数
- `errorCallback`: 失败回调函数
- `isSingle`: 是否单次定位，可选参数，默认为true
  - `true`: 单次定位
  - `false`: 连续定位

#### 返回数据示例
```javascript
{
    "time": "2023-xx-xx xx:xx:xx",    // 定位时间
    "locType": 61,                    // 定位类型
    "latitude": 39.xxx,              // 纬度
    "longitude": 116.xxx,            // 经度
    "radius": 30.0,                  // 精度半径
    "coorType": "gcj02",            // 坐标系类型
    "addr": "xxx路xxx号",            // 地址信息
    "province": "xxx省",            // 省份
    "city": "xxx市",                // 城市
    "district": "xxx区",            // 区县
    "street": "xxx路"               // 街道
}
```

### 停止定位

```javascript
navigator.baidulocation.stop()
```

在执行完get操作后，定位服务会持续在后台运行，可以通过此方法停止定位服务。

### 配置定位参数

```javascript
navigator.baidulocation.config(mCoorType, mSpan)
```

#### 参数说明
- `mCoorType`: 请求的坐标系类型，可选参数，默认为"gcj02"
  - `gcj02`: 国测局坐标系
  - `bd09`: 百度墨卡托坐标系
  - `bd09ll`: 百度经纬度坐标系
- `mSpan`: 位置数据回调时间间隔，单位毫秒，可选参数，默认为1000

## 注意事项

1. 使用本插件前，需要先申请百度地图API密钥（AK）
2. Android 6.0及以上版本需要动态申请定位权限
3. 插件会自动处理Android后台定位，无需额外配置
4. 建议在设备就绪后再调用定位相关接口

## 示例代码

```javascript
// 单次定位
navigator.baidulocation.get(
    function(location) {
        console.log('定位成功：', location);
    },
    function(error) {
        console.error('定位失败：', error);
    },
    true
);

// 连续定位
navigator.baidulocation.get(
    function(location) {
        console.log('持续定位：', location);
    },
    function(error) {
        console.error('定位失败：', error);
    },
    false
);

// 配置定位参数
navigator.baidulocation.config('bd09ll', 2000);

// 停止定位
navigator.baidulocation.stop();
```

## 许可证

Apache License 2.0