package com.hospital.caller;

import com.hospital.config.RegionConfig;
import com.hospital.dto.OpenApiWrapper;
import com.hospital.dto.PharmacyApiItem;
import com.hospital.dto.PharmacyApiResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class PharmacyApiCaller {
    
    @Value("${hospital.pharmacy.api.base-url}")
    private String baseUrl;
    
    @Value("${hospital.pharmacy.api.key}")
    private String serviceKey;
    
    private final RegionConfig regionConfig; 
    
    public PharmacyApiCaller(RegionConfig regionConfig) { 
        this.regionConfig = regionConfig;
    }
    
    /**
     * HospitalMainApiCaller와 동일한 구조 - PharmacyApiResponse 반환
     */
    public PharmacyApiResponse callApi(String queryParams) {
        String fullUrl = null;
        
        try {
            // 쿼리 파라미터를 그대로 사용 (HospitalMainApiCaller와 동일)
            fullUrl = baseUrl + "?serviceKey=" + serviceKey + "&" + queryParams;
                    
            log.debug("약국 API 호출: {}", fullUrl);
            
            // URL 연결
            URL url = new URL(fullUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            
            if (inputStream == null) {
                log.warn("API 응답 스트림이 비어있음");
                return new PharmacyApiResponse(); // 빈 응답 객체 반환
            }
            
            // UTF-8로 명시적 디코딩
            JAXBContext context = JAXBContext.newInstance(PharmacyApiResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            
            // XML → 객체 변환
            PharmacyApiResponse result = (PharmacyApiResponse) unmarshaller.unmarshal(reader);
            
            if (result == null) {
                log.warn("API 응답 파싱 결과가 비어있음");
                return new PharmacyApiResponse(); // 빈 응답 객체 반환
            }
            
            log.debug("약국 API 응답 파싱 성공");
            return result; // PharmacyApiResponse 전체 반환
        
        } catch (MalformedURLException e) {
            log.error("잘못된 URL 형식: {}", fullUrl);
            throw new RuntimeException("Pharmacy API URL 형식 오류: " + e.getMessage(), e);
            
        } catch (IOException e) {
            log.error("네트워크 연결 오류 - URL: {}", fullUrl);
            throw new RuntimeException("Pharmacy API 네트워크 오류: " + e.getMessage(), e);
            
        } catch (JAXBException e) {
            log.error("XML 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("Pharmacy API XML 파싱 오류: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("약국 API 호출 중 예외 발생", e);
            throw new RuntimeException("약국 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
    
    /**
     * 기존 메서드 - 하위 호환성을 위해 유지
     */
    @Deprecated
    public List<PharmacyApiItem> callApiByDistrict(String sgguCd) {
        // 기존 지역별 호출 메서드 (필요시 사용)
        String queryParams = "sidoCd=" + regionConfig.getSidoCode() + "&sgguCd=" + sgguCd + "&pageNo=1&numOfRows=1000";
        PharmacyApiResponse response = callApi(queryParams);
        return response != null && response.getBody() != null ? response.getBody().getItems() : List.of();
    }
}