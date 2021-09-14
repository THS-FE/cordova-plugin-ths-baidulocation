# cordova-plugin-ths-baidulocation

基于百度定位SDK的Cordova插件

## 支持平台

Android

## 安装插件

```bash
# 通过npm 安装插件
cordova plugin add cordova-plugin-ths-baidulocation  --variable BD_AK=keyvalue
# 通过github安装
cordova plugin add https://github.com/THS-FE/cordova-plugin-ths-baidulocation  --variable BD_AK=keyvalue
# 通过本地文件路径安装
cordova plugin add 文件路径  --variable BD_AK=keyvalue
# keyvalue 为在百度地图开放平台上注册的百度安卓定位SDK 
```

[BD_AK申请](http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/create-project/key)

**说明： ionic 项目命令前加上ionic，即ionic cordova plugin xxxxx**

## 使用方法

配置定位参数

```typescript
  /**
   * 配置请求定位参数，该方法配置后，会起到全局效果，所有的请求操作都将生效.不设置，将使用默认值进行定位
   * @param {String} mCoorType 请求坐标类型 可选 默认gcj02
   */
navigator.baidulocation.config('bd09ll');
```

开启定位

```typescript
    /**
     * 获取位置信息
     * @param {Function} successCallback 成功获取数据回调
     * @param {Function} errorCallback 失败回调 
     * @param {boolean} isSingle 是否单次返回数据 可选 默认 true，只返回一次数据，false successCallback方法将持续收到位置数据，适合轨迹类持续收集位置信息场景开发
     */
navigator.baidulocation.get((res) => {
      console.log('tab1', res);
    }, (err) => {
      alert(err);
}, false);
```

停止定位

```typescript
    /**
     * 在执行完get 操作后，理论上定位服务会持续后台定位，该操作可停止所有定位请求，包括正在持续获取位置的请求
     */
navigator.baidulocation.stop();
```





**说明：使用ts 进行开发时，需要在文件上变声明下declare const navigator，不然会报错;**

```typescript
import { Component, OnInit, Input } from '@angular/core';
import { WebIntent } from '@ionic-native/web-intent/ngx';
declare const navigator;
@Component({
  selector: 'app-explore-container',
  templateUrl: './explore-container.component.html',
  styleUrls: ['./explore-container.component.scss'],
})
```

## 常见错误

