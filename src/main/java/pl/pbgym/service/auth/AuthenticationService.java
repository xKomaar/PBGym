package pl.pbgym.service.auth;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.*;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.worker.Permission;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.domain.user.worker.Worker;
import pl.pbgym.dto.auth.*;
import pl.pbgym.repository.user.*;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.repository.user.worker.PermissionRepository;
import pl.pbgym.repository.user.worker.WorkerRepository;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

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
        try {
            Address address = modelMapper.map(postMemberRequestDto.getAddress(), Address.class);
            addressRepository.save(address);
            Member member = modelMapper.map(postMemberRequestDto, Member.class);
            member.setPassword(passwordEncoder.encode(member.getPassword()));
            member.setAddress(address);

            memberRepository.save(member);
            logger.info("Zarejestrowano nowego członka: {}, email: {}", member.getName(), member.getEmail());
        } catch (Exception e) {
            logger.error("Nie udało się zarejestrować nowego członka. Szczegóły: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void registerTrainer(PostTrainerRequestDto postTrainerRequestDto) {
        try {
            Address address = modelMapper.map(postTrainerRequestDto.getAddress(), Address.class);
            addressRepository.save(address);
            Trainer trainer = modelMapper.map(postTrainerRequestDto, Trainer.class);
            trainer.setPassword(passwordEncoder.encode(trainer.getPassword()));
            trainer.setAddress(address);
            trainer.setVisible(false);

            trainerRepository.save(trainer);
            logger.info("Zarejestrowano nowego trenera: {}, email: {}", trainer.getName(), trainer.getEmail());
        } catch (Exception e) {
            logger.error("Nie udało się zarejestrować nowego trenera. Szczegóły: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void registerWorker(PostWorkerRequestDto postWorkerRequestDto) {
        try {
            Address address = modelMapper.map(postWorkerRequestDto.getAddress(), Address.class);
            addressRepository.save(address);
            Worker worker = modelMapper.map(postWorkerRequestDto, Worker.class);
            worker.setPassword(passwordEncoder.encode(worker.getPassword()));
            worker.setAddress(address);

            workerRepository.save(worker);

            if (!postWorkerRequestDto.getPermissions().isEmpty()) {
                for (PermissionType p : postWorkerRequestDto.getPermissions()) {
                    Permission permission = new Permission();
                    permission.setWorker(worker);
                    permission.set(p);
                    permissionRepository.save(permission);
                }
            }
            logger.info("Zarejestrowano nowego pracownika: {}, email: {}", worker.getName(), worker.getEmail());
        } catch (Exception e) {
            logger.error("Nie udało się zarejestrować nowego pracownika. Szczegóły: {}", e.getMessage());
            throw e;
        }
    }

    public AuthenticationResponseDto authenticate(PostAuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            AbstractUser abstractUser = abstractUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            logger.info("Pomyślnie uwierzytelniono użytkownika: {}", request.getEmail());
            return new AuthenticationResponseDto(generateJwtToken(abstractUser), abstractUser.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Nie udało się uwierzytelnić użytkownika: {}. Szczegóły: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    public String generateJwtToken(AbstractUser abstractUser) {
        try {
            String token = jwtService.generateToken(abstractUser);
            logger.info("Wygenerowano token JWT dla użytkownika: {}", abstractUser.getEmail());
            return token;
        } catch (Exception e) {
            logger.error("Nie udało się wygenerować tokena JWT dla użytkownika: {}. Szczegóły: {}", abstractUser.getEmail(), e.getMessage());
            throw e;
        }
    }
}
