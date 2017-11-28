/**
 * Created by Alien on 2017/3/10.
 * 初始化地图
 */
(function(){
    var positions = [];
    function getPostions() {
        $.ajax({
            url: '/data',
            type: 'get',
            async: false,
            success: function (res) {
                // console.log(res);
                let arr = res.split("@");
                positions = JSON.parse(arr[0]);
                window.positions = positions;
            }
        });
    }
    function initialize() {
        createMap();        //创建地图
        setMapEvent();      //设置地图事件
        addMapControl();    //增加地图控件
        getPostions();      //获取数据
        // addMarker(positions,map);   //加载海量点
    }

    function createMap() {
        const map = new BMap.Map("map",{});
        const point = new BMap.Point(120.16711642992,30.25283633644);
        map.centerAndZoom(point, 15);

        const styleJson = [
            {
                "featureType": "all",
                "elementType": "all",
                "stylers": {
                    "lightness": 10,
                    "saturation": -100
                }
            }
        ];

        map.setMapStyle({styleJson});
        window.map = map;
    }

    function setMapEvent() {
        map.enableDragging();//地图拖拽事件
        map.enableKeyboard();//启用键盘上下左右键移动地图
    }

//地图控件添加函数
    function addMapControl() {
        //缩放控件
        let naviControl = new BMap.NavigationControl({
            anchor : BMAP_ANCHOR_TOP_LEFT
        });
        map.addControl(naviControl);

        //缩略图控件
        let overControl = new BMap.OverviewMapControl({
            anchor : BMAP_ANCHOR_BOTTOM_RIGHT,
            isOpen : false,
            offset : new BMap.Size(50, 50)
        });
        map.addControl(overControl);

        //比例尺控件
        let scaleControl = new BMap.ScaleControl({
            anchor : BMAP_ANCHOR_BOTTOM_LEFT,
            offset : new BMap.Size(50, 20)
        });
        map.addControl(scaleControl);
    }

    function addMarker(data,obj) {
        if (document.createElement('canvas').getContext) {
            var points = [];  // 添加海量点数据
            for (var i = 0; i < data.length; i++) {
                points.push(new BMap.Point(data[i].lng, data[i].lat));
            }
            var options = {
                size: BMAP_POINT_SIZE_NORMAL,
                shape: BMAP_POINT_SHAPE_CIRCLE,
                color: '#03a9f4'
            };
            var pointCollection = new BMap.PointCollection(points, options);  // 初始化PointCollection

            pointCollection.addEventListener("click",function (event) {
                let selStation = findPointInfo(event);
                let stationname = selStation.stationname;
                $(".sel-station").val(stationname);
            });
            obj.addOverlay(pointCollection);  // 添加Overlay
        }
    }

    function findPointInfo(target){
        var point = [];
        point.push(target.point.lng, target.point.lat);
        var mirror = [];
        for(var i=0; i<positions.length; i++){
            mirror.push(positions[i].lng, positions[i].lat);
            if(point.toString() == mirror.toString()){   //比较两个数组要先转换为字符串
                return positions[i];
            }
            mirror.length = 0;
        }
    }

    initialize();
})();
