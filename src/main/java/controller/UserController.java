package controller;

import model.RoleRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;

import java.util.List;

public class UserController {

    static private ITable<RoleRow> storageRole = StorageFactory.getRoleInstance();


    public static int getTzOffsetSeconds(UserRow user) {
        return 3 * 60 * 60; // Moscow
    }


    public static long getDefaultRoleId() {
        final long DEFAULT_ROLE_ID = 1;
        long result = DEFAULT_ROLE_ID;
        try {
            long last_role = DEFAULT_ROLE_ID;
            List<RoleRow> roles = storageRole.select();
            result = 0;
            for (RoleRow role : roles) {
                if (role.is_default)
                    result = role.role_id;
                last_role = role.role_id;
            }
            if (result == 0) {
                result = last_role;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}