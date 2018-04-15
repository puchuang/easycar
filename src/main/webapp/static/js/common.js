$(function() {
	$('body').keydown(function(event) {
	      $(':focus').filter('button').blur();
	      if(event.keyCode == '13' &&  $("#mb_con").length == 0) {
	          $(':focus').blur();
	  }else if(event.keyCode == '13' && $("#mb_con").length > 0) {
	          $("#mb_btn_ok").click();
	      }

	  });

	/**
	 * 严格校验身份证号码 公民身份号码是特征组合码,由十七位数字本体码和一位数字校验码组成.
	 * 排列顺序从左至右依次为:六位数字地址码,八位数字出生日期码, 三位数字顺序码和一位数字校验码.顺序码的奇数分给男性,偶数分给女性.
	 * 校验码是根据前面十七位数字码,按照ISO7064:1983.MOD11-2校验码计算出来的检验码.
	 * 
	 * @param IDNo
	 *            身份证号
	 * @param flagName
	 *            用于提示；身份证所属人
	 */
	chekcIdCard = function(IDNo, flagName) {
		if (isNull(flagName)) {
			flagName = "";
		}

		var SystemDate = new Date(), year = SystemDate.getFullYear(), month = SystemDate
				.getMonth() + 1, day = SystemDate.getDate(), yyyy, // 年
		mm, // 月
		dd, // 日
		birthday, // 生日
		sex; // 性别
		var id = IDNo;
		var id_length = id.length;

		if (id_length === 0) {
			alert("请输入" + flagName + "的身份证号码号码!");
			return false;
		}
		if (id_length != 18) {
			alert("输入" + flagName + "的身份证号号位数错误！");
			return false;
		}
		if (id_length === 18) {
			for (var i = 0; i < id_length - 1; i++) {
				if (isNaN(IDNo.charAt(i))) {
					alert(flagName + "身份证号号中前17位中不能有字符！");
					return false;
				}
			}
			if (isNaN(IDNo.charAt(17)) && IDNo.charAt(17) != "X"
					&& IDNo.charAt(17) != "x") {
				alert(flagName + "身份证校验错误，请认真检查！");
				return false;
			}
			if (IDNo.indexOf("X") > 0 && IDNo.indexOf("X") != 17
					|| IDNo.indexOf("x") > 0 && IDNo.indexOf("x") != 17) {
				alert(flagName + "身份证中\"X\"输入位置不正确！");
				return false;
			}
			yyyy = id.substring(6, 10);
			if (yyyy > year || yyyy < 1900) {
				alert(flagName + "身份证号号年度非法！");
				return false;
			}
			mm = id.substring(10, 12);
			if (mm > 12 || mm <= 0) {
				alert(flagName + "身份证号号月份非法！");
				return false;
			}
			if (yyyy == year && mm > month) {
				alert(flagName + "身份证号号月份非法！");
				return false;
			}
			dd = id.substring(12, 14);
			if (dd > 31 || dd <= 0) {
				alert(flagName + "身份证号号日期非法！");
				return false;
			}

			// 4,6,9,11月份日期不能超过30
			if ((mm == 4 || mm == 6 || mm == 9 || mm == 11) && (dd > 30)) {
				alert(flagName + "身份证号号日期非法！");
				return false;
			}

			// 判断2月份
			if (mm == 2) {
				if (leapYear(yyyy)) {
					if (dd > 29) {
						alert(flagName + "身份证号号日期非法！");
						return false;
					}
				} else {
					if (dd > 28) {
						alert(flagName + "身份证号号日期非法！");
						return false;
					}
				}
			}
			if (yyyy == year && mm == month && dd > day) {
				alert(flagName + "身份证号号日期非法！");
				return false;
			}
			if (id.charAt(17) == "x" || id.charAt(17) == "X") {
				if ("x" != getVerifyBit(id) && "X" != getVerifyBit(id)) {
					alert(flagName + "身份证校验错误，请认真检查！");
					return false;
				}
			} else {
				if (id.charAt(17) != getVerifyBit(id)) {
					alert(flagName + "身份证校验错误，请认真检查！");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断是否是闰年
	 * 
	 * @param year
	 *            年份
	 */
	leapYear = function(year) {
		if (year % 100 === 0) {
			if (year % 400 === 0) {
				return true;
			}
		} else {
			if ((year % 4) === 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 计算身份证校验码 原理: ∑(a[i]*W[i]) mod 11 ( i = 2, 3, ..., 18 )(1) "*" 表示乘号
	 * i--------表示身份证号码每一位的序号， 从右至左，最左侧为18，最右侧为1。 a[i]-----表示身份证号码第 i 位上的号码
	 * W[i]-----表示第 i 位上的权值 W[i] = 2^(i-1) mod 11 计算公式 (1) 令结果为 R 根据下表找出 R
	 * 对应的校验码即为要求身份证号码的校验码C。 R 0 1 2 3 4 5 6 7 8 9 10 C 1 0 X 9 8 7 6 5 4 3 2 X
	 * 就是 10，罗马数字中的 10 就是 X 15位转18位中,计算校验位即最后一位
	 * 
	 * @param targetID
	 */
	getVerifyBit = function(targetID) {
		var result, nNum = eval(targetID.charAt(0) * 7 + targetID.charAt(1) * 9
				+ targetID.charAt(2) * 10 + targetID.charAt(3) * 5
				+ targetID.charAt(4) * 8 + targetID.charAt(5) * 4
				+ targetID.charAt(6) * 2 + targetID.charAt(7) * 1
				+ targetID.charAt(8) * 6 + targetID.charAt(9) * 3
				+ targetID.charAt(10) * 7 + targetID.charAt(11) * 9
				+ targetID.charAt(12) * 10 + targetID.charAt(13) * 5
				+ targetID.charAt(14) * 8 + targetID.charAt(15) * 4
				+ targetID.charAt(16) * 2);
		nNum = nNum % 11;
		switch (nNum) {
		case 0:
			result = "1";
			break;
		case 1:
			result = "0";
			break;
		case 2:
			result = "X";
			break;
		case 3:
			result = "9";
			break;
		case 4:
			result = "8";
			break;
		case 5:
			result = "7";
			break;
		case 6:
			result = "6";
			break;
		case 7:
			result = "5";
			break;
		case 8:
			result = "4";
			break;
		case 9:
			result = "3";
			break;
		case 10:
			result = "2";
			break;
		}
		return result;
	}

	// 校验手机号，11位，130-139,150-159,180-189开头
	checkMobile = function(mobile) {
		if (mobile.length != 11) {
			return false;
		}
		var myreg = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/;
		if (!myreg.test(mobile)) {
			return false;
		}
		return true;
	}

	// 判断字符串是不是空
	isNull = function(text) {
		if (text == '' || text == null || text == 'undefined') {
			return true;
		}
		return false;
	}

	// 日期比较,a>b返回0，a<b返回1, a=b返回3
	function compareDate(a, b) {
		var arr = a.split("-");
		var starttime = new Date(arr[0], arr[1], arr[2]);
		var starttimes = starttime.getTime();

		var arrs = b.split("-");
		var endtime = new Date(arrs[0], arrs[1], arrs[2]);
		var endtimes = endtime.getTime();

		if (starttimes > endtimes) {
			return 0;
		} else if (starttimes < endtimes) {
			return 1;
		} else if (starttimes = endtimes) {
			return 2;
		}

	}
	/* form表单拼接为json */
	$.fn.serializeObject = function() {
	var o = {};
	var a = this.serializeArray();
	$.each(a, function() {
		if (o[this.name]) {
			if (!o[this.name].push) {
				o[this.name] = [ o[this.name] ];
			}
			o[this.name].push(this.value || '');
		} else {
			o[this.name] = this.value || '';
		}
	});
	return o;
	};
	
	//获取系统时间YYYY-mm-dd
	getNowDate = function(){
		var d = new Date();
		var date = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate();
		return date;
	}
	
	jQuery.download = function(url, method, filePath, fileName){
	    jQuery('<form action="'+url+'" method="'+(method||'post')+'">' +  // action请求路径及推送方法
	                '<input type="text" name="filePath" value="'+filePath+'"/>' + // 文件路径
	                '<input type="text" name="fileName" value="'+fileName+'"/>' + // 文件名称
	            '</form>')
	    .appendTo('body').submit().remove();
	};
	
});
//拖拽
function drag(id1,id2){
    
   var d_box = document.getElementById(id1);
   var drop = document.getElementById(id2);
   //鼠标在drop上按下 变为可拖动状态 鼠标在页面上移动让盒子跟着走
   //onmousedown 鼠标按下事件
   drop.onmousedown = function (event) {

       var event = event || window.event;
       var pageX = event.pageX || event.clientX + document.documentElement.scrollLeft;
       var pageY = event.pageY || event.clientY + document.documentElement.scrollTop;
       var boxX = pageX - d_box.offsetLeft;
       var boxY = pageY - d_box.offsetTop;
      
       document.onmousemove = function (event) {
    	   
           var event = event || window.event;
           var pageX = event.pageX || event.clientX + document.documentElement.scrollLeft;
           var pageY = event.pageY || event.clientY + document.documentElement.scrollTop;
           var x=pageX - boxX;
           var y=pageY - boxY;
           var a=d_box.offsetWidth;
           var b=d_box.offsetHeight;
           var windowsX=document.body.offsetWidth ;
           var windowsY=document.body.offsetHeight ;
           if(x<0){
        	   x=0
           };
           if(x>(windowsX-a)){
        	   x=windowsX-a
           }
           if(y<0){
        	   y=0
           };
           if(y>(windowsY-b)){
        	   y=windowsY-b
           }
           d_box.style.left = (x + 328)+"px";
           d_box.style.top = (y+178) + "px";
           console.log(d_box.style.left);
           console.log(d_box.style.top);
           window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();

       }
   }

   document.onmouseup = function () {
       document.onmousemove = null;
   }
}
