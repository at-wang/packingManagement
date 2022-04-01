package cn.itwang.packingmanagement.test;

import cn.itwang.packingmanagement.dao.CarDao;
import cn.itwang.packingmanagement.dao.ParkingCardDao;
import cn.itwang.packingmanagement.dao.UserDao;
import cn.itwang.packingmanagement.domain.Car;
import cn.itwang.packingmanagement.domain.CarVO;
import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.CarService;
import cn.itwang.packingmanagement.service.ParkingCardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CarServiceTest {

    @Autowired
    private CarDao carDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CarService carService;

    @Autowired
    private ParkingCardService parkingCardService;

    @Test
    public void queryCarInformation(){
        String licencePlate="京";
        List<CarVO> carVOS = carDao.selectCarInformation(licencePlate, 0, 10);
        System.out.println(carVOS);
    }

    @Test
    public void queryCarCount(){
        String licencePlate="京";
        long l = carDao.selectCarCount(licencePlate);
        System.out.println(l);
    }

    @Test
    public void selectPoi(){
        List<CarVO> carVOS = carDao.selectPoi();
        System.out.println(carVOS);
    }

    @Test
    public void test2(){
        User user=new User();
        user.setUsername("神");
        user.setUserPhone("21111111");
        user.setUserSex("dada");
        int insert = userDao.insert(user);
        if (insert>0){
            Integer userId = user.getUserId();
            System.out.println(userId);
        }
    }

    @Test
    public void test3() throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(new Date());
        Date parse = sdf.parse(format);
        System.out.println(format);
    }

    @Test
    public void test4(){
        Result result = carService.queryCarTypeCount("");
        System.out.println(result.getData());
    }

    @Test
    public void test5(){
        Result result = parkingCardService.queryCardCount("2022-01-23", "2022-01-24");
        System.out.println(result);
    }
}
