package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.ParkingCard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingCardDao extends BaseMapper<ParkingCard> {
}
