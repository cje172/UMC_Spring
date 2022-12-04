package com.example.demo.src.follow;

import com.example.demo.src.follow.model.GetFollowRes;
import com.example.demo.src.follow.model.PostFollowReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class FollowDao {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // ******************************************************************************

    // 팔로우 등록
    public int createFollow(PostFollowReq postFollowReq) {
        String createFollowQuery = "insert into Follow (followerIdx, followingIdx) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] createFollowParams = new Object[]{postFollowReq.getFollowerIdx(), postFollowReq.getFollowingIdx()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createFollowQuery, createFollowParams);
        // userIdx -> postBoardReq.getUserIdx(), title -> postBoardReq.getTitle(), content -> postBoardReq.getContent() 로 매핑(대응)시킨다음 쿼리문을 실행한다.
        // 즉 DB의 User Table에 (userIdx, title, content)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    // 팔로우 존재 확인
    public int checkFollowExist(int followerIdx, int followingIdx) {
        String checkFollowExistQuery = "select exists(select * from Follow where followerIdx = ? and followingIdx = ?)";
        Object[] checkFollowParams = new Object[]{followerIdx, followingIdx};
        return this.jdbcTemplate.queryForObject(checkFollowExistQuery,
                int.class,
                checkFollowParams);
    }

    // 해당 followerIdx로 갖는 팔로우 정보 조회
    public List<GetFollowRes> getFollowingList(int followerIdx) {
        String getFollowQuery = "select * from Follow where followerIdx = ?"; // 해당 postIdx 만족하는 게시글을 조회하는 쿼리문
        int getFollowParams = followerIdx;
        return this.jdbcTemplate.query(getFollowQuery,
                (rs, rowNum) -> new GetFollowRes(
                        rs.getInt("followIdx"),
                        rs.getInt("followerIdx"),
                        rs.getInt("followingIdx")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getFollowParams); // 한 개의 게시글을 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 해당 followingIdx로 갖는 팔로우 정보 조회
    public List<GetFollowRes> getFollowerList(int followingIdx) {
        String getFollowQuery = "select * from Follow where followingIdx = ?"; // 해당 postIdx 만족하는 게시글을 조회하는 쿼리문
        int getFollowParams = followingIdx;
        return this.jdbcTemplate.query(getFollowQuery,
                (rs, rowNum) -> new GetFollowRes(
                        rs.getInt("followIdx"),
                        rs.getInt("followerIdx"),
                        rs.getInt("followingIdx")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getFollowParams); // 한 개의 게시글을 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 팔로우 취소
    public int deleteFollow(int followerIdx, int followingIdx){
        String deletePostQuery = "update Follow set status = 'I' WHERE followerIdx = ? and followingIdx = ? ";
        Object[] deletePostParams = new Object[]{followerIdx, followingIdx};

        return this.jdbcTemplate.update(deletePostQuery,deletePostParams);
    }

}
