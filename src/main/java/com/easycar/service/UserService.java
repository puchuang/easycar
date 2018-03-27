package com.easycar.service;

import com.easycar.dao.DaoSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service("userService")
public class UserService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    public Map<String,String> getUser(Map<String,Object> map) throws Exception{
        return (Map<String,String>)dao.findForObject("userMapper.getUserByNameAndPassword",map);
    }

    public Map<String,String> findUserByNameAndPassword(Map<String,String> map) throws Exception{
        return (Map<String,String>)dao.findForObject("userMapper.findUserByNameAndPassword",map);
    }
}
