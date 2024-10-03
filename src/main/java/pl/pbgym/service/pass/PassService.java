package pl.pbgym.service.pass;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.offer.Offer;
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.pass.GetPassResponseDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.exception.offer.OfferNotActiveException;
import pl.pbgym.exception.offer.OfferNotFoundException;
import pl.pbgym.exception.pass.MemberAlreadyHasActivePassException;
import pl.pbgym.exception.payment.NoPaymentMethodException;
import pl.pbgym.exception.payment.PaymentMethodExpiredException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.offer.OfferRepository;
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
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final MemberService memberService;
    private final PaymentService paymentService;

    @Autowired
    public PassService(OfferRepository offerRepository, PassRepository passRepository, MemberRepository memberRepository, ModelMapper modelMapper, MemberService memberService, PaymentService paymentService) {
        this.offerRepository = offerRepository;
        this.passRepository = passRepository;
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
                                if (pass.isActive()) {
                                    throw new MemberAlreadyHasActivePassException("Member already has an active pass!");
                                }
                            });

                            try {
                                paymentService.registerPayment(offer.getMonthlyPrice(), member);
                            } catch (NoPaymentMethodException e) {
                                throw new NoPaymentMethodException("No payment method, pass not created.");
                            } catch (PaymentMethodExpiredException e) {
                                throw new PaymentMethodExpiredException("Payment method expired, pass not created");
                            }

                            //if there is an inactive pass, it can be deleted and replaced with new one
                            currentPass.ifPresent(passRepository::delete);
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
        pass.setActive(true);
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

    @Transactional
    public void deactivateExpiredPasses() {
        passRepository.deactivateExpiredPasses();
    }

    @Transactional
    public void chargeForActivePasses() {
        List<Pass> passes = passRepository.findAllActive();
        LocalDate today = LocalDate.now();
        passes.forEach(pass -> {
            LocalDate nextPaymentDate = pass.getDateOfNextPayment();
            if (nextPaymentDate.equals(today)) {
                try {
                    this.chargeForAPass(pass);

                    if(pass.getDateOfNextPayment().plusMonths(1).getMonthValue() ==
                            pass.getDateEnd().getMonthValue()) {
                        //this was the last payment (upfront)
                        pass.setDateOfNextPayment(null);
                    } else {
                        pass.setDateOfNextPayment(pass.getDateOfNextPayment().plusMonths(1));
                    }
                } catch (NoPaymentMethodException | PaymentMethodExpiredException e) {
                    //deactivate a pass if the payment doesn't come through
                    pass.setActive(false);
                }
            }
        });
    }

    @Transactional
    public void chargeForAPass(Pass pass) {
        try {
            paymentService.registerPayment(pass.getMonthlyPrice(), pass.getMember());
        } catch (NoPaymentMethodException e) {
            throw new NoPaymentMethodException("No payment method, pass deactivated.");
        } catch (PaymentMethodExpiredException e) {
            throw new PaymentMethodExpiredException("Payment method expired, pass deactivated");
        }
    }
}
