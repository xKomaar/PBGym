package pl.pbgym.service.pass;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pl.pbgym.service.user.member.PaymentService;
import pl.pbgym.service.user.member.MemberService;
import pl.pbgym.service.user.trainer.GroupClassService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PassService {

    private static final Logger logger = LoggerFactory.getLogger(PassService.class);

    private final OfferRepository offerRepository;
    private final PassRepository passRepository;
    private final HistoricalPassRepository historicalPassRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final MemberService memberService;
    private final PaymentService paymentService;
    private final GroupClassService groupClassService;

    @Autowired
    public PassService(OfferRepository offerRepository, PassRepository passRepository, HistoricalPassRepository historicalPassRepository, MemberRepository memberRepository, ModelMapper modelMapper, MemberService memberService, PaymentService paymentService, GroupClassService groupClassService) {
        this.offerRepository = offerRepository;
        this.passRepository = passRepository;
        this.historicalPassRepository = historicalPassRepository;
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
        this.memberService = memberService;
        this.paymentService = paymentService;
        this.groupClassService = groupClassService;
    }

    @Transactional
    public void createPass(String email, PostPassRequestDto passRequestDto) {
        logger.info("Tworzenie karnetu dla użytkownika o emailu {} z ofertą o ID {}.", email, passRequestDto.getOfferId());
        memberRepository.findByEmail(email).ifPresentOrElse(
                (member -> offerRepository.findById(passRequestDto.getOfferId()).ifPresentOrElse(
                        offer -> {
                            if (!offer.isActive()) {
                                logger.error("Oferta o ID {} nie jest aktywna.", passRequestDto.getOfferId());
                                throw new OfferNotActiveException("Offer is not active with id " + passRequestDto.getOfferId());
                            }
                            Optional<Pass> currentPass = passRepository.findByMemberEmail(email);
                            currentPass.ifPresent(pass -> {
                                logger.error("Członek o emailu {} już posiada aktywny karnet.", email);
                                throw new MemberAlreadyHasActivePassException("Member already has an active pass!");
                            });

                            try {
                                paymentService.registerPayment(offer.getMonthlyPrice() + offer.getEntryFee(), member);
                                logger.info("Płatność za karnet użytkownika o emailu {} została zarejestrowana.", email);
                            } catch (NoPaymentMethodException | PaymentMethodExpiredException e) {
                                logger.error("Błąd podczas płatności za karnet: {}.", e.getMessage());
                                throw new PassNotCreatedDueToPaymentFailure(e.getMessage());
                            }

                            currentPass.ifPresent(p -> {
                                logger.info("Usunięcie nieaktywnego karnetu dla użytkownika o emailu {}.", email);
                                passRepository.delete(p);
                                passRepository.flush();
                            });
                            Pass pass = createPassClass(member, offer);
                            passRepository.save(pass);
                            logger.info("Dodano karnet dla użytkownika o emailu {} z ID karnetu {}.", email, pass.getId());
                        },
                        () -> {
                            logger.error("Nie znaleziono oferty o ID {}.", passRequestDto.getOfferId());
                            throw new OfferNotFoundException("Offer not found with id " + passRequestDto.getOfferId());
                        }
                )),
                () -> {
                    logger.error("Nie znaleziono członka o emailu {}.", email);
                    throw new MemberNotFoundException("Member not found with email " + email);
                }
        );
    }

    public GetPassResponseDto getPassByEmail(String email) {
        logger.info("Pobieranie karnetu dla użytkownika o emailu {}.", email);
        if (memberService.memberExists(email)) {
            return passRepository.findByMemberEmail(email)
                    .map(pass -> {
                        logger.info("Znaleziono karnet o ID {} dla użytkownika o emailu {}.", pass.getId(), email);
                        return modelMapper.map(pass, GetPassResponseDto.class);
                    })
                    .orElseGet(() -> {
                        logger.info("Brak aktywnego karnetu dla użytkownika o emailu {}.", email);
                        return null;
                    });
        } else {
            logger.error("Nie znaleziono członka o emailu {}.", email);
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public List<GetHistoricalPassResponseDto> getHistoricalPassesByEmail(String email) {
        logger.info("Pobieranie historycznych karnetów dla użytkownika o emailu {}.", email);
        if (memberService.memberExists(email)) {
            List<GetHistoricalPassResponseDto> historicalPasses = historicalPassRepository.findAllByMemberEmail(email)
                    .stream()
                    .map(historicalPass -> modelMapper.map(historicalPass, GetHistoricalPassResponseDto.class))
                    .toList();
            logger.info("Znaleziono {} historycznych karnetów dla użytkownika o emailu {}.", historicalPasses.size(), email);
            return historicalPasses;
        } else {
            logger.error("Nie znaleziono członka o emailu {}.", email);
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    @Transactional
    public void deactivateExpiredPasses() {
        logger.info("Dezaktywacja wygasłych karnetów.");
        List<Pass> passes = passRepository.getExpiredPassesForDeactivation();
        passes.forEach(this::deactivatePass);
        logger.info("Zdezaktywowano {} wygasłych karnetów.", passes.size());
    }

    @Transactional
    public void deactivatePass(Pass pass) {
        logger.info("Dezaktywacja karnetu o ID {} dla użytkownika o emailu {}.", pass.getId(), pass.getMember().getEmail());
        HistoricalPass historicalPass = modelMapper.map(pass, HistoricalPass.class);
        historicalPass.setDateEnd(LocalDateTime.now());

        Member member = pass.getMember();
        groupClassService.getAllUpcomingGroupClassesByMemberEmail(member.getEmail())
                .forEach(getGroupClassResponseDto -> {
                    groupClassService.signOutOfGroupClass(getGroupClassResponseDto.getId(), member.getEmail());
                    logger.info("Użytkownik o emailu {} został wypisany z zajęć grupowych o ID {}.", member.getEmail(), getGroupClassResponseDto.getId());
                });

        historicalPassRepository.save(historicalPass);
        passRepository.delete(pass);
        logger.info("Karnet o ID {} został zdezaktywowany.", pass.getId());
    }

    @Transactional(noRollbackFor = {NoPaymentMethodException.class, PaymentMethodExpiredException.class})
    public void chargeForActivePasses() {
        logger.info("Pobieranie opłat za aktywne karnety.");
        List<Pass> passes = passRepository.findAll();
        LocalDate today = LocalDate.now();
        passes.forEach(pass -> {
            if (pass.getDateOfNextPayment().equals(today)) {
                try {
                    paymentService.registerPayment(pass.getMonthlyPrice(), pass.getMember());
                    logger.info("Zarejestrowano opłatę za karnet o ID {} dla użytkownika o emailu {}.", pass.getId(), pass.getMember().getEmail());

                    if (pass.getDateOfNextPayment().plusMonths(1).getMonthValue() ==
                            pass.getDateEnd().getMonthValue()) {
                        pass.setDateOfNextPayment(null);
                        logger.info("Ostatnia opłata za karnet o ID {} została pobrana.", pass.getId());
                    } else {
                        pass.setDateOfNextPayment(pass.getDateOfNextPayment().plusMonths(1));
                    }
                } catch (NoPaymentMethodException | PaymentMethodExpiredException e) {
                    logger.error("Błąd przy pobieraniu opłaty za karnet o ID {}: {}. Karnet zostanie zdezaktywowany.", pass.getId(), e.getMessage());
                    this.deactivatePass(pass);
                }
            }
        });
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
}
