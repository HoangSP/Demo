package com.thienhoang.quanly.dao;


import com.thienhoang.quanly.exception.BankTransactionException;
import com.thienhoang.quanly.mapper.BankAccountMapper;
import com.thienhoang.quanly.model.BankAccountInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
@Transactional
public class BankAccountDao extends JdbcDaoSupport {

    @Autowired
    public BankAccountDao(DataSource dataSource){
        this.setDataSource(dataSource);
    }

    public List<BankAccountInfo> getBankAccounts(){
        // Select ba.Id, ba.Full_Name, ba.Balance From Bank_Account ba
        String sql = BankAccountMapper.BASE_SQL;
        Object[] params = new Object[]{};
        BankAccountMapper mapper = new BankAccountMapper();
        List<BankAccountInfo> list = this.getJdbcTemplate().query(sql,params, mapper);
        return list;
    }


    public BankAccountInfo findBankAccount(Long id){
        // Select ba.Id, ba.Full_Name, ba.Balance From Bank_Account ba
        // Where ba.Id = ?
        String sql = BankAccountMapper.BASE_SQL + "where ba.id=?";
        Object[] params = new Object[]{id};
        BankAccountMapper mapper= new BankAccountMapper();
        try {
            BankAccountInfo bankAccountInfo = this.getJdbcTemplate().queryForObject(sql, params, mapper);
            return bankAccountInfo;
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    // MANDATORY: Giao dịch bắt buộc phải được tạo sẵn trước đó.
    @Transactional(propagation = Propagation.MANDATORY)
    public void addAmount(Long id, double amount) throws BankTransactionException{
        BankAccountInfo bankAccountInfo = this.findBankAccount(id);
        if (bankAccountInfo == null){
            throw new BankTransactionException("Account not found" + id);
        }

        double newBalance = bankAccountInfo.getBalance() + amount;
        if(bankAccountInfo.getBalance() + amount < 0){
            throw new BankTransactionException(
                    "The money in the account '" + id + "' is not enough (" + bankAccountInfo.getBalance() + ")");
        }
        bankAccountInfo.setBalance(newBalance);
        // Update to DB
        String sqlUpdate = "Update Bank_Account set Balance = ? where Id = ?";
        this.getJdbcTemplate().update(sqlUpdate, bankAccountInfo.getBalance(), bankAccountInfo.getId());
    }

    // Không được bắt BankTransactionException trong phương thức này.
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = BankTransactionException.class)
    public void sendMoney(Long fromAccountId, Long toAccountId, double amount) throws BankTransactionException {

        addAmount(toAccountId, amount);
        addAmount(fromAccountId, -amount);
    }
}
