package com.easycar.service.WXService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easycar.dao.DaoSupport;
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

    public List<Map<String,String>> getTripListByCondition(Map<String, Object> map) throws Exception {
        List<Map<String,String>> tripList = new ArrayList<Map<String, String>>();
        //根据是人找车还是车找人查询不同的列表（方便后续管理）
        if(map != null && !"".equals(map.get("triptype"))) {
            if("1".equals(map.get("triptype"))) {//1:车找人
                tripList= (List<Map<String,String>>)dao.findForObject("wxmapper.getDriverTripListByCondition",map);
            }else if("0".equals(map.get("triptype"))) {//0:人找车
                tripList= (List<Map<String,String>>)dao.findForObject("wxmapper.getCustomerTripListByCondition",map);
            }else{//否则证明参数非法，直接返回null
                tripList = null;
            }

        }
        return tripList;
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
}
