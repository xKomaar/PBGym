package pl.pbgym.service.statistics;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserCounterService.class);

    private final Set<String> emailsOfUsersAtTheGym = ConcurrentHashMap.newKeySet();
    private final Set<GymEntry> tempGymEntries = ConcurrentHashMap.newKeySet();
    private final AbstractUserRepository abstractUserRepository;
    private final GymEntryRepository gymEntryRepository;

    public UserCounterService(AbstractUserRepository abstractUserRepository, GymEntryRepository gymEntryRepository) {
        this.abstractUserRepository = abstractUserRepository;
        this.gymEntryRepository = gymEntryRepository;
    }

    public void registerUserAction(String email) {
        logger.info("Rejestrowanie akcji użytkownika o emailu {}.", email);
        abstractUserRepository.findByEmail(email).ifPresentOrElse(abstractUser -> {
            if (abstractUser instanceof Worker) {
                logger.error("Pracownik o emailu {} próbował wejść na siłownię, co jest niedozwolone.", email);
                throw new WorkerNotAllowedToBeScannedException("Worker is not allowed here!");
            } else if (abstractUser instanceof Member member) {
                if (member.getPass() == null) {
                    logger.error("Członek o emailu {} nie ma aktywnego karnetu.", email);
                    throw new NoActivePassException("Member with email " + email + " doesn't have an active pass!");
                }
            }

            if (emailsOfUsersAtTheGym.contains(email)) {
                GymEntry gymEntry = tempGymEntries.stream()
                        .filter(entry -> entry.getAbstractUser().getEmail().equals(email))
                        .findFirst()
                        .orElseThrow(() -> {
                            logger.error("Nie znaleziono GymEntry dla użytkownika o emailu {} podczas rejestrowania wyjścia.", email);
                            return new EntityNotFoundException("GymEntry not found for email: " + email);
                        });

                gymEntry.setDateTimeOfExit(LocalDateTime.now());
                gymEntryRepository.save(gymEntry);
                logger.info("Zarejestrowano wyjście użytkownika o emailu {}. Data i czas wyjścia: {}.", email, gymEntry.getDateTimeOfExit());

                tempGymEntries.remove(gymEntry);
                emailsOfUsersAtTheGym.remove(email);
            } else {
                GymEntry gymEntry = new GymEntry();
                gymEntry.setDateTimeOfEntry(LocalDateTime.now());
                gymEntry.setAbstractUser(abstractUser);
                tempGymEntries.add(gymEntry);
                emailsOfUsersAtTheGym.add(email);
                logger.info("Zarejestrowano wejście użytkownika o emailu {}. Data i czas wejścia: {}.", email, gymEntry.getDateTimeOfEntry());
            }
        }, () -> {
            logger.error("Nie znaleziono użytkownika o emailu {} w bazie danych.", email);
            throw new EntityNotFoundException("User with email " + email + " not found");
        });
    }

    public int getCurrentUserCount() {
        int count = emailsOfUsersAtTheGym.size();
        logger.info("Aktualna liczba użytkowników na siłowni: {}.", count);
        return count;
    }
}
