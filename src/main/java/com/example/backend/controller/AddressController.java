package com.example.backend.controller;

import com.example.backend.dto.AddressDTO;
import com.example.backend.model.Address;
import com.example.backend.model.User;
import com.example.backend.repository.AddressRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:5173")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Address>> getUserAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Address> addresses = addressRepository.findByUser(userOptional.get());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        Optional<Address> addressOptional = addressRepository.findById(id);

        if (userOptional.isEmpty() || addressOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Address address = addressOptional.get();
        if (!address.getUser().getUserId().equals(userOptional.get().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody AddressDTO addressDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        Address address = Address.builder()
                .user(user)
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .addressType(addressDTO.getAddressType())
                .build();

        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.ok(savedAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        Optional<Address> addressOptional = addressRepository.findById(id);

        if (userOptional.isEmpty() || addressOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Address address = addressOptional.get();
        if (!address.getUser().getUserId().equals(userOptional.get().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setCountry(addressDTO.getCountry());
        address.setAddressType(addressDTO.getAddressType());

        Address updatedAddress = addressRepository.save(address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        Optional<Address> addressOptional = addressRepository.findById(id);

        if (userOptional.isEmpty() || addressOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Address address = addressOptional.get();
        if (!address.getUser().getUserId().equals(userOptional.get().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        addressRepository.delete(address);
        return ResponseEntity.noContent().build();
    }
}
