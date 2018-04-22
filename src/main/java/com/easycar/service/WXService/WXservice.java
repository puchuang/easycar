package com.easycar.service.WXService;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easycar.dao.DaoSupport;
import com.easycar.entity.Page;
import com.easycar.service.common.RegionService;
import com.easycar.util.*;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("wxService")
public class WXservice {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    
    @Resource(name = "regionService")
    private RegionService regionService;

    private Logger logger;

    public List<Map<String,String>> getTripListByCondition(Page page) throws Exception {

        return (List<Map<String,String>>)dao.findForList("wxmapper.getDriverTriplistPage",page);
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
     * @param pd
     * @return
     */
    public String insertTrip(PageData pd) throws Exception {
        String result="";
        Map<String,Object> map = new HashMap<String,Object>();
        if(!pd.isEmpty()) {
            String StartCityName = "";
            String EndCityName = "";
            //根据区域id查找区域名称(小程序端暂时不适用cityid，所以需要判断并给出默认值)
            if(pd.containsKey("StartCityId") && !"".equals(pd.getString("StartCityId"))) {
                StartCityName = regionService.getRegionNameById(pd.getString("StartCityId"));

            }else{
                pd.put("StartCityId",null);
            }
            if(pd.containsKey("EndCityId") && !"".equals(pd.getString("EndCityId"))) {
                EndCityName = regionService.getRegionNameById(pd.getString("EndCityId"));
            }else{
                pd.put("EndCityId",null);
            }
            pd.put("StartCityName",StartCityName);
            pd.put("EndCityName",EndCityName);

            if(pd.getString("UserId") == null) {
                pd.put("UserId","");
            }
            if(pd.containsKey("Price") && pd.getString("Price").equals("")) {
                pd.put("Price","0");
            }

            if(!pd.containsKey("Top")) {
                pd.put("Top","0");
            }
            //小程序回传会StartDate和StartTime两个字段表示出发时间，而web端只会传startTime
            if(pd.containsKey("StartDate") && !"".equals(pd.getString("StartDate"))) {
                //拼装数据库需要的starttime的格式
                String StartTime = pd.getString("StartDate")+" "+pd.getString("StartTime")+":00";
                pd.put("StartTime",StartTime);
            }
            pd.put("SerialNo", DateUtil.getDateRandomCode());
            dao.save("wxmapper.insertTrip",pd);
            result = "1";
        }else {
            result = "3";
        }
        return result;
    }

    /**
     * 本系统登陆方法（保存微信返回的openid）
     * @param pd
     * @return
     */
    public Map<String,Object> systemLogin(PageData pd) {
        Map<String,Object> returnMap = new HashMap<String, Object>();//返回数据
        Map<String,Object> userMap = new HashMap<String, Object>();//保存微信返回的用户信息
        String result="fail";
        String msg="";
        String userId = "";
        if(pd.isEmpty()) {
            msg="请求数据为空，请检查后重试";
        }else{
            String code = pd.getString("code");
            Map<String,Object> loginMap = loginByCode(code);
            if(loginMap.containsKey("ret") && "success".equals(loginMap.get("ret")))  {//说明与微信通信成功，并取得了openid和sessionKey
                String openid = loginMap.get("openid").toString();
                String session_key = loginMap.get("session_key").toString();

                //根据openid查找用户，存在则从数据看取出user信息，不存在则插入数据
                try{
                    Map<String, Object> localuserMap = findUserByOpenid(openid);//根据openid查询用户是否已存在
                    if(localuserMap == null || "".equals(localuserMap.get("userId"))) {//用户不存在
                        //调用工具类解析微信加密数据，得到明文信息
                        String iv = pd.getString("iv");
                        String encryptedData = pd.getString("encryptedData");

                        JSONObject userInfo = WeiXinUtil.getUserInfo(encryptedData, session_key, iv);
                        if(userInfo != null && !userInfo.isEmpty()) {//判断返回数据不为空，则微信解析成功，查看用户是否存在，存在则更新，不存在则保存
                            userMap.put("openId",userInfo.get("openId"));//encryptedData这种加密信息中有openid等敏感信息
                            userMap.put("nickName",userInfo.get("nickName"));
                            userMap.put("gender",userInfo.get("gender"));
                            userMap.put("avatarUrl",userInfo.get("avatarUrl"));
                            userMap.put("city",userInfo.get("city"));
                            userMap.put("province",userInfo.get("province"));
                            userMap.put("country",userInfo.get("country"));
                            //执行插入操作
                            userId =  UuidUtil.get32UUID();
                            userMap.put("userId",userId);
                            dao.save("wxmapper.saveUserInfo",userMap);

                            userMap.remove("openId");//给前端发送信息时不能携带openId
                            returnMap.put("userInfo",userMap);
                            result = "success";
                            msg = "保存微信用户信息成功";
//                            logger.info("保存微信用户信息成功，用户昵称："+userMap.get("nickname"));
                    }else{
                        msg = "解析微信加密数据失败";
                        result = "fail";
                    }
                }else{
                        result = "success";
                        msg = "登录成功";
                        returnMap.put("userInfo",localuserMap);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                returnMap.put("sk",session_key);
            }else{
                msg = "访问微信失败";
                result = "fail";
            }
        }
        returnMap.put("result",result);
        returnMap.put("msg",msg);
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
//        logger.info("向微信发起请求，地址及参数为："+url+"?"+param);
        String result = HttpRequestUtil.sendGet(url, param);
//        logger.info("微信返回结果："+result);
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
        if(result != null && !"".equals(result)) {//说明有返回
            JSONObject jsonObject = JSON.parseObject(result);
            if(jsonObject.containsKey("errcode")) {//说明有错误信息
                ret="fail";
                msg=result;
            }else if(jsonObject.containsKey("openid")) {
                ret="success";
                msg="成功返回openid和session_key";
                map.put("openid",jsonObject.get("openid"));
                map.put("session_key",jsonObject.get("session_key"));
//                map.put("unionid",jsonObject.get("unionid"));
            }else{
                ret="fail";
                msg="未知错误";
            }

            map.put("ret",ret);
            map.put("msg",msg);
        }
        return map;
    }

    /**
     * 根据openid查找用户信息
     * @param openid
     * @return
     * @throws Exception
     */
    public Map<String,Object> findUserByOpenid(String openid) throws Exception{
        return(Map<String,Object>)dao.findForObject("wxmapper.findUserByOpenid",openid);
    }
}
