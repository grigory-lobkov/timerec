package controller;

import model.RoleRow;
import model.TzRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;

import java.util.List;

public class UserController {

    static private ITable<RoleRow> storageRole = StorageFactory.getRoleInstance();
    static private ITable<TzRow> storageTz = StorageFactory.getTzInstance();


    public static int getTzOffsetSeconds(UserRow user) {
        if (user.tz_utc_offset != -1) return user.tz_utc_offset * 60;
        if (user.tz_id > 0)
            try {
                TzRow tz = storageTz.select(user.tz_id);
                user.tz_utc_offset = tz.utc_offset;
                return user.tz_utc_offset * 60;
            } catch (Exception e) {
                e.printStackTrace();
            }
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