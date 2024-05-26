package pl.pbgym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Address;
import pl.pbgym.repository.AbstractUserRepository;

@Service
public class AbstractUserService {
    private final AbstractUserRepository abstractUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AbstractUserService(AbstractUserRepository abstractUserRepository, PasswordEncoder passwordEncoder) {
        this.abstractUserRepository = abstractUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean userExists(String email) {
        return abstractUserRepository.findByEmail(email).isPresent();
    }

    protected void updateAbstractUser(AbstractUser existingAbstractUser, AbstractUser newAbstractUser) {
        if(!passwordEncoder.matches(newAbstractUser.getPassword(), existingAbstractUser.getPassword())) {
            existingAbstractUser.setPassword(passwordEncoder.encode(newAbstractUser.getPassword()));
        }
        if(!existingAbstractUser.getName().equals(newAbstractUser.getName())) {
            existingAbstractUser.setName(newAbstractUser.getName());
        }
        if(!existingAbstractUser.getSurname().equals(newAbstractUser.getSurname())) {
            existingAbstractUser.setSurname(newAbstractUser.getSurname());
        }
        if(!existingAbstractUser.getBirthdate().equals(newAbstractUser.getBirthdate())) {
            existingAbstractUser.setBirthdate(newAbstractUser.getBirthdate());
        }
        if(!existingAbstractUser.getPesel().equals(newAbstractUser.getPesel())) {
            existingAbstractUser.setPesel(newAbstractUser.getPesel());
        }
        if(!existingAbstractUser.getPhoneNumber().equals(newAbstractUser.getPhoneNumber())) {
            existingAbstractUser.setPhoneNumber(newAbstractUser.getPhoneNumber());
        }
        
        Address existingAddress = existingAbstractUser.getAddress();
        Address newAddress = newAbstractUser.getAddress();
        if(!existingAddress.getCity().equals(newAddress.getCity())) {
            existingAbstractUser.getAddress().setCity(newAddress.getCity());
        }
        if(!existingAddress.getStreetName().equals(newAddress.getStreetName())) {
            existingAbstractUser.getAddress().setStreetName(newAddress.getStreetName());
        }
        if(!existingAddress.getBuildingNumber().equals(newAddress.getBuildingNumber())) {
            existingAbstractUser.getAddress().setBuildingNumber(newAddress.getBuildingNumber());
        }
        if(!existingAddress.getApartmentNumber().equals(newAddress.getApartmentNumber())) {
            existingAbstractUser.getAddress().setApartmentNumber(newAddress.getApartmentNumber());
        }
        if(!existingAddress.getPostalCode().equals(newAddress.getPostalCode())) {
            existingAbstractUser.getAddress().setPostalCode(newAddress.getPostalCode());
        }
    }
}