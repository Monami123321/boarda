package site.gongtong.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import site.gongtong.member.config.MemberDetails;
import site.gongtong.member.dto.EditProfileDto;
import site.gongtong.member.dto.PasswordChangeDto;
import site.gongtong.member.dto.ReviewDto;
import site.gongtong.member.model.Follow;
import site.gongtong.member.model.Member;
import site.gongtong.member.service.FollowService;
import site.gongtong.member.service.MemberDetailsService;
import site.gongtong.member.service.MyPageService;
import site.gongtong.review.model.Review;

import java.security.SecureRandom;
import java.util.*;

@CrossOrigin(origins = "http://localhost:5173")
@Controller
@RequestMapping("/mypage")
@Slf4j
@RequiredArgsConstructor
public class MyPageController {

    @Autowired
    MemberDetailsService memberDetailsService;
    @Autowired
    MyPageService myPageService;
    @Autowired
    FollowService followService;
    @Autowired
    PasswordEncoder passwordEncoder;

    /*무조건 보여주기
        //{닉네임, 프사}
        //작성 피드들

        //본인일 때
        //{아이디, 비밀번호, 닉네임, 생일, 성별, 프사}
        //작성 피드들
        //팔로우 목록*/

    @GetMapping("/profile") //토큰으로 본인인지 확인 필요
    public ResponseEntity<ReviewDto> viewProfile(@RequestParam(value = "id") String id) {

        log.info("mypage enter reque!!");

//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        ResponseEntity<Map<String, Object>> response = null;

        MemberDetails dbMember = null;
        ReviewDto reviewDto = new ReviewDto();
        //리뷰 리스트
        List<Review> reviews = new ArrayList<>();
        try {
            dbMember = memberDetailsService.loadUserByUsername(id);
            // 정상 처리
            if(dbMember != null) {
                //멤버 프로필 내용 넣기
                reviewDto.setMember(mapToMember(dbMember));

                //리스트 뽑기
                reviews = myPageService.getReviewListByNum(myPageService.idToNum(id));

                for(int i = 0; i< reviews.size(); i++){
                    log.info(reviews.get(i).toString());
                }
                if(reviews == null || reviews.size() == 0) {
                    return new ResponseEntity<ReviewDto>(reviewDto, HttpStatus.OK);
                } else {
                    System.out.println("size>??? "+reviews.size());
                    reviewDto.setReviews(reviews);
                }

//                response = ResponseEntity
//                        .status(HttpStatus.OK)
//                        .body(resultMap);
            }
        } catch (Exception e) { //로그인 멤버 찾아오다가 오류
            e.printStackTrace();
//            resultMap.put("message", e.getMessage());
            return new ResponseEntity<ReviewDto>((ReviewDto) null, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<ReviewDto>(reviewDto, HttpStatus.OK);
    }

    public static Member mapToMember(MemberDetails dbMember) {
        Member showMember = new Member();
        showMember.setNickname(dbMember.getNickname());
        showMember.setProfileImage(dbMember.getProfileImage());
        showMember.setNum(dbMember.getNum()); // 마이페이지에는 안 나오게 하면 됨.


        if(true) { //토큰으로 사용자 인증 후 넣을지 말지 저장
            showMember.setId(dbMember.getUsername());
            showMember.setBirth(dbMember.getBirth());
            showMember.setGender(dbMember.getGender());
        }

        return showMember;
    }


    @PutMapping("/profile")
    public ResponseEntity<String> modifyProfile(@RequestParam(name = "id") String id,
                                                @RequestBody EditProfileDto editProfileDto) {

        log.info("profile modify start!!");

        //read only는 원래 값 그대로 넣기 (id기반으로 Member 찾아서 넣기)
        Member member = myPageService.findById(id);
        editProfileDto.setNum(member.getNum());
        editProfileDto.setId(member.getId());
        editProfileDto.setBirth(member.getBirth());
        editProfileDto.setGender(member.getGender());

        //프사, 닉변은 빈값 아니면 하기. (비번은 따로)
        if(editProfileDto.getProfileImage().equals("")) {
            editProfileDto.setProfileImage(member.getProfileImage());
        } else {
            editProfileDto.setProfileImage(editProfileDto.getProfileImage());
        }
        if(editProfileDto.getNickname().equals("")) {
            editProfileDto.setNickname(member.getNickname());
        } else {
            editProfileDto.setNickname(editProfileDto.getNickname());
        }
        // ㄴ editDto완성

        try {
            if(myPageService.modifyProfile(editProfileDto) > 0) {
                return new ResponseEntity<> ("프로필 수정 성공 -db확인", HttpStatus.OK);
            } else {
                return new ResponseEntity<> ("프로필 수정 안 됨 - 내용이 같음", HttpStatus.OK);
            }
        } catch (Exception e) {
//            resultMap.put("message", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //비번 찾기
    @PostMapping("/forgetpwd")
    public ResponseEntity<Integer> forgetPwd(@RequestParam(name = "id") String id) {
        //1. 임시 비번 만들기
            //1-랜덤 문자열 생성
        String newRawPwd = getRandomPwd(10);
//          System.out.println("tmp rawPwd: "+newRawPwd);
            //2-위의 문자열 bcrypt로 암호화하기
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String newEncodedPwd = encoder.encode(newRawPwd); //암호화된 문자열
//          System.out.println("tmp encodedPwd: "+newEncodedPwd);

        //2. db에 비번 바꾸기
        try {
            if (myPageService.setPwd(id, newEncodedPwd) > 0) { //성공!
                //비번바꾸기 성공하면 1!
                return new ResponseEntity<>(1, HttpStatus.OK);
            } else { //실패ㅠ = 0
                //모종의 이유로 실패 - 디비는 갔다 옴
                return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage()); //뭐가 잘못된 건지 찎어보기
            //내부 이유로 실패 - 2
            return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //랜덤 문자 만들기
    public String getRandomPwd(int length) {
        char[] rndAllCharacters = new char[]{
                //number
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                //uppercase
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                //lowercase
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                //special symbols
                '@', '$', '!', '%', '*', '?', '&'
        };
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();

        int rndAllCharactersLength = rndAllCharacters.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(rndAllCharacters[random.nextInt(rndAllCharactersLength)]);
        }

        return stringBuilder.toString();
    }


    //비번 변경
    @PutMapping("/modifypwd")
    public ResponseEntity<Integer> modifyPwd(@RequestBody PasswordChangeDto passwordChangeDto) {
        //1. 입력된 id 기반으로 해당 유저 entity 찾기
        Member member;
        try {
            member = myPageService.findById(passwordChangeDto.getId());
            if(member == null)
                return new ResponseEntity<>(0, HttpStatus.NOT_FOUND); // id에 해당하는 유저 없음

            //2. member의 비번 - 입력된 현재비번 동일성 여부
            if( !passwordEncoder.matches(passwordChangeDto.getCurPwd(), member.getPassword()) )
                return new ResponseEntity<>(2, HttpStatus.UNAUTHORIZED); // 현재 비밀번호 불일치 시 땡~!!

            //3. db에 새로운 비밀번호 encode해서 대체하기
            String newPwd = passwordChangeDto.getNewRawPwd();
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String newEncodedPwd = encoder.encode(newPwd);
            try {
                if (myPageService.setPwd(passwordChangeDto.getId(), newEncodedPwd) > 0) {
                    return new ResponseEntity<>(1, HttpStatus.OK); //성공
                }
            } catch (Exception e) {
                System.out.println(e.getMessage()); //뭐가 잘못된 건지 찍어보기
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(3, HttpStatus.INTERNAL_SERVER_ERROR); // 모종의 이유로 실패
    }

    //회원 탈퇴
    @DeleteMapping("/profile")
    public ResponseEntity<Integer> deleteMember(@RequestParam String id) {
        Member member;
        try {
            member = myPageService.findById(id);
            if( myPageService.deleteMember(id) > 0) return new ResponseEntity<>(1, HttpStatus.OK); //탈퇴 완료
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR); //탈퇴 실패
    }

    //팔로우 하기
    @PostMapping("/follow")
    public ResponseEntity<Integer> registFollow(@RequestParam(name = "id") String myId,
                                                @RequestParam(name = "nickname") String yourNickname,
                                                @RequestParam(name = "flag") char flag) {
        Member memMe;
        Member memYou;
        try {
            //1. 아이디, 닉네임 기반 멤버 찾아오기
            memMe = myPageService.findById(myId);
            memYou = myPageService.findByNickname(yourNickname);
            if(memMe==null || memYou==null) {
//                log.info("follow; null Object input error!");
                return new ResponseEntity<>(0, HttpStatus.NOT_FOUND); //해당 유저 찾을 수 없으면 안 됨
            }
            if(memMe==memYou) {
//                log.info("follow; same Object cannot have relationship!");
                return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST); //팔로우==팔로잉은 안 됨
            }

            //2. id 뽑아서, 디비에 관계 저장
                //이미 있는 관계는 패스
            if( followService.existRelation(memMe.getNum(), memYou.getNum()) > 0 ) {
//                log.info("follow; Already existed relationship!");
                return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST); //이미 있는 관계가 또 들어오면 무시
            }
                //이미 있는 관계가 아니면 수행하기
            Follow newRelation = followService.save(memMe, flag, memYou);
            if (newRelation == null) new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR); //객체 안 만들어짐
            
        } catch (Exception e) {
            e.printStackTrace(); //예상치 못한 다중 테이블 참조 오류를 발생
            return new ResponseEntity<>(2, HttpStatus.INTERNAL_SERVER_ERROR);
        }
//        log.info("follow making; GOODDDD!");
        return new ResponseEntity<>(1, HttpStatus.OK); //성공!
    }

}

