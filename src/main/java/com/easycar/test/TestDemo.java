package com.easycar.test;

import com.easycar.dao.DaoSupport;

import com.easycar.service.common.CommonService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class TestDemo {

    public static void main(String[] args) {
//        testDatasource();

//        testUpdate();
    }

    public static void testDatasource() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/ApplicationContext.xml");
        DaoSupport daoSupport = (DaoSupport) context.getBean("daoSupport");
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("user_name","李白");
        try {
            Map<String,String> userMap = (Map<String, String>) daoSupport.findForObject("userMapper.getUserByNameAndPassword", map);
             daoSupport.findForObject("userMapper.userCount", "");
            System.out.println(daoSupport.findForObject("userMapper.userCount", ""));
//            System.out.println(userMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void testUpdate() {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/ApplicationContext.xml");
//        CommonService commonService = (CommonService) context.getBean("commonService");
//
//        commonService.updateStatus("ecu_trip_driver","IsEffective","2","SerialNo","20180415234318648");
//
//    }
}
