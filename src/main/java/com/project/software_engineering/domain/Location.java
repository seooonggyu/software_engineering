package com.project.software_engineering.domain;

public enum Location {
    HCA("효암채플"),
    LIBRARY("효암채플 별관"),
    GLC("GLC"),
    HDH("현동홀"),
    NHM("느헤미야홀"),
    NTH("뉴턴홀"),
    ANH("올네이션스홀"),
    OH("오석관"),
    CSH("코너스톤홀"),
    KGH("김영길그레이스스쿨"),
    D301("갈대상자관"),
    D503("비전관"),
    D504("창조관"),
    D506("로뎀관"),
    D508("국제관"),
    D509("은혜관"),
    D512("하용조관"),
    SU("학생회관"),
    B501("산학협력관"),
    EBEN("에벤에셀관"),
    B507("SW TOWN"),
    HGU1("야외공연장"),
    HGU2("히딩크필드"),
    HGU3("히딩크 옆 농구장"),
    HGU4("학관 쪽 농구장"),
    HGU5("채플 쪽 농구장"),
    HGU6("버스정류장"),
    HGU7("택시정류장"),
    HGU8("로맨틱잔디"),
    HGU9("평봉필드");

    private final String description;

    Location(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
