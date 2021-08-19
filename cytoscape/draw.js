var cy = cytoscape({
            container: document.getElementById('cy'),
            boxSelectionEnabled: true,
            autounselectify: true,
             motionBlur:!1,
             maxZoom:2.5,
             minZoom:.2,
             wheelSensitivity:.1,//滑动滚轮一次缩放大小
             textureOnViewport:!1,
            style: [{
                    selector: 'node',
                    style: {
                        "content": function (ele) {
                            return ele.data('label') || ele.data('id')
                        },
                        "text-valign": 'center',
                        "width": function (a) {
                            return 60
                        },
                        "height": function (a) {
                            return 60
                        },
                        "background-color": function (a) {
                            if (a.data("type")=="Source") {
                                return '#EAA829';
                            } 
                            else if(a.data("type")=="Class"){
                                return '#47D2AC';
                            }
                            else if(a.data("type")=="Interface"){
                                return '#2196F4';
                            }
                            else if(a.data("type")=="AbsClass"){
                                return '#FF99FF';
                            }
                            else if(a.data("type")=="MemClass"){
                                return '#9900FF';
                            }
                            else{
                                return '#000000';
                            }
                        },
                        "color": '#fff',
                        "border-color": function (a) {
                            return '#000000';
                        },
                        "border-width": 1,
                        "text-wrap": "wrap",
                        "font-size": 10,
                        "font-family": "microsoft yahei",
                        "overlay-color": "#fff",
                        "overlay-opacity": 0,
                        "background-opacity": 1,
                        "shape": "ellipse",
                        "z-index-compare": "manual",
                        "z-index": 20,
                        "padding": function (a) {
                            return 3
                        },
                        "text-max-width": 60,
                        "text-margin-y": function (a) {
                            return 4
                        },
                        "label": function (a) {
                            a = a.data("id");
                            var b = a.length;
                            return 5 >= b ? a : 5 <= b && 9 >= b ? a.substring(0, b - 5) + "\n" + a.substring(
                                b - 5, b) : 9 <= b && 13 >= b ? a.substring(0, 4) + "\n" + a.substring(
                                4, 9) + "\n" + a.substring(9, 13) : a.substring(0, 4) + "\n" + a.substring(
                                4, 9) + "\n" + a.substring(9, 12) + ".."
                        },
                    }
                },
                {
                    selector: 'edge',
                    style: {
                        // 添加箭头
                        "line-style": function (a) {
                            return "solid"
                        },
                        "curve-style": "bezier",
                        "control-point-step-size": 20,
                        "target-arrow-shape": "triangle",
                        "target-arrow-color": function (a) {
                            // return a.data("color")
                            return '#000'
                        },
                        "arrow-scale": .5,
                        "line-color": function (a) {
                            // return a.data("color")
                            return '#000'
                        },
                        "label": function (a) {
                            return a.data("label")
                        },
                        "text-opacity": .8,
                        "font-size": 10,
                        "background-color": function (a) {
                            return "#333"
                        },
                        "width": 1,
                        "overlay-color": "#fff",
                        "overlay-opacity": 0,
                        "font-family": "microsoft yahei"
                    }
                },
                {
                    selector: ':selected',
                    style: {
                        "border-width": 3,
                        "border-color": '#333',
                        "background-color": 'black',
                        "line-color": 'black',
                        "target-arrow-color": 'black',
                        "source-arrow-color": 'black'
                    }
                },
                {
                    selector: ".nodeHover", //节点变暗，有悬停效果
                    style: {
                        "shape": "ellipse",
                        "background-opacity": .8
                    }
                },
                {
                    selector: ".nodeActive",
                    style: {
                        "border-color": '#4EA2F0',
                        "border-width": 10,
                        "border-opacity": .5
                    }
                },
                {
                    selector: ".edgeShow",
                    style: {
                        "color": "#000",
                        "text-opacity": 1,
                        "text-background-color": "#fff",
                        "text-background-opacity": .8,
                        "text-background-padding": 0,
                        "font-weight": 400,
                        "label": function (a) {
                            return a.data("label")
                        },
                        "font-size": 10,
                        "arrow-scale": .8,
                        "width": 1.5,
                        "source-text-margin-y": 20,
                        "target-text-margin-y": 20,
                    },
                },
                {
                    selector: ".edgeActive",
                    style: {
                        "arrow-scale": .8,
                        "width": 1.5,
                        "color": "#330",
                        "text-opacity": 1,
                        "font-size": 10,
                        "text-background-color": "#fff",
                        "text-background-opacity": .8,
                        "text-background-padding": 0,
                        "source-text-margin-y": 20,
                        "target-text-margin-y": 20,
                        "z-index-compare": "manual",
                        "z-index": 1,
                        "line-color": function (a) { //直线颜色
                            return "#4EA2F0"
                        },
                        "target-arrow-color": function (a) { //箭头颜色
                            return "#4EA2F0"
                        },
                        label: function (a) {
                            return a.data("label")
                        }
                    }
                },
                {
                    selector: ".dull",
                    style: {
                        "z-index": 1,
                        "opacity": .2
                    }
                }
            ],
            elements: window.element_data,
            layout: {
                name: 'grid',//用哪种方式排列，可选：breadthfirst(广度优先)、cose(缝制，乱交)、preset(预设)、circle(圆形)、grid(矩形)
                idealEdgeLength: 60,
                nodeOverlap: 20,
                refresh: 20,
                fit: true,
                padding: 0,
                randomize: false,
                componentSpacing: 20,
                nodeRepulsion: 100,
                edgeElasticity: 10,
                nestingFactor: 5,
                animate:false,//出来动画
                gravity: 80,
                numIter: 4400,
                initialTemp: 200,
                coolingFactor: 0.95,
                minTemp: 1.0
            }
        })
        cy.collection("edge").addClass("edgeShow");
        cy.on("mouseover", "node", function (a) {
            $('#cy').css('cursor', 'move');
            let c = a.target;
            c.addClass("nodeHover");
            // cy.collection("edge").removeClass("edgeShow");
            cy.collection("edge").removeClass("edgeActive");
            c.neighborhood("edge").addClass("edgeActive");
        })
        cy.on("mouseout", "node", function (a) {
            $('#cy').css('cursor', 'default');
            let c = a.target;
            c.removeClass("nodeHover");
            cy.collection("edge").removeClass("edgeActive");
        })
        cy.on("click", "node", function (a) {
            let c = a.target;
            c.removeClass("nodeActive");
            cy.collection("edge").removeClass("edgeActive");
        })
        cy.on("vmousedown", "node", function (a) { //监听鼠标左键按下
            let c = a.target;
            cy.collection("edge").addClass('dull');
            cy.collection("node").addClass('dull');
            c.removeClass("dull");
            c.neighborhood("edge").removeClass("dull");
            c.neighborhood("edge").addClass("edgeActive");
            c.neighborhood("edge").connectedNodes().removeClass("dull"); //当前节点的邻域边的边缘节点！
            // c.neighborhood("node").removeClass("dull");//与上面意义相同
        })
        cy.on("tapend", "node", function (a) { //监听鼠标左键释放
            let c = a.target;
            cy.collection("edge").removeClass('dull');
            cy.collection("node").removeClass('dull');
            c.neighborhood("edge").removeClass("edgeActive");
            c.neighborhood("node").removeClass("nodeActive");
        })

        //线
        cy.on("mouseover", "edge", function (a) {
            let c = a.target;
            cy.collection("edge").removeClass("edgeActive");
            c.addClass("edgeActive")
        })
        cy.on("mouseout", "edge", function (a) {
            let c = a.target;
            c.removeClass("edgeActive")
        })