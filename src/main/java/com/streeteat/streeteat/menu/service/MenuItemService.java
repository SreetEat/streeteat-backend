package com.streeteat.streeteat.menu.service;

import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.menu.dto.MenuItemRequestDto;
import com.streeteat.streeteat.menu.dto.MenuItemResponseDto;
import com.streeteat.streeteat.menu.model.MenuItem;
import com.streeteat.streeteat.menu.repository.MenuItemRepository;
import com.streeteat.streeteat.vendor.model.Vendor;
import com.streeteat.streeteat.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final VendorService vendorService;

    @Transactional
    public MenuItemResponseDto createItem(Long vendorId, MenuItemRequestDto request, String requesterEmail, boolean isAdmin) {
        Vendor vendor = vendorService.findOrThrow(vendorId);
        assertOwnerOrAdmin(vendor, requesterEmail, isAdmin);

        MenuItem item = MenuItem.builder()
                .vendor(vendor)
                .name(request.getName())
                .price(request.getPrice())
                .available(request.getAvailable() == null || request.getAvailable())
                .build();

        return toResponse(menuItemRepository.save(item));
    }

    public List<MenuItemResponseDto> listForVendor(Long vendorId, boolean onlyAvailable) {
        vendorService.findOrThrow(vendorId); // 404 if vendor doesn't exist
        List<MenuItem> items = onlyAvailable
                ? menuItemRepository.findByVendorIdAndAvailableTrue(vendorId)
                : menuItemRepository.findByVendorId(vendorId);
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponseDto updateItem(Long vendorId, Long itemId, MenuItemRequestDto request, String requesterEmail, boolean isAdmin) {
        MenuItem item = findOrThrow(vendorId, itemId);
        assertOwnerOrAdmin(item.getVendor(), requesterEmail, isAdmin);

        item.setName(request.getName());
        item.setPrice(request.getPrice());
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long vendorId, Long itemId, String requesterEmail, boolean isAdmin) {
        MenuItem item = findOrThrow(vendorId, itemId);
        assertOwnerOrAdmin(item.getVendor(), requesterEmail, isAdmin);
        menuItemRepository.delete(item);
    }

    // ---------- helpers ----------

    private MenuItem findOrThrow(Long vendorId, Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));
        if (!item.getVendor().getId().equals(vendorId)) {
            throw new BadRequestException("Menu item does not belong to vendor " + vendorId);
        }
        return item;
    }

    private void assertOwnerOrAdmin(Vendor vendor, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (vendor.getOwner() == null || !vendor.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new BadRequestException("You do not own this vendor's menu");
        }
    }

    private MenuItemResponseDto toResponse(MenuItem item) {
        return MenuItemResponseDto.builder()
                .id(item.getId())
                .vendorId(item.getVendor().getId())
                .name(item.getName())
                .price(item.getPrice())
                .available(item.isAvailable())
                .build();
    }
}
