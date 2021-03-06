package com.ray.demo.order.controller;

import com.ray.demo.order.client.util.BaseResponse;
import com.ray.demo.order.util.BaseException;
import com.ray.demo.order.util.BaseResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ray on 2017/6/23.
 */
@ControllerAdvice
public class AppAdviceHandler {

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public BaseResponse<?> handleException(Exception e, HttpServletRequest request){
        BaseResponse<?> response = new BaseResponse<>();
        response.setCode(BaseResponseCode.ERROR.getCode());

        if(e instanceof BaseException && StringUtils.isNoneEmpty(e.getMessage())){
            response.setMessage(e.getMessage());
        }
        else {
            e.printStackTrace();
            response.setMessage(BaseResponseCode.ERROR.getDescribe());
        }

        return response;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(true);
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }



}
