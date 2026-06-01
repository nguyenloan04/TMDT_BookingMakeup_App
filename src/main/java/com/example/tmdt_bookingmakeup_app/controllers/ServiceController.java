package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.request.service.CreateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.service.UpdateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.service.ServiceDto;
import com.example.tmdt_bookingmakeup_app.services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;

    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceDto> createService(@RequestBody CreateServiceRequest request) {
        ServiceDto created = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable UUID id, @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok("Service deleted successfully");
    }
}
