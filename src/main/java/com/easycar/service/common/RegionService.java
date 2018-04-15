package com.easycar.service.common;

import com.easycar.dao.DaoSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("regionService")
public class RegionService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    public List<Map<String,Object>> getRegionsByType(String codeSelect) throws Exception{
        return (List<Map<String,Object>>)dao.findForList("commonMapper.getRegionsByType",codeSelect);
    }
}
