package cn.itwang.packingmanagement.domain;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pm_car")
public class Car {
    @TableId
    @TableField("car_id")
    private Integer carId;

    @TableField("parking_id")
    private Integer parkingId;

    @TableField("user_id")
    private Integer userId;
    /**
     * 车牌
     */
    @TableField("licence_plate")
    private String licencePlate;

    @TableField("type")
    private String type;
    @TableField("car_state")
    private String carState;

}
