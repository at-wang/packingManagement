package cn.itwang.packingmanagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pm_parking_record")
public class ParkingRecord {
    @TableId
    @TableField("parking_record_id")
    private Integer parkingRecordId;

    @TableField("car_id")
    private Integer carId;

    @TableField("parking_card_id")
    private Integer parkingCardId;

    @TableField("parking_id")
    private Integer parkingId;

    @TableField("stop_time")
    private Date stopTime;

    @TableField("drive_away_time")
    private Date driveAwayTime;


}
