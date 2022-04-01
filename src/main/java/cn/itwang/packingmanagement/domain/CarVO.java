package cn.itwang.packingmanagement.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarVO {
    private Integer carId;
    private Integer parkingId;
    private Integer userId;
    private String licencePlate;
    private String type;
    private String userName;
    private String userPhone;
    private String userSex;
    private String parkingName;
    private long total;
    private String carState;

    private List<String> carSuggestion;


}
