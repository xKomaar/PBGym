package pl.pbgym.repository.user.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.user.trainer.GroupClass;

import java.util.List;

public interface GroupClassRepository extends JpaRepository<GroupClass, Long> {

    @Query("SELECT gc FROM GroupClass gc WHERE gc.date > CURRENT_TIMESTAMP")
    List<GroupClass> findAllUpcomingGroupClasses();

    @Query("SELECT gc FROM GroupClass gc WHERE gc.date <= CURRENT_TIMESTAMP")
    List<GroupClass> findAllHistoricalGroupClasses();

    @Query("SELECT gc FROM GroupClass gc JOIN gc.members m WHERE m.email = :email AND gc.date > CURRENT_TIMESTAMP")
    List<GroupClass> findUpcomingGroupClassesByMemberEmail(@Param("email") String email);

    @Query("SELECT gc FROM GroupClass gc JOIN gc.members m WHERE m.email = :email AND gc.date <= CURRENT_TIMESTAMP")
    List<GroupClass> findHistoricalGroupClassesByMemberEmail(@Param("email") String email);

    @Query("SELECT gc FROM GroupClass gc WHERE gc.trainer.email = :email AND gc.date > CURRENT_TIMESTAMP")
    List<GroupClass> findUpcomingGroupClassesByTrainerEmail(@Param("email") String email);

    @Query("SELECT gc FROM GroupClass gc WHERE gc.trainer.email = :email AND gc.date <= CURRENT_TIMESTAMP")
    List<GroupClass> findHistoricalGroupClassesByTrainerEmail(@Param("email") String email);
}