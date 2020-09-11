package sc.whorl.web.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.common.RT;
import sc.whorl.web.dao.UserMapper;
import sc.whorl.web.model.Users;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/all")
    public RT<List<Users>> restPage() {
        return RT.success().setResult(userMapper.mySeleteAll());
    }


}
