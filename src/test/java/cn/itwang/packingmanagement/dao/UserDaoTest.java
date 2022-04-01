package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;


@SpringBootTest
@RunWith(SpringRunner.class)
public class UserDaoTest {
    @Autowired
    private UserDao userDao;
    @Test
    public void add() {

        User user=new User(7,"wrwq","11333111","男");
        int add = userDao.add(user);
        System.out.println(add);
    }

    @Test
    public void  insert(){
        List<User> users = userDao.selectList(null);
        System.out.println(users);
        System.out.println(users);
    }
}