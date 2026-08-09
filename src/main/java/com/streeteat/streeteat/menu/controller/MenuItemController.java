package com.streeteat.streeteat.menu.controller;

import com.streeteat.streeteat.menu.dto.MenuItemRequestDto;
import com.streeteat.streeteat.menu.dto.MenuItemResponseDto;
import com.streeteat.streeteat.menu.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors/{vendorId}/menu")
@RequiredArgsConstructor
@Tag(name = "Menu APIs", description = "Vendor menu CRUD")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Operation(summary = "List a vendor's menu (public)")
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDto>> list(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "false") boolean onlyAvailable
    ) {
        return ResponseEntity.ok(menuItemService.listForVendor(vendorId, onlyAvailable));
    }

    @Operation(summary = "Add a menu item (vendor owner or admin)")
    @PostMapping
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> create(
            @PathVariable Long vendorId,
            @Valid @RequestBody MenuItemRequestDto request,
            Authentication auth
    ) {
        boolean isAdmin = isAdmin(auth);
        return ResponseEntity.ok(menuItemService.createItem(vendorId, request, auth.getName(), isAdmin));
    }

    @Operation(summary = "Update a menu item (vendor owner or admin)")
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> update(
            @PathVariable Long vendorId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequestDto request,
            Authentication auth
    ) {
        boolean isAdmin = isAdmin(auth);
        return ResponseEntity.ok(menuItemService.updateItem(vendorId, itemId, request, auth.getName(), isAdmin));
    }

    @Operation(summary = "Delete a menu item (vendor owner or admin)")
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long vendorId,
            @PathVariable Long itemId,
            Authentication auth
    ) {
        boolean isAdmin = isAdmin(auth);
        menuItemService.deleteItem(vendorId, itemId, auth.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
