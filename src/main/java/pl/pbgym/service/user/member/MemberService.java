package pl.pbgym.service.user.member;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.user.member.GetAllMembersResponseDto;
import pl.pbgym.dto.user.member.GetMemberResponseDto;
import pl.pbgym.dto.user.member.UpdateMemberRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

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
        logger.info("Pobieranie danych członka z adresem email: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.map(m -> {
            logger.info("Znaleziono członka z adresem email: {}", email);
            return modelMapper.map(m, GetMemberResponseDto.class);
        }).orElseThrow(() -> {
            logger.error("Nie znaleziono członka z adresem email: {}", email);
            return new MemberNotFoundException("Member not found with email: " + email);
        });
    }

    public List<GetAllMembersResponseDto> getAllMembers() {
        logger.info("Pobieranie listy wszystkich członków");
        return memberRepository.findAll().stream()
                .map(member -> {
                    GetAllMembersResponseDto responseDto = modelMapper.map(member, GetAllMembersResponseDto.class);
                    Pass pass = member.getPass();
                    if (pass != null) {
                        responseDto.setPassActive(true);
                        responseDto.setPassDateEnd(pass.getDateEnd());
                    } else {
                        responseDto.setPassActive(false);
                        responseDto.setPassDateEnd(null);
                    }
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMember(String email, UpdateMemberRequestDto updateMemberRequestDto) {
        logger.info("Aktualizacja danych członka z adresem email: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    modelMapper.map(updateMemberRequestDto, m);
                    logger.info("Pomyślnie zaktualizowano dane członka z adresem email: {}", email);
                },
                () -> {
                    logger.error("Nie znaleziono członka z adresem email: {}", email);
                    throw new MemberNotFoundException("Member not found with email: " + email);
                });
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        logger.info("Aktualizacja hasła dla członka z adresem email: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    if (!passwordEncoder.matches(oldPassword, m.getPassword())) {
                        logger.error("Niepoprawne stare hasło dla członka z adresem email: {}", email);
                        throw new IncorrectPasswordException("Old password is incorrect");
                    } else {
                        m.setPassword(passwordEncoder.encode(newPassword));
                        logger.info("Pomyślnie zaktualizowano hasło dla członka z adresem email: {}", email);
                    }
                },
                () -> {
                    logger.error("Nie znaleziono członka z adresem email: {}", email);
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public void updatePasswordWithoutOldPasswordCheck(String newPassword, String email) {
        logger.info("Aktualizacja hasła bez sprawdzania starego hasła dla członka z adresem email: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    m.setPassword(passwordEncoder.encode(newPassword));
                    logger.info("Pomyślnie zaktualizowano hasło dla członka z adresem email: {}", email);
                },
                () -> {
                    logger.error("Nie znaleziono członka z adresem email: {}", email);
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public AuthenticationResponseDto updateEmail(String email, String newEmail) {
        logger.info("Aktualizacja adresu email z {} na {}", email, newEmail);
        Optional<Member> member = memberRepository.findByEmail(email);
        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto();
        member.ifPresentOrElse(m -> {
                    m.setEmail(newEmail);
                    String jwt = authenticationService.generateJwtToken(m);
                    authenticationResponseDto.setJwt(jwt);
                    logger.info("Pomyślnie zaktualizowano adres email dla członka z {} na {}", email, newEmail);
                },
                () -> {
                    logger.error("Nie znaleziono członka z adresem email: {}", email);
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
        return authenticationResponseDto;
    }

    public boolean memberExists(String email) {
        logger.info("Sprawdzanie czy członek z adresem email {} istnieje", email);
        boolean exists = memberRepository.findByEmail(email).isPresent();
        logger.info("Czy członek z adresem email {} istnieje: {}", email, exists);
        return exists;
    }
}
