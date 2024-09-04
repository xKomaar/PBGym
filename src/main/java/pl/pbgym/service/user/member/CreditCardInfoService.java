package pl.pbgym.service.user.member;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.member.CreditCardInfo;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.user.member.GetCreditCardInfoResponseDto;
import pl.pbgym.dto.user.member.GetFullCreditCardInfoRequest;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.member.CreditCardInfoNotFoundException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.repository.user.member.MemberRepository;

import java.util.Optional;

@Service
public class CreditCardInfoService {

    private final MemberService memberService;
    private final ModelMapper modelMapper;
    private final CreditCardInfoRepository creditCardInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public CreditCardInfoService(MemberService memberService, ModelMapper modelMapper, CreditCardInfoRepository creditCardInfoRepository, PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.modelMapper = modelMapper;
        this.creditCardInfoRepository = creditCardInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void saveCreditCardInfo(String email, PostCreditCardInfoRequestDto requestDto) {
        if(memberService.memberExists(email)) {
            CreditCardInfo creditCardInfo = modelMapper.map(requestDto, CreditCardInfo.class);
            creditCardInfoRepository.save(creditCardInfo);
        }
        else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public GetCreditCardInfoResponseDto getHiddenCreditCardInfo(String email) {
        if(memberService.memberExists(email)) {
            GetCreditCardInfoResponseDto getCreditCardInfoResponseDto;
            getCreditCardInfoResponseDto = creditCardInfoRepository.findByMemberEmail(email)
                    .map(info -> modelMapper.map(info, GetCreditCardInfoResponseDto.class))
                    .orElse(null);
            if(getCreditCardInfoResponseDto != null) {
                getCreditCardInfoResponseDto.setCardNumber(
                        "************" + getCreditCardInfoResponseDto.getCardNumber().substring(12)
                );
                getCreditCardInfoResponseDto.setCvc("***");
            }
            return getCreditCardInfoResponseDto;
        }
        else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public GetCreditCardInfoResponseDto getFullCreditCardInfo(String email, GetFullCreditCardInfoRequest requestDto) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> {
                    if(!passwordEncoder.matches(requestDto.getPassword(), m.getPassword())) {
                        throw new IncorrectPasswordException("Password is incorrect");
                    }
                },
                () -> {
                    throw new MemberNotFoundException("Member not found with email " + email);
                });
        return creditCardInfoRepository.findByMemberEmail(email)
                .map(info -> modelMapper.map(info, GetCreditCardInfoResponseDto.class))
                .orElse(null);
    }

    @Transactional
    public void deleteCreditCardInfo(Long id) {
        Optional<CreditCardInfo> info = creditCardInfoRepository.findById(id);
        info.ifPresentOrElse(creditCardInfoRepository::delete,
                () -> {
                    throw new CreditCardInfoNotFoundException("CreditCardInfo not found with id: " + id);
                });
    }
}

