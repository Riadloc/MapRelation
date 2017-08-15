/**
 * Created by Alien on 2017/3/10.
 * 基本事件
 */
(function () {
    var myChart= null,colors=[],size,time= null,sign= null,oldcores= [],trace= [],tmplines=[],scatters= [],relations= [],scatIds= [],curvelines= [],cores= [],selScatter= [];

    (function initialize() {
        initialDefinite();
        bindListener();
    })();

    function initialDefinite() {
        sign = false;
        myChart = echarts.init($('.histogram')[0], 'dark');
        colors= [red, purple, blue, limeA700, yellow600, orange600, indigoA400, cyanA200, pink, pink200, greenA200, blue200, brown600,
            yellow300, lime900, deepOrange400, green900, purple900, red900, orange200, redA100, pink900,
            purple200, cyan200, cyan900, lime400, lightBlue900, deepPurpleA200, green400, brown200, deepPurple200,
            indigo900, indigo100,indigo300,blueGrey700 ,grey700,grey400,black,deepOrange200];
        size = [BMAP_POINT_SIZE_SMALL,BMAP_POINT_SIZE_NORMAL,BMAP_POINT_SIZE_BIG,BMAP_POINT_SIZE_BIGGER,BMAP_POINT_SIZE_HUGE],
            $(".fromDate").flatpickr({
                minDate: "2014-03-01",
                maxDate: "2014-06-22"
            });
        $(".toDate").flatpickr({
            minDate: "2014-03-02",
            maxDate: "2014-06-23"
        });
        $(".the-day").flatpickr({
            minDate: "2014-03-01",
            maxDate: "2014-06-23"
        });
    }

    function bindListener() {
        $(".day").click(() => generateFilesByTime("day"));
        $(".hour").click(() => generateFilesByTime("hour"));
        $(".block").click(() => generateFilesByTime("block"));
        $(".undir-day").click(() => generateFilesByTime("udday"));
        $(".undir-block").click(() => generateFilesByTime("udblock"));
        $(".day—scatter").click(() => {if(sign) addScatter("day");});
        $(".hour—scatter").click(() => sign&&addScatter("hour"));
        $(".day-core").click(() => sign && addCore("day"));
        $(".scatter-relation").click(() => sign && addScatterRels());
        $(".hour-core").click(() => sign && addCore("hour"));
        $(".cluster").click(() => sign && generateFilesByTime("cluster"));
        $(".select-scatter").click(() => sign && selectScatter());
        $(".day—scatter-filter").click(function(){
            if (sign) {
                map.clearOverlays();
                $(".loading").show();
                let id = trace[0];
                addCurvlines(relations[id],null);
            }
        });
        $('#xFile').change(function(e){
            clearOverlays();
            if((!!cores.length) && (!oldcores.length)) {
                oldcores = cores;
                cores = [];
            }
            $(".file-name").text(e.currentTarget.files[0].name);
            sign = true;
            console.log(e.currentTarget.files[0]);//e就是你获取的file对象
        });
    }

    function clearOverlays() {
        $('.charts').hide();
        map.clearOverlays();
    }

    function showLoading() {
        $('.charts').show();
        myChart.showLoading();
    }

    var Common = {
        getTime(){
            let time = $(".file-name").text();
            if (time.length >= 28) {
                time = time.slice(6,-4);
            } else if (time.length == 19) {
                time = time.substr(6,9);
            } else if (time.length == 14){
                time = time.substr(6,4);
            } else {
                time = time.substr(6,2);
            }
            return time;
        },
        getInfo(point) {
            let info = {};
            for (let i=0;i<positions.length;i++) {
                let obj = positions[i];
                if ([obj.lng, obj.lat].toString() == [point.lng, point.lat].toString()) {
                    info = [obj.stationid,obj.stationname];
                    break;
                }
            }
            return info;
        },
        getID(point) {
            for (let key in scatters) {
                if (scatters[key].indexOf(point) != -1) {
                    return key;
                }
            }
        },
        getLngLat(id) {    //返回坐标点
            let point = {};
            for (let i=0;i<positions.length;i++) {
                let obj = positions[i];
                if (obj.stationid === id) {
                    let xlng = parseFloat(obj.lng);
                    let ylat = parseFloat(obj.lat);
                    point = {lng: xlng, lat: ylat};
                    break;
                }
            }
            return point;
        },
        getWeight(maxnum,num) {
            let weight = (num/maxnum).toFixed(2);
            if (weight < 0.1)   weight = 0.1;
            return weight;
        },
        color() {
            cores = getCore(scatters);let colorize=[];
            if (!!oldcores.length) {
                let len = oldcores.length;
                let flag = new Array(len);
                cores.forEach(function (obj,i) {
                    if (Math.min.apply(Math, flag) == 1) {
                        colorize[i] = colors[(++len)%38];
                        console.log(flag);
                    } else {
                        let distance = oldcores.map(function (item, j) {
                            return flag[j]?1E+10:map.getDistance(obj, item);
                        });
                        let min = Math.min.apply(Math, distance);
                        let index = distance.indexOf(min);
                        colorize[i] = colors[index % 38];
                        flag[index] = 1;
                    }
                });
            } else {
                colorize = colors;
            }
            return colorize;
        },
        getSize(index,distance) {
            let len = scatters[index].length;
            return Math.round(4*(len/distance));
        },
        getDistance() {
            let distance=[];
            for(key in scatters) {
                distance.push(scatters[key].length);
            }
            return Math.max(...distance);
        },
        getColor(weight) {
            let compute1 = d3.interpolate(green,yellow);
            let compute2 = d3.interpolate(yellow,red);
            switch (true){
                case weight>0&&weight<=0.5: return compute1(weight*2);break;
                case weight>0.5&&weight<=1: return compute2(weight*2-1);break;
                default: alert("出错！");
            }
        },
        initHistogram() {
            myChart.setOption({
                color: ['#3388DB'],
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {type : 'shadow'}
                }, grid: {
                    left: '3%', right: '15%', bottom: '3%', containLabel: true
                }, xAxis : [
                    {
                        name: 'rentNum',
                        type : 'category',
                        data : [],
                        axisTick: {alignWithLabel: true}
                    }
                ], yAxis : [{
                    name: 'freqNum',
                    type : 'value'
                }
                ], series : [{
                    name:'热度',
                    type:'bar',
                    barWidth: '90%',
                    data:[]
                }
                ]
            });
        },
        initStackChart() {
            myChart.setOption({
                tooltip : {
                    trigger: 'axis',
                    axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                        type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                    }
                },
                legend: {
                    data: []
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis : [
                    {
                        type : 'category',
                        data : ['06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21']
                    }
                ],
                yAxis : [
                    {
                        type : 'value'
                    }
                ],
                series : [
                    {
                        name:'',
                        type:'bar',
                        stack: '热度',
                        data:[]
                    }
                ]
            })
        }
    };

    function generateFilesByTime(timeType) {
        let url,params;
        switch (timeType) {
            case("day"): {
                url = "/byday";
                params = {
                    from: $(".from1").val(),
                    to: $(".to1").val()
                };
                break;
            }
            case("udday"): {
                url = "/byudday";
                params = {
                    from: $(".from2").val(),
                    to: $(".to2").val()
                };
                break;
            }
            case("hour"): {
                url = "/byday";
                params = {
                    day: $(".the-day").val()
                };
                break;
            }
            case("block"): {
                url = "/byblock";
                params = {
                    from: $(".from1").val(),
                    to: $(".to1").val()
                };
                break;
            }
            case("udblock"): {
                url = "/byudblock";
                params = {
                    from: $(".from2").val(),
                    to: $(".to2").val()
                };
                break;
            }
            case("cluster"): {
                let time = Common.getTime();
                if (selScatter.length == 0) {
                    alert("请先选择聚点 or 选择文件错误");
                    return;
                }
                let stations = [];
                selScatter.forEach(function (key) {
                    scatters[key].forEach(function (item) {
                        stations.push(item);
                    })
                });
                url = "/bycluster";
                params = {
                    from: "",
                    to: "",
                    stations: stations.join(","),
                    number: selScatter.length,
                    traditional: true
                };
                if (time.length>4) {
                    params.from = time.substr(0,4);
                    params.to = time.substr(5,4);
                } else {
                    params.from = time;
                    params.to = time;
                }
                $.post(url,params).then((res) => {
                    console.log(res);
                }).catch((e) => console.log(e));
                break;
            }
        }

        function generateTask(url,params) {
            $.get(url,params).then((res) => {
                console.log(res);
            })
        }
        if(timeType!='cluster') {
            generateTask(url,params);
        }
    }

    function addScatter(type) {
        Scatter(type, function () {
            let color = Common.color();
            getScatter(scatters).forEach(function (item,index) {
                let options = {
                    color: color[index%38],
                    size: size[1]
                };
                Marker(item, options, "info");
                // if(!cores[0].length) {
                //     cores = getCore(scatters);
                // }
            });
            $(".loading").hide();
        });
    }

    function addCore(type) {
        let options = null;
        $('.charts').show();myChart.showLoading();
        Core(type, function () {
            clearOverlays();
            let color = Common.color();
            let distance = Common.getDistance();
            showHistogram();
            if (type == "day") {
                cores.forEach(function (item,index) {
                    options = {
                        color: color[index%38],
                        size: size[Common.getSize(index+1,distance)]
                    };
                    Marker([item],options);
                });
            } else {
                cores.forEach(function (item,index) {
                    let idx = selScatter[index];
                    options = {
                        color: color[idx%38],
                        size: size[Common.getSize(idx,distance)]
                    };
                    Marker([item],options);
                });
            }
            addCurvlines(curvelines, cores);
        });
    }

    function addScatterRels() {
        let color = Common.color();
        Scatter("cluster", function () {
            map.clearOverlays();
            // getScatter(scatters).forEach(function (item,index) {
            //     let options = {
            //         color: color[index%38],
            //         size: size[1]
            //     };
            //     Marker(item,options);
            //     // if(!cores[0].length) {
            //     //     cores = getCore(scatters);
            //     // }
            // });

            for (let key in relations) {
                addCurvlines(relations[key],null,color);
            }
            $(".loading").hide();
        });
    }

    function Scatter(type,fun) {
        let src,params;
        $(".loading").show();
        map.clearOverlays();
        // if (time == Common.getTime()) {
        //     fun();
        //     return;
        // } else {
        time = Common.getTime();
        // }
        switch(type) {
            case "day": {
                src = "/dayscatter";
                params = {
                    day: time
                };
                break;
            }
            case "cluster": {
                src = "/scaRel";
                params = {
                    day: time,
                    idArray: selScatter.toString()
                };
                break;
            }
            case "hour": {
                src = "/hourscatter";
                params = {
                    hour: time
                };
                break;
            }
        }
        $.get(src,params).then((res) => {
            res = res.split("@");
            scatters = JSON.parse(res[0])[0];
            relations = JSON.parse(res[1])[0];
            fun();
        })
    }

    function Core(type,fun) {
        let src,params;
        $(".loading").show();
        map.clearOverlays();
        let time = Common.getTime();
        switch(type) {
            case "day": {
                src = "/daycore";
                params = {
                    day: time
                };
                break;
            }
            case "hour": {
                src = "/hourcore";
                params = {
                    day: "2014-04-05",
                    hour: time
                };
                break;
            }
        }
        $.get(src,params).then((res) => {
            res = res.split("@");
            scatters = JSON.parse(res[0])[0];
            curvelines = JSON.parse(res[1]);
            fun();
        })
    }

    function getScatter(points) {
        let father = [];
        for (let key in points) {
            let son = [];
            points[key].forEach(function (point) {
                let marker = Common.getLngLat(point);
                if (marker.lng == 0 || marker.lng == null || marker.lng == undefined) {
                    console.log("marker:"+marker+"  point:"+point);
                } else {
                    son.push(new BMap.Point(marker.lng,marker.lat));
                }
            });
            father.push(son);
        }
        return father;
    }

    function getCore(points) {
        let cores = [];
        scatIds.length = 0;
        for (let key in points) {
            let xsum=0,ysum=0,xav,yav,len=0;
            points[key].forEach(function (point) {
                let marker = Common.getLngLat(point);
                if (marker.lng) {
                    xsum += marker.lng;
                    ysum += marker.lat;
                    len++;
                }
            });
            xav = xsum/(len);
            yav = ysum/(len);
            scatIds.push(key);
            cores.push(new BMap.Point(xav,yav));
        }
        return cores;
    }

    function selectScatter() {
        map.clearOverlays();
        selScatter.length = 0;
        Scatter("day", function () {
            let color = Common.color();
            let distance = Common.getDistance();
            cores.forEach(function (item,index) {
                Marker([item],{
                    color: color[index%38],
                    size: size[Common.getSize(index+1,distance)]
                }, "select");
            });
            $(".loading").hide();
        });
    }

    function newMarker(data) {
        map.addOverlay(new BMap.Circle(data,40,{fillColor:"#f00",fillOpacity:0.8,strokeOpacity: 0}));
    }

    function Marker(data,options,type) {
        let pointCollection = getPointCollection(data,options);
        map.addOverlay(pointCollection);
        function getPointCollection(data,opt) {
            let points = [];  // 添加海量点数据
            for (let i = 0; i < data.length; i++) {
                if (data[i] == null || data[i] == 0 || data[i] == undefined) {
                    continue;
                }
                points.push(data[i]);
            }
            let options = {
                size: opt.size,
                shape: BMAP_POINT_SHAPE_CIRCLE,
                color: opt.color
            };
            return new BMap.PointCollection(points, options);
        }
        switch(type) {
            case "info": showInfoEvent(pointCollection);break;
            case "select": showSelectEvent(pointCollection);break;
            default: break;
        }
        function showInfoEvent(points) {
            (function addInfoWindow(points) {
                points.addEventListener("click", function(obj){
                    let info = Common.getInfo(obj.point);
                    let id = Common.getID(info[0]);
                    let infoWindow = new BMap.InfoWindow("站点ID: "+info[0]+"<br/>" +
                        "站点名： "+info[1]);
                    map.openInfoWindow(infoWindow,obj.point); //开启信息窗口
                    if (trace.indexOf(id) == -1) {
                        $('.charts').show();myChart.showLoading();
                        trace.shift();trace.push(id);
                        showHistogram2(id);
                        if (tmplines.length >0 ) {
                            tmplines.forEach(function (item) {
                                map.removeOverlay(item);
                            });
                            tmplines.length = 0;
                        }
                    }
                });
            })(points);
        }

        function showSelectEvent(points) {
            points.addEventListener("click", function (obj) {
                let index = cores.indexOf(obj.point) + 1 + "";
                if (~selScatter.indexOf(index)) {
                    selScatter.splice(selScatter.indexOf(index),1);
                    let i = cores.indexOf(obj.point);
                    obj.target.setStyles({
                        size: size[5],
                        shape: BMAP_POINT_SHAPE_CIRCLE,
                        color: colors[i%38]
                    });
                } else {
                    obj.target.setStyles({
                        size: size[5],
                        shape: BMAP_POINT_SHAPE_CIRCLE,
                        color: grey700
                    });
                    selScatter.push(cores.indexOf(obj.point) + 1 + "");
                }
            });
        }
    }

    function addCurvlines(lines,cores,opt) {
        let filter = parseInt($(".filter").val());
        let maxnum = parseInt($(".max-num").text());
        const markers = new Set();
        if(Number.isNaN(maxnum) || opt) {
            maxnum = getMaxNum();
            $(".max-num").html(maxnum);
        }
        let id = trace[0];
        lines.forEach(function (item) {
            let num = parseInt(item.nums);
            let weight = Common.getWeight(maxnum,num);
            if (num >= filter) {
                let marker1 = cores?cores[scatIds.indexOf(item.leaseid)]:new BMap.Point(Common.getLngLat(item.lease).lng,Common.getLngLat(item.lease).lat);
                let marker2 = cores?cores[scatIds.indexOf(item.returnid)]:new BMap.Point(Common.getLngLat(item.return).lng,Common.getLngLat(item.return).lat);
                let curveline = new BMapLib.CurveLine(
                    [marker1,marker2],{strokeColor: Common.getColor(weight), strokeWeight: (9*weight), strokeOpacity:0.8}
                );
                tmplines.push(curveline);
                curveline.addEventListener("click",function (obj) {
                    let points = obj.currentTarget.cornerPoints;
                    let infoWindow = new BMap.InfoWindow("流量: "+num+"<br/>权重： "+weight +"<br/>"+ Common.getInfo(points[0])[1] +"->"+Common.getInfo(points[1])[1], {width: 100,height: 90,title: "联系"});
                    map.openInfoWindow(infoWindow,obj.point);
                    console.log(item);
                    if(opt) {
                        $.get('/curveInfo',{
                            leaseStation: item.lease,
                            returnStation: item.return,
                            time: Common.getTime()
                        }).then((res) => {
                            showLoading();
                            showStackChart(JSON.parse(res));
                        })
                    }
                });
                if(opt) {
                    newMarker(marker1);
                    newMarker(marker2);
                }
                // console.log(Common.getID(marker1)+" "+Common.getID(marker2));
                map.addOverlay(curveline);
                addArrow(curveline,{strokeColor:Common.getColor(weight), strokeWeight:(6*weight), strokeOpacity:0.8});
            }
        });
        if ((!cores)&&id) {
            let array = [];
            scatters[id].forEach(function (point) {
                let marker = Common.getLngLat(point);
                array.push(new BMap.Point(marker.lng,marker.lat));
            });
            Marker(array, {color: colors[(parseInt(id)-1)%38], size: size[1]}, "info");
        }
        function addArrow(lines,line_style) {
            //arrow
            // let lines = tmplines;
            let length = 14;
            let angleValue = Math.PI/7;
            // lines.forEach(function (item) {
            let linePoint = lines.getPath();
            let arrowCount = linePoint.length;
            let middle = arrowCount / 2;
            let pixelStart = map.pointToPixel(linePoint[Math.floor(middle)]);
            let pixelEnd = map.pointToPixel(linePoint[Math.ceil(middle)]);
            let angle = angleValue;
            let r = length;
            let delta = 0;
            let param = 0;
            let pixelTemX, pixelTemY;
            let pixelX, pixelY, pixelX1, pixelY1;
            if (pixelEnd.x - pixelStart.x == 0) {
                pixelTemX = pixelEnd.x;
                if (pixelEnd.y > pixelStart.y) {
                    pixelTemY = pixelEnd.y - r;
                } else {
                    pixelTemY = pixelEnd.y + r;
                }
                pixelX = pixelTemX - r * Math.tan(angle);
                pixelX1 = pixelTemX + r * Math.tan(angle);
                pixelY = pixelY1 = pixelTemY;
            } else {
                delta = (pixelEnd.y - pixelStart.y) / (pixelEnd.x - pixelStart.x);
                param = Math.sqrt(delta * delta + 1);
                if ((pixelEnd.x - pixelStart.x) < 0) {
                    pixelTemX = pixelEnd.x + r / param;
                    pixelTemY = pixelEnd.y + delta * r / param;
                } else {
                    pixelTemX = pixelEnd.x - r / param;
                    pixelTemY = pixelEnd.y - delta * r / param;
                }
                pixelX = pixelTemX + Math.tan(angle) * r * delta / param;
                pixelY = pixelTemY - Math.tan(angle) * r / param;
                pixelX1 = pixelTemX - Math.tan(angle) * r * delta / param;
                pixelY1 = pixelTemY + Math.tan(angle) * r / param;
            }
            let pointArrow = map.pixelToPoint(new BMap.Pixel(pixelX, pixelY));
            let pointArrow1 = map.pixelToPoint(new BMap.Pixel(pixelX1, pixelY1));
            let Arrow = new BMap.Polyline([pointArrow, linePoint[Math.ceil(middle)], pointArrow1], line_style);
            tmplines.push(Arrow);
            map.addOverlay(Arrow);
            // });
        }
        function getMaxNum() {
            let maxArr = [];
            for(let key in relations) {
                let max = 0;
                relations[key].forEach((item) => {
                    max = Math.max(max, item.nums);
                });
                maxArr.push(max);
            }
            return Math.max(...maxArr);
        }
        $(".loading").hide();
    }

    function showHistogram() {
        Common.initHistogram();
        let numbers = [],maxnum = 0;
        curvelines.forEach(function (obj) {
            let num = parseInt(obj.nums);
            if (num > 0) {
                numbers.push(num);
                maxnum = maxnum>num?maxnum:num;
            }
        });
        $(".max-num").html(maxnum);
        $(".gradient-max").html(maxnum);
        let data = getData(maxnum, numbers);
// 异步加载数据
        myChart.setOption({
            xAxis: [
                {
                    data: data[0]
                }
            ],
            series: [
                {
                    name: '个数',
                    data: data[1]
                }
            ]
        });
        function getData(maxnum,numbers) {
            let space = 100;
            let xAxis = [],series = [];
            let times = Math.ceil(maxnum/space);
            for (let i=0;i<times;i++) {
                xAxis.push((i+1)*space);
                let s = numbers.filter(function (item) {
                    return (item >= i*space && item < (i+1)*space)
                }).length;
                series.push(s);
            }
            return [xAxis,series]
        }
        myChart.hideLoading();
    }

    function showStackChart(data) {
        Common.initStackChart();
        let legend = [], series = [];
// 异步加载数据
        console.log(data);
        data.forEach((item) => {
            const key = Object.keys(item)[0];
            console.log(key);
            legend.push(key);
            series.push({
                name: key,
                type:'bar',
                stack: '热度',
                data: item[key]
            })
        });
        myChart.setOption({
            legend: {
                data: legend
            },
            series: series
        });
        myChart.hideLoading();
    }

    function showHistogram2(id) {
        Common.initHistogram();
        let data = getData2(id);
// 异步加载数据
        myChart.setOption({
            xAxis: [
                {
                    data: data[0]
                }
            ],
            series: [
                {
                    name: '个数',
                    data: data[1]
                }
            ]
        });
        function getData2(id) {
            let maxnum = 0;
            relations[id].forEach(function (item) {
                let num = parseInt(item.nums);
                maxnum = (num>maxnum)?num:maxnum;
            });
            $(".max-num").html(maxnum);
            $(".gradient-max").html(maxnum);
            let space = 1;
            let xAxis = [],series = [];
            let times = Math.ceil(maxnum/space);
            for (let i=0;i<=times;i++) {
                xAxis.push((i+1)*space);
                let s = relations[id].filter(function (item) {
                    let num = parseInt(item.nums);
                    return (num >= i*space && num < (i+1)*space)
                }).length;
                series.push(s);
            }
            return [xAxis,series]
        }
        myChart.hideLoading();
    }
})();