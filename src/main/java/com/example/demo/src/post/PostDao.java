package com.example.demo.src.post;

import com.example.demo.src.post.model.GetPostRes;
import com.example.demo.src.post.model.PatchPostReq;
import com.example.demo.src.post.model.PostPostReq;
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
public class PostDao {

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // ******************************************************************************

    // 게시물 등록
    public int createPost(PostPostReq postPostReq) {
        String createPostQuery = "insert into Post (userIdx, content) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] createPostParams = new Object[]{postPostReq.getUserIdx(), postPostReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createPostQuery, createPostParams);
        // userIdx -> postBoardReq.getUserIdx(), title -> postBoardReq.getTitle(), content -> postBoardReq.getContent() 로 매핑(대응)시킨다음 쿼리문을 실행한다.
        // 즉 DB의 User Table에 (userIdx, title, content)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    // 해당 nickname을 갖는 유저들의 정보 조회
    public List<GetPostRes> getPostsByUserIdx(int userIdx) {
        String getPostsByUserIdxQuery = "select * from Post where userIdx =?"; // 해당 이메일을 만족하는 유저를 조회하는 쿼리문
        int getPostsByUserIdxParams = userIdx;
        return this.jdbcTemplate.query(getPostsByUserIdxQuery,
                (rs, rowNum) -> new GetPostRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getPostsByUserIdxParams); // 해당 닉네임을 갖는 모든 User 정보를 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 해당 postIdx 갖는 게시물 조회
    public GetPostRes getPost(int postIdx) {
        String getPostQuery = "select * from Post where postIdx = ?"; // 해당 postIdx 만족하는 게시글을 조회하는 쿼리문
        int getPostParams = postIdx;
        return this.jdbcTemplate.queryForObject(getPostQuery,
                (rs, rowNum) -> new GetPostRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getPostParams); // 한 개의 게시글을 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 게시물 수정
    public int modifyPost(PatchPostReq patchPostReq) {
        String modifyPostQuery = "update Post set content = ? where postIdx = ? "; // 해당 boardIdx를 만족하는 Board를 해당 title과 content로 변경한다.
        Object[] modifyPostParams = new Object[]{patchPostReq.getContent(), patchPostReq.getPostIdx()}; // 주입될 값들(title, content, boardIdx) 순

        return this.jdbcTemplate.update(modifyPostQuery, modifyPostParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // 게시물 삭제
    public int deletePost(int postIdx){
        String deletePostQuery = "update Post set status = 'D' WHERE postIdx = ? ";
        Object[] deletePostParams = new Object[]{postIdx};

        return this.jdbcTemplate.update(deletePostQuery,deletePostParams);
    }

}
