package org.example.itemcounting.JWT;

import java.security.Principal;

public record AuthenticatedUser(Long userId, String username, Long warehouseId) implements Principal {
    @Override
    public String getName() {
        return username;
    }
}
