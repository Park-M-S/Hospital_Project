package com.hospital.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class PharmacyApiResponse {

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ItemsWrapper {

        @XmlElement(name = "item")
        private List<PharmacyApiItem> item;
    }
}
