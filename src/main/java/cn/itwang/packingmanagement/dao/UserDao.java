package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface UserDao  extends BaseMapper<User>{
    public int add(User user);
}
