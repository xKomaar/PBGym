package pl.pbgym.service.user_counter;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.gym_entry.GymEntry;
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

    private final Set<Long> idsOfUsersAtTheGym = ConcurrentHashMap.newKeySet();
    private final Set<GymEntry> tempGymEntries = ConcurrentHashMap.newKeySet();
    private final AbstractUserRepository abstractUserRepository;
    private final GymEntryRepository gymEntryRepository;

    public UserCounterService(AbstractUserRepository abstractUserRepository, GymEntryRepository gymEntryRepository) {
        this.abstractUserRepository = abstractUserRepository;
        this.gymEntryRepository = gymEntryRepository;
    }

    public void registerUserAction(Long userId) {
        abstractUserRepository.findById(userId).ifPresentOrElse(abstractUser -> {
            if(abstractUser instanceof Worker) {
                throw new WorkerNotAllowedToBeScannedException("Worker is not allowed here!");
            } else if(abstractUser instanceof Member member) {
                if(member.getPass() == null || !member.getPass().isActive()) {
                    throw new NoActivePassException("Member with id " + userId + "doesn't have an active pass!");
                }
            }
            // If the user is on the list, we register an exit.
            // If the user is not on the list, we register an entry
            if(idsOfUsersAtTheGym.contains(userId)) {
                GymEntry gymEntry = tempGymEntries.stream()
                        .filter(entry -> entry.getAbstractUser().getId().equals(userId))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("GymEntry not found for userId: " + userId));

                gymEntry.setDateTimeOfExit(LocalDateTime.now());
                gymEntryRepository.save(gymEntry);

                tempGymEntries.remove(gymEntry);
                idsOfUsersAtTheGym.remove(userId);
            }
            else {
                GymEntry gymEntry = new GymEntry();
                gymEntry.setDateTimeOfEntry(LocalDateTime.now());
                gymEntry.setAbstractUser(abstractUser);
                tempGymEntries.add(gymEntry);
                idsOfUsersAtTheGym.add(userId);
            }
        }, () -> {
            throw new EntityNotFoundException("User with id " + userId + " not found");
        });
    }

    public int getCurrentUserCount() {
        return idsOfUsersAtTheGym.size();
    }
}
