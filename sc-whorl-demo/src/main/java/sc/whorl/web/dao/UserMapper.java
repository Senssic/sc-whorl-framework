package sc.whorl.web.dao;


import org.springframework.stereotype.Repository;

import java.util.List;

import sc.whorl.system.common.ScMapper;
import sc.whorl.web.model.Users;


/**
 * 用户Mapper
 */
@Repository
public interface UserMapper extends ScMapper<Users> {
    public List<Users> mySeleteAll();
}
