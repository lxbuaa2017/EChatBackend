package com.example.echatbackend.controller.reference;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.controller.BaseController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

@CrossOrigin
@RestController
public class ImageController extends BaseController {
    @PostMapping("/api/upload")
    public ResponseEntity<Object> uploadPicture(@RequestBody MultipartFile file, HttpServletRequest request) {
        String url;//返回存储路径
        String imageName = file.getOriginalFilename();//获取文件名加后缀
        if (imageName != null && !imageName.equals("")) {
            String returnUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();//存储路径
            String fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + imageName.substring(imageName.lastIndexOf("."));//新的文件名
            try {
                file.transferTo(new File("images/" + fileName).getAbsoluteFile());
                url = returnUrl + "/images/" + fileName;
                JSONObject response = new JSONObject();
                response.put("url", url);
                return requestSuccess(response);
            } catch (Exception e) {
                e.printStackTrace();
                return requestFail("系统异常，图片上传失败");
            }
        }
        return requestFail("请选择要上传的文件！");
    }

    @GetMapping(value = "/images/{fileName}", produces = {
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    public Object getImage(@PathVariable String fileName) {
        try {
            return ImageIO.read(new FileInputStream(new File("images/" + fileName)));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
