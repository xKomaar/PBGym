package pl.pbgym.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.*;
import pl.pbgym.dto.auth.*;
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

    public void setAbstractUserFields(PostAbstractUserDto request, AbstractUser abstractUser) {
        abstractUser.setEmail(request.getEmail());
        abstractUser.setPassword(passwordEncoder.encode(request.getPassword()));
        abstractUser.setName(request.getName());
        abstractUser.setSurname(request.getSurname());
        abstractUser.setBirthdate(request.getBirthdate());
        abstractUser.setPesel(request.getPesel());
        abstractUser.setPhoneNumber(request.getPhoneNumber());

        PostAddressRequestDto postAddressRequestDto = request.getAddress();
        Address address = new Address();
        address.setCity(postAddressRequestDto.getCity());
        address.setStreetName(postAddressRequestDto.getStreetName());
        address.setBuildingNumber(postAddressRequestDto.getBuildingNumber());
        address.setApartmentNumber(postAddressRequestDto.getApartmentNumber());
        address.setPostalCode(postAddressRequestDto.getPostalCode());
        addressRepository.save(address);

        abstractUser.setAddress(address);
    }

    public void registerMember(PostMemberRequestDto request) {
        Member member = new Member();
        setAbstractUserFields(request, member);

        memberRepository.save(member);
    }

    public void registerTrainer(PostTrainerRequestDto request) {
        Trainer trainer = new Trainer();
        setAbstractUserFields(request, trainer);

        trainerRepository.save(trainer);
    }

    public void registerWorker(PostWorkerRequestDto request) {
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

    public PostAuthenticationResponseDto authenticate(PostAuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        AbstractUser abstractUser = abstractUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String jwt = jwtService.generateToken(abstractUser);
        return new PostAuthenticationResponseDto(jwt);
    }
}