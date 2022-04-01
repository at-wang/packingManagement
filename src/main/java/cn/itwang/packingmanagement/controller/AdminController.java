package cn.itwang.packingmanagement.controller;


import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping("/admin")
@RestController
@Api(tags = "管理员管理")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @ApiOperation(value = "登录管理")
    @GetMapping("login")
    public Result login(@RequestParam String username,@RequestParam String password){
        Result result = adminService.login(username, password);
        return result;
    }

    @ApiOperation(value = "修改密码")
    @PutMapping("revisePassword")
    public Result revisePassword(@RequestParam Integer id,@RequestParam String password){
        Result result = adminService.modifyPassword(id, password);
        return result;
    }
}
