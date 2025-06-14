package com.hospital.parser;

import com.hospital.dto.api.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ✅ 진료과목 API 응답을 파싱하고, 정규화 및 중복 제거하여 MedicalSubject 엔티티 리스트로 변환하는 클래스
 */
@Component
public class MedicalSubjectApiParser {

    // ✅ 특정 진료과명을 통합 분류하기 위한 기준 세트들
    private static final Set<String> DENTAL_SUBJECTS = Set.of(
        "치과", "치과교정과", "치과보존과", "치과보철과", "치주과", "영상치의학과",
        "소아치과", "예방치과", "구강내과", "구강병리과", "구강악안면외과", "통합치의학과"
    );

    private static final Set<String> ORIENTAL_SUBJECTS = Set.of(
        "한방내과", "한방부인과", "한방소아과", "한방신경정신과",
        "한방안·이비인후·피부과", "한방재활의학과", "한방응급", "침구과", "사상체질과"
    );

    private static final Set<String> NEUROLOGY_SUBJECTS = Set.of(
        "신경과", "신경외과", "정신건강의학과"
    );

    /**
     * ✅ API 응답을 파싱하고 정제된 MedicalSubject 리스트 반환
     * @param response JSON 파싱된 응답 객체
     * @param hospitalCode 병원 고유 코드 (YKIHO)
     * @return 중복 제거 및 정규화된 진료과목 리스트
     */
    public List<MedicalSubject> parse(MedicalSubjectApiResponse response, String hospitalCode) throws Exception {
        // ✅ 응답 구조 유효성 검증
        if (response.getResponse() == null ||
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem() == null) {
            throw new Exception("진료과목 API 응답 구조가 예상과 다릅니다.");
        }

        // ✅ 중복 진료과 제거용 Set
        Set<String> seenSubjects = new HashSet<>();

        return response.getResponse().getBody().getItems().getItem().stream()
            .map(item -> normalizeSubjectName(item.getDgsbjtCdNm())) // ✅ 진료과명 정규화
            .filter(seenSubjects::add) // ✅ 중복 제거: Set에 없던 값만 통과
            .map(subjectName -> {
                MedicalSubject subject = new MedicalSubject();
                subject.setHospitalCode(hospitalCode);
                subject.setSubjectName(subjectName);
                return subject;
            })
            .collect(Collectors.toList());
    }

    /**
     * ✅ 진료과명을 정규화하여 주요 그룹(치과/한의과/신경과)으로 통합
     * @param rawName 원본 진료과명
     * @return 정제된 과목명
     */
    private String normalizeSubjectName(String rawName) {
        if (rawName == null) return "기타";
        if (DENTAL_SUBJECTS.contains(rawName)) return "치과";
        if (ORIENTAL_SUBJECTS.contains(rawName)) return "한의과";
        if (NEUROLOGY_SUBJECTS.contains(rawName)) return "신경과";
        return rawName; // 그 외는 그대로 유지
    }
}
