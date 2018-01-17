/**
 * Created by Alien on 2017/3/10.
 * 主脚本
 */
(function () {
    var positions = {},myChart,bmap,colors=[],size,oldcores= [],trace= [],tmplines=[],scatters= {},relations= [],scatIds= [],curvelines= [],cores= [],selScatter= [];

    (function initialize() {
        getPositions();
        initialDefinite();
        bindListener();
        addSubWay();
    })();

    function initialDefinite() {
        // 构造图表
        $('.charts').show();myChart = echarts.init($('.histogram')[0]);$('.charts').hide();
        // 颜色数组
        bmap = initialBMap();
        colors= [red, purple, blue, limeA700, yellow600, orange600, indigoA400, cyanA200, pink, pink200, greenA200, blue200, brown600,
            yellow300, lime900, deepOrange400, green900, purple900, red900, orange200, redA100, pink900, cyan200, cyan900, lime400, lightBlue900, deepPurpleA200, green400, brown200, deepPurple200,
            indigo900, indigo100,indigo300,blueGrey700 ,grey700,grey400,black,deepOrange200];
        // 海量点大小数组
        size = [BMAP_POINT_SIZE_SMALL,BMAP_POINT_SIZE_NORMAL,BMAP_POINT_SIZE_BIG,BMAP_POINT_SIZE_BIGGER,BMAP_POINT_SIZE_HUGE];
        // 初始化日期选择
        $(".from1, .to1, .from2, .to2, .the-day").flatpickr({minDate: "2014-03-01", maxDate: "2014-06-22"});
        $(".startTimeNY, .endTimeNY").flatpickr({minDate: "2016-11-01", maxDate: "2016-12-31"});
        $.fn.extend({
            _click: function (callback) {
                $(this).click(function (e) {
                    const target = e.target;
                    $(target).attr("disabled", "disabled");
                    const node = $("<img class='icon-loading' src='./assets/images/loading.svg' width='13px' height='13px'/>");
                    $(target).css("opacity", ".8");
                    const text = $(target).text();
                    $(target).text("努力加载中...");
                    $(target).prepend(node);
                    callback().then(() => {
                        $(target).css("opacity", "1");
                        $(target).text(text);
                        $(target).removeAttr("disabled");
                    }).catch((e) => {
                        console.error(e);
                        $(target).css("opacity", "1");
                        $(target).text(text);
                        $(target).removeAttr("disabled");
                    })
                })
            }
        });
    }
    // 绑定事件
    function bindListener() {
        $(".day")._click(() => generateFilesByTime("day"));                      //有向-生成关系文件[天]
        $(".day-ny")._click(() => generateFilesByTime("ny-day"));                      //有向-生成关系文件[天]
        $(".day-ch")._click(() => generateFilesByTime("ch-day"));                      //有向-生成关系文件[天]
        $(".block")._click(() => generateFilesByTime("block"));                  //有向-生成关系文件[时间段]
        $(".block-ny")._click(() => generateFilesByTime("ny-block"));                  //有向-生成关系文件[时间段]
        $(".block-ch")._click(() => generateFilesByTime("ch-block"));                  //有向-生成关系文件[时间段]
        $(".undir-day")._click(() => generateFilesByTime("udday"));              //无向-生成关系文件[天]
        $(".undir-block")._click(() => generateFilesByTime("udblock"));          //无向-生成关系文件[时间段]
        $(".cluster")._click(() => generateFilesByTime("cluster"));      //生成聚类关系文件
        $(".day—scatter")._click(() => addScatter());           //聚类散点
        $(".day-core")._click(() => addCore());                     //聚类中心联系
        $(".scatter-relation").click(() => addScatterRels());           //聚类关联显示
        $(".scatter-rel").click(() => {
            const len = Object.keys(scatters).length - 1;
            for (let i=0; i<len; i++) {
                selScatter.push(i);
            }
            addScatterRels();
        });           //散点关联
        // $(".louvain_cluster").click(() => getCommunity());      生成聚类关系文件
        $(".select-scatter").click(() => selectScatter());              //聚类中心选择
        $(".day—scatter-filter").click(function () {                             //聚类内散点联系
            if (!checkHasUpload()) return false;
            // Common.clearMap();
            $(".loading-field").show();
            let id = trace[0];
            // addCurvlines(relations[id], null);
            addLines(relations[id]);
        });
        $(".cp_btn").click(() => showCirclePack());
        $(".next").click(function () {
            const height1 = $("#comm-content").height();
            const height2 = $("#traj-iframe").contents().height();
            const className = $(this).attr("class").split(" ")[1];
            setTimeout(() => { $(".next").show(); $(this).hide(); }, 400);
            let width, height;
            if (className === 'left') { width = '0'; height = height1; }
            else { width = '-100%'; height = height2; }
            $(".wrapper").css("transform", "translateX(" + width + ")").css("height", height);
        });
        $('.file-load input[type="file"]').change(function (e) {
            clearOverlays();
            if ((!!cores.length) && (!oldcores.length)) {
                oldcores = cores;
                cores = [];
            }
            scatters = {};
            const fileName = e.currentTarget.files[0].name;
            if (fileName.includes('cluster')) $('.scatter-rel').show();
            else $('.scatter-rel').hide();
            $(this).parents('.file-load').find(".file-name").text(fileName);
            if (fileName.slice(0,2) === 'ny') {
                map.panTo(new BMap.Point(-73.98669373016,40.73741431289));
            } else {
                map.panTo(new BMap.Point(120.16711642992,30.25283633644));
            }
            if ($(this).attr("id") === 'louFile') {
                $("#louLevel").empty();
                getLouvainLevelNum();
            }
            console.log(e.currentTarget.files[0]);//e就是你获取的file对象
        })
    }
    function initialBMap() {
        const bmap = echarts.init($("#bmap")[0]);
        const option = {
            title: {
                text: '公共自行车流量图',
                subtext: 'data from Hangzhou PBS',
                left: 'center'
            },
            tooltip : {
                trigger: 'item'
            },
            bmap: {
                center: [120.16711642992, 30.25283633644],
                zoom: 15,
                mapStyle: {
                    styleJson: [{
                            "featureType": "all",
                            "elementType": "all",
                            "stylers": {
                                "lightness": 10,
                                "saturation": -100
                            }
                    }]
                }
            },
            series : [
                {
                    name: 'scatter',
                    type: 'scatter',
                    coordinateSystem: 'bmap',
                    data: [],
                    symbolSize: 10,
                    label: {
                        normal: {
                            formatter: '{b}',
                            position: 'right',
                            show: false
                        },
                        emphasis: {
                            show: true
                        }
                    },
                    itemStyle: {
                        normal: {
                            color: function ({data}) {
                                return colors[data[2]-0]
                            }
                        }
                    }
                }
            ]
        };
        bmap.setOption(option);
        bmap.on('click', function (params) {
            console.log(params);
            const id = params.data[2];
            if(params.componentSubType === 'scatter') {
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
            }

        });
        const bmapInstance = bmap.getModel().getComponent('bmap').getBMap();
        bmapInstance.enableDragging();
        addMapControl(bmapInstance);
        addSubWay(bmapInstance);
        function addMapControl(instance) {
            //缩放控件
            const naviControl = new BMap.NavigationControl({
                anchor : BMAP_ANCHOR_TOP_LEFT
            });
            instance.addControl(naviControl);

            //缩略图控件
            const overControl = new BMap.OverviewMapControl({
                anchor : BMAP_ANCHOR_BOTTOM_RIGHT,
                isOpen : false,
                offset : new BMap.Size(50, 50)
            });
            instance.addControl(overControl);

            //比例尺控件
            const scaleControl = new BMap.ScaleControl({
                anchor : BMAP_ANCHOR_BOTTOM_LEFT,
                offset : new BMap.Size(50, 20)
            });
            instance.addControl(scaleControl);
        }
        return bmap;
    }
    
    // 绘制地铁一号线
    function addSubWay(instance) {
        let points = [
            [[120.354264,30.316329],[120.341868,30.315456]],[[120.341221,30.315425],[120.332328,30.315347]],[[120.331735,30.315362],[120.319284,30.31569]],[[120.318745,30.315721],[120.285185,30.31689]],[[120.284897,30.31689],[120.282094,30.31664],[120.274549,30.313835],[120.273183,30.313772]],[[120.273183,30.313772],[120.268117,30.313866],[120.261541,30.31293],[120.259026,30.312026]],[[120.258595,30.311777],[120.247815,30.30657]],[[120.247384,30.306344],[120.234161,30.301332],[120.23028,30.300396]],[[120.229633,30.300241],[120.221225,30.298183],[120.214398,30.294441],[120.199342,30.290324]],[[120.198678,30.290106],[120.197708,30.289794],[120.194761,30.28967],[120.187323,30.29101],[120.18373,30.290886]],[[120.183155,30.290886],[120.178879,30.290168],[120.175897,30.289171],[120.173238,30.287705],[120.172268,30.287011],[120.172268,30.286075]],[[120.172268,30.285576],[120.172375,30.281335],[120.170112,30.276563],[120.17004,30.275752],[120.170255,30.26861]],[[120.170291,30.268111],[120.170507,30.26078]],[[120.170579,30.26025],[120.170902,30.254978],[120.172232,30.25267],[120.173489,30.251765],[120.174064,30.251703]],[[120.174567,30.251671],[120.183766,30.251297],[120.186497,30.250767],[120.187323,30.250268]],[[120.18779,30.249987],[120.197205,30.242905]],[[120.1976,30.242499],[120.203672,30.237164]],[[120.204139,30.236727],[120.213087,30.227444],[120.222824,30.216163],[120.222896,30.215726]],[[120.223004,30.215188],[120.223974,30.206074]],[[120.224046,30.205513],[120.225447,30.195243],[120.226489,30.193745],[120.226956,30.193464]],[[120.227388,30.193183],[120.228034,30.192746],[120.235652,30.191029],[120.237017,30.19003]],[[120.237449,30.189718],[120.241006,30.186222],[120.24,30.182101],[120.240718,30.174226]]
        ];
        points.forEach(item => {
            let pointsArr = item.map(point => new BMap.Point(point[0], point[1]));
            let polyline = new BMap.Polyline(pointsArr, {
                strokeColor: '#DC0000', strokeWeight: 2, strokeOpacity: 1
            });
            if(instance) instance.addOverlay(polyline);
            else map.addOverlay(polyline);
        });
    }
    // 后端获取站点数据
    function getPositions() {
        $.ajax({
            url: '/data',
            type: 'get',
            timeout: 120000
        }).then((res) => {
            let arr = res.split("@");
            positions["HZ"] = JSON.parse(arr[0]);
            $.getJSON('/assets/stations_newyork.json').then((data) => {
                const stationBeanList = data.stationBeanList;
                const NYStations = stationBeanList.map((item) => {
                    return {
                        stationname: item.stationName,
                        stationid: item.id,
                        lng: item.longitude,
                        lat: item.latitude
                    }
                });
                positions["NY"] = NYStations;
                window.positions = positions;
            }).catch(e => {console.error(e)});
        }).catch((e) => {
            layer.alert("获取站点数据失败！\n请刷新网页或检查网络！")
        })
    }

    // 检查是否上传文件
    function checkHasUpload() {
        const active = $(".tab-active").find("a").attr("href");
        if ($(active).find('.file-name').text() === '选择上传net文件') {
            layer.alert('请先上传net文件!', {icon: 6});
            return false;
        }
        return true;
    }

    // 获取Louvain的Level数
    function getLouvainLevelNum() {
        const fileName = Common.getFileName();
        $.ajax({
            url: '/getlevelnum',
            type: 'POST',
            data: {fileName: fileName},
            timeout: 120000,
            contentType: "application/x-www-form-urlencoded",
            success: function(res) {
                const level = parseInt(res, 10);
                const $level_select = $("#louLevel");
                if (level) {
                    for (let i=1;i<=level;i++) {
                        const node = $("<option value="+i+">"+i+"</option>");
                        $level_select.append(node);
                    }
                    $level_select.val(level);
                }
            },
            error: function (e) {
                layer.alert("获取失败！");
            }
        })
    }

    // 清理地图覆盖物（除了地铁一号线）
    function clearOverlays() {
        map.clearOverlays();
        addSubWay();
    }

    // 显示加载图表
    function showLoading() {
        $('.charts').show();
        myChart.showLoading();
    }

    function showCirclePack() {
        let root = { name: 'circle' };
        const children = [];
        if (Object.keys(scatters).length) {
            const svgNode = $("<svg width='550' height='550'></svg>");
            $(".circle-packing").append(svgNode);
            root.code = scatters.code[0];
            let color = [], sign = false;
            if ($(".circle-packing svg").length === 1) {
                color = Common.color();
                sign = true;
            }
            for (let key in scatters) {
                let item = {};
                item.name = key;
                item.children = scatters[key].map((it) => {
                    let currentColor;
                    if (sign) {
                        currentColor = color[parseInt(key)];
                        colorMapping[it] = currentColor;
                    } else {
                        if (colorMapping.hasOwnProperty(it))
                            currentColor = colorMapping[it];
                        else {
                            currentColor = color[parseInt(key)];
                            colorMapping[it] = currentColor;
                        }
                    }
                    const info = Common.getInfoById(it);
                    return {name: info.name, size: 10, color: currentColor, id: info.id}
                });
                children.push(item);
            }
            root.children = children;
            const svg = d3.selectAll(".circle-packing svg").filter(function (d, i, n) {
                    return i === n.length - 1
                }),
                diameter = +svg.attr("width"),
                g = svg.append("g").attr("transform", "translate(2,2)"),
                format = d3.format(",d");
            const pack = d3.pack()
                .size([diameter - 4, diameter - 4]);
            root = d3.hierarchy(root)
                .sum(function(d) { return d.size; })
                .sort(function(a, b) { return b.value - a.value; });
            const node = g.selectAll(".node")
                .data(pack(root).descendants())
                .enter().append("g")
                .attr("class", function(d) { return d.children ? "node" : "leaf node"; })
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
                .on("click", function (d) {
                    layer.tips('站点ID：'+ d.data.id +  '<br/>站点名：' + d.data.name, this);
                });
            node.append("title")
                .text(function(d) { return d.data.name + "\n" + format(d.value); });
            node.append("circle")
                .attr("r", function(d) { return d.r; })
                .attr("style", function(d) { return d.children ? "" : `fill: ${d.data.color}`; });
            $(".circle-packing").show();
        } else {
            layer.msg("请先加载聚类散点！");
            return false;
        }
    }

    // 公共函数
    var Common = {
        getTime() {
            // const time = $(".file-name").text();
            const active = $(".tab-active").find("a").attr("href");
            const time = $(active).find(".file-name").text();
            const reg = /(_\d{4}){1,2}/g;
            if (reg.test(time)) {
                return time.match(reg)[0].slice(1);
            } else {
                alert('选择文件错误！')
            }
        },
        getFileName() {
            const active = $(".tab-active").find("a").attr("href");
            return $(active).find(".file-name").text().slice(0,-4);
        },
        getInfo(point) {
            const type = Common.getFileName().slice(0,2);
            const key = type == 'ny' ? 'NY' : 'HZ';
            let info = [];
            for (let i=0;i<positions[key].length;i++) {
                let obj = positions[key][i];
                if (Math.abs(obj.lng - point.lng) <= 1e-5 && Math.abs(obj.lat - point.lat) <= 1e-5 ) {
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
        getInfoById(id) {
            let info = travel('HZ');
            if (!info.name) {
                info = travel('NY');
            }
            function travel(key) {
                for (let i=0;i<positions[key].length;i++) {
                    let obj = positions[key][i];
                    if (obj.stationid === id) {
                        return {
                            name: obj.stationname,
                            id: id
                        };
                    }
                }
                return {name: '', id: ''};
            }
            return info;
        },
        getLngLat(id) {    //返回坐标点
            let point = {};
            const type = Common.getFileName().slice(0,2);
            const key = type == 'ny' ? 'NY' : 'HZ';
            for (let i=0;i<positions[key].length;i++) {
                let obj = positions[key][i];
                if (obj.stationid == id) {
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
            cores = handleCore(scatters);let colorize=[];
            if (!!oldcores.length) {
                let len = oldcores.length;
                let flag = new Array(len);
                cores.forEach(function (obj,i) {
                    if (Math.min.apply(Math, flag) == 1) {
                        colorize[i] = colors[(++len)%38];
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
            for(let key in scatters) {
                distance.push(scatters[key].length);
            }
            return Math.max.apply(Math, distance);
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
        clearMap() {
            map.clearOverlays();
            addSubWay();
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
                        name: 'flowNum',
                        type : 'category',
                        data : [],
                        axisTick: {alignWithLabel: true},
                        axisLabel: {
                            color: '#000',
                            fontWeight: 'bold'
                        },
                        nameTextStyle: {
                            color: '#000',
                            fontWeight: 'bold'
                        }
                    }
                ], yAxis : [{
                    name: 'freqNum',
                    type : 'value',
                    axisLabel: {
                        color: '#000',
                        fontWeight: 'bold'
                    },
                    nameTextStyle: {
                        color: '#000',
                        fontWeight: 'bold'
                    }
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
                tooltip : {trigger: 'axis',
                    axisPointer : {type : 'shadow'}},
                legend: {data: []},
                grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
                xAxis : [
                    {
                        type : 'category',
                        data : ['06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21']
                    }],
                yAxis : [{type : 'value'}],
                series : [{name:'', type:'bar', stack: '热度', data:[]}]
            })
        }
    };

    // 生成文件函数
    function generateFilesByTime(timeType) {
        let url,params;
        const active = $(".tab-active").find("a").attr("href");
        if (!checkHasUpload()) return false;
        switch (timeType) {
            case("day"): {
                url = "/byday";
                params = {from: $(active).find(".from1").val(), to: $(active).find(".to1").val()};break;
            }
            case("udday"): {
                url = "/byudday";
                params = {from: $(".from2").val(), to: $(".to2").val()};break;
            }
            case("ny-day"): {
                url = "/generate";
                params = {from: $(".ny-data-btn .startTimeNY").val(), to: $(".ny-data-btn .endTimeNY").val(), type: "day", city: 'ny'};break;
            }
            case("ch-day"): {
                url = "/generate";
                params = {from: $(".ny-data-btn .startTimeNY").val(), to: $(".ny-data-btn .endTimeNY").val(), type: "day", city: 'ch'};break;
            }
            case("hour"): {
                url = "/byday";
                params = {day: $(".the-day").val()};break;
            }
            case("block"): {
                url = "/byblock";
                params = {from: $(active).find(".from1").val(), to: $(active).find(".to1").val()};break;
            }
            case("ny-block"): {
                url = "/generate";
                params = {from: $(".ny-data-btn .startTimeNY").val(), to: $(".ny-data-btn .endTimeNY").val(), type: "block", city: 'ny'};break;
            }
            case("ch-block"): {
                url = "/generate";
                params = {from: $(".ny-data-btn .startTimeNY").val(), to: $(".ny-data-btn .endTimeNY").val(), type: "block", city: 'ch'};break;
            }
            case("udblock"): {
                url = "/byudblock";
                params = {from: $(".from2").val(), to: $(".to2").val()};break;
            }
            case("cluster"): {
                let time = Common.getTime();
                if (selScatter.length == 0) {
                    alert("请先选择聚点 or 选择文件错误");return;
                }
                let stations = [];
                selScatter.forEach(function (key) {
                    scatters[key].forEach(function (item) {
                        stations.push(item);
                    })
                });
                url = "/bycluster";
                params = {from: "", to: "", stations: stations.join(","), number: selScatter.length, traditional: true };
                if (time.length>4) {
                    params.from = time.substr(0,4);
                    params.to = time.substr(5,4);
                } else {
                    params.from = time;
                    params.to = time;
                }
                return new Promise((resolve, reject) => {
                    $.post(url,params).then((res) => {layer.alert(res);resolve()}).catch((e) => {layer.error(e);reject()});
                });
            }
        }

        return new Promise((resolve, reject) => {
            $.get(url,params).then((res) => {
                layer.alert('生成文件成功！');
                resolve();
            }).catch((e) => {
                layer.alert('生成文件失败！');
                reject();
            })
        })
    }

    // 显示聚类散点
    function addScatter() {
        if (!checkHasUpload()) return false;
        return new Promise((resolve,reject) => {
            Scatter('scatter').then(() => {
                let color = Common.color();
                convertData(scatters);
                handleScatter(scatters).forEach(function (item,index) {
                    let options = {
                        color: color[index%38],
                        size: size[1]
                    };
                    Marker(item, options, "info");
                    // if(!cores[0].length) {
                    //     cores = handleCore(scatters);
                    // }
                    resolve()
                });
                $(".loading-field").hide();
            }).catch((e) => reject(e));
        })
    }
    function convertData(scatter) {
        let data = [];
        for (let key in scatter) {
            scatter[key].forEach(function (point) {
                let marker = Common.getLngLat(point);
                if (marker.lng == 0 || marker.lng == null || marker.lng == undefined) {
                    console.log("marker:"+marker+"  point:"+point);
                } else {
                    data.push([marker.lng, marker.lat, key]);
                }
            });
        }
        bmap.setOption({
            series: [{
                name: 'scatter',
                data: data
            }]
        })
    }
    // 显示聚类中心联系
    function addCore() {
        if (!checkHasUpload()) return false;
        return new Promise((resolve,reject) => {
            let options = null;
            $('.charts').show();
            myChart.showLoading();
            const comm_type = $(".tab-active").attr("data-name");
            Core(comm_type).then(function () {
                clearOverlays();
                let color = Common.color();
                let distance = Common.getDistance();
                showHistogram();
                cores.forEach(function (item, index) {
                    options = {
                        color: color[index % 38],
                        size: size[Common.getSize(index, distance)]
                    };

                    Marker([item], options);
                });
                $(".loading-field").hide();
                // console.log(curvelines);
                addCurvlines(curvelines, cores);
                resolve();
            }).catch((e) => reject(e));
        });
    }

    //  部分聚类散点联系
    function addScatterRels() {
        if (!checkHasUpload()) return false;
        return new Promise((resolve,reject) => {
            let color = Common.color();
            Scatter("sel_cluster").then(() => {
                Common.clearMap();
                for (let key in relations) {
                    let idx = key.split('-');
                    console.log(idx);
                    addCurvlines(relations[key], null, [color[idx[0] - 1], color[idx[1] - 1]]);
                }
                $(".loading-field").hide();
                resolve();
            }).catch(() => reject());
        });
    }

    // 后端获取数据公共接口
    function loadAjax(data) {
        const content = data.content;
        Common.clearMap();
        const time = Common.getTime();
        const fileName = Common.getFileName();
        $(".loading-field").show();
        content.day = time;
        content.fileName = fileName;
        $.ajax({
            url: data.url,
            type: 'POST',
            data: content,
            timeout: 120000,
            contentType: "application/x-www-form-urlencoded",
            success: function(res) {
                data.success(res);
            },
            error: function (e) {
               data.error(e);
            }
        })
    }

    // 获取散点及散点联系
    function Scatter(type) {
        let content = {};
        content.comm_type = $(".tab-active").attr("data-name");
        if (type === "sel_cluster") content.idArray = selScatter.toString();
        if (content.comm_type === 'louvain') content.level= $("#louLevel option:selected").val();
        content.func_type = type;
        return new Promise((resolve, reject) => {
            loadAjax({
                url: '/community',
                content: content,
                success: function (res) {
                    res = res.split("@");
                    scatters = JSON.parse(res[0])[0];
                    scatters.code = [(new Date()).getTime()];
                    relations = JSON.parse(res[1])[0];
                    resolve();
                },
                error: function (e) {
                    layer.alert("获取聚类散点失败！");
                    $(".loading-field").hide();
                    reject(e);
                }
            });
        });

    }

    // 获取散点及聚类间联系
    function Core(comm_type) {
        let content = {};
        content.func_type = 'cluster';
        content.comm_type = comm_type;
        if (content.comm_type === 'louvain') content.level= $("#louLevel option:selected").val();
        return new Promise((resolve, reject) => {
            loadAjax({
                url: '/community',
                content: content,
                success: function (res) {
                    res = res.split("@");
                    scatters = JSON.parse(res[0])[0];
                    scatters.code = [(new Date()).getTime()];
                    curvelines = JSON.parse(res[1]);
                    resolve();
                },
                error: function (e) {
                    layer.alert("获取聚类失败！");
                    $(".loading-field").hide();
                    reject(e);
                }
            })
        });
    }
    
    // 处理散点数据
    function handleScatter(points) {
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

    // 处理聚类中心数据
    function handleCore(points) {
        let cores = [];
        scatIds.length = 0;
        for (let key in points) {
            if (key === 'code') continue;
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

    // 选择聚类中心
    function selectScatter() {
        Common.clearMap();
        selScatter.length = 0;
        if (!checkHasUpload()) return false;
        Scatter("scatter").then(() => {
            let color = Common.color();
            let distance = Common.getDistance();
            cores.forEach(function (item,index) {
                Marker([item],{
                    color: color[index%38],
                    size: size[Common.getSize(index,distance)]
                }, "select");
            });
            $(".loading-field").hide();
        }).catch((e) => {
            layer.msg("出错！");
            console.error(e);
        });
    }

    // 用Circle覆盖物绘制的圆形覆盖物
    function newMarker(data, color) {
        map.addOverlay(new BMap.Circle(data,40,{fillColor:color,fillOpacity:0.8,strokeOpacity: 0.8,strokeWeight:0,strokeColor: color}));
    }

    // 海量点加载方式绘制圆形覆盖物
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
            points.addEventListener("click", function(obj){
                let info = Common.getInfo(obj.point);
                let id = Common.getID(info[0] + '');
                let infoWindow = new BMap.InfoWindow("站点ID: "+info[0]+"<br/>" +
                    "站点名： "+info[1], { enableAutoPan: false, width: 0, height: 0 });
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
        }

        // 响应点击散点函数
        function showSelectEvent(points) {
            points.addEventListener("click", function (obj) {
                let index = cores.indexOf(obj.point) + "";
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
                    selScatter.push(cores.indexOf(obj.point) + "");
                }
            });
        }
    }

    function addLines(lines) {
        const active = $(".tab-active").find("a").attr("href");
        const filter = parseInt($(active).find(".filter").val());
        const maxnum = parseInt($(active).find(".max-num").text());
        const planePath = 'path://M1705.06,1318.313v-89.254l-319.9-221.799l0.073-208.063c0.521-84.662-26.629-121.796-63.961-121.491c-37.332-0.305-64.482,36.829-63.961,121.491l0.073,208.063l-319.9,221.799v89.254l330.343-157.288l12.238,241.308l-134.449,92.931l0.531,42.034l175.125-42.917l175.125,42.917l0.531-42.034l-134.449-92.931l12.238-241.308L1705.06,1318.313z';
        const coords = [];
        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            if (line.nums-0 >= filter) {
                const weight = Common.getWeight(maxnum, line.nums-0);
                const fromCoord = Object.values(Common.getLngLat(line.lease));
                const toCoord = Object.values(Common.getLngLat(line.return));
                const series = {
                    name: i + '',
                    type: 'lines',
                    coordinateSystem: 'bmap',
                    zlevel: 1,
                    symbol: ['none', 'arrow'],
                    symbolSize: 10,
                    effect: {
                        show: true,
                        period: 6,
                        trailLength: 0,
                        symbol: planePath,
                        symbolSize: 15
                    },
                    lineStyle: {
                        normal: {
                            color: Common.getColor(weight),
                            width: 10,
                            opacity: 0.6,
                            curveness: 0.3
                        }
                    },
                    data: [fromCoord, toCoord]
                };
                coords.push(series);
            }
        }
        console.log(coords);
        bmap.setOption({
            series: coords
        });
        $(".loading-field").hide();
    }

    // 加载弧线
    function addCurvlines(lines,cores,opt) {
        const active = $(".tab-active").find("a").attr("href");
        const filter = parseInt($(active).find(".filter").val());
        let maxnum = parseInt($(active).find(".max-num").text());
        const markers = new Set();
        // 求站点联系关联度最大值
        if(Number.isNaN(maxnum) || opt) {
            maxnum = getMaxNum();
            $(".max-num").html(maxnum);
        }
        let id = trace[0];
        lines.forEach(function (item ,index) {
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
                if(opt != null) {
                    newMarker(marker1, opt[0]);
                    newMarker(marker2, opt[1]);
                }
                // console.log(Common.getID(marker1)+" "+Common.getID(marker2));
                map.addOverlay(curveline);
                addArrow(curveline,{strokeColor:Common.getColor(weight), strokeWeight:(6*weight), strokeOpacity:0.8});
            }
        });
        if ((!cores)&&id) {
            let array = [];
            const colors = Common.color();
            scatters[id].forEach(function (point) {
                let marker = Common.getLngLat(point);
                array.push(new BMap.Point(marker.lng,marker.lat));
            });
            Marker(array, {color: colors[(id-1)%38], size: size[1]}, "info");
        }
        // 绘制箭头函数
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
        $(".loading-field").hide();
    }

    // 显示柱状图
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

    // 显示层叠柱状图
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