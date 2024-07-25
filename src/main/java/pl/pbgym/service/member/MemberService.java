package pl.pbgym.service.member;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.Member;
import pl.pbgym.dto.member.GetMemberResponseDto;
import pl.pbgym.dto.member.UpdateMemberRequestDto;
import pl.pbgym.exception.member.MemberNotFoundException;
import pl.pbgym.repository.MemberRepository;

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

    @Transactional
    public void updateMember(String email, UpdateMemberRequestDto updateMemberRequestDto) {
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresentOrElse(m -> modelMapper.map(updateMemberRequestDto, m),
            () -> {
                throw new MemberNotFoundException("Member not found with email: " + email);
            });
    }
}
