package com.baiyajin.systemuser.controller;

import com.baiyajin.systemuser.entity.SystemUser;
import com.baiyajin.systemuser.service.SystemUserInterface;
import com.baiyajin.systemuser.util.ConversionBetweenObjectsAndMaps;
import com.baiyajin.systemuser.util.HashSalt;
import com.baiyajin.systemuser.util.PhoneUtils;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/SystemUserController")
public class SystemUserController {

    @Autowired
    private SystemUserInterface systemUserInterface;




    /*登录*/
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = Exception.class)
    @ResponseBody
    public List<Map<String,Object>> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String,Object> map) {

        Map<String,Object> m = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();


        try {

            if(null == map.get("password") && "" == map.get("password")){
                m.put("return_message","密码不能为空");
                list.add(m);
                return list;
            }

            if(null != map.get("phone") && "" != map.get("phone") && PhoneUtils.isPhone(map.get("phone").toString())){

                String salt = HashSalt.encode(Long.parseLong(map.get("phone").toString()));
                String hashSalt = HashSalt.getMD5(salt);
                String ecPassWord = new SimpleHash("SHA-1", map.get("password").toString(), hashSalt).toString();



                /*通过手机去数据库拿密码作比对*/
                map.remove("password");
                List<SystemUser> systemUsers = systemUserInterface.selectByMap(map);

                if(systemUsers.size() > 0 && systemUsers.get(0).getPassword().equals(ecPassWord)){
                    Map<String, Object> stringObjectMap = ConversionBetweenObjectsAndMaps.objectToMap(systemUsers);
                    list.add(stringObjectMap);
                    return list;
                }else if(systemUsers.size() == 0){
                    m.put("return_message","此用户名/手机号还没注册");
                    list.add(m);
                    return list;
                }else if(!systemUsers.get(0).getPassword().equals(ecPassWord)){
                    m.put("return_message","密码错误");
                    list.add(m);
                    return list;
                }
            }

            m.put("return_message","手机号格式不对");
            list.add(m);
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            m.put("return_message",e.getMessage());
            list.add(m);
            return list;
        }
    }









}



