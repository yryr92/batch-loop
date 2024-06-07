package com.example.loop.domain;

import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.support.rowset.RowSet;

public class ProductRowMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(RowSet rs) throws Exception {
        // TODO Auto-generated method stub
        Product product = new Product();
        product.setBomId(rs.getCurrentRow()[0]);
        product.setFileName(rs.getCurrentRow()[1]);
        product.setVPath(rs.getCurrentRow()[2]);
        product.setVersion(rs.getCurrentRow()[3]);
        product.setReleaseNote(rs.getCurrentRow()[4]);
        return product;
    }
    
}
