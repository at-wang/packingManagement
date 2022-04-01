package cn.itwang.packingmanagement.service.impl;


import cn.itwang.packingmanagement.dao.AdminDao;
import cn.itwang.packingmanagement.domain.Admin;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.AdminService;
import cn.itwang.packingmanagement.utils.MD5Utils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminDao, Admin> implements AdminService {

    @Autowired
    private AdminDao adminDao;

    /**
     * 登录方法
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public Result login(String username, String password) {
        //lqw条件查询
        LambdaQueryWrapper<Admin> lqw = new LambdaQueryWrapper<>();
        lqw.eq(username.length() > 0, Admin::getUsername, username);
        List<Admin> users = adminDao.selectList(lqw);//查询用户
        if (users.size() == 0) {//如果用户为空则登录失败
            return new Result(false, "用户名不存在，登录失败", null);
        } else {
            String md5Password = MD5Utils.md5(password);//MD5工具类加密密码
            if (md5Password.equals(users.get(0).getPassword())) {
                //如果登录成功，则需要生成令牌token
                //使用jwt规则生产token字符串
                JwtBuilder builder = Jwts.builder();
                HashMap<String, Object> map = new HashMap<>();
                map.put("key1", "value");
                map.put("key2", "value");
                String token = builder.setSubject(username)
                        .setClaims(map)//map中可以存放用户的权限信息
                        .setId(users.get(0).getId() + "")//设置用户id为token
                        .setIssuedAt(new Date()) //设置token生成的时间
                        .setExpiration(new Date(System.currentTimeMillis() + 30 * 1000))//设置token过期时间
                        .signWith(SignatureAlgorithm.HS256, "wang123666")//设置加密方式和密码
                        .compact();
                System.out.println(token);
                return new Result(true, token, users.get(0));
            } else {//密码错误
                return new Result(false, "登录失败，用户名或密码错误", null);
            }
        }
    }

    /**
     * 修改密码
     *
     * @param id
     * @param password
     * @return
     */
    @Override
    public Result modifyPassword(Integer id, String password) {
        if (password.length() > 5 && password.length() < 11) {
            Admin admin = adminDao.selectById(id);
            String md5Password = MD5Utils.md5(password);
            admin.setPassword(md5Password);
            int update = adminDao.updateById(admin);
            if (update > 0) {
                return new Result(true, "修改成功");
            } else {
                return new Result(false, "修改失败");
            }
        } else {
            return new Result(false, "请输入密码为5到11位");
        }

    }

}
