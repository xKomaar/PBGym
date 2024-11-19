package pl.pbgym.service.user.member;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.member.Payment;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.user.member.GetPaymentResponseDto;
import pl.pbgym.dto.user.member.GetCreditCardInfoResponseDto;
import pl.pbgym.exception.payment.NoPaymentMethodException;
import pl.pbgym.exception.payment.PaymentMethodExpiredException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.user.member.PaymentRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final CreditCardInfoService creditCardInfoService;
    private final MemberService memberService;
    private final ModelMapper modelMapper;

    public PaymentService(PaymentRepository paymentRepository, CreditCardInfoService creditCardInfoService, MemberService memberService, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.creditCardInfoService = creditCardInfoService;
        this.memberService = memberService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void registerPayment(Double amount, Member member) throws NoPaymentMethodException, PaymentMethodExpiredException {
        if (member == null || member.getCreditCardInfo() == null) {
            logger.error("Nie udało się zarejestrować płatności - brak informacji o karcie kredytowej dla użytkownika: {}", member != null ? member.getEmail() : "nieznany");
            throw new NoPaymentMethodException("No credit card information");
        }

        GetCreditCardInfoResponseDto creditCardInfoResponseDto = creditCardInfoService.getHiddenCreditCardInfo(member.getEmail());
        if (isCreditCardExpired(creditCardInfoResponseDto)) {
            logger.error("Nie udało się zarejestrować płatności - karta kredytowa użytkownika {} jest przeterminowana", member.getEmail());
            throw new PaymentMethodExpiredException("Credit card is expired");
        }

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setCardNumber(creditCardInfoResponseDto.getCardNumber());
        payment.setExpirationMonth(creditCardInfoResponseDto.getExpirationMonth());
        payment.setExpirationYear(creditCardInfoResponseDto.getExpirationYear());
        payment.setName(member.getName());
        payment.setSurname(member.getSurname());
        payment.setEmail(member.getEmail());
        payment.setPesel(member.getPesel());

        paymentRepository.save(payment);
        logger.info("Pomyślnie zarejestrowano płatność w wysokości {} PLN dla użytkownika {}", amount, member.getEmail());
    }

    public List<GetPaymentResponseDto> getAllPaymentsByEmail(String email) {
        logger.info("Pobieranie historii płatności dla użytkownika z adresem email: {}", email);
        if (memberService.memberExists(email)) {
            List<Payment> payments = paymentRepository.findAllByMemberEmail(email);
            logger.info("Znaleziono {} płatności dla użytkownika z adresem email: {}", payments.size(), email);
            return payments.stream()
                    .map(payment -> modelMapper.map(payment, GetPaymentResponseDto.class))
                    .toList();
        } else {
            logger.error("Nie znaleziono członka z adresem email: {}", email);
            throw new MemberNotFoundException("Member not found!");
        }
    }

    private boolean isCreditCardExpired(GetCreditCardInfoResponseDto creditCardInfo) {
        int expirationMonth = Integer.parseInt(creditCardInfo.getExpirationMonth());
        int expirationYear = Integer.parseInt(creditCardInfo.getExpirationYear()) + 2000;

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        boolean expired = (expirationYear < currentYear) || (expirationYear == currentYear && expirationMonth < currentMonth);
        logger.info("Sprawdzanie ważności karty kredytowej - karta {}przeterminowana", expired ? "" : "nie ");
        return expired;
    }
}
