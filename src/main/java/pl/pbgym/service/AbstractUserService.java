package pl.pbgym.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Address;
import pl.pbgym.domain.Member;
import pl.pbgym.exception.member.MemberNotFoundException;
import pl.pbgym.repository.AbstractUserRepository;

import java.util.Optional;

@Service
public class AbstractUserService {
    private final AbstractUserRepository abstractUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AbstractUserService(AbstractUserRepository abstractUserRepository, PasswordEncoder passwordEncoder) {
        this.abstractUserRepository = abstractUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword, String email) {
        Optional<AbstractUser> abstractUser = abstractUserRepository.findByEmail(email);
        abstractUser.ifPresentOrElse(u -> {
                    if(!passwordEncoder.matches(oldPassword, u.getPassword())) {
                        throw new RuntimeException("Old password is incorrect");
                    } else {
                        u.setPassword(passwordEncoder.encode(newPassword));
                    }
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    public boolean userExists(String email) {
        return abstractUserRepository.findByEmail(email).isPresent();
    }

    //TODO: METODY UPDATE BEDA PODZIELONE, A NIE, ŻE CALY OBIEKT NA RAZ
    public void updateAbstractUser(AbstractUser existingAbstractUser, AbstractUser newAbstractUser) {
        if (!passwordEncoder.matches(newAbstractUser.getPassword(), existingAbstractUser.getPassword())) {
            existingAbstractUser.setPassword(passwordEncoder.encode(newAbstractUser.getPassword()));
        }
        if (!existingAbstractUser.getName().equals(newAbstractUser.getName())) {
            existingAbstractUser.setName(newAbstractUser.getName());
        }
        if (!existingAbstractUser.getSurname().equals(newAbstractUser.getSurname())) {
            existingAbstractUser.setSurname(newAbstractUser.getSurname());
        }
        if (!existingAbstractUser.getBirthdate().equals(newAbstractUser.getBirthdate())) {
            existingAbstractUser.setBirthdate(newAbstractUser.getBirthdate());
        }
        if (!existingAbstractUser.getPesel().equals(newAbstractUser.getPesel())) {
            existingAbstractUser.setPesel(newAbstractUser.getPesel());
        }
        if (!existingAbstractUser.getPhoneNumber().equals(newAbstractUser.getPhoneNumber())) {
            existingAbstractUser.setPhoneNumber(newAbstractUser.getPhoneNumber());
        }

        Address existingAddress = existingAbstractUser.getAddress();
        Address newAddress = newAbstractUser.getAddress();
        if (!existingAddress.getCity().equals(newAddress.getCity())) {
            existingAbstractUser.getAddress().setCity(newAddress.getCity());
        }
        if (!existingAddress.getStreetName().equals(newAddress.getStreetName())) {
            existingAbstractUser.getAddress().setStreetName(newAddress.getStreetName());
        }
        if (!existingAddress.getBuildingNumber().equals(newAddress.getBuildingNumber())) {
            existingAbstractUser.getAddress().setBuildingNumber(newAddress.getBuildingNumber());
        }
        if (!existingAddress.getApartmentNumber().equals(newAddress.getApartmentNumber())) {
            existingAbstractUser.getAddress().setApartmentNumber(newAddress.getApartmentNumber());
        }
        if (!existingAddress.getPostalCode().equals(newAddress.getPostalCode())) {
            existingAbstractUser.getAddress().setPostalCode(newAddress.getPostalCode());
        }
    }
}