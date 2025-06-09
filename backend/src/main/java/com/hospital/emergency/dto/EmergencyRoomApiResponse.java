package com.hospital.emergency.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JacksonXmlRootElement(localName = "response") // XML 루트 엘리먼트가 <response> 임을 명시
public class EmergencyRoomApiResponse {

    @JacksonXmlProperty(localName = "header") // <header> 태그 매핑
    private Header header;

    @JacksonXmlProperty(localName = "body") // <body> 태그 매핑
    private Body body;

    // --- 중첩 클래스: Header ---
    @Data
    @NoArgsConstructor
    public static class Header {
        @JacksonXmlProperty(localName = "resultCode") // <resultCode> 태그 매핑
        private String resultCode;

        @JacksonXmlProperty(localName = "resultMsg") // <resultMsg> 태그 매핑
        private String resultMsg;
    }

    // --- 중첩 클래스: Body ---
    @Data
    @NoArgsConstructor
    public static class Body {
        @JacksonXmlProperty(localName = "items") // <items> 태그 매핑
        private Items items;

        @JacksonXmlProperty(localName = "numOfRows") // <numOfRows> 태그 매핑
        private Integer numOfRows; // int 대신 Integer로 선언하여 null 처리 가능성 대비

        @JacksonXmlProperty(localName = "pageNo") // <pageNo> 태그 매핑
        private Integer pageNo;

        @JacksonXmlProperty(localName = "totalCount") // <totalCount> 태그 매핑
        private Integer totalCount;
    }

    // --- 중첩 클래스: Items ---
    @Data
    @NoArgsConstructor
    public static class Items {
        // <item> 태그가 여러 개 올 수 있으므로 List로 처리
    	@JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private java.util.List<EmergencyRoomApiItem> item; // Item 클래스는 별도 파일로 정의
    }
}