package org.example.itemcounting.business.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class DistanceService {
    public double getRandomDistance() {
        return ThreadLocalRandom.current().nextDouble(1.0, 100.0);
    }
}
