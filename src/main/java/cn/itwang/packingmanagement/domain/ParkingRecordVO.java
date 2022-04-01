package cn.itwang.packingmanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRecordVO {
    //parking_name,stop_time,drive_away_time,parking_charge,licence_plate,card_number
    private Integer parkingRecordId;
    private String parkingName;
    private Date stopTime;
    private Date driveAwayTime;
    private String stopTimeVO;
    private String driveAwayTimeVO;
    private double parkingCharge;
    private String licencePlate;
    private String cardNumber;
    private double costMoney;
    private long total;
    private int cardBalance;

}

