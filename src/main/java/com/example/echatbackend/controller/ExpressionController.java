package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;

@CrossOrigin
@RestController
public class ExpressionController extends BaseController{
    // 返回所有表情
    @GetMapping("/expre/getExpression")
    public ResponseEntity<Object> getExpression() {
        String expressionsPath="src/main/resources/static/expressions";//表情存储位置
        File expressions=new File(expressionsPath);
        int num=0;
        ArrayList<JSONObject> resultExpressions=new ArrayList<>();
        for(File expressionGroup:expressions.listFiles()){//表情按照类型分组为多个子文件夹
            if(expressionGroup.isDirectory()){
                for(File expression:expressionGroup.listFiles()){
                    num++;
                    JSONObject anExpression=new JSONObject();
                    anExpression.put("url","/expressions/"+expressionGroup.getName()+"/"+expression.getName());
                    anExpression.put("name",expression.getName());
                    anExpression.put("id",num);//id顺序编号
                    anExpression.put("info",expression.getName());//info暂为name
                    resultExpressions.add(anExpression);
                }
            }
        }
        JSONObject result=new JSONObject();
        result.put("code",0);
        result.put("data",resultExpressions);

        return requestSuccess(result);
    }
}
