package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    protected ResponseEntity<Object> requestSuccess() {
        return requestSuccess(new JSONObject());
    }

    protected ResponseEntity<Object> requestSuccess(@NotNull JSONObject response) {
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    protected ResponseEntity<Object> requestFail(String msg) {
        var response = new JSONObject();
        response.put("success", false);
        response.put("msg", msg);
        return ResponseEntity.ok(response);
    }
}
