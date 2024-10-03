package pl.pbgym.util.pass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.pbgym.service.pass.PassService;

@Component
public class PassScheduler {

    private final PassService passService;

    @Autowired
    public PassScheduler(PassService passService) {
        this.passService = passService;
    }

    //schedule to see if the payment should be made for any active passes,
    //if yes, then the payment will try to go through and if it doesn't, the pass will deactivate
    @Scheduled(cron = "0 0 13 * * ?") //Checking every day at 13:00
    public void chargeForActivePasses() {
        passService.chargeForActivePasses();
    }

    //schedule to see if the expiry date has passed and the pass should be deactivated
    @Scheduled(cron = "0 0 0 * * ?") //passes have a set hour of 23:59, so check at 00:00 every day is appropriate.
    public void deactivateExpiredPasses() {
        passService.deactivateExpiredPasses();
    }
}
