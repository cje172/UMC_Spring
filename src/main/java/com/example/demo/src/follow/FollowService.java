package com.example.demo.src.follow;

import com.example.demo.config.BaseException;
import com.example.demo.src.follow.model.PostFollowReq;
import com.example.demo.src.follow.model.PostFollowRes;
import com.example.demo.src.post.PostDao;
import com.example.demo.src.post.PostProvider;
import com.example.demo.src.post.model.PostPostReq;
import com.example.demo.src.post.model.PostPostRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.*;

/**
 * Service란?
 * Controller에 의해 호출되어 실제 비즈니스 로직과 트랜잭션을 처리: Create, Update, Delete 의 로직 처리
 * 요청한 작업을 처리하는 관정을 하나의 작업으로 묶음
 * dao를 호출하여 DB CRUD를 처리 후 Controller로 반환
 */
@Service
public class FollowService {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final FollowDao followDao;
    private final FollowProvider followProvider;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    @Autowired //readme 참고
    public FollowService(FollowDao followDao, FollowProvider followProvider, JwtService jwtService) {
        this.followDao = followDao;
        this.followProvider = followProvider;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!

    }

    // ******************************************************************************
    // 팔로우 등록(POST)
    public PostFollowRes createFollow(PostFollowReq postFollowReq) throws BaseException {
        if (followProvider.checkFollowExist(postFollowReq.getFollowerIdx(), postFollowReq.getFollowingIdx()) == 1) {
            throw new BaseException(POST_FOLLOW_EXISTS_FOLLOW);
        }
        try {
            int followIdx = followDao.createFollow(postFollowReq);
            return new PostFollowRes(followIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 팔로우 취소(Patch)
    public void deleteFollow(int followerIdx, int followingIdx) throws BaseException {
        try {
            int result = followDao.deleteFollow(followerIdx, followingIdx);
            if (result == 0) {
                throw new BaseException(DELETE_FAIL_FOLLOW);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
