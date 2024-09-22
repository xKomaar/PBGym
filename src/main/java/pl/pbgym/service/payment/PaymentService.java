package pl.pbgym.service.payment;

import org.springframework.stereotype.Service;
import pl.pbgym.repository.payment.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
}
