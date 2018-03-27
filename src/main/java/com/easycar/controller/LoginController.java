package com.easycar.controller;

import com.easycar.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("login")
public class LoginController extends BaseController {

    @Resource(name = "userService")
    private UserService userService;

    @RequestMapping("/toLogin")
    public ModelAndView toLogin() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("user/login");
        return mv;
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public Map<String ,String > getUser() {
        Map<String ,String > returnMp = new HashMap<String, String>();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("user_name","李白");
        map.put("password","000000");
        String address="";
        try {
            Map<String, String> usermap = userService.getUser(map);
            address = usermap.get("address");
            System.out.println(usermap.toString());
            returnMp.put("msg","查询成功");
            returnMp.put("address",address);

        } catch (Exception e) {
            e.printStackTrace();
            returnMp.put("msg","查询失败");
        }
        return returnMp;
    }


    @RequestMapping("/login")
    @ResponseBody
    public ModelAndView login(String userName,String password) throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        ModelAndView mv = new ModelAndView();
        map.put("userName",userName);
        map.put("password",password);
        String msg = "";
        //根据用户名和密码查找用户
        Map<String,String>  userMap= userService.findUserByNameAndPassword(map);
        if(userMap == null || "".equals(userMap.get("user_name"))) {
            msg = "用户名或密码错误！";
        }else if("admin".equals(userMap.get("user_name")) && "0".equals(userMap.get("role_id"))) {
            //只有登陆者是admin且角色id是‘0’时，则登陆者为最高权限管理员
            mv.setViewName("user/admin_index");
        }else{
            mv.setViewName("user/user_index");
        }
        mv.addObject("user_name",userMap.get("user_name"));
        return mv;
    }
}
