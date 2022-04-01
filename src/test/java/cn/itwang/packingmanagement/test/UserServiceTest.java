package cn.itwang.packingmanagement.test;

import cn.itwang.packingmanagement.dao.CarDao;
import cn.itwang.packingmanagement.dao.UserDao;
import cn.itwang.packingmanagement.domain.Car;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Autowired
    private CarDao carDao;
    @Test
    public void test(){
       LambdaQueryWrapper<Car> lqw=new LambdaQueryWrapper<>();
       lqw.eq(Car::getUserId,3);
        List<Car> cars = carDao.selectList(lqw);
        if (cars.size()==0){
            System.out.println("你是擦擦擦");
        }
        System.out.println(cars);
    }

    @Test
    public void test2(){
        Random random=new Random();
        String s="";
        for (int i=0;i<10;i++){
            s+=random.nextInt(10);
        }
        String randomNumber=s+"@";
        System.out.println(randomNumber);


    }
}
