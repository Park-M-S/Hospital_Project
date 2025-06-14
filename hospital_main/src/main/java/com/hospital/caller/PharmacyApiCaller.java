package com.hospital.caller;

import com.hospital.dto.api.OpenApiWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Component
public class PharmacyApiCaller {

	@Value("${hospital.pharmacy.api.base-url}")
	private String baseUrl;

	@Value("${hospital.pharmacy.api.key}")
	private String serviceKey;
	
    public OpenApiWrapper.Body callApiByDistrict(String sgguCd) {
        try {
            String fullUrl = baseUrl + "getParmacyBasisList"
                    + "?serviceKey=" + serviceKey
                    + "&sidoCd=310000"
                    + "&sgguCd=" + sgguCd
                    + "&numOfRows=1000"
                    + "&_type=xml";

            // RestTemplate 대신 직접 연결
            URL url = new URL(fullUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // UTF-8로 명시적 디코딩
            JAXBContext context = JAXBContext.newInstance(OpenApiWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            // XML → 객체 변환
            OpenApiWrapper result = (OpenApiWrapper) unmarshaller.unmarshal(reader);
            return result.getBody();

        } catch (Exception e) {
            throw new RuntimeException("약국 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
