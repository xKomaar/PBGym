package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
