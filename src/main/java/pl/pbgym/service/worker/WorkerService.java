package pl.pbgym.service.worker;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.Worker;
import pl.pbgym.domain.Worker;
import pl.pbgym.dto.worker.UpdateWorkerRequestDto;
import pl.pbgym.dto.worker.GetWorkerResponseDto;
import pl.pbgym.exception.worker.WorkerNotFoundException;
import pl.pbgym.exception.worker.WorkerNotFoundException;
import pl.pbgym.repository.WorkerRepository;

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
