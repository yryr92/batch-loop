package com.example.loop.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Product {
    
    private String bomId;
    private String fileName;
    private String vPath;
    private String version;
    private String releaseNote;

}
