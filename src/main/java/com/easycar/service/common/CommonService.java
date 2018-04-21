package com.easycar.service.common;

import com.easycar.dao.DaoSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("commonService")
public class CommonService{
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 更新单个状态的公用方法
     * @param tableName
     * @param statusName
     * @param resultCode
     * @return
     */
    public boolean updateStatus(String tableName,String statusName,String resultCode,String queryColumn,String queryLimit) {
        boolean result = false;
        Map<String,String> paramMap = new HashMap<String, String>();
        paramMap.put("tableName",tableName);
        paramMap.put("statusName",statusName);
        paramMap.put("statusResult",resultCode);
        paramMap.put("queryColumn",queryColumn);
        paramMap.put("queryLimit",queryLimit);

        try {
            dao.update("commonMapper.updateStatus",paramMap);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 根据条件删除记录的公用方法
     * @param tableName
     * @param queryColumn
     * @param queryLimit
     * @return
     */
    public boolean deleteByCondition(String tableName,String queryColumn,String queryLimit) {
        boolean result = false;
        Map<String,String> paramMap = new HashMap<String, String>();
        paramMap.put("tableName",tableName);
        paramMap.put("queryColumn",queryColumn);
        paramMap.put("queryLimit",queryLimit);

        try {
            dao.delete("commonMapper.deleteByCondition",paramMap);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
