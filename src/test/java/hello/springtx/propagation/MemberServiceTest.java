package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService    = @Transactional: OFF
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional: ON
     */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    = @Transactional: OFF
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional: ON  ( occurred RuntimeException )
     */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외 - outerTxOff_fail";

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then: log 데이터는 롤백된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    = @Transactional: ON
     * memberRepository = @Transactional: OFF
     * logRepository    = @Transactional: OFF
     */
    @Test
    void singleTx() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    = @Transactional: ON
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional: ON
     */
    @Test
    void outerTxOn_success() {
        // given
        String username = "outerTxOn_success";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    = @Transactional: ON
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional: ON  ( occurred RuntimeException )
     */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외 - outerTxOn_fail";

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then: 모든 데이터는 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    = @Transactional: ON
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional: ON  ( occurred RuntimeException )
     */
    @Test
    void recoverException_fail() {
        // given
        String username = "로그예외 - recoverException_fail";

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then: 모든 데이터는 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    = @Transactional: ON
     * memberRepository = @Transactional: ON
     * logRepository    = @Transactional(REQUIRES_NEW): ON  ( occurred RuntimeException )
     */
    @Test
    void recoverException_success() {
        // given
        String username = "로그예외 - recoverException_success";

        // when
        memberService.joinV2(username);

        // then: member 저장, log 데이터는 롤백된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

}
