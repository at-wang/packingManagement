package cn.itwang.packingmanagement.controller;

import cn.itwang.packingmanagement.entity.Result;

import cn.itwang.packingmanagement.utils.MyBaiduUtils;
import cn.itwang.packingmanagement.utils.QiniuUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/licensePlate")
@Api(tags = "车牌识别",value = "车牌识别")
public class LicencePlateController {

    @PostMapping("recognition")
    @ApiOperation(value = "车牌识别")
    public Result test(@RequestParam MultipartFile file) throws Exception{
        //七牛云
        String originalFilename = file.getOriginalFilename();//原始文件名
        int index = originalFilename.lastIndexOf(".");//得到最后一个.的索引
        String imageName = originalFilename.substring(index);//截取后缀名 例子.jpg
        String imgUrl = UUID.randomUUID().toString() + imageName;//获取唯一id
        QiniuUtils.upload2Qiniu(file.getBytes(),imgUrl);
        String url = QiniuUtils.domainName + imgUrl;
        HashMap<String,String> licencePlateMap=new HashMap<>();
        String licencePlate = null;
        try {
            licencePlate = MyBaiduUtils.getLicencePlate(url);
        } catch (Exception e) {
            return new Result(false,"请上传带有汽车车牌的图片");
        }
        System.out.println(licencePlate);//输出车牌号
        licencePlateMap.put("licencePlate",licencePlate);
        licencePlateMap.put("imageUrl",url);
        return new Result(true,"上传成功",licencePlateMap);
    }

}
