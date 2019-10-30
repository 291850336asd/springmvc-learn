package com.meng.custom.service;

import com.meng.custom.annotion.Service;

@Service("userService")
public class UserServiceImpl  implements UserService {

    @Override
    public boolean addUser() {
        System.out.println("add user ok");
        return true;
    }
}
