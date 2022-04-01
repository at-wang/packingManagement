package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public interface UserService extends IService<User> {
    //查询用户信息
    public Result queryUserInformation(String userName,int currentPage,int pageSize);
    //添加用户信息
    public Result addUserInformation(User user) throws IOException;
    //修改用户信息
    public Result modifyUserInformation(User user) throws IOException;
    //删除用户信息
    public Result removeUserInformation(Integer userId) throws IOException;
    //导出数据
    public void exportUser(HttpServletResponse response) throws IOException;
    //自动补全
    public Result queryUserCompletion(String prefix) throws IOException;
    //查询男女数量
    public Result queryUserSexCount(String sex);
}
