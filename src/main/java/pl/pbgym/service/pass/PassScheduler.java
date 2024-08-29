package pl.pbgym.service.pass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PassScheduler {

    private final PassService passService;

    @Autowired
    public PassScheduler(PassService passService) {
        this.passService = passService;
    }

    //schedule to see if the payment was made and if the pass should be active/inactive
    //if the payment method is saved, the payment should go through automatically.
    //if the payment doesn't go through, the pass is deactivated until payment

    //schedule to see if the expiry date has passed and the pass should be deactivated

    @Scheduled(cron = "0 0 0 * * ?") //passes have a set hour of 23:59, so check at 00:00 every day is appropriate.
    public void deactivateExpiredPasses() {
        passService.deactivateExpiredPasses();
    }
}
