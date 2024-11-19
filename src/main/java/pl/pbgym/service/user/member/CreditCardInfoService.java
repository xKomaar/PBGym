package pl.pbgym.service.user.member;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.member.CreditCardInfo;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.user.member.GetCreditCardInfoResponseDto;
import pl.pbgym.dto.user.member.GetFullCreditCardInfoRequest;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.member.CreditCardInfoAlreadyPresentException;
import pl.pbgym.exception.user.member.CreditCardInfoNotFoundException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.util.encryption.EncryptionUtil;

import java.util.Optional;

@Service
public class CreditCardInfoService {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardInfoService.class);

    private final MemberService memberService;
    private final ModelMapper modelMapper;
    private final CreditCardInfoRepository creditCardInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final EncryptionUtil encryptionUtil;

    public CreditCardInfoService(MemberService memberService, ModelMapper modelMapper, CreditCardInfoRepository creditCardInfoRepository, PasswordEncoder passwordEncoder, MemberRepository memberRepository, @Qualifier("creditCardEncryptionUtil") EncryptionUtil encryptionUtil) {
        this.memberService = memberService;
        this.modelMapper = modelMapper;
        this.creditCardInfoRepository = creditCardInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Transactional
    public void saveCreditCardInfo(String email, PostCreditCardInfoRequestDto requestDto) {
        logger.info("Próba zapisania informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    creditCardInfoRepository.findByMemberEmail(email).ifPresent((info -> {
                        logger.error("Nieudana próba zapisania informacji o karcie kredytowej. Użytkownik o emailu {} już posiada zapisane dane karty.", email);
                        throw new CreditCardInfoAlreadyPresentException("There is already credit card info added to this user!");
                    }));

                    CreditCardInfo creditCardInfo = modelMapper.map(requestDto, CreditCardInfo.class);
                    creditCardInfo.setMember(m);

                    creditCardInfo.setCardNumber(encrypt(creditCardInfo.getCardNumber()));
                    creditCardInfo.setCvc(encrypt(creditCardInfo.getCvc()));
                    creditCardInfo.setExpirationMonth(encrypt(creditCardInfo.getExpirationMonth()));
                    creditCardInfo.setExpirationYear(encrypt(creditCardInfo.getExpirationYear()));

                    creditCardInfoRepository.save(creditCardInfo);
                    logger.info("Pomyślnie zapisano informacje o karcie kredytowej dla użytkownika o emailu: {}", email);
                },
                () -> {
                    logger.error("Nie znaleziono użytkownika o emailu: {}", email);
                    throw new MemberNotFoundException("Member not found with email " + email);
                });
    }

    public GetCreditCardInfoResponseDto getHiddenCreditCardInfo(String email) {
        logger.info("Pobieranie ukrytych informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
        if (memberService.memberExists(email)) {
            return creditCardInfoRepository.findByMemberEmail(email)
                    .map(info -> {
                        GetCreditCardInfoResponseDto dto = modelMapper.map(info, GetCreditCardInfoResponseDto.class);
                        dto.setCardNumber("************" + (decrypt(dto.getCardNumber())).substring(12));
                        dto.setCvc("***");
                        dto.setExpirationMonth(decrypt(dto.getExpirationMonth()));
                        dto.setExpirationYear(decrypt(dto.getExpirationYear()));
                        logger.info("Pomyślnie pobrano ukryte informacje o karcie kredytowej dla użytkownika o emailu: {}", email);
                        return dto;
                    })
                    .orElse(null);
        } else {
            logger.error("Nie znaleziono użytkownika o emailu: {}", email);
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public GetCreditCardInfoResponseDto getFullCreditCardInfo(String email, GetFullCreditCardInfoRequest requestDto) {
        logger.info("Pobieranie pełnych informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), m.getPassword())) {
                        logger.error("Niepoprawne hasło podane przy próbie pobrania pełnych informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
                        throw new IncorrectPasswordException("Password is incorrect");
                    }
                },
                () -> {
                    logger.error("Nie znaleziono użytkownika o emailu: {}", email);
                    throw new MemberNotFoundException("Member not found with email " + email);
                });
        return creditCardInfoRepository.findByMemberEmail(email)
                .map(info -> {
                    GetCreditCardInfoResponseDto dto = modelMapper.map(info, GetCreditCardInfoResponseDto.class);
                    dto.setCardNumber(decrypt(dto.getCardNumber()));
                    dto.setCvc(decrypt(dto.getCvc()));
                    dto.setExpirationMonth(decrypt(dto.getExpirationMonth()));
                    dto.setExpirationYear(decrypt(dto.getExpirationYear()));
                    logger.info("Pomyślnie pobrano pełne informacje o karcie kredytowej dla użytkownika o emailu: {}", email);
                    return dto;
                })
                .orElse(null);
    }

    @Transactional
    public void deleteCreditCardInfo(String email) {
        logger.info("Próba usunięcia informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
        Optional<CreditCardInfo> info = creditCardInfoRepository.findByMemberEmail(email);
        info.ifPresentOrElse(creditCardInfo -> {
                    creditCardInfoRepository.delete(creditCardInfo);
                    logger.info("Pomyślnie usunięto informacje o karcie kredytowej dla użytkownika o emailu: {}", email);
                },
                () -> {
                    logger.error("Nie znaleziono informacji o karcie kredytowej dla użytkownika o emailu: {}", email);
                    throw new CreditCardInfoNotFoundException("CreditCardInfo not found for email: " + email);
                });
    }

    private String encrypt(String plainText) {
        try {
            return encryptionUtil.encrypt(plainText);
        } catch (Exception e) {
            logger.error("Nieudana próba szyfrowania: {}", e.getMessage());
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    private String decrypt(String encryptedText) {
        try {
            return encryptionUtil.decrypt(encryptedText);
        } catch (Exception e) {
            logger.error("Nieudana próba odszyfrowania: {}", e.getMessage());
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}
