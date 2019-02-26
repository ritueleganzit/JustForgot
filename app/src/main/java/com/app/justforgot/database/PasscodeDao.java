package com.app.justforgot.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.sqlite.SQLiteConstraintException;

import java.util.List;
@Dao
public interface PasscodeDao {



    @Query("Select password from passcode where name in(:names)")

    String findPassword(String names);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PasscodeData passcodeData);


    @Query("UPDATE passcode set password = :passcodeData where name=:name")
    void update(String name, String passcodeData);



}
