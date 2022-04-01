package cn.itwang.packingmanagement.controller;

import cn.itwang.packingmanagement.domain.User;
import cn.itwang.packingmanagement.entity.Result;
import cn.itwang.packingmanagement.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/user")
@Api(tags = "车主管理",value = "车主信息相关接口")
public class UserController {

    @Autowired
    private UserService userService;


    @ApiOperation("查询车主信息")
    @GetMapping("getUserInformation")
    public Result getUserInformation(String username,int currentPage,int pageSize){
        Result result = userService.queryUserInformation(username, currentPage, pageSize);
        return result;
    }

    @ApiOperation("添加车主信息")
    @PostMapping("addUserInformation")
    public Result addUserInformation(@RequestBody User user) throws IOException {
        Result result = userService.addUserInformation(user);
        return result;
    }

    @ApiOperation("修改车主信息")
    @PutMapping("reviseUserInformation")
    public Result reviseUserInformation(@RequestBody User user) throws IOException {
        Result result = userService.modifyUserInformation(user);
        return result;
    }

    @ApiOperation("删除车主信息")
    @DeleteMapping("cancelUserInformation")
    public Result cancelUserInformation(@RequestParam Integer userId) throws IOException {
        Result result = userService.removeUserInformation(userId);
        return result;
    }

    @ApiOperation("导出车主信息")
    @GetMapping("exportUser")
    public void exportUser(HttpServletResponse response) throws IOException {
        userService.exportUser(response);
    }

    @ApiOperation("自动补全")
    @GetMapping("getUserCompletion")
    public Result getUserCompletion(@RequestParam String prefix)throws IOException{
        Result result = userService.queryUserCompletion(prefix);
        return result;
    }

    @ApiOperation("查询性别数量")
    @GetMapping("getUserSexCount")
    public Result getUserSexCount(@RequestParam String sex){
        Result result = userService.queryUserSexCount(sex);
        return result;
    }
}
