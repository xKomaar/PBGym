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

    protected void updateAbstractUser(Address newAddress) {

        Optional<Address> existingAddress = addressRepository.findById(newAddress.getId());
        existingAddress.ifPresent(address -> {
            if(!address.getCity().equals(newAddress.getCity())) {
                address.setCity(newAddress.getCity());
            }
            if(!address.getStreetName().equals(newAddress.getStreetName())) {
                address.setStreetName(newAddress.getStreetName());
            }
            if(!address.getBuildingNr().equals(newAddress.getBuildingNr())) {
                address.setBuildingNr(newAddress.getBuildingNr());
            }
            if(address.getApartmentNr() != null) {
                if(newAddress.getApartmentNr() != null) {
                    if(!address.getApartmentNr().equals(newAddress.getApartmentNr())) {
                        address.setApartmentNr(newAddress.getApartmentNr());
                    }
                } else {
                    address.setApartmentNr(null);
                }
            } else {
                address.setApartmentNr(newAddress.getApartmentNr());
            }
            if(!address.getPostalCode().equals(newAddress.getPostalCode())) {
                address.setPostalCode(newAddress.getPostalCode());
            }
            addressRepository.flush();
        });
    }
}
