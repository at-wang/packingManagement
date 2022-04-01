package cn.itwang.packingmanagement;

import cn.itwang.packingmanagement.utils.QiniuUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PackingManagementApplicationTests {

    @Test
    void contextLoads() {
      //  QiniuUtils.upload2Qiniu("d:\\images\\a.jpeg","哈哈哈");
        System.out.println("成功");
    }

}
