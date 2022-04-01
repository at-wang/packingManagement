package cn.itwang.packingmanagement.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pm_user")
public class User {
    @TableId
    @TableField("user_id")
    private Integer UserId;

    @TableField("user_name")
    private String username;

    @TableField("user_phone")
    private String userPhone;

    @TableField("user_sex")
    private String userSex;
}
