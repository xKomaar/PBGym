package pl.pbgym.service.member;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.Member;
import pl.pbgym.dto.member.GetMemberResponseDto;
import pl.pbgym.dto.worker.GetWorkerResponseDto;
import pl.pbgym.exception.member.MemberNotFoundException;
import pl.pbgym.repository.MemberRepository;
import pl.pbgym.service.AbstractUserService;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public MemberService(MemberRepository memberRepository, ModelMapper modelMapper) {
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
    }

    public GetMemberResponseDto getMemberByEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.map(m -> modelMapper.map(m, GetMemberResponseDto.class))
                .orElseThrow(() -> new MemberNotFoundException("Member not found with email: " + email));
    }
}
