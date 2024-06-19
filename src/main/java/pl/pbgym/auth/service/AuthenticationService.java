package pl.pbgym.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.pbgym.auth.requests.*;
import pl.pbgym.domain.*;
import pl.pbgym.repository.*;

@Service
public class AuthenticationService {

    private final AbstractUserRepository abstractUserRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final WorkerRepository workerRepository;
    private final AddressRepository addressRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AbstractUserRepository abstractUserRepository, MemberRepository memberRepository, TrainerRepository trainerRepository, WorkerRepository workerRepository,
                                 AddressRepository addressRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService, AuthenticationManager authenticationManager) {
        this.abstractUserRepository = abstractUserRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.workerRepository = workerRepository;
        this.addressRepository = addressRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public void setAbstractUserFields(AbstractUserRequest request, AbstractUser abstractUser) {
        abstractUser.setEmail(request.getEmail());
        abstractUser.setPassword(passwordEncoder.encode(request.getPassword()));
        abstractUser.setName(request.getName());
        abstractUser.setSurname(request.getSurname());
        abstractUser.setBirthdate(request.getBirthdate());
        abstractUser.setPesel(request.getPesel());
        abstractUser.setPhoneNumber(request.getPhoneNumber());

        AddressRequest addressRequest = request.getAddress();
        Address address = new Address();
        address.setCity(addressRequest.getCity());
        address.setStreetName(addressRequest.getStreetName());
        address.setBuildingNumber(addressRequest.getBuildingNumber());
        address.setApartmentNumber(addressRequest.getApartmentNumber());
        address.setPostalCode(addressRequest.getPostalCode());
        addressRepository.save(address);

        abstractUser.setAddress(address);
    }

    public void registerMember(MemberRegisterRequest request) {
        Member member = new Member();
        setAbstractUserFields(request, member);

        memberRepository.save(member);
    }

    public void registerTrainer(TrainerRegisterRequest request) {
        Trainer trainer = new Trainer();
        setAbstractUserFields(request, trainer);

        trainerRepository.save(trainer);
    }

    public void registerWorker(WorkerRegisterRequest request) {
        Worker worker = new Worker();
        setAbstractUserFields(request, worker);

        worker.setPosition(request.getPosition());
        worker.setIdCardNumber(request.getIdCardNumber());
        workerRepository.save(worker);

        if(!request.getPermissionsList().isEmpty()) {
            for(Permissions p : request.getPermissionsList()) {
                Permission permission = new Permission();
                permission.setWorker(worker);
                permission.set(p);
                permissionRepository.save(permission);
            }
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        AbstractUser abstractUser = abstractUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String jwt = jwtService.generateToken(abstractUser);
        return new AuthenticationResponse(jwt);
    }
}