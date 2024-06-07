package com.example.loop.domain;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.support.rowset.RowSet;
import org.springframework.lang.Nullable;

public class EmployeeRowmapper implements RowMapper<Employee> {

    @Override
    public Employee mapRow(RowSet rs) throws Exception {
        Employee employee = new Employee();

        employee.setId(Integer.parseInt(rs.getCurrentRow()[0]));
        employee.setFirstName(rs.getCurrentRow()[1]);
        employee.setLastName(rs.getCurrentRow()[2]);
        employee.setEmail(rs.getCurrentRow()[3]);
        employee.setGender(rs.getCurrentRow()[4]);
        employee.setIpAddress(rs.getCurrentRow()[5]);
        
        return employee;
        
    }

    
}
