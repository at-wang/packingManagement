package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.ParkingRecord;
import cn.itwang.packingmanagement.domain.ParkingRecordVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ParkingRecordDao extends BaseMapper<ParkingRecord> {
    //查询停车记录
    public List<ParkingRecordVO> selectParkingRecord(String licencePlate, int start, int end);
    //查询停车记录数量
    public long selectParkingRecordCount(String licencePlate);
}
