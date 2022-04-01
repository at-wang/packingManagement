package cn.itwang.packingmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private boolean flag;
    private String message;
    private Object data;

    public Result(boolean flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    public Result(boolean flag, Object data) {
        this.flag = flag;
        this.data = data;
    }
    public Result(boolean flag){
        this.flag=flag;
    }
    public Result(String message){
        this.message=message;
    }
}
