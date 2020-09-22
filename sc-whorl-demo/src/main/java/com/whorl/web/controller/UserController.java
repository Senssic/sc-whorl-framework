package com.whorl.web.controller;


import com.google.common.collect.Sets;

import com.sc.whorl.authorization.annotate.WhorlAuthorize;
import com.sc.whorl.authorization.auth.EmbedUser;
import com.sc.whorl.authorization.utils.SecureUtil;
import com.sc.whorl.system.common.RT;
import com.whorl.web.dao.UserMapper;
import com.whorl.web.model.Users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "查询用户信息")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/all")
    @ApiOperation("all")
    @WhorlAuthorize(value = "@auth.hasRole('ADMIN')")
    public RT<List<Users>> restPage(@RequestBody Users username) {
        return RT.success().setResult(userMapper.mySeleteAll());
    }


    @GetMapping("/login")
    @ApiOperation("login")
    public RT<String> restPage() {
        EmbedUser embedUser = new EmbedUser();
        embedUser.setUserId(1001L);
        embedUser.setLoginName("TESTADMIN");
        embedUser.setLoginTime(new Date());
        embedUser.setRoles(Sets.newHashSet("ADMIN"));
        embedUser.setPermissions(Sets.newHashSet("USER"));
        return RT.success().setResult(SecureUtil.getJwtToken(embedUser));
    }


}
