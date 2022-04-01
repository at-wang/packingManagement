package cn.itwang.packingmanagement.test;


import cn.itwang.packingmanagement.dao.AdminDao;
import cn.itwang.packingmanagement.utils.MD5Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class md5Test {

    @Test
    public void test(){
        String s = MD5Utils.md5("184312110");
        System.out.println(s);
    }

}
