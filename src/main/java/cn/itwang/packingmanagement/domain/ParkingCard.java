package cn.itwang.packingmanagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pm_parking_card")
public class ParkingCard {
    @TableId
    @TableField("parking_card_id")
    private Integer parkingCardId;
    @TableField("card_number")
    private String cardNumber;
    @TableField("card_balance")
    private Integer cardBalance;
    @TableField("card_owner")
    private String cardOwner;
    @TableField("created_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

}
