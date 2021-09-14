var exec = require('cordova/exec');
module.exports = {
    /**
     *
     exec方法一共5个参数:
     第一个 :成功回调
     第二个 :失败回调
     第三个 :将要调用的对象名(在config.xml中配置,对应于feature的name属性，value就是本地实现的java类)
     第四个 :调用的方法名(java类中通过action识别方法名)
     第五个 :传递的参数（json格式）
     */
    //startNavigating ： 插件的导航方法，方法名自己取。
    //longtOrig,latOrig,longtDes,latDes：分别表示起点和终点的经纬度。
    //通过调用cordova.exec（）方法链接本地的java实现。
    /**
     * 获取位置信息
     * @param {Function} successCallback 成功获取数据回调
     * @param {Function} errorCallback 失败回调 
     * @param {boolean} isSingle 位置数据回调间隔 可选 默认 true
     */
    get: function (successCallback, errorCallback,isSingle) {
        cordova.exec(
            successCallback,
            errorCallback,
            "BaiduLocation", "get", [isSingle]);
    },
    /**
     * 在执行完get 操作后，理论上定位服务会持续后台定位，该操作可停止定位
     */
    stop: function () {
        cordova.exec(
            null,
            null,
            "BaiduLocation", "stop", []);
    },
    /**
     * 配置请求定位参数，该方法配置后，会起到全局效果，所有的请求操作都将生效.不设置，将使用默认值进行定位
     * @param {String} mCoorType 请求坐标类型 可选 默认gcj02
     * @param {int} mSpan 位置数据回调间隔 可选 默认 1000,注：该参数暂时未实现，可忽略
     */
    config: function(mCoorType,mSpan){
        cordova.exec(
            null,
            null,
            "BaiduLocation", "config", [mCoorType,mSpan]);
    }
    // /**
    //  * 持续回调该方法，返回定位数据
    //  * @param {Object} locationData 定位数据
    //  */
    // onReceiveLocationInAndroidCallback:function(locationData){
    //     locationData = JSON.stringify(locationData);
    //     locationData = JSON.parse(locationData);
    //     cordova.fireDocumentEvent('baiduLocation.onReceiveLocation', locationData);
    // }
};

