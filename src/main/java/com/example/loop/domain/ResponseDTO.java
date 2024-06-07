package com.example.loop.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseDTO {

    public String bomId;
    public boolean newYn;
    public List<fileMeta> contents;
    
    @Getter
    @ToString
    public static class fileMeta {
        public String fileName;
        public String vpath;
        public String version;
        public List<fileRelease> release;

    }

    @Getter
    @ToString
    public static class fileRelease {
        public String os;
        public String note;
        public String desc;
    }

}
