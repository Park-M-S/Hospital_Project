package com.hospital.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class PharmacyApiItem {

    @XmlElement(name = "ykiho")
    private String ykiho;

    @XmlElement(name = "yadmNm")
    private String yadmNm;

    @XmlElement(name = "addr")
    private String addr;

    @XmlElement(name = "emdongNm")
    private String emdongNm;

    @XmlElement(name = "telno")
    private String telno;

    @XmlElement(name = "XPos")
    private Double xPos;

    @XmlElement(name = "YPos")
    private Double yPos;

    @XmlElement(name = "sgguCd")
    private String sgguCd;

    @XmlElement(name = "sidoCd")
    private String sidoCd;
}
