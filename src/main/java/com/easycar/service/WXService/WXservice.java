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
                StartCityName = pd.getString("StartStation").substring(0,2);//没有出发城市id时，默认取出发地的前两个字
                pd.put("StartCityId",null);
            }
            if(pd.containsKey("EndCityId") && !"".equals(pd.getString("EndCityId"))) {
                EndCityName = regionService.getRegionNameById(pd.getString("EndCityId"));
            }else{
                EndCityName = pd.getString("Destination").substring(0,2);
                pd.put("EndCityId",null);
            }

            if((pd.containsKey("CartBrand") && pd.get("CartBrand") == null) || !pd.containsKey("CartBrand")) {
                pd.put("CartBrand","");//如果CartBrand为null则存为空字符串
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
    public Map<String,Object> systemLogin(PageData pd) throws Exception{
        Map<String,Object> returnMap = new HashMap<String, Object>();//返回数据
        Map<String,Object> userMap = new HashMap<String, Object>();//保存微信返回的用户信息
        String result="fail";
        String msg="";
        String userId = "";
        String appcode="";
        if(pd.isEmpty()) {
            msg="请求数据为空，请检查后重试";
        }else if(pd.containsKey("code")){//当前逻辑，有code证明进入小程序但未授权，有userId则证明用户授权成功
            String code = pd.getString("code");
            if(pd.containsKey("appcode")) {
                appcode = pd.getString("appcode");
            }else{
                msg = "appcode为空";
            }
            Map<String,Object> loginMap = loginByCode(code,appcode);
            if(loginMap.containsKey("ret") && "success".equals(loginMap.get("ret")))  {//说明与微信通信成功，并取得了openid和sessionKey
                String openid = loginMap.get("openid").toString();
//                String unionid = loginMap.get("unionid").toString();
                String session_key = loginMap.get("session_key").toString();

                    //根据openid查找用户，存在则从数据看取出user信息，不存在则插入数据
                    userMap.put("openid",openid);
                    Map<String, Object> localuserMap = findUserByUniouidOrUserid(userMap);//根据openid查询用户是否已存在
                    //如果没有传过来用户信息encryptedData且没有保存过次用户，则直接保存openid并生产userid
                    if(!pd.containsKey("encryptedData") && localuserMap == null) {

                        userMap.put("openId",openid);
                        userId =  UuidUtil.get32UUID();
                        userMap.put("userId",userId);

                        //其他项默认为空
                        userMap.put("nickName","");
                        userMap.put("gender",0);
                        userMap.put("avatarUrl","");
                        userMap.put("city","");
                        userMap.put("province","");
                        userMap.put("country","");
                        userMap.put("unionid","");
                        dao.save("wxmapper.saveUserInfo",userMap);
                    }else{//否则，即包含了用户信息，则查询用户是否存在且不存在nickName字段，证明没有保存用户具体信息，则更新，存在nickName字段则不更新，不存在用户信息则插入
                        if(localuserMap != null && !localuserMap.get("openid").equals("") && !localuserMap.get("nickName").equals("")){//存在unionid和nickName，已保存用户完整信息则无需执行操作
                            result = "success";
                            msg = "登录成功";
                            returnMap.put("userInfo",localuserMap);
                        } else if(localuserMap != null && !localuserMap.get("openid").equals("") && localuserMap.get("nickName").equals("")) {//存在unionid，但不存在nickName，则更新用户详细信息
                            pd.put("session_key",session_key);
                            Map<String, Object> weixinMap = transferPdToMap(pd);
                            if(weixinMap != null && weixinMap.get("result").equals("success")) {//解析成功
                                userMap.putAll(weixinMap);
                                userMap.put("openid",localuserMap.get("openid"));
                                //执行更新操作
                                dao.update("wxmapper.updateUserInfo",userMap);

                                userMap.remove("openId");//给前端发送信息时不能携带openId
                                returnMap.put("userInfo",userMap);
                                result = "success";
                                msg = "更新微信用户信息成功";
                            }else{
                                result = "fail";
                                msg = "更新微信用户信息失败";
                            }
                        }/*else if(localuserMap == null || "".equals(localuserMap.get("userId"))) {//用户不存在
                            pd.put("session_key",session_key);
                            Map<String, Object> weixinMap = transferPdToMap(pd);
                            if(weixinMap != null && weixinMap.get("result").equals("success")) {//解析成功
                                userMap.putAll(weixinMap);
                                userMap.put("unionid",localuserMap.get("unionid"));
                                //执行插入操作
                                userId =  UuidUtil.get32UUID();
                                userMap.put("userId",userId);
                                dao.save("wxmapper.saveUserInfo",userMap);

                                userMap.remove("openId");//给前端发送信息时不能携带openId
                                returnMap.put("userInfo",userMap);
                                result = "success";
                                msg = "保存微信用户信息成功";
                            }else{
                                result = "fail";
                                msg = "保存微信用户信息失败";
                            }
                        }*/
                    }
                returnMap.put("sk",session_key);
            }else{
                msg = "访问微信失败";
                result = "fail";
            }
        }else if(pd.containsKey("userId")){//说明用户授权成功，更新用户信息
            userId = pd.getString("userId");
            //根据userId查找用户，存在则从数据看取出user信息，不存在则插入数据
            userMap.put("userId",userId);
            Map<String, Object> localuserMap = findUserByUniouidOrUserid(userMap);//根据userId查询用户是否已存在
            if(localuserMap == null) {
                msg = "不存在此用户";
                result = "fail";
            }else{
                Map<String, Object> weixinMap = transferPdToMap(pd);
                if(weixinMap != null && weixinMap.get("result").equals("success")) {//解析成功
                    userMap.putAll(weixinMap);
                    //执行更新操作
                    dao.update("wxmapper.updateUserInfo",userMap);

                    userMap.remove("openId");//给前端发送信息时不能携带openId
                    returnMap.put("userInfo",userMap);
                    result = "success";
                    msg = "更新微信用户信息成功";
                }else{
                    result = "fail";
                    msg = "更新微信用户信息失败";
                }
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
    public Map<String,Object> loginByCode(String code,String appcode) {
        String ret="fail";
        String msg="";
        String appid="";
        String secret = "";
        if("easycar".equalsIgnoreCase(appcode)) {
            appid="wx5172ed59fb860071";
            secret = "16e4d2579058842ba22e50be7739bad2";
        }else if("pujingquan".equalsIgnoreCase(appcode)){
            appid="wxa75c23f5dd298888";
            secret = "e191252c125472b789a0145c634dee08";
        }

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
     * 根据unionid或者UserId查找用户信息
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String,Object> findUserByUniouidOrUserid(Map<String,Object> map) throws Exception{
        return(Map<String,Object>)dao.findForObject("wxmapper.findUserByOpenid",map);
    }

    /**
     * 得到pd中的信息，调用微信解析方法，并封装用户的map
     * @param pd
     * @return
     */
    public Map<String,Object> transferPdToMap(PageData pd){
        Map<String,Object> resulrMap = new HashMap<String, Object>();
        String msg = "";
        String result = "";
        if(pd == null) {
            resulrMap = null;
            result = "fail";
            msg= "pd中没有参数，无法解析，请检查";

        }else if(pd.containsKey("encryptedData") && pd.containsKey("iv")) {
            String iv = pd.getString("iv");
            String encryptedData = pd.getString("encryptedData");

            JSONObject userInfo = WeiXinUtil.getUserInfo(encryptedData, pd.getString("session_key"), iv);
            if(userInfo != null && !userInfo.isEmpty()) {//判断返回数据不为空，则微信解析成功，查看用户是否存在，存在则更新，不存在则保存
                resulrMap.put("openId",userInfo.get("openId"));//encryptedData这种加密信息中有openid等敏感信息
                resulrMap.put("nickName",userInfo.get("nickName"));
                resulrMap.put("gender",userInfo.get("gender"));
                resulrMap.put("avatarUrl",userInfo.get("avatarUrl"));
                resulrMap.put("city",userInfo.get("city"));
                resulrMap.put("province",userInfo.get("province"));
                resulrMap.put("country",userInfo.get("country"));
                resulrMap.put("unionid",userInfo.get("unionId"));//unionid从解析的用户信息中获取

                result = "success";
                msg = "解析并封装微信用户信息成功";
//                            logger.info("保存微信用户信息成功，用户昵称："+userMap.get("nickname"));
            }else{
                result = "fail";
                msg = "解析微信加密数据失败";
            }
        }
        resulrMap.put("msg",msg);
        resulrMap.put("result",result);
        return  resulrMap;
    }
}
