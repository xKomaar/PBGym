package pl.pbgym.service.user.member;

import org.modelmapper.ModelMapper;
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
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
            creditCardInfoRepository.findByMemberEmail(email).ifPresent((info -> {
                throw new CreditCardInfoAlreadyPresentException("There is already credit card info added to this user!");
            }));

            CreditCardInfo creditCardInfo = modelMapper.map(requestDto, CreditCardInfo.class);
            creditCardInfo.setMember(m);

            creditCardInfo.setCardNumber(encrypt(creditCardInfo.getCardNumber()));
            creditCardInfo.setCvc(encrypt(creditCardInfo.getCvc()));
            creditCardInfo.setExpirationMonth(encrypt(creditCardInfo.getExpirationMonth()));
            creditCardInfo.setExpirationYear(encrypt(creditCardInfo.getExpirationYear()));

            creditCardInfoRepository.save(creditCardInfo);
        },
        () -> {
            throw new MemberNotFoundException("Member not found with email " + email);
        });
    }

    public GetCreditCardInfoResponseDto getHiddenCreditCardInfo(String email) {
        if (memberService.memberExists(email)) {
            GetCreditCardInfoResponseDto getCreditCardInfoResponseDto;
            getCreditCardInfoResponseDto = creditCardInfoRepository.findByMemberEmail(email)
                    .map(info -> {
                        GetCreditCardInfoResponseDto dto = modelMapper.map(info, GetCreditCardInfoResponseDto.class);
                        dto.setCardNumber("************" + (decrypt(dto.getCardNumber())).substring(12));
                        dto.setCvc("***");
                        dto.setExpirationMonth(decrypt(dto.getExpirationMonth()));
                        dto.setExpirationYear(decrypt(dto.getExpirationYear()));
                        return dto;
                    })
                    .orElse(null);
            return getCreditCardInfoResponseDto;
        } else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public GetCreditCardInfoResponseDto getFullCreditCardInfo(String email, GetFullCreditCardInfoRequest requestDto) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), m.getPassword())) {
                        throw new IncorrectPasswordException("Password is incorrect");
                    }
                },
                () -> {
                    throw new MemberNotFoundException("Member not found with email " + email);
                });
        return creditCardInfoRepository.findByMemberEmail(email)
                .map(info -> {
                    GetCreditCardInfoResponseDto dto = modelMapper.map(info, GetCreditCardInfoResponseDto.class);
                    dto.setCardNumber(decrypt(dto.getCardNumber()));
                    dto.setCvc(decrypt(dto.getCvc()));
                    dto.setExpirationMonth(decrypt(dto.getExpirationMonth()));
                    dto.setExpirationYear(decrypt(dto.getExpirationYear()));
                    return dto;
                })
                .orElse(null);
    }

    @Transactional
    public void deleteCreditCardInfo(String email) {
        Optional<CreditCardInfo> info = creditCardInfoRepository.findByMemberEmail(email);
        info.ifPresentOrElse(creditCardInfoRepository::delete,
                () -> {
                    throw new CreditCardInfoNotFoundException("CreditCardInfo not found for email: " + email);
                });
    }

    private String encrypt(String plainText) {
        try {
            return encryptionUtil.encrypt(plainText);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    private String decrypt(String encryptedText) {
        try {
            return encryptionUtil.decrypt(encryptedText);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}

