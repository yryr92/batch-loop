package com.example.loop.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.loop.domain.ResponseDTO;
import com.example.loop.domain.ResponseDTO.fileMeta;
import com.example.loop.domain.ResponseDTO.fileRelease;
import com.example.loop.repository.LoopRepository;

@Service
public class LoopService {
    
    @Autowired
    private LoopRepository repository;

    public void setContents(ResponseDTO dto, fileMeta fm) {
        repository.insertContents(dto, fm);
    }

    public int solution(String[] friends, String[] gifts) {
        int answer = 0;
        HashMap<String, Integer> map = new HashMap<>();

        for(String gift : gifts) {
            if(map.containsKey(gift)) {
                int a = map.get(gift) + 1;
                map.replace(gift, a);
            } else {
                map.put(gift, 1);
            }
        }

        return answer;
    }


}
