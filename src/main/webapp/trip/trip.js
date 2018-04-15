﻿
$(function () {
    //1.初始化Table
    var oTable = new TableInit();
    oTable.Init();

    //2.初始化Button的点击事件
    var oButtonInit = new ButtonInit();
    oButtonInit.Init();

});

var TableInit = function () {
    var oTableInit = new Object();
    //初始化Table
    oTableInit.Init = function () {
        $('#tableList').bootstrapTable({
            url: 'WxLogin/getTripList',         //请求后台的URL（*）
            method: 'POST',                      //请求方式（*）
            contentType:"application/x-www-form-urlencoded;charset=UTF-8",
            toolbar: '#tableListToolbar',       //工具按钮用哪个容器
            striped: true,                      //是否显示行间隔色
            cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
            pagination: true,                   //是否显示分页（*）
            sortable: false,                     //是否启用排序
            sortOrder: "asc",                   //排序方式
            queryParams: oTableInit.queryParams,//传递参数（*）
            sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
            pageNumber: 1,                       //初始化加载第一页，默认第一页
            pageSize: 10,                       //每页的记录行数（*）
            pageList: [10, 25, 50, 100],        //可供选择的每页的行数（*）
            search: true,                       //是否显示表格搜索，此搜索是客户端搜索，不会进服务端，所以，个人感觉意义不大
            strictSearch: true,
            showColumns: true,                  //是否显示所有的列
            showRefresh: true,                  //是否显示刷新按钮
            minimumCountColumns: 2,             //最少允许的列数
            clickToSelect: true,                //是否启用点击选中行
            height: 500,                        //行高，如果没有设置height属性，表格自动根据记录条数觉得表格高度
            uniqueId: "ID",                     //每一行的唯一标识，一般为主键列
            showToggle: true,                    //是否显示详细视图和列表视图的切换按钮
            cardView: false,                    //是否显示详细视图
            detailView: false,                   //是否显示父子表
            iconSize: "outline",
            responseHandler: function(res) {
                return {
                    "total": res.total,//总页数
                    "rows": res.rows   //数据
                };
            },
            icons: {
                refresh: "glyphicon-repeat",
                toggle: "glyphicon-list-alt",
                columns: "glyphicon-list"
            }
        });
    };

    //得到查询的参数
    oTableInit.queryParams = function (params) {
        console.info(params);
        var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
            // limit: params.limit,   //页面大小
            // offset: params.offset,  //页码
            currentPage:params.offset/(params.limit)+1,
            showCount:params.limit,
            // departmentname: $("#txt_search_departmentname").val(),
            statu: $("#txt_search_statu").val()
        };
        return temp;
    };
    return oTableInit;
};


var ButtonInit = function () {
    var oInit = new Object();
    var postdata = {};

    oInit.Init = function () {
        $("#btn_add").click(function () {
            layer.msg('添加开始');
            layer.open({
                type: 2,
                area:['770px','510px'],
                title: '添加行程',
                content: 'form.html',
                offset: 'r',
                shadeClose: true,
                maxmin: true,
                shade: 0.1,
                btn: ['确认', '取消'],
                yes: function (index, layero) {
                    var iframeWin = window[layero.find('iframe')[0]['name']];
                    iframeWin.submitForm();
                    // $("#signupForm").click(function () {
                    //
                    // })
                    // var dat = $('#signupForm').serializeObject();
                    // alert($("#tripType").val());
                    // $.ajax({
                    //     type: 'POST',
                    //     url: 'WxLogin/submitTrip.do',
                    //     dataType: "json",
                    //     data:dat,
                    //     success: function (data) {
                    //         if(data.result == "success") {
                    //             layer.msg(data.message,function () {
                    //                 layer.close(index);
                    //             })
                    //         }else{
                    //             layer.msg(data.message,function () {
                    //             })
                    //         }
                    //     },
                    //     error:function () {
                    //         layer.msg("未知原因导致添加行程失败",function () {
                    //         })
                    //     }
                    // })


                },
                btn2: function (index, layero) {
                    layer.msg('取消添加');
                }
            });
        });

        $("#btn_edit").click(function () {
            var arrselections = $("#tableList").bootstrapTable('getSelections');
            if (arrselections.length > 1) {
                layer.msg('只能选择一行进行编辑');

                return;
            }
            if (arrselections.length <= 0) {
                layer.msg('请选择有效数据');

                return;
            }
            layer.open({
                type: 2,
                area: ['770px', '510px'],
                title: '添加行程',
                content: 'form.html',
                offset: 'r',
                shadeClose: true,
                maxmin: true,
                shade: 0.1,
                btn: ['确认', '取消'],
                yes: function (index, layero) {                    
                    //var iframeWin = window[layero.find('iframe')[0]['name']];
                    //iframeWin.submitForm();
                },
                btn2: function (index, layero) {
                    layer.msg('取消添加');
                }

            });
            $("#myModalLabel").text("编辑");
            $("#txt_departmentname").val(arrselections[0].DEPARTMENT_NAME);
            $("#txt_parentdepartment").val(arrselections[0].PARENT_ID);
            $("#txt_departmentlevel").val(arrselections[0].DEPARTMENT_LEVEL);
            $("#txt_statu").val(arrselections[0].STATUS);

            postdata.DEPARTMENT_ID = arrselections[0].DEPARTMENT_ID;
            $('#myModal').modal();
        });

        $("#btn_delete").click(function () {
            var arrselections = $("#tableList").bootstrapTable('getSelections');
            if (arrselections.length <= 0) {
                layer.msg('请选择要删除的数据');
                return;
            }
            layer.confirm('确认要删除选择的数据吗？', {
                btn: ['确认', '取消'] //按钮
            }, function () {
                $.ajax({
                    type: "post",
                    url: "/Home/Delete",
                    data: { "": JSON.stringify(arrselections) },
                    success: function (data, status) {
                        if (status == "success") {
                            layer.msg('提交数据成功');
                            $("#tableList").bootstrapTable('refresh');
                        }
                    },
                    error: function () {
                        layer.msg('删除失败');
                    },
                    complete: function () {

                    }

                });
            }, function () {
                layer.msg('您取消了操作');
            });
        });

        $("#btn_submit").click(function () {
            postdata.DEPARTMENT_NAME = $("#txt_departmentname").val();
            postdata.PARENT_ID = $("#txt_parentdepartment").val();
            postdata.DEPARTMENT_LEVEL = $("#txt_departmentlevel").val();
            postdata.STATUS = $("#txt_statu").val();
            $.ajax({
                type: "post",
                url: "/Home/GetEdit",
                data: { "": JSON.stringify(postdata) },
                success: function (data, status) {
                    if (status == "success") {
                        layer.msg('提交数据成功');
                        $("#tableList").bootstrapTable('refresh');
                    }
                },
                error: function () {
                    layer.msg('Error');
                },
                complete: function () {

                }

            });
        });

        $("#btn_query").click(function () {
            $("#tableList").bootstrapTable('refresh');
        });
    };

    return oInit;
};