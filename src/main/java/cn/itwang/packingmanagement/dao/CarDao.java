package cn.itwang.packingmanagement.dao;

import cn.itwang.packingmanagement.domain.Car;
import cn.itwang.packingmanagement.domain.CarVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CarDao extends BaseMapper<Car> {
    //查询车辆信息
    public List<CarVO> selectCarInformation(String licencePlate, int start, int end);
    //查询车辆数量
    public long selectCarCount(String licencePlate);
    //poi报表
    public List<CarVO> selectPoi();
}
