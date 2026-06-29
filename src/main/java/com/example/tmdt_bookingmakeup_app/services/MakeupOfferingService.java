package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.request.service.CreateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.service.UpdateServiceRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.service.ServiceDto;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class MakeupOfferingService {

    private final ServiceRepository serviceRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;

    @Autowired
    public MakeupOfferingService(ServiceRepository serviceRepository, ServiceOwnerRepository serviceOwnerRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceOwnerRepository = serviceOwnerRepository;
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

    @Transactional
    public ServiceDto createService(CreateServiceRequest request, UUID ownerId) {
        ServiceOwner owner = serviceOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("ServiceOwner not found with id: " + ownerId));

        Service service = new Service();
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setCategory(request.category());
        service.setDuration(request.duration());
        service.setImageUrl(request.imageUrl());
        service.setActive(true);
        service.setRating(0.0);
        service.setOwner(owner);
        
        Service saved = serviceRepository.save(service);
        return mapToDto(saved);
    }

    @Transactional
    public ServiceDto updateService(UUID id, UpdateServiceRequest request, UUID requesterId) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        if (service.getOwner() == null || !service.getOwner().getUserId().equals(requesterId)) {
            throw new RuntimeException("Access Denied: You do not own this service");
        }
        
        if (request.name() != null) service.setName(request.name());
        if (request.description() != null) service.setDescription(request.description());
        if (request.price() != null) service.setPrice(request.price());
        if (request.category() != null) service.setCategory(request.category());
        if (request.duration() != null) service.setDuration(request.duration());
        if (request.isActive() != null) service.setActive(request.isActive());
        if (request.imageUrl() != null) service.setImageUrl(request.imageUrl());
        
        Service updated = serviceRepository.save(service);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteService(UUID id, UUID requesterId) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        if (service.getOwner() == null || !service.getOwner().getUserId().equals(requesterId)) {
            throw new RuntimeException("Access Denied: You do not own this service");
        }
        serviceRepository.delete(service);
    }

    public List<ServiceDto> getServicesByOwnerId(UUID ownerId) {
        return serviceRepository.findByOwnerUserId(ownerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ServiceDto mapToDto(Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        if (service.getOwner() != null) {
            dto.setOwnerId(service.getOwner().getUserId());
        }
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setCategory(service.getCategory());
        dto.setDuration(service.getDuration());
        dto.setActive(service.isActive());
        dto.setRating(service.getRating());
        dto.setImageUrl(service.getImageUrl());
        return dto;
    }
}
