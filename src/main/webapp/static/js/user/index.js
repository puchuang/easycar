window.onload=function () {
    alert("js加载成功")
}

function getUsers() {
    $.ajax({
        url:"login/getUser",
        dataType:"json",
        type:"POST",
        success:function (data) {

            $("#user_msg").val(data.address);
            alert(data.msg);

        },
        error:function (daya) {
            alert("查询失败");
        }
    })
}