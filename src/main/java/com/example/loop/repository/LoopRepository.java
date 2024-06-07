package com.example.loop.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.loop.domain.ResponseDTO;
import com.example.loop.domain.ResponseDTO.fileMeta;
import com.example.loop.domain.ResponseDTO.fileRelease;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class LoopRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    private int flag;

    public void insertContents(ResponseDTO dto, fileMeta fm) {
        String sql = "INSERT INTO npc_content values(default, ?, ?, ?, ?, ?, ?, ?, ?, default)";

        flag = 0;

        if(dto.isNewYn()) flag = 1;

        //namedJdbcTemplate.update(sql, null)

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, dto.getBomId());
                ps.setInt(2, flag);
                ps.setString(3, fm.getFileName());
                ps.setString(4, fm.getVpath());
                ps.setString(5, fm.getVersion());
                ps.setString(6, fm.getRelease().get(i).getOs());
                ps.setString(7, fm.getRelease().get(i).getNote());
                ps.setString(8, fm.getRelease().get(i).getDesc());

            }

            @Override
            public int getBatchSize() {
               return fm.getRelease().size();
            }
            
        });

    }

}
