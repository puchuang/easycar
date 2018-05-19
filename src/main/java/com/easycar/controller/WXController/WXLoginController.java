package com.easycar.controller.WXController;

import com.easycar.controller.BaseController;
import com.easycar.entity.Page;
import com.easycar.service.WXService.WXservice;
import com.easycar.service.common.CommonService;
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

    @Resource(name = "commonService")
    private CommonService commonService;

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

        if(!pageData.containsKey("searchPar")) {
            pageData.put("searchPar","");
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
     * @return
     */
    @RequestMapping("/getTripInfo ")
    @ResponseBody
    public Map<String,Object> getTripDetail() {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        Map<String,Object> detailMap = new HashMap<String,Object>();
        String result = "fail";

        PageData pd = this.getPageData();
        String serialNo = pd.get("SerialNo").toString();
        if(!StringUtils.isEmpty(serialNo)){
            try{
                detailMap =  wXservice.getTripBySerialNo(serialNo);
                result = "success";
                logger.info("查询成功，流水号："+serialNo);
            }catch (Exception e){
                result = "fail";
                logger.info("查询错误，流水号："+serialNo);
                e.printStackTrace();
            }
        }
        returnMap.put("result",result);
        returnMap.put("data",detailMap);
        return returnMap;
    }

    /**
     * 提交行程
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
                outContent = "请勿重复发布";
                outType = "fail";
                logger.info("形成已存在，不能重复发布");
            }else if("3".equals(result)) {
                outContent = "发布失败";
                outType = "fail";
                logger.info("行程发布失败");
            }else if("4".equals(result)) {
                outContent = "每天限发布3条";
                outType = "fail";
                logger.info("每个客户每天仅允许发布3条，当前已超过次数限制");
            }else if("5".equals(result)) {
                outContent = "无权发布";
                outType = "fail";
                logger.info("黑名单用户无权发布行程，userId="+pageData.getString("UserId"));
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

    /**
     * 登陆入口（小程序）
     * @param request
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public Map<String,Object> login(HttpServletRequest request) {
        request.getSession().getId();

        PageData pageData = this.getPageData();

        Map<String,Object> loginMap = wXservice.systemLogin(pageData);
        if(loginMap != null && loginMap.containsKey("result")) {
            return  loginMap;
        }else{
            return null;
        }
    }

    /**
     * 支持多条删除，以逗号分隔即可
     * 删除行程的方法，逻辑删除，置为失效即可
     * @return
     */
    @RequestMapping("/removeTrip")
    @ResponseBody
    public Map<String,Object> removeTrip() {
        Map<String,Object> map = new HashMap<String, Object>();
        PageData pageData = this.getPageData();//得到页面传过来的数据
        String result = "fail";
        String msg = "";

        if(pageData.containsKey("listSerialNo") || !"".equals(pageData.getString("listSerialNo"))) {
            StringBuffer sb = new StringBuffer("(");
            String serialNo = pageData.getString("listSerialNo");
            sb.append(serialNo).append(")");

            boolean b = commonService.updateStatus("ecu_trip_driver", "IsEffective", "2", "SerialNo", sb.toString());

            if(b) {
                result = "success";
                msg = "删除行程成功";
            }else{
                result = "fail";
                msg = "删除失败";
            }
        }else{
            result = "fail";
            msg = "请先选择需要删除的数据";
        }
        map.put("result",result);
        map.put("msg",msg);
        return map;
    }

    /**
     * 加入/取消加入 行程
     * @return
     */
    @RequestMapping("/joinTrip")
    @ResponseBody
    public Map<String,Object> joinTrip() {
        Map<String,Object> map = new HashMap<String,Object>();
        String result = "fail";
        String joinOrCancel = "";
        String msg = "加入行程失败";
//        PageData pd = this.getPageData();
        Page page = getPage();
        try {
            String returnMsg = wXservice.saveJoinTrip(page);
            if(page.getPd().get("joinType").equals("1")) {
                joinOrCancel = "加入";
            }else if(page.getPd().get("joinType").equals("2")) {
                joinOrCancel = "取消";
            }
            if(returnMsg.equals("1")) {
                msg = joinOrCancel+"行程成功";
                result  = "success";

            }else if(returnMsg.equals("2")){
                msg = "限加入2个行程";
                result = "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = joinOrCancel+"行程失败";
            result = "fail";
        }
        map.put("result",result);
        map.put("msg",msg);
        return map;
    }

    /**
     * 根据条件获取行程列表

     * @return
     * @throws Exception
     */
    @RequestMapping("/getTripListByUserId")
    @ResponseBody
//    public Map<String,Object> getTripList(String pageIndx, String triptype, String startCity, String endCity, String startDate) {
    public Map<String,Object> getTripListByUserId(Page page) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();

        PageData pageData = this.getPageData();
        pageData.put("StartTime","");
        page.setPd(pageData);
        try {
            list = wXservice.getTripListByUserId(page);
            logger.info("查询行程列表成功");
        }catch (Exception e){
            logger.info("查询行程列表失败");
            e.printStackTrace();
        }
//        returnMap.put("outType",outType);
        int total = page.getTotalResult();
        returnMap.put("total",total);
        returnMap.put("rows",list);
        return returnMap;
    }
}
