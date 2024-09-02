package pl.pbgym.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.worker.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
