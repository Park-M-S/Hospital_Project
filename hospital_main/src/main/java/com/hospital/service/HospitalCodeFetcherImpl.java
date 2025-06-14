package com.hospital.service;

import com.hospital.repository.HospitalMainApiRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalCodeFetcherImpl implements HospitalCodeFetcher {

    private final HospitalMainApiRepository hospitalMainRepository;

    public HospitalCodeFetcherImpl(HospitalMainApiRepository hospitalMainRepository) {
        this.hospitalMainRepository = hospitalMainRepository;
    }

    @Override
    public List<String> getAllHospitalCodes() {
        return hospitalMainRepository.findAllHospitalCodes();
        
        
    }
}
