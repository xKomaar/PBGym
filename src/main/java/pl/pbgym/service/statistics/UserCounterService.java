package pl.pbgym.service.statistics;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.worker.Worker;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.exception.user_counter.WorkerNotAllowedToBeScannedException;
import pl.pbgym.repository.gym_entry.GymEntryRepository;
import pl.pbgym.repository.user.AbstractUserRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserCounterService {

    private final Set<String> emailsOfUsersAtTheGym = ConcurrentHashMap.newKeySet();
    private final Set<GymEntry> tempGymEntries = ConcurrentHashMap.newKeySet();
    private final AbstractUserRepository abstractUserRepository;
    private final GymEntryRepository gymEntryRepository;

    public UserCounterService(AbstractUserRepository abstractUserRepository, GymEntryRepository gymEntryRepository) {
        this.abstractUserRepository = abstractUserRepository;
        this.gymEntryRepository = gymEntryRepository;
    }

    public void registerUserAction(String email) {
        abstractUserRepository.findByEmail(email).ifPresentOrElse(abstractUser -> {
            if(abstractUser instanceof Worker) {
                throw new WorkerNotAllowedToBeScannedException("Worker is not allowed here!");
            } else if(abstractUser instanceof Member member) {
                if(member.getPass() == null || !member.getPass().isActive()) {
                    throw new NoActivePassException("Member with email " + email + "doesn't have an active pass!");
                }
            }
            // If the user is on the list, we register an exit.
            // If the user is not on the list, we register an entry
            if(emailsOfUsersAtTheGym.contains(email)) {
                GymEntry gymEntry = tempGymEntries.stream()
                        .filter(entry -> entry.getAbstractUser().getEmail().equals(email))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("GymEntry not found for email: " + email));

                gymEntry.setDateTimeOfExit(LocalDateTime.now());
                gymEntryRepository.save(gymEntry);

                tempGymEntries.remove(gymEntry);
                emailsOfUsersAtTheGym.remove(email);
            }
            else {
                GymEntry gymEntry = new GymEntry();
                gymEntry.setDateTimeOfEntry(LocalDateTime.now());
                gymEntry.setAbstractUser(abstractUser);
                tempGymEntries.add(gymEntry);
                emailsOfUsersAtTheGym.add(email);
            }
        }, () -> {
            throw new EntityNotFoundException("User with email " + email + " not found");
        });
    }

    public int getCurrentUserCount() {
        return emailsOfUsersAtTheGym.size();
    }
}
