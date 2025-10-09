package ru.itmo.is.space_marine_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.space_marine_backend.dto.response.MinCoordinatesDTO;
import ru.itmo.is.space_marine_backend.service.SpaceMarineOpsService;

import java.util.Map;

@RestController
@RequestMapping("/api/space-marines/ops")
@RequiredArgsConstructor
public class SpaceMarineOpsController {

    private final SpaceMarineOpsService opsService;

    @GetMapping("/sum-health")
    public ResponseEntity<Long> sumHealth() {
        return ResponseEntity.ok(opsService.getSumHealth());
    }

    @GetMapping("/avg-health")
    public ResponseEntity<Double> avgHealth() {
        return ResponseEntity.ok(opsService.getAvgHealth());
    }

    @GetMapping("/min-coordinates")
    public ResponseEntity<MinCoordinatesDTO> minCoordinates() {
        return ResponseEntity.ok(opsService.getMinCoordinates());
    }
}

