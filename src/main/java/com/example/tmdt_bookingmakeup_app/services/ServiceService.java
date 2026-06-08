package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.request.service.CreateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.service.UpdateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.service.ServiceDto;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ServiceDto getServiceById(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return mapToDto(service);
    }

    public ServiceDto createService(CreateServiceRequest request) {
        Service service = new Service();
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setCategory(request.category());
        service.setDuration(request.duration());
        service.setActive(true);
        service.setRating(0.0);
        
        Service saved = serviceRepository.save(service);
        return mapToDto(saved);
    }

    public ServiceDto updateService(UUID id, UpdateServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        if (request.name() != null) service.setName(request.name());
        if (request.description() != null) service.setDescription(request.description());
        if (request.price() != null) service.setPrice(request.price());
        if (request.category() != null) service.setCategory(request.category());
        if (request.duration() != null) service.setDuration(request.duration());
        if (request.isActive() != null) service.setActive(request.isActive());
        
        Service updated = serviceRepository.save(service);
        return mapToDto(updated);
    }

    public void deleteService(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        serviceRepository.delete(service);
    }

    private ServiceDto mapToDto(Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        if (service.getOwner() != null) {
            dto.setOwnerId(service.getOwner().getId());
        }
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setCategory(service.getCategory());
        dto.setDuration(service.getDuration());
        dto.setActive(service.isActive());
        dto.setRating(service.getRating());
        return dto;
    }
}
