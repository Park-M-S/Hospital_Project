package com.hospital.entity;

import java.util.List;

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
@Table(name = "youtube_category")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YouTubeCategory {

	@Id
	@Column(name = "main_category", nullable = false)
	private String mainCategory;

	@Column(name = "detail_category", nullable = false, length = 1000)
	private String detailCategory;

}
