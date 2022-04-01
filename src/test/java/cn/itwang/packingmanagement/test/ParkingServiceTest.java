package cn.itwang.packingmanagement.test;

import cn.itwang.packingmanagement.dao.ParkingDao;
import cn.itwang.packingmanagement.domain.Parking;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ParkingServiceTest {

    @Autowired
    private ParkingDao parkingDao;
    @Test
    public void queryParkingInformation(){
        String parkingName="沈阳工业大学辽阳分校停车场";
        int currentPage=1;
        int pageSize=2;
        LambdaQueryWrapper<Parking> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(parkingName), Parking::getParkingName, parkingName);
        IPage<Parking> page = new Page(currentPage, pageSize);
        page = parkingDao.selectPage(page, lqw);
    }
}
