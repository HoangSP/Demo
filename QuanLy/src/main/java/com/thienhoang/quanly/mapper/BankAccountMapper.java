package com.thienhoang.quanly.mapper;

import com.thienhoang.quanly.model.BankAccountInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class BankAccountMapper implements RowMapper<BankAccountInfo> {

    public static final String BASE_SQL = "Select ba.id, ba.FULL_NAME, ba.BALANCE From Bank_Account ba ";

    @Override
    public BankAccountInfo mapRow(ResultSet rs, int rowNum) throws SQLException{
        Long id = rs.getLong("id");
        String fullName = rs.getString("Full_Name");
        double balance = rs.getDouble("balance");
        return new BankAccountInfo(id, fullName, balance);
    }
}
