package cn.itwang.packingmanagement.test;

import cn.itwang.packingmanagement.utils.QiniuUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class qiniuTest {


    /**
     * 上传图片到七牛云
     */
    @Test
    public void testUpload(){
        QiniuUtils.upload2Qiniu("d:\\images\\a.jpeg","哈哈哈");
        System.out.println("成功");
    }
}
