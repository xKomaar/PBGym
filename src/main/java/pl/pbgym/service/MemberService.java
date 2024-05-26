package pl.pbgym.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.Member;
import pl.pbgym.repository.MemberRepository;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AbstractUserService abstractUserService;

    @Autowired
    public MemberService(MemberRepository memberRepository, AbstractUserService abstractUserService) {
        this.memberRepository = memberRepository;
        this.abstractUserService = abstractUserService;
    }

    public void updateMember(Member newMember) {
        Optional<Member> existingMember = memberRepository.findById(newMember.getId());
        existingMember.ifPresent(member -> {
            abstractUserService.updateAbstractUser(member, newMember);

            memberRepository.flush();
        });
    }
}
