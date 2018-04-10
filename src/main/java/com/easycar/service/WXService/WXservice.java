package com.easycar.service.WXService;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easycar.dao.DaoSupport;
import com.easycar.util.HttpRequestUtil;
import com.easycar.util.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("wxService")
public class WXservice {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    private Logger logger;

    public List<Map<String,String>> getTripListByCondition(Map<String, Object> map) throws Exception {
        List<Map<String,String>> tripList = new ArrayList<Map<String, String>>();
        /*if(map != null && !"".equals(map.get("triptype"))) {
            if("1".equals(map.get("triptype"))) {//1:车找人
                tripList= (List<Map<String,String>>)dao.findForObject("wxmapper.getDriverTripListByCondition",map);
            }else if("0".equals(map.get("triptype"))) {//0:人找车
                tripList= (List<Map<String,String>>)dao.findForObject("wxmapper.getCustomerTripListByCondition",map);
            }else{//否则证明参数非法，直接返回null
                tripList = null;
            }

        }*/
        tripList= (List<Map<String,String>>)dao.findForList("wxmapper.getDriverTripListByCondition",map);
        return tripList;
    }

    public Integer getCount(Map<String, Object> map) throws Exception{
        return (Integer)dao.findForObject("wxmapper.getCount",map);
    }
    /**
     * 根据流水号查找行程详情
     * @param serialNo
     * @return
     * @throws Exception
     */
    public Map<String,Object> getTripBySerialNo(String serialNo) throws Exception {
        return (Map<String,Object>)dao.findForObject("wxmapper.getTripBySerialNo",serialNo);
    }

    /**
     * 插入行程信息(单条)
     * @param openid
     * @param tripData
     * @return
     */
    public String insertTrip(String openid, String tripData) throws Exception {
        String result="";
        JSONObject json = JSON.parseObject(tripData);
        Map<String,Object> map = new HashMap<String,Object>();
        if(json != null && !json.isEmpty()) {
            map.put("LinkmanName",json.get("LinkmanName"));
            map.put("LinkmanId",json.get("LinkmanId"));
            map.put("Phone",json.get("Phone"));
            map.put("StartCityId",json.get("StartCityId"));
            map.put("StartStation",json.get("StartStation"));
            map.put("EndCityId",json.get("EndCityId"));
            map.put("Destination",json.get("Destination"));
            map.put("Byway",json.get("Byway"));
            map.put("CartBrandId",json.get("CartBrandId"));
            map.put("CarModelId",json.get("CarModelId"));
            map.put("CarNumber",json.get("CarNumber"));
            map.put("SeatTotal",json.get("SeatTotal"));
            map.put("StartTime",json.get("StartTime"));
            map.put("Top",json.get("Top"));
            map.put("Remark",json.get("Remark"));
            map.put("SeatRemain",json.get("SeatRemain"));

            dao.save("wxmapper.insertTrip",map);
            result = "1";
        }else {
            result = "3";
        }
        return result;
    }

    /**
     * 本系统登陆方法（保存微信返回的openid）
     * @param reqJson
     * @return
     */
    public Map<String,Object> systemLogin(String reqJson) {
        Map<String,Object> returnMap = new HashMap<String, Object>();//返回数据
        Map<String,String> userMap = new HashMap<String, String>();//保存微信返回的用户信息
        String result="fail";
        String msg="";
        if(StringUtils.isEmpty(reqJson)) {
            msg="reqJson为空，请检查后重试";
        }else{
            JSONObject json = JSON.parseObject(reqJson);
            String code = json.getString("code");
            String userInfo = json.getString("userInfo");
            if(!StringUtils.isEmpty(userInfo)) {
                JSONObject userJson = JSON.parseObject(userInfo);
                userMap.put("nickName",userJson.get("nickName").toString());
                userMap.put("gender",userJson.get("gender").toString());
                userMap.put("avatarUrl",userJson.get("avatarUrl").toString());
                userMap.put("city",userJson.get("city").toString());
                userMap.put("province",userJson.get("province").toString());
                userMap.put("country",userJson.get("country").toString());
            }
            if(StringUtils.isEmpty(reqJson)) {
                msg="code为空，请检查后重试";
            }else{
                Map<String, Object> resultMap = loginByCode(code);
                String resultValue=resultMap.get("ret").toString();
                if("success".equals(resultValue)) {//查询成功，处理openid和session_key
                    String openid=resultMap.get("openid").toString();
                    String session_key=resultMap.get("session_key").toString();
                    try {
                        Map<String, Object> localuserMap = findUserByOpenid(openid);//查询用户是否已存在
                        if(localuserMap == null || "".equals(localuserMap.get("userId"))) {//用户不存在
                            //执行插入操作

                        }else{
                            //执行更新操作(每次执行登录操作  session_key一定需要更新)
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return returnMap;
    }

    /**
     * 向微信发起请求并登陆得到openid
     * @param code
     * @return
     */
    public Map<String,Object> loginByCode(String code) {
        String ret="fail";
        String msg="";

        String appid="wx68cce42499e45048";
        String secret = "f6d0f0fc9b52b9b6c27261167dfe083b";
        String grant_type = "authorization_code";
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        String param = "appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type="+grant_type;
        Map<String,Object> map = new HashMap<String,Object>();

        //向微信发起请求，获取openid
        logger.info("向微信发起请求，地址及参数为："+url+"?"+param);
        String result = HttpRequestUtil.sendGet(url, param);
        logger.info("微信返回结果："+result);
        /*  微信返回示例
             //正常返回的JSON数据包
            {
                  "openid": "OPENID",
                  "session_key": "SESSIONKEY",
            }

            //满足UnionID返回条件时，返回的JSON数据包
            {
                "openid": "OPENID",
                "session_key": "SESSIONKEY",
                "unionid": "UNIONID"
            }
            //错误时返回JSON数据包(示例为Code无效)
            {
                "errcode": 40029,
                "errmsg": "invalid code"
            }
         */
        if(result != null && "".equals(result)) {//说明有返回
            JSONObject jsonObject = JSON.parseObject(result);
            if(jsonObject.containsKey("errcode")) {//说明有错误信息
                ret="fail";
                msg=result;
            }else if(jsonObject.containsKey("openid")) {
                ret="success";
                msg="成功返回openid和session_key";
                map.put("openid",jsonObject.get("openid"));
                map.put("session_key",jsonObject.get("session_key"));
            }else{
                ret="fail";
                msg="未知错误";
            }
            map.put("ret",ret);
            map.put("msg",msg);
        }
        return map;
    }

    public Map<String,Object> findUserByOpenid(String openid) throws Exception{
        return(Map<String,Object>)dao.findForObject("wxmapper.findUserByOpenid",openid);
    }
}
