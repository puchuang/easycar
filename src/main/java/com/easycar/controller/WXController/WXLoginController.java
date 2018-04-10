package com.easycar.controller.WXController;

import com.alibaba.fastjson.JSONObject;
import com.easycar.controller.BaseController;
import com.easycar.service.WXService.WXservice;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("*/WxLogin")
public class WXLoginController extends BaseController{

    @Resource(name = "wxService")
    private WXservice wXservice;

    /**
     * 根据条件获取行程列表

     * @return
     * @throws Exception
     */
    @RequestMapping("/getTripList")
    @ResponseBody
//    public Map<String,Object> getTripList(String pageIndx, String triptype, String startCity, String endCity, String startDate) {
    public Map<String,Object> getTripList(HttpServletRequest request) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        Map<String,Object> map = new HashMap<String, Object>();
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        String outType = "fail";
        Integer count=0;

        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");
        map.put("offset",Integer.parseInt(offset));
        map.put("limit",Integer.parseInt(limit));

        //默认已前台校验非空，后台不再校验
//        int pageNumber = Integer.parseInt(pageIndx);
//        int pageNumber = 1;
//        int pageSize =
//        int start = (pageNumber-1)*pageSize+1;//
//        map.put("start",start);
//        map.put("pageSize",pageSize);//默认每页10条
//        map.put("triptype",triptype);
//        map.put("startCity",startCity);
//        map.put("endCity",endCity);
//        map.put("startDate",startDate);
        try {
            count = wXservice.getCount(map);
            list = wXservice.getTripListByCondition(map);
            outType = "success";
            logger.info("查询行程列表成功");
        }catch (Exception e){
            outType = "fail";
            logger.info("查询行程列表失败");
            e.printStackTrace();
        }

//        returnMap.put("outType",outType);
        returnMap.put("total",count);
        returnMap.put("rows",list);
        return returnMap;
    }

    /**
     * 根据流水号获取行程详细信息
     * @param serialNo
     * @return
     */
    @RequestMapping("/getTripInfo ")
    @ResponseBody
    public Map<String,Object> getTripDetail(String serialNo) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        Map<String,Object> detailMap = new HashMap<String,Object>();
        String outType = "fail";
        if(!StringUtils.isEmpty(serialNo)){
            try{
                detailMap =  wXservice.getTripBySerialNo(serialNo);
                outType = "success";
                logger.info("查询成功，流水号："+serialNo);
            }catch (Exception e){
                outType = "fail";
                logger.info("查询错误，流水号："+serialNo);
                e.printStackTrace();
            }
        }
        returnMap.put("outType",outType);
        returnMap.put("data",detailMap);
        return returnMap;
    }

    /**
     * 根据流水号获取行程详细信息
     * @param openid  微信用户唯一标识
     * @param tripData 详细的行程信息字符串
     * @return
     */
    @RequestMapping("/submitTrip")
    @ResponseBody
    public Map<String,Object> submitTrip (String openid,String tripData ) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        String outType = "fail";
        String outContent = "发布失败";
        try {
            String result = wXservice.insertTrip(openid,tripData);
            if("1".equals(result)) {
                outContent = "发布成功";
                outType = "success";
                logger.info("行程发布成功");
            }else if("2".equals(result)) {
                outContent = "当天不能重复发布";
                outType = "fail";
                logger.info("当天不能重复发布");
            }else if("3".equals(result)) {
                outContent = "发布失败";
                outType = "fail";
                logger.info("行程发布失败");
            }
        } catch (Exception e) {
            outContent = "发布失败";
            outType = "fail";
            logger.info("系统异常，导致行程发布失败");
            e.printStackTrace();
        }

        returnMap.put("outType",outType);
        returnMap.put("outContent",outContent);
        return returnMap;
    }

    @RequestMapping("/login")
    @ResponseBody
    public Map<String,Object> login(String reqJson) {
        Map<String,Object> map = new HashMap<String,Object>();

        Map<String,Object> loginMap = wXservice.systemLogin(reqJson);
        return  map;
    }
}
