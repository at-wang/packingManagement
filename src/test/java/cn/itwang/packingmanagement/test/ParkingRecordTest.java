package cn.itwang.packingmanagement.test;


import cn.itwang.packingmanagement.dao.ParkingRecordDao;
import cn.itwang.packingmanagement.domain.ParkingRecordVO;
import cn.itwang.packingmanagement.utils.DateUtils;
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
public class ParkingRecordTest {

    @Autowired
    private ParkingRecordDao parkingRecordDao;
    @Test
    public void test(){
        long l = parkingRecordDao.selectParkingRecordCount("");
        System.out.println(l);
    }

    @Test
    public void test2() throws InterruptedException {
//        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date();
//        String format = simpleDateFormat.format(date);
//        System.out.println(format);
//        int date1 = DateUtils.date(format);
//        System.out.println(date1);
//        Thread.sleep(3000);
//        Date date2 = new Date();
//        String format1 = simpleDateFormat.format(date2);
//        System.out.println(format1);
//        int date3 = DateUtils.date(format1);
//        System.out.println(date3);
//
//        System.out.println(date3-date1);
    }

    @Test
    public void test3(){
        List<ParkingRecordVO> parkingRecordVOS = parkingRecordDao.selectParkingRecord("", 0, 0);
        System.out.println(parkingRecordVOS);
    }
    @Test
    public void test4(){
//        long date = DateUtils.date("2018-02-21 14:11:11", "2018-02-22 14:21:11");
//        System.out.println(date);
    }

    @Test
    public void test5() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = format.parse("2018-02-21 14:11:11");
        Date date = format.parse("2018-07-21 14:11:11");
        System.out.println(parse);
        System.out.println(date);
        long between = date.getTime() - parse.getTime();
        System.out.println(between);
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        System.out.println(day + "天" + hour + "小时" + min + "分" + s + "秒");

    }
}
