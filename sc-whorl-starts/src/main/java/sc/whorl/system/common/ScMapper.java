package sc.whorl.system.common;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;


public interface ScMapper<T> extends Mapper<T>, MySqlMapper<T> {

}
