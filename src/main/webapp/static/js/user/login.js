$(function () {
    $("#login").click(function () {
        var userName = $("#userName").val();
        var password = $("#password").val();

        //登陆前进行用户名和密码的非空校验
        if ($.trim(userName) == "") {
            alert("请输入用户名");
            return false;
        }
        if ($.trim(userName) == "") {
            alert("password");
            return false;
        }
        $.ajax({
            url:"login/login",
            dataType:"json",
            type:"POST",
            data:{"userName":userName,"password":password}
            success:function (data) {

            },
            error:function (data) {
              alert("登录失败，请重新尝试");
            }

        })
    })
})