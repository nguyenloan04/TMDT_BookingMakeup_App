package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.response.service.ServiceDto;
import com.example.tmdt_bookingmakeup_app.models.interaction.ServiceFavourite;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceFavouriteRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceFavouriteService {

    private final ServiceFavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final MakeupOfferingService makeupOfferingService;

    @Autowired
    public ServiceFavouriteService(ServiceFavouriteRepository favouriteRepository,
                                   UserRepository userRepository,
                                   ServiceRepository serviceRepository,
                                   MakeupOfferingService makeupOfferingService) {
        this.favouriteRepository = favouriteRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.makeupOfferingService = makeupOfferingService;
    }

    @Transactional
    public void addFavourite(UUID customerId, UUID serviceId) {
        if (favouriteRepository.existsByCustomerIdAndServiceId(customerId, serviceId)) {
            return;
        }
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + serviceId));

        ServiceFavourite fav = new ServiceFavourite();
        fav.setCustomer(customer);
        fav.setService(service);
        favouriteRepository.save(fav);
    }

    @Transactional
    public void removeFavourite(UUID customerId, UUID serviceId) {
        ServiceFavourite fav = favouriteRepository.findByCustomerIdAndServiceId(customerId, serviceId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));
        favouriteRepository.delete(fav);
    }

    public List<ServiceDto> getFavourites(UUID customerId) {
        List<ServiceFavourite> list = favouriteRepository.findByCustomerId(customerId);
        return list.stream()
                .map(fav -> makeupOfferingService.mapToDto(fav.getService()))
                .collect(Collectors.toList());
    }

    public boolean isFavourite(UUID customerId, UUID serviceId) {
        return favouriteRepository.existsByCustomerIdAndServiceId(customerId, serviceId);
    }
}
