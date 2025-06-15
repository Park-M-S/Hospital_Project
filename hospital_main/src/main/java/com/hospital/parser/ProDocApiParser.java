package com.hospital.parser;

import com.hospital.dto.api.ProDocApiItem;
import com.hospital.dto.api.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProDocApiParser {

    // ✅ 치과 관련 진료과를 하나의 "치과" 카테고리로 묶기 위한 기준 Set
    private static final Set<String> DENTAL_SUBJECTS = Set.of(
        "치과", "치과교정과", "치과보존과", "치과보철과", "치주과", "영상치의학과",
        "소아치과", "예방치과", "구강내과", "구강병리과", "구강악안면외과", "통합치의학과"
    );

    // ✅ 한의학 관련 진료과를 "한의원"으로 통합
    private static final Set<String> ORIENTAL_SUBJECTS = Set.of(
        "한방내과", "한방부인과", "한방소아과", "한방신경정신과",
        "한방안·이비인후·피부과", "한방재활의학과", "한방응급", "침구과", "사상체질과"
    );

    // ✅ 신경 관련 진료과를 "신경과"로 통합
    private static final Set<String> NEUROLOGY_SUBJECTS = Set.of(
        "신경과", "신경외과", "정신건강의학과"
    );

    /**
     * ✅ ProDocApiResponse → ProDoc 리스트로 파싱
     * @param response 공공 API로부터 받은 전문의 데이터 응답
     * @param hospitalCode 병원 코드 (ykiho)
     * @return 정규화된 ProDoc 객체 리스트
     */
    public List<ProDoc> parse(ProDocApiResponse response, String hospitalCode) {
        // 응답 검증
        if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) {
            return List.of(); // 빈 리스트 반환
        }

        List<ProDoc> result = new ArrayList<>();
        List<ProDocApiItem> items = response.getResponse().getBody().getItems().getItem();

        if (items == null) return result;

        for (ProDocApiItem item : items) {
            String rawSubjectName = item.getSubjectName();  // 원본 진료과명
            Integer count = item.getProDocCount();          // 전문의 수

            // ✅ 과목명 정규화 (치과/한의원/신경과 등으로 통일)
            String normalized = normalizeSubjectName(rawSubjectName);

            // ✅ 엔티티 매핑
            ProDoc doc = new ProDoc();
            doc.setHospitalCode(hospitalCode);
            doc.setSubjectName(normalized);
            doc.setProDocCount(count != null ? count : 0); // null 방지

            result.add(doc);
        }

        return result;
    }

    /**
     * ✅ 과목명 정규화 함수
     * 예: 치과보철과 → 치과, 한방소아과 → 한의원 등
     * @param rawName API에서 내려온 과목 이름
     * @return 정규화된 이름
     */
    private String normalizeSubjectName(String rawName) {
        if (rawName == null) return "알수없음";

        if (DENTAL_SUBJECTS.contains(rawName)) return "치과";
        if (ORIENTAL_SUBJECTS.contains(rawName)) return "한의원";
        if (NEUROLOGY_SUBJECTS.contains(rawName)) return "신경과";

        return rawName; // 정규화 대상이 아니면 그대로 유지
    }
}
