$(function () {
    //初始化出发与目的城市
    $.ajax({
        type: 'POST',
        url: 'region/getCities.do',
        dataType: "json",
        data:{'codeSelect': 'city'},
        success: function (data) {
            if (data != null && data.length > 0) {
                var opstr = "";
                $.each(data, function (i, n) {
                    opstr += " <option value=\"" + data[i].code + "\">" + data[i].name + "</option>";
                })
                // var myobj = document.getElementById(obj);
                $("#StartCityId").append(opstr);
                $("#EndCityId").append(opstr);
            }
        }
    })
});
function submitForm() {
    var validForm = $("#signupForm").Validform({
        tiptype: 4,
        callback: function (data) {
            if (data.outType == "success") {
                parent.layer.msg(data.outContent,function () {
                    //提交成功刷新列表页面
                    parent.$("#tableList").bootstrapTable('refresh');
                    //关闭增加页面
                    parent.layer.closeAll();
                });


            }
            else
            {
                //失败弹出提示错误
                alert('添加失败');
            }
        }
    });
    validForm.ajaxPost(false, false, 'WxLogin/submitTrip.do');
}

//点击按钮时，改变形成类型
function changeType(tripType) {
    $("#tripType").val(tripType);
	if (tripType == 2)
            {
                $("#btnCar").removeClass("btn-default").addClass('btn-primary');
                $("#btnCus").removeClass("btn-primary").addClass('btn-default');
            }
            else if (tripType == 1)
            {
                $("#btnCar").removeClass("btn-primary").addClass('btn-default');
                $("#btnCus").removeClass("btn-default").addClass('btn-primary');
            }
    // alert($("#tripType").val());
}

/*
function GetAllOrders(obj) {
    var myobj = document.getElementById(obj);
    if (myobj.options.length == 1) {
        $.ajax({
            type: 'POST',
            url: 'region/getCities.do',
            dataType: "json",
            data:{'codeSelect': 'city'},
            success: function (data) {
                if (data != null && data.length > 0) {
                    var opstr = "";
                    $.each(data, function (i, n) {
                        opstr += " <option value=\"" + data[i].code + "\">" + data[i].name + "</option>";
                    })
                    var myobj = document.getElementById(obj);

                    $("#" + obj).append(opstr);
                }
            }
        })

    }


}*/
