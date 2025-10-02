package com.hospital.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.dto.UnifiedSearchResponse;
import com.hospital.service.UnifiedSearchService;

@RestController
@RequestMapping("/search")
public class UnifiedSearchController {

	private final UnifiedSearchService unifiedSearchService;

	@Autowired
	public UnifiedSearchController(UnifiedSearchService unifiedSearchService) {
		this.unifiedSearchService = unifiedSearchService;
	}

	@GetMapping(value = "/unifiedData", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UnifiedSearchResponse> searchData(@RequestParam("searchName") String hospitalName) {

		return unifiedSearchService.search(hospitalName);

	}

}
