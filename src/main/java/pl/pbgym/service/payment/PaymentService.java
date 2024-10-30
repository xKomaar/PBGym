package pl.pbgym.service.payment;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.payment.Payment;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.payment.GetPaymentResponseDto;
import pl.pbgym.dto.user.member.GetCreditCardInfoResponseDto;
import pl.pbgym.exception.payment.NoPaymentMethodException;
import pl.pbgym.exception.payment.PaymentMethodExpiredException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.repository.payment.PaymentRepository;
import pl.pbgym.service.user.member.CreditCardInfoService;
import pl.pbgym.service.user.member.MemberService;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

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
    public void registerPayment(Double amount, Member member)
            throws NoPaymentMethodException, PaymentMethodExpiredException {
        if(member == null || member.getCreditCardInfo() == null) {
            throw new NoPaymentMethodException("No credit card information");
        } else {
            if (isCreditCardExpired(creditCardInfoService.getHiddenCreditCardInfo(member.getEmail()))) {
                throw new PaymentMethodExpiredException("Credit card is expired");
            }
        }

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setCardNumber(member.getCreditCardInfo().getCardNumber());
        payment.setExpirationMonth(member.getCreditCardInfo().getExpirationMonth());
        payment.setExpirationYear(member.getCreditCardInfo().getExpirationYear());
        payment.setName(member.getName());
        payment.setSurname(member.getSurname());
        payment.setEmail(member.getEmail());
        payment.setPesel(member.getPesel());

        paymentRepository.save(payment);
    }

    public List<GetPaymentResponseDto> getAllPaymentsByEmail(String email) {
        if(memberService.memberExists(email)) {
            List<Payment> payments = paymentRepository.findAllByMemberEmail(email);
            return payments
                    .stream()
                    .map(payment -> modelMapper.map(payment, GetPaymentResponseDto.class))
                    .toList();
        } else {
            throw new MemberNotFoundException("Member not found!");
        }
    }

    private boolean isCreditCardExpired(GetCreditCardInfoResponseDto creditCardInfo) {
        int expirationMonth = Integer.parseInt(creditCardInfo.getExpirationMonth()); // "MM"
        int expirationYear = Integer.parseInt(creditCardInfo.getExpirationYear()) + 2000; // "YY" to full year

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        return (expirationYear < currentYear) ||
                (expirationYear == currentYear && expirationMonth < currentMonth);
    }
}
