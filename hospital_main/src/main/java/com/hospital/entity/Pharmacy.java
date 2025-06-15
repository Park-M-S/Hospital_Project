// 📁 com.hospital.entity.Pharmacy.java
package com.hospital.entity;

import com.hospital.dto.api.PharmacyApiItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pharmacy")
public class Pharmacy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pharmacy_name", nullable = false)
	private String name;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private String phone;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Column(name = "ykiho", unique = true)
	private String ykiho; // 병원/약국 고유 식별자

	/**
	 * 약국 데이터가 유효한지 검사
	 */
	public boolean isValid() {
		// 필수 필드 검증
		if (isEmptyString(ykiho) || isEmptyString(name)) {
			return false;
		}

		// 좌표 유효성 검사 (한국 좌표 범위)
		return latitude != null && longitude != null && latitude >= 33.0 && latitude <= 43.0 && longitude >= 124.0
				&& longitude <= 132.0;
	}

	/**
	 * 문자열이 비어있는지 검사
	 */
	private boolean isEmptyString(String str) {
		return str == null || str.trim().isEmpty();
	}

}
