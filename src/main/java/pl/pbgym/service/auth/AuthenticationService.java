package pl.pbgym.service.auth;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ModelMapper modelMapper;

    @Autowired
    public AuthenticationService(AbstractUserRepository abstractUserRepository, MemberRepository memberRepository, TrainerRepository trainerRepository, WorkerRepository workerRepository,
                                 AddressRepository addressRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService, AuthenticationManager authenticationManager, ModelMapper modelMapper) {
        this.abstractUserRepository = abstractUserRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.workerRepository = workerRepository;
        this.addressRepository = addressRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void registerMember(PostMemberRequestDto postMemberRequestDto) {
        Address address = modelMapper.map(postMemberRequestDto.getAddress(), Address.class);
        addressRepository.save(address);
        Member member = modelMapper.map(postMemberRequestDto, Member.class);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setAddress(address);

        memberRepository.save(member);
    }

    @Transactional
    public void registerTrainer(PostTrainerRequestDto postTrainerRequestDto) {
        Address address = modelMapper.map(postTrainerRequestDto.getAddress(), Address.class);
        addressRepository.save(address);
        Trainer trainer = modelMapper.map(postTrainerRequestDto, Trainer.class);
        trainer.setPassword(passwordEncoder.encode(trainer.getPassword()));
        trainer.setAddress(address);

        trainerRepository.save(trainer);
    }

    @Transactional
    public void registerWorker(PostWorkerRequestDto postWorkerRequestDto) {
        Address address = modelMapper.map(postWorkerRequestDto.getAddress(), Address.class);
        addressRepository.save(address);
        Worker worker = modelMapper.map(postWorkerRequestDto, Worker.class);
        worker.setPassword(passwordEncoder.encode(worker.getPassword()));
        worker.setAddress(address);

        workerRepository.save(worker);

        if(!postWorkerRequestDto.getPermissionsList().isEmpty()) {
            for(Permissions p : postWorkerRequestDto.getPermissionsList()) {
                Permission permission = new Permission();
                permission.setWorker(worker);
                permission.set(p);
                permissionRepository.save(permission);
            }
        }
    }

    public AuthenticationResponseDto authenticate(PostAuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        AbstractUser abstractUser = abstractUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthenticationResponseDto(generateJwtToken(abstractUser));
    }

    public String generateJwtToken(AbstractUser abstractUser) {
        return jwtService.generateToken(abstractUser);
    }
}