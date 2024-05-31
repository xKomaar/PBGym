package pl.pbgym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.Address;
import pl.pbgym.repository.AddressRepository;

import java.util.Optional;

@Service
public class AddressService {
    
    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    //TODO: BEZ SENSU ZMIENIAC WSZYSTKIE ATRYBUTY, PO PROSTU ZASEJWUJ NOWEGO I TYLE
    protected void updateAddress(Address newAddress) {

        Optional<Address> existingAddress = addressRepository.findById(newAddress.getId());
        existingAddress.ifPresent(address -> {
            if(!address.getCity().equals(newAddress.getCity())) {
                address.setCity(newAddress.getCity());
            }
            if(!address.getStreetName().equals(newAddress.getStreetName())) {
                address.setStreetName(newAddress.getStreetName());
            }
            if(!address.getBuildingNumber().equals(newAddress.getBuildingNumber())) {
                address.setBuildingNumber(newAddress.getBuildingNumber());
            }
            if(address.getApartmentNumber() != null) {
                if(newAddress.getApartmentNumber() != null) {
                    if(!address.getApartmentNumber().equals(newAddress.getApartmentNumber())) {
                        address.setApartmentNumber(newAddress.getApartmentNumber());
                    }
                } else {
                    address.setApartmentNumber(null);
                }
            } else {
                address.setApartmentNumber(newAddress.getApartmentNumber());
            }
            if(!address.getPostalCode().equals(newAddress.getPostalCode())) {
                address.setPostalCode(newAddress.getPostalCode());
            }
            addressRepository.flush();
        });
    }
}
