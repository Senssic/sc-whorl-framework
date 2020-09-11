package com.whorl.web.controller;


import com.whorl.web.dao.UserMapper;
import com.whorl.web.model.Users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.common.RT;

@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "查询用户信息")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/all")
    @ApiOperation("fasdfasdf")
    public RT<List<Users>> restPage(@RequestBody Users username) {
        return RT.success().setResult(userMapper.mySeleteAll());
    }


}
