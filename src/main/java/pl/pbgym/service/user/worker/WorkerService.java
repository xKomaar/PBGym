package pl.pbgym.service.user.worker;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.Permission;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.domain.user.Worker;
import pl.pbgym.dto.user.worker.UpdateWorkerAuthorityRequestDto;
import pl.pbgym.dto.user.worker.UpdateWorkerRequestDto;
import pl.pbgym.dto.user.worker.GetWorkerResponseDto;
import pl.pbgym.exception.user.worker.WorkerNotFoundException;
import pl.pbgym.repository.user.PermissionRepository;
import pl.pbgym.repository.user.WorkerRepository;

import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public WorkerService(WorkerRepository workerRepository, PermissionRepository permissionRepository, ModelMapper modelMapper) {
        this.workerRepository = workerRepository;
        this.permissionRepository = permissionRepository;
        this.modelMapper = modelMapper;
    }

    public GetWorkerResponseDto getWorkerByEmail(String email) {
        Optional<Worker> optionalWorker = workerRepository.findByEmail(email);
        if (optionalWorker.isPresent()) {
            Worker worker = optionalWorker.get();
            GetWorkerResponseDto responseDto = modelMapper.map(worker, GetWorkerResponseDto.class);
            responseDto.setPermissions(worker.getMappedPermissions());
            return responseDto;
        } else {
            throw new WorkerNotFoundException("Worker not found with email: " + email);
        }
    }

    @Transactional
    public void updateWorker(String email, UpdateWorkerRequestDto updateWorkerRequestDto) {
        Optional<Worker> worker = workerRepository.findByEmail(email);
        worker.ifPresentOrElse(w -> modelMapper.map(updateWorkerRequestDto, w),
                () -> {
                    throw new WorkerNotFoundException("Worker not found with email: " + email);
                });
    }

    @Transactional
    public void updateWorkerAuthority(String email, UpdateWorkerAuthorityRequestDto updateWorkerAuthorityRequestDto) {
        Optional<Worker> worker = workerRepository.findByEmail(email);
        worker.ifPresentOrElse(w -> {
                    w.setPosition(updateWorkerAuthorityRequestDto.getPosition());
                    permissionRepository.deleteAll(w.getPermissions());
                    if(!updateWorkerAuthorityRequestDto.getPermissions().isEmpty()) {
                        for(Permissions p : updateWorkerAuthorityRequestDto.getPermissions()) {
                            Permission permission = new Permission();
                            permission.setWorker(w);
                            permission.set(p);
                            permissionRepository.save(permission);
                        }
                    }
                },
                () -> {
                    throw new WorkerNotFoundException("Worker not found with email: " + email);
                });
    }
}
