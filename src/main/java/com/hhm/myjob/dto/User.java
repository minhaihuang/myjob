package com.hhm.myjob.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author: huanghm
 * @Date: 2022/05/14
 * @Description:
 */
@Data
public class User {
    private String name;
    private String password;
    private int age;
    private Date birthday;
    private String desc;
}
