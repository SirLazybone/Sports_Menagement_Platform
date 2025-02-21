package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.repositories.AfterMatchPenaltyRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.AfterMatchPenaltyService;
import org.springframework.stereotype.Service;

@Service
public class AfterMatchPenaltyServiceImpl implements AfterMatchPenaltyService {
    private final AfterMatchPenaltyRepository afterMatchPenaltyRepository;
    public AfterMatchPenaltyServiceImpl(AfterMatchPenaltyRepository afterMatchPenaltyRepository) {
        this.afterMatchPenaltyRepository = afterMatchPenaltyRepository;
    }
}
