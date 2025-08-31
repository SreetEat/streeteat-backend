package com.streeteat.streeteat.delivery.service;

import com.streeteat.streeteat.delivery.model.Delivery;
import com.streeteat.streeteat.delivery.model.DeliveryPartner;
import com.streeteat.streeteat.delivery.model.DeliveryStatus;
import com.streeteat.streeteat.delivery.repository.DeliveryPartnerRepository;
import com.streeteat.streeteat.delivery.repository.DeliveryRepository;
import com.streeteat.streeteat.delivery.model.*;
import com.streeteat.streeteat.delivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepo;
    private final DeliveryRepository deliveryRepo;

    public DeliveryPartner registerPartner(DeliveryPartner partner) {
        return partnerRepo.save(partner);
    }

    public Delivery updateDeliveryAction(Long deliveryId, String action) {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (action.equalsIgnoreCase("ACCEPT")) {
            delivery.setStatus(DeliveryStatus.ACCEPTED);
        } else {
            delivery.setStatus(DeliveryStatus.REJECTED);
        }
        return deliveryRepo.save(delivery);
    }

    public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setStatus(status);

        if (status == DeliveryStatus.DELIVERED) {
            DeliveryPartner partner = delivery.getDeliveryPartner();
            partner.setEarnings(partner.getEarnings() + 50); // sample logic
            partnerRepo.save(partner);
        }

        return deliveryRepo.save(delivery);
    }

    public double getEarnings(Long partnerId) {
        DeliveryPartner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        return partner.getEarnings();
    }
}
