package pl.pbgym.service.pass;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pl.pbgym.domain.offer.Offer;
import pl.pbgym.domain.pass.HistoricalPass;
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.pass.GetHistoricalPassResponseDto;
import pl.pbgym.dto.pass.GetPassResponseDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.exception.offer.OfferNotActiveException;
import pl.pbgym.exception.offer.OfferNotFoundException;
import pl.pbgym.exception.pass.MemberAlreadyHasActivePassException;
import pl.pbgym.exception.pass.PassNotCreatedDueToPaymentFailure;
import pl.pbgym.exception.payment.NoPaymentMethodException;
import pl.pbgym.exception.payment.PaymentMethodExpiredException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.pass.HistoricalPassRepository;
import pl.pbgym.repository.pass.PassRepository;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.service.payment.PaymentService;
import pl.pbgym.service.user.member.MemberService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PassService {

    private final OfferRepository offerRepository;
    private final PassRepository passRepository;
    private final HistoricalPassRepository historicalPassRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final MemberService memberService;
    private final PaymentService paymentService;

    @Autowired
    public PassService(OfferRepository offerRepository, PassRepository passRepository, HistoricalPassRepository historicalPassRepository, MemberRepository memberRepository, ModelMapper modelMapper, MemberService memberService, PaymentService paymentService) {
        this.offerRepository = offerRepository;
        this.passRepository = passRepository;
        this.historicalPassRepository = historicalPassRepository;
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
        this.memberService = memberService;
        this.paymentService = paymentService;
    }

    @Transactional
    public void createPass(String email, PostPassRequestDto passRequestDto) {
        memberRepository.findByEmail(email).ifPresentOrElse(
                (member -> offerRepository.findById(passRequestDto.getOfferId()).ifPresentOrElse(
                        offer -> {
                            if (!offer.isActive()) {
                                throw new OfferNotActiveException("Offer is not active with id " + passRequestDto.getOfferId());
                            }
                            Optional<Pass> currentPass = passRepository.findByMemberEmail(email);
                            currentPass.ifPresent(pass -> {
                                throw new MemberAlreadyHasActivePassException("Member already has an active pass!");
                            });

                            try {
                                paymentService.registerPayment(offer.getMonthlyPrice() + offer.getEntryFee(), member);
                            } catch (NoPaymentMethodException | PaymentMethodExpiredException e) {
                                throw new PassNotCreatedDueToPaymentFailure(e.getMessage());
                            }

                            //if there is an inactive pass, it can be deleted and replaced with new one
                            currentPass.ifPresent(p -> {
                                passRepository.delete(p);
                                passRepository.flush();
                            });
                            Pass pass = createPassClass(member, offer);
                            passRepository.save(pass);
                        },
                        () -> {
                            throw new OfferNotFoundException("Offer not found with id " + passRequestDto.getOfferId());
                        }
                )),
                () -> {
                    throw new MemberNotFoundException("Member not found with email " + email);
                }
        );
    }

    private static Pass createPassClass(Member member, Offer offer) {
        LocalDateTime dateStart = LocalDateTime.now();
        LocalDate dateOfNextPayment = dateStart.toLocalDate().plusMonths(1);
        LocalDateTime dateEnd = dateStart.plusMonths(offer.getDurationInMonths()).withHour(23).withMinute(59).withSecond(59);

        Pass pass = new Pass();
        pass.setTitle(offer.getTitle());
        pass.setDateStart(dateStart);
        pass.setDateOfNextPayment(dateOfNextPayment);
        pass.setDateEnd(dateEnd);
        pass.setMonthlyPrice(offer.getMonthlyPrice());
        pass.setMember(member);
        return pass;
    }

    public GetPassResponseDto getPassByEmail(String email) {
        if(memberService.memberExists(email)) {
            return passRepository.findByMemberEmail(email)
                    .map(pass -> modelMapper.map(pass, GetPassResponseDto.class))
                    .orElse(null);
        }
         else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public List<GetHistoricalPassResponseDto> getHistoricalPassesByEmail(String email) {
        if(memberService.memberExists(email)) {
            return historicalPassRepository.findAllByMemberEmail(email)
                    .stream()
                    .map(historicalPass -> modelMapper.map(historicalPass, GetHistoricalPassResponseDto.class))
                    .toList();
        }
        else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    @Transactional
    public void deactivateExpiredPasses() {
        List<Pass> passes = passRepository.getExpiredPassesForDeactivation();
        passes.forEach(this::deactivatePass);
    }

    @Transactional
    public void deactivatePass(Pass pass) {
        HistoricalPass historicalPass = modelMapper.map(pass, HistoricalPass.class);
        historicalPass.setDateEnd(LocalDateTime.now());

        historicalPassRepository.save(historicalPass);

        passRepository.delete(pass);
    }

    @Transactional(noRollbackFor = {NoPaymentMethodException.class, PaymentMethodExpiredException.class})
    public void chargeForActivePasses() {
        List<Pass> passes = passRepository.findAll();
        LocalDate today = LocalDate.now();
        passes.forEach(pass -> {
            if (pass.getDateOfNextPayment().equals(today)) {
                try {
                    paymentService.registerPayment(pass.getMonthlyPrice(), pass.getMember());

                    if(pass.getDateOfNextPayment().plusMonths(1).getMonthValue() ==
                            pass.getDateEnd().getMonthValue()) {
                        //this was the last payment (upfront)
                        pass.setDateOfNextPayment(null);
                    } else {
                        pass.setDateOfNextPayment(pass.getDateOfNextPayment().plusMonths(1));
                    }
                } catch (NoPaymentMethodException | PaymentMethodExpiredException e) {
                    //deactivate a pass if the payment doesn't come through
                    this.deactivatePass(pass);
                }
            }
        });
    }
}
