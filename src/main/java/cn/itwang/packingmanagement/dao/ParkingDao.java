package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.Parking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingDao extends BaseMapper<Parking> {

}
