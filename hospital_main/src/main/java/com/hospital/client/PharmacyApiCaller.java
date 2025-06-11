package com.hospital.client;

import com.hospital.dto.api.OpenApiWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Component
public class PharmacyApiCaller {

    private static final String BASE_URL = "https://apis.data.go.kr/B551182/pharmacyInfoService/";
    private static final String SERVICE_KEY = "iJsu9ygUVo24pnKXWsntyEmfZtNPVq5WoaRHYNoq7JQv0Jhq3LyRzf/P7QXb3I2Kw1i1lcRBEukiJoZfoWX56g==";

    public OpenApiWrapper.Body callApiByDistrict(String sgguCd) {
        try {
            String fullUrl = BASE_URL + "getParmacyBasisList"
                    + "?serviceKey=" + SERVICE_KEY
                    + "&sidoCd=310000"
                    + "&sgguCd=" + sgguCd
                    + "&numOfRows=1000"
                    + "&_type=xml";

            // ✅ RestTemplate 대신 직접 연결
            URL url = new URL(fullUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // ✅ UTF-8로 명시적 디코딩
            JAXBContext context = JAXBContext.newInstance(OpenApiWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            // ✅ XML → 객체 변환
            OpenApiWrapper result = (OpenApiWrapper) unmarshaller.unmarshal(reader);
            return result.getBody();

        } catch (Exception e) {
            throw new RuntimeException("약국 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
