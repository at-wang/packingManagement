package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.Admin;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;


public interface AdminService extends IService<Admin> {
    //登录
    public Result login(String username,String password);
    //根据id修改密码
    public Result modifyPassword(Integer id,String password);
}
