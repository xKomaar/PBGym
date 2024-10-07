package pl.pbgym.service.user_counter;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.worker.Worker;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.exception.user_counter.WorkerNotAllowedToBeScannedException;
import pl.pbgym.repository.user.AbstractUserRepository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserCounterService {

    private final Set<Long> idsOfUsersAtTheGym = ConcurrentHashMap.newKeySet();
    private final AbstractUserRepository abstractUserRepository;

    public UserCounterService(AbstractUserRepository abstractUserRepository) {
        this.abstractUserRepository = abstractUserRepository;
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
            // If the user is not on the list, we implement an entry
            if(idsOfUsersAtTheGym.contains(userId)) {
                idsOfUsersAtTheGym.remove(userId);
            }
            else {
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
