package cn.itwang.packingmanagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pm_parking")
public class Parking {
    @TableId
    @TableField("parking_id")
    private Integer parkingId;
    @TableField("parking_name")
    private String parkingName;
    /**
     * 车牌
     */
    @TableField("parking_space")
    private String parkingSpace;

    @TableField("parking_charge")
    private double parkingCharge;

    @TableField("car_number")
    private String carNumber;

}
