package pl.pbgym.service.user.worker;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.Worker;
import pl.pbgym.dto.offer.worker.UpdateWorkerRequestDto;
import pl.pbgym.dto.offer.worker.GetWorkerResponseDto;
import pl.pbgym.exception.user.worker.WorkerNotFoundException;
import pl.pbgym.repository.user.WorkerRepository;

import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public WorkerService(WorkerRepository workerRepository, ModelMapper modelMapper) {
        this.workerRepository = workerRepository;
        this.modelMapper = modelMapper;
    }

    public GetWorkerResponseDto getWorkerByEmail(String email) {
        Optional<Worker> worker = workerRepository.findByEmail(email);
        return worker.map(m -> modelMapper.map(m, GetWorkerResponseDto.class))
                .orElseThrow(() -> new WorkerNotFoundException("Worker not found with email: " + email));
    }

    @Transactional
    public void updateWorker(String email, UpdateWorkerRequestDto updateWorkerRequestDto) {
        Optional<Worker> worker = workerRepository.findByEmail(email);
        worker.ifPresentOrElse(w -> modelMapper.map(updateWorkerRequestDto, w),
                () -> {
                    throw new WorkerNotFoundException("Worker not found with email: " + email);
                });
    }
}
