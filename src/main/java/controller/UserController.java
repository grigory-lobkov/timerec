package controller;

import model.UserRow;

public class UserController {


    public static int getTzOffsetSeconds(UserRow user) {
        return 3*60*60; // Moscow
    }

}