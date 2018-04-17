package com.easycar.controller.WXController;

import com.alibaba.fastjson.JSONObject;
import com.easycar.controller.BaseController;
import com.easycar.entity.Page;
import com.easycar.service.WXService.WXservice;
import com.easycar.service.common.RegionService;
import com.easycar.util.PageData;
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
    public Map<String,Object> getTripList(Page page) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        String outType = "fail";


        PageData pageData = this.getPageData();
        if(!pageData.containsKey("startCity")) {
            pageData.put("startCity","");
        }
        if(!pageData.containsKey("endCity")) {
            pageData.put("endCity","");
        }
        if(!pageData.containsKey("tripType")) {
            pageData.put("tripType","");
        }
        if(!pageData.containsKey("curTime")) {
            pageData.put("curTime","");
        }
        page.setPd(pageData);
        try {

            list = wXservice.getTripListByCondition(page);
            outType = "success";
            logger.info("查询行程列表成功");
        }catch (Exception e){
            outType = "fail";
            logger.info("查询行程列表失败");
            e.printStackTrace();
        }

//        returnMap.put("outType",outType);
        int total = page.getTotalResult();
        returnMap.put("total",total);
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
     * @return
     */
    @RequestMapping("/submitTrip")
    @ResponseBody
    public Map<String,Object> submitTrip () {
        PageData pageData = this.getPageData();
        Map<String,Object> returnMap = new HashMap<String,Object>();
        String outType = "fail";
        String outContent = "发布失败";
        try {
            String result = wXservice.insertTrip(pageData);
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
