package com.easycar.controller;

import com.easycar.service.common.RegionService;
import com.easycar.util.PageData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("*/region")
public class RegionController extends BaseController {

    @Resource(name = "regionService")
    private RegionService regionService;

    @RequestMapping("/getCities")
    @ResponseBody
    public List<Map<String,Object>> getCity() {
        List<Map<String,Object>> returnMap = new ArrayList<Map<String, Object>>();
        PageData pageData = this.getPageData();
        String codeSelect = pageData.getString("codeSelect");
        if(codeSelect != null && !"".equals(codeSelect)) {
            try{
                returnMap = regionService.getRegionsByType(codeSelect);
                logger.info("查询成功，查询结果："+returnMap.toString());
            }catch (Exception e){
                returnMap = null;
                e.printStackTrace();
                logger.info("查询失败，发生异常");
            }
        }else{
            returnMap = null;
        }
        return returnMap;
    }

}
