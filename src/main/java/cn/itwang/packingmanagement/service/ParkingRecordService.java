package cn.itwang.packingmanagement.service;

import cn.itwang.packingmanagement.domain.ParkingRecord;
import cn.itwang.packingmanagement.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public interface ParkingRecordService extends IService<ParkingRecord> {
    //查询停车记录
    public Result queryParkingRecord(String licencePlate,int currentPage,int pageSize);
    //根据id删除记录
    public Result removeParkingRecordById(Integer parkingRecordId);
    //导出数据
    public void exportParkingRecord(HttpServletResponse httpServletResponse) throws IOException;

    //自动补全
    public Result queryParkingRecordCompletion(String prefix) throws IOException;
    //添加
    public Result addParkingRecord(ParkingRecord parkingRecord) throws IOException;
    //根据id车辆修改离开时间
    public Result modifyAwayTime(Integer parkingRecordId);
    //查询办卡数量

}
