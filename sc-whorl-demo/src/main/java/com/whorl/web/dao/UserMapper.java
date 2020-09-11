package com.whorl.web.dao;


import com.whorl.web.model.Users;

import org.springframework.stereotype.Repository;

import java.util.List;

import sc.whorl.system.common.ScMapper;


/**
 * 用户Mapper
 */
@Repository
public interface UserMapper extends ScMapper<Users> {
    public List<Users> mySeleteAll();
}
