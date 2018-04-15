package com.easycar.service.common;

import com.easycar.dao.DaoSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service("commonService")
public class CommonService{
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    public Map<String,String> findByType(String type) {
        try {
            return (Map<String,String>)dao.findForObject("commonMapper.findByType",type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
