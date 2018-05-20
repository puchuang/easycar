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
     * 获取该行程参与者的相关信息
     * @param serialNo
     * @return
     * @throws Exception
     */
    public List<Map<String,Object>> getJoinerListBySerialNo(String serialNo) throws Exception{
        return (List<Map<String,Object>>)dao.findForList("wxmapper.getJoinerListBySerialNo",serialNo);
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
            String validate = validateTrip(pd);
            if("".equals(validate)) {
                pd.put("SerialNo", DateUtil.getDateRandomCode());
                dao.save("wxmapper.insertTrip",pd);
                result = "1";
            }else{
                result = validate;
            }
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
            String appcode = pd.getString("appcode");
            Map<String,Object> loginMap = loginByCode(code,appcode);
            if(loginMap.containsKey("ret") && "success".equals(loginMap.get("ret")))  {//说明与微信通信成功，并取得了openid和sessionKey
                String openid = loginMap.get("openid").toString();
                String session_key = loginMap.get("session_key").toString();

                //根据openid查找用户，存在则从数据看取出user信息，不存在则插入数据
                try{
                    //调用工具类解析微信加密数据，得到明文信息(获取unionid并查询数据库中是否存在)
                    String iv = pd.getString("iv");
                    String encryptedData = pd.getString("encryptedData");

                    JSONObject userInfo = WeiXinUtil.getUserInfo(encryptedData, session_key, iv);
                    if(userInfo != null && !userInfo.isEmpty()) {
                        userMap.put("unionid",userInfo.get("unionId"));
                        Map<String, Object> localuserMap = findUserByUniouidOrUserid(userMap);//根据unionid查询用户是否已存在
                        if(localuserMap == null || "".equals(localuserMap.get("userId"))) {//用户不存在,保存用户信息
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
                        } else{//存在，则直接返回用户信息

                        result = "success";
                        msg = "登录成功";
                        returnMap.put("userInfo",localuserMap);
                    }
                    }else{
                        msg = "解析微信加密数据失败";
                        result = "fail";
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
     * 校验用户是否未发布过行程，避免重复发布（根据用户id和出发时间）
     * @param pd
     * @return false：已发布 true:未发布
     */
    public boolean trivalExist(PageData pd) throws Exception{
        Map<String,String> map = (Map<String,String>)dao.findForObject("wxmapper.trivalExist",pd);
        if(map == null || map.isEmpty()) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 查找用户今天发布消息的形成id集合（限制每天发布的数量）
     * @param pd
     * @return
     * @throws Exception
     */
    public List<Map<String,String>> findCountToday(PageData pd) throws Exception{
        return (List<Map<String,String>>)dao.findForList("wxmapper.findCountToday",pd);
    }

    /**
     * 查询用户或手机号码是否存在黑名单中
     * @param map
     * @return false:不在黑名单  true：在黑名单中
     * @throws Exception
     */
    public boolean isBlackList(Map<String,Object> map) throws Exception{
        List<Map<String,String>> forList = (List<Map<String,String>>)dao.findForList("wxmapper.isBlackList", map);
        if(forList == null || forList.size() == 0) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * 插入行程前进行必要的校验
     * @param pd
     * @return
     * @throws Exception
     */
    public String validateTrip(PageData pd) throws Exception{
        String result = "";
        boolean blackList = isBlackList(pd);
        if(blackList) {
            result = "5";
        }else{
            boolean b = trivalExist(pd);
            if(b) {
                List<Map<String, String>> countToday = findCountToday(pd);
                //限制每天发布信息的数量，每个用户每天发布不超过3条
                if(countToday != null && countToday.size() >= 3) {
                    result = "4";
                }
            }else{
                result = "2";
            }
        }
        return result;
    }

    /**
     * 加入行程
     * @param page
     * @return success：成功 fail:失败
     */
    public String saveJoinTrip(Page page) throws Exception{
        String result = "";
        PageData pd = page.getPd();
        if(pd.get("joinType").equals("1")) {//加入行程
            if(pd.containsKey("serialNo") && pd.containsKey("userId")) {//确定前端传回来的参数包含需要的数据
                String validate = validateJoinTrip(page);
                if("".equals(validate)) {
                    //查询是否加入过，加入过则更新为有效
                    Map<String,String> midMap = (Map<String,String>)dao.findForObject("wxmapper.isExists",pd);
                    if(midMap != null && !midMap.isEmpty()) {
                        pd.put("IsEffective","1");
                        dao.update("wxmapper.updateJoinTrip",pd);//仅更新为有效即可
                    }else{//没有加入过则保存记录
                        dao.save("wxmapper.saveJoinTrip",pd);//保存加入行程至中间表
                        dao.update("wxmapper.updateTripSeat",pd);
                    }
                    result = "1";
                }else{
                    result = validate;
                }
            }
        }else if(pd.get("joinType").equals("2")) {//取消行程
            pd.put("IsEffective","2");
            if(pd.containsKey("serialNo") && pd.containsKey("userId")) {//确定前端传回来的参数包含需要的数据
                    dao.update("wxmapper.updateJoinTrip",pd);//已加入的行程更新为无效
                    dao.update("wxmapper.updateTripSeat",pd);
                    result = "1";
            }else{
                result = "4";
            }
        }
        return result;
    }

    /**
     * 根据用户id获取以前加入的行程（按出发时间倒叙排列）
     * @param page
     * @return
     * @throws Exception
     */
    public List<Map<String,String>> getTripListByUserId(Page page) throws Exception {

        return (List<Map<String,String>>)dao.findForList("wxmapper.getUserTriplistPage",page);
    }

    /**
     * 加入行程前校验是否已加入当天出发的其他行程
     * @param page
     * @return true：加入的当天其他行程数量小于2，可以继续加入，否则  false 大于等于2则不允许加入
     */
    public String validateJoinTrip(Page page) throws Exception{
//        boolean result  = false;
        String result = "";
        PageData pd = page.getPd();
        Map<String, Object> serialNoMap = getTripBySerialNo(pd.getString("serialNo"));
        int joinNumber = 1 ;
        int SeatRemain = Integer.parseInt(serialNoMap.get("SeatRemain").toString());
        //判断如果剩余座位小于要加入的人数，则返回 3  剩余座位不足
        if(SeatRemain < joinNumber) {
            result = "3";
        }else{
            pd.put("StartTime",serialNoMap.get("StartTime"));
            page.setPd(pd);
            List<Map<String, String>> tripList = getTripListByUserId(page);
            if(tripList != null && tripList.size() >= 2) {
                result = "2";
            }else{
                result = "";
            }
        }

        return result;
    }
}
