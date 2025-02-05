package com.team69.itproject.entities.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("返回实体类")
public class ResponseEntity<T> {
    @ApiModelProperty("返回码")
    private int code;
    @ApiModelProperty(value = "返回信息", notes = "无返回数据或报错则显示此信息")
    private String msg;
    @ApiModelProperty(value = "返回数据", notes = "无返回信息则为null")
    private T data;

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<T>().code(200).msg("OK");
    }

    public static <T> ResponseEntity<T> ok(T data) {
        if (data == null) {
            return ok();
        }
        return new ResponseEntity<T>().code(200).data(data);
    }

    public static <T> ResponseEntity<T> error(int code, T data) {
        return new ResponseEntity<T>().code(code).data(data);
    }

    private ResponseEntity<T> code(int code) {
        this.code = code;
        return this;
    }

    private ResponseEntity<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    private ResponseEntity<T> data(T data) {
        this.data = data;
        return this;
    }
}


