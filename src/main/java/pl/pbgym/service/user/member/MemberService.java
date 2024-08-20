package pl.pbgym.service.user.member;


import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.Member;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.user.member.GetMemberResponseDto;
import pl.pbgym.dto.user.member.UpdateMemberRequestDto;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.user.MemberRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Autowired
    public MemberService(MemberRepository memberRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    public GetMemberResponseDto getMemberByEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.map(m -> modelMapper.map(m, GetMemberResponseDto.class))
                .orElseThrow(() -> new MemberNotFoundException("Member not found with email: " + email));
    }

    @Transactional
    public void updateMember(String email, UpdateMemberRequestDto updateMemberRequestDto) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> modelMapper.map(updateMemberRequestDto, m),
            () -> {
                throw new MemberNotFoundException("Member not found with email: " + email);
            });
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    if(!passwordEncoder.matches(oldPassword, m.getPassword())) {
                        throw new RuntimeException("Old password is incorrect");
                    } else {
                        m.setPassword(passwordEncoder.encode(newPassword));
                    }
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public void updatePasswordWithoutOldPasswordCheck(String newPassword, String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> m.setPassword(passwordEncoder.encode(newPassword)),
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public AuthenticationResponseDto updateEmail(String email, String newEmail) {
        Optional<Member> member = memberRepository.findByEmail(email);
        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto();
        member.ifPresentOrElse(m -> {
                    m.setEmail(newEmail);
                    String jwt = authenticationService.generateJwtToken(m);
                    authenticationResponseDto.setJwt(jwt);
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
        return authenticationResponseDto;
    }
}
