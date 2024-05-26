package pl.pbgym.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.pbgym.auth.domain.AuthenticationRequest;
import pl.pbgym.auth.domain.AuthenticationResponse;
import pl.pbgym.auth.domain.MemberRegisterRequest;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Address;
import pl.pbgym.domain.Member;
import pl.pbgym.repository.AbstractUserRepository;
import pl.pbgym.repository.AddressRepository;
import pl.pbgym.repository.MemberRepository;

@Service
public class AuthenticationService {

    private final AbstractUserRepository abstractUserRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AbstractUserRepository abstractUserRepository, MemberRepository memberRepository,
                                 AddressRepository addressRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService, AuthenticationManager authenticationManager) {
        this.abstractUserRepository = abstractUserRepository;
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public void registerMemberWithAddress(MemberRegisterRequest request) {
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setName(request.getName());
        member.setSurname(request.getSurname());
        member.setBirthdate(request.getBirthdate());
        member.setPesel(request.getPesel());
        member.setPhoneNumber(request.getPhoneNumber());

        Address address = request.getAddress();
        addressRepository.save(address);

        member.setAddress(address);
        memberRepository.save(member);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        AbstractUser abstractUser = abstractUserRepository.findByEmail(request.getEmail())
                .orElseThrow();
        String jwt = jwtService.generateToken(abstractUser);
        return new AuthenticationResponse(jwt);
    }
}