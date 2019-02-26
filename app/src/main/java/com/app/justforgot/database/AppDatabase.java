package com.app.justforgot.database;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {PasscodeData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    public abstract PasscodeDao passcodeDao();
}
