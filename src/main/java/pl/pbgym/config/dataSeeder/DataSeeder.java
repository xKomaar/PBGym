package pl.pbgym.config.dataSeeder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("test")
public class DataSeeder implements CommandLineRunner {

    private final AbstractUserRepository abstractUserRepository;

    private final AuthenticationService authenticationService;

    @Autowired
    public DataSeeder(AbstractUserRepository abstractUserRepository, AuthenticationService authenticationService) {
        this.abstractUserRepository = abstractUserRepository;
        this.authenticationService = authenticationService;
    }

    @Override
    public void run(String... args) throws Exception {
        abstractUserRepository.deleteAll();
        this.loadWorkerData();
    }

    private void loadWorkerData() {
        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber(1);
        postAddressRequestDto.setPostalCode("15-123");

        PostWorkerRequestDto adminWorkerRequest = new PostWorkerRequestDto();
        adminWorkerRequest.setEmail("admin@worker.com");
        adminWorkerRequest.setPassword("12345678");
        adminWorkerRequest.setName("Admin");
        adminWorkerRequest.setSurname("Worker");
        adminWorkerRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        adminWorkerRequest.setPesel("12345678912");
        adminWorkerRequest.setPhoneNumber("123456789");
        adminWorkerRequest.setIdCardNumber("ABC123456");
        adminWorkerRequest.setPosition("Owner");
        adminWorkerRequest.setAddress(postAddressRequestDto);

        List<Permissions> permissions = new ArrayList<>();
        permissions.add(Permissions.ADMIN);
        adminWorkerRequest.setPermissions(permissions);

        authenticationService.registerWorker(adminWorkerRequest);
    }
}
